import copy
import functools
import itertools
import logging
import sys
from collections import OrderedDict
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
from joblib import dump, load
from sklearn.metrics import confusion_matrix
from tqdm import tqdm

from .config import CfgNode as CN

logger = logging.getLogger(__name__)


# -----------------------------------------------------------------------------
# I/O
# -----------------------------------------------------------------------------
def get_data(dataset_file, cat_train_val=False):
    d = np.load(dataset_file, allow_pickle=True)
    d = {k: d[k] for k in d.files if (len(d[k]) > 0) and (d[k][0] is not None)}
    if cat_train_val:
        d["X_train"] = np.concatenate((d["X_train"], d["X_val"]), axis=0)
        d["y_train"] = np.concatenate((d["y_train"], d["y_val"]))
        d["meta_train"] = list(d["meta_train"]) + list(d["meta_val"])
        [d.pop(k) for k in ["X_val", "y_val", "meta_val"]]
    return d


def load_model(file):
    return load(file)["model"]


def save_model(model, file):
    model = {"model": model}
    dump(model, file)


def preprocess_single_file(
    file,
    label=None,
    *,
    head_drop=150,
    n_samples=10,
    len_sample=300,
    preprocess="normalized",
    switch_prob=-1,
):
    import pandas as pd

    file = Path(file)
    df = pd.read_csv(file)
    left, t_left = (
        df.Value[df.Limb == "LEFT_ARM"].to_numpy()[head_drop:],
        df.Time[df.Limb == "LEFT_ARM"].to_numpy()[head_drop:],
    )
    right, t_right = (
        df.Value[df.Limb == "RIGHT_ARM"].to_numpy()[head_drop:],
        df.Time[df.Limb == "RIGHT_ARM"].to_numpy()[head_drop:],
    )
    del df

    # align left and right from head
    length = min(len(left), len(right))
    left, t_left = left[:length], t_left[:length]
    right, t_right = right[:length], t_right[:length]

    if preprocess == "normalized":
        _max = max(max(left), max(right))
        left /= _max
        right /= _max

    elif preprocess == "first_order":
        left = np.gradient(left, t_left)
        right = np.gradient(right, t_right)

    elif preprocess == "second_order":
        for i in range(2):
            left = np.gradient(left, t_left)
            right = np.gradient(right, t_right)
    else:
        logger.error(
            f"method must be one of 'normalized', 'first_order' or \
            'second_order' (got '{preprocess}')"
        )
        raise ValueError()

    def switch_label(label):
        if label == 1 or label is None:
            return label

        if label == 2:
            return 3

        if label == 3:
            return 2

    def append(idx_start, label):
        switch = np.random.rand() < switch_prob
        if not switch:
            samples.append(left[idx_start : idx_start + len_sample])
            samples.append(right[idx_start : idx_start + len_sample])
        else:
            samples.append(right[idx_start : idx_start + len_sample])
            samples.append(left[idx_start : idx_start + len_sample])
            label = switch_label(label)

        metas.append(
            {
                "file": file.name,
                "drop": head_drop,
                "pre_length": length,
                "post_start": idx_start,
                "post_length": len_sample,
                "switch": switch,
                "label": label,
            }
        )

    samples = []
    metas = []
    if n_samples == "center":
        idx_start = length // 2 - len_sample // 2
        append(idx_start, label)
        n_samples = 0  # skip the following for loop

    for _ in range(n_samples):
        idx_start = np.random.randint(len(left) - len_sample)
        append(idx_start, label)

    samples = np.concatenate(samples, axis=0).reshape(len(samples) // 2, len_sample * 2)
    return samples, metas


# -----------------------------------------------------------------------------
# train and evaluation helper
# -----------------------------------------------------------------------------
def train_model(model_module, cfg, data, print_acc=True):
    X_train, y_train = data["X_train"], data["y_train"]
    model = model_module.get_model(cfg[cfg.MODEL])
    model = model.fit(X_train, y_train)
    accuracy = eval_model(model, data, print_acc, cfg.OUTPUT_DIR)

    if cfg.OUTPUT_DIR:
        model_path = Path(cfg.OUTPUT_DIR).joinpath(f"{cfg.MODEL}.joblib")
        save_model(model, model_path)

    return model, accuracy


def eval_model(model, data, print_acc=True, output_dir=""):
    accuracy = OrderedDict()
    for set in ["train", "val", "test"]:
        X = data.get("X_" + set, None)
        if X is None:
            continue

        y = data.get("y_" + set, None)
        if y is not None:
            accuracy[set] = model.score(X, y)

        metas = data.get("meta_" + set, None)
        if metas is not None:
            predictions = model.predict(X)
            for m, pred in zip(metas, predictions):
                m["prediction"] = pred  # inplace modification

    acc_str = log_accuracy(accuracy)
    if print_acc:
        logger.info(acc_str)

    if output_dir:
        if len(accuracy) > 0:
            output_path = Path(output_dir).joinpath("accuracy.txt")
            logger.info(f"save accuracy to {output_path}")
            with open(output_path, "w") as f:
                f.write(acc_str)

        metas = list(itertools.chain(*[data[k] for k in data if k.startswith("meta_")]))
        if len(metas) > 0:
            import pandas as pd

            output_path = Path(output_dir).joinpath("predictions.csv")
            logger.info(f"save predictions to {output_path}")
            df = pd.DataFrame(metas)
            df.to_csv(output_path)

    return accuracy


def tune_hyperparameters(model_module, cfg, data):
    model_name = cfg.MODEL
    cfg_model = cfg[model_name].clone()
    output_dir = cfg.OUTPUT_DIR

    cfg.defrost()
    cfg.pop(model_name)
    model_module.add_cfg_model(cfg)

    keys = list(cfg_model.keys())
    choices = list(itertools.product(*list(cfg_model.values())))
    if len(choices) < 2:
        logger.error(f"need at least 2 choices to tune hyper-paramaters, got {len(choices)}")
        raise ValueError

    logger.info(
        f"Running {len(choices)} experiments with different combination of hyper-parameters..."
    )

    best_val = {"val": -1}
    cfg.OUTPUT_DIR = ""  # temperorary turn off to prevent saving
    for c in tqdm(choices):
        cfg_model = CN(dict(zip(keys, c)))
        cfg[model_name].merge_from_other_cfg(cfg_model)

        model, accuracy = train_model(model_module, cfg, data, False)
        if accuracy["val"] > best_val["val"]:
            best_val = copy.deepcopy(accuracy)
            best_model = model
            best_cfg_model = cfg_model

    if output_dir:
        cfg.OUTPUT_DIR = output_dir
        cfg[model_name].merge_from_other_cfg(best_cfg_model)

        output_dir = Path(output_dir)
        model_path = output_dir.joinpath(f"{model_name}.joblib")
        save_model(best_model, model_path)
        cfg_path = output_dir.joinpath(f"{model_name}_best_hps.yaml")
        with open(cfg_path, "w") as f:
            f.write(cfg.dump())

    cfg.freeze()
    logger.info(f"Best validation accuracy {best_val['val']:.2f} by \n{str(best_cfg_model)}")
    eval_model(best_model, data, True, output_dir)

    return best_model, accuracy


def tune_datasets(alg_module, cfg, datasets):
    logger.info(f"Running {len(datasets)} experiments with different datasets...")
    best_val = {"val": -1}
    for dataset in tqdm(datasets):
        data = get_data(dataset)
        model, accuracy = train_model(alg_module, cfg, data, False)

        if accuracy["val"] > best_val["val"]:
            best_val = copy.deepcopy(accuracy)
            best_dataset = dataset
            best_model = model

    acc_str = log_accuracy(accuracy)
    logger.info(f"Best validation accuracy {best_val['val']:.2f} by {str(best_dataset)}")
    logger.info(acc_str)
    return best_model, best_dataset


def log_accuracy(acc_dict):
    if len(acc_dict) == 0:
        return "No accuracy to show"

    s = " / ".join(acc_dict.keys()) + " accuracy"
    s += ": "
    s += " / ".join(["{:.2f}"] * len(acc_dict))
    s = s.format(*acc_dict.values())
    return s


def plot_confusion_matrix(model, X, y, labels=None, plot_to=None):
    num_classes = len(model.classes_)
    if labels is None:
        labels = [f"Class{_+1}" for _ in range(num_classes)]
    pred_labels = ["Pred. " + _ for _ in labels]
    true_labels = ["True " + _ for _ in labels]

    cm = confusion_matrix(y, model.predict(X))
    cm_norm = cm / cm.sum(axis=1)

    fmt = "{:>15}" * (len(model.classes_) + 1)
    s = fmt.format("", *pred_labels)
    fmt = "{:>8.0f} ({:.2f})" * len(model.classes_)
    fmt = "\n{:>15}" + fmt
    for i, label in enumerate(true_labels):
        acc = np.vstack((cm[i], cm_norm[i])).T.flatten().tolist()
        s += fmt.format(label, *acc)

    if plot_to is not None:
        fig, ax = plt.subplots()
        im = ax.imshow(cm_norm, cmap=plt.cm.Blues)
        cmap_min, cmap_max = im.cmap(0), im.cmap(256)
        thresh = (cm.max() + cm.min()) / 2.0

        ax.set_xticks(np.arange(num_classes))
        ax.set_yticks(np.arange(num_classes))
        ax.set_xticklabels(pred_labels)
        ax.set_yticklabels(true_labels)

        for i, j in itertools.product(range(num_classes), range(num_classes)):
            color = cmap_max if cm[i, j] < thresh else cmap_min
            text = "{:.0f} ({:.2f})".format(cm[i, j], cm_norm[i, j])
            text = ax.text(j, i, text, ha="center", va="center", color=color)

        ax.set_title("Confusion Matrix")
        fig.colorbar(im, ax=ax)
        fig.savefig(plot_to, bbox_inches="tight", dpi=200)

    return s


# -----------------------------------------------------------------------------
# Loger
# -----------------------------------------------------------------------------
class _ColorfulFormatter(logging.Formatter):
    grey = "\x1b[38;1m"
    red_bg = "\x1b[41;1m"
    green_bg = "\x1b[46;1m"
    red = "\x1b[91;1m"
    green = "\x1b[92;1m"
    blue = "\x1b[94;1m"
    cyan = "\x1b[96;1m"
    reset = "\x1b[0m"

    fmt = "{}[%(asctime)s] %(name)s [%(levelname)s]:{} %(message)s{}"

    FORMATS = {
        logging.DEBUG: fmt.format(grey, "", reset),
        logging.INFO: fmt.format(green, reset, ""),
        logging.WARNING: fmt.format(red, "", reset),
        logging.ERROR: fmt.format(red_bg, reset, ""),
        logging.CRITICAL: fmt.format(cyan, reset, ""),
    }

    def format(self, record):
        fmt = self.FORMATS.get(record.levelno)
        formatter = logging.Formatter(fmt, datefmt="%m/%d %H:%M:%S")
        return formatter.format(record)


@functools.lru_cache()  # so that calling setup_logger multiple times won't add many handlers
def setup_logger(name="limbresml"):
    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)
    logger.propagate = False

    ch = logging.StreamHandler(stream=sys.stdout)
    ch.setLevel(logging.DEBUG)
    formatter = _ColorfulFormatter()
    ch.setFormatter(formatter)
    logger.addHandler(ch)
    return logger
