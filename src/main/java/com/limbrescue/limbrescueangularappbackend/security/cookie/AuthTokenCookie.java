package com.limbrescue.limbrescueangularappbackend.security.cookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class AuthTokenCookie extends Cookie {

  public static final String NAME = "auth_jwt";
  private static final String PATH = "/";
  private static final Pattern UID_PATTERN = Pattern.compile("uid=([A-Za-z0-9]*)");
  private static final Pattern HMAC_PATTERN = Pattern.compile("hmac=([A-Za-z0-9+/=]*)");
  private static final String HMAC_SHA_512 = "HmacSHA512";
  private static final String secretKey = "y.E@EZ!FbtCwXYB-2v_n.?*xgzRqgtkq2d2_A_U!W2hubL@URHRzMP96WNPxEcXY";;

  private final String payload;
  private final String hmac;

  public AuthTokenCookie(String auth_jwt) {
    super(NAME, "");
    this.payload = auth_jwt;
    this.hmac = calculateHmac(this.payload, secretKey);
    this.setPath(PATH);
    this.setMaxAge((int) Duration.of(1, ChronoUnit.HOURS).toSeconds());
    this.setHttpOnly(true);
  }


  @Override
  public String getValue() {
    return payload + "&hmac=" + hmac;
  }



  private String calculateHmac(String payload, String secretKey) {
    byte[] secretKeyBytes = Objects.requireNonNull(secretKey).getBytes(StandardCharsets.UTF_8);
    byte[] valueBytes = Objects.requireNonNull(payload).toString().getBytes(StandardCharsets.UTF_8);

    try {
      Mac mac = Mac.getInstance(HMAC_SHA_512);
      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, HMAC_SHA_512);
      mac.init(secretKeySpec);
      byte[] hmacBytes = mac.doFinal(valueBytes);
      return Base64.getEncoder().encodeToString(hmacBytes);

    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Only for testing.
   */
  String getHmac() {
    return hmac;
  }

}