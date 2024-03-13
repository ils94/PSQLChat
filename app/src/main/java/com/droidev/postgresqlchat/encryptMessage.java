package com.droidev.postgresqlchat;


import android.content.Context;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class encryptMessage {
    private static final String ALGORITHM = "AES";

    public static String encryptString(Context context, String stringToEncrypt) throws Exception {

        TinyDB tinyDB = new TinyDB(context);

        String key = tinyDB.getString("encryptKey");

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(stringToEncrypt.getBytes()));
    }

    public void generateSecretKey(Context context) throws NoSuchAlgorithmException {

        TinyDB tinyDB = new TinyDB(context);

        tinyDB.remove("encryptKey");

        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256); // AES key size
        SecretKey secretKey = keyGenerator.generateKey();

        tinyDB.putString("encryptKey", Base64.getEncoder().encodeToString(secretKey.getEncoded()));

    }

    public static String decryptString(String stringToDecrypt, String secretKey) throws Exception {
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(secretKey), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(stringToDecrypt));
        return new String(decryptedBytes);
    }
}
