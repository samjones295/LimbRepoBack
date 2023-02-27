package com.limbrescue.limbrescueangularappbackend.security.encryption;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;


import java.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class AES {
    //Fields

    private final int KEY_SIZE = 128;
    private final int KEY_SIZE_BYTES = 16;
    private final int DATA_LENGTH = 128;
    private Cipher encryptionCipher;
    
    //Constructors
    public AES() {
    }

     public String init() {
        KeyGenerator keyGenerator = null;
        String encodedKey = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        }catch(NoSuchAlgorithmException e) {
                System.out.println("Something is wrong");
        }
        
        return encodedKey;
    }
    
    //Encrypt
    public String encrypt(String data, String encodedKey) {

        byte[] encryptedBytes = null;
        try {
            byte[] dataInBytes = data.getBytes();
            encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, KEY_SIZE_BYTES, "AES"); 
            encryptionCipher.init(Cipher.ENCRYPT_MODE, originalKey);
            encryptedBytes = encryptionCipher.doFinal(dataInBytes);
            
        }catch(NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException");
        }catch(NoSuchPaddingException e){
            System.out.println("NoSuchPaddingException");
        }catch(InvalidKeyException e){
            System.out.println("InvalidKeyException");
        }
        catch(IllegalBlockSizeException e){
            System.out.println("IllegalBlockSizeException");
        }catch(BadPaddingException e){
            System.out.println("BadPaddingException");
        }

        return encode(encryptedBytes);
        
    }

    //Encrypt
    public String decrypt(String encryptedData, String encodedKey) {
        byte[] decryptedBytes = null;
        try {
            byte[] dataInBytes = decode(encryptedData);
            Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, KEY_SIZE/8, "AES"); 
            GCMParameterSpec spec = new GCMParameterSpec(DATA_LENGTH, encryptionCipher.getIV());
            decryptionCipher.init(Cipher.DECRYPT_MODE, originalKey, spec);
            decryptedBytes = decryptionCipher.doFinal(dataInBytes);
            
        }catch(NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException");
        }catch(InvalidAlgorithmParameterException e){
            System.out.println("InvalidAlgorithmParameterException");
        }catch(NoSuchPaddingException e){
            System.out.println("NoSuchPaddingException");
        }catch(InvalidKeyException e){
            System.out.println("InvalidKeyException");
        }
        catch(IllegalBlockSizeException e){
            System.out.println("IllegalBlockSizeException");
        }catch(BadPaddingException e){
            System.out.println("BadPaddingException");
        }

        return new String(decryptedBytes);

        
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
    
}