package com.ayush.imagesteganographylibrary.Utils;

import android.util.Log;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    //Шифрование
    /**
     * @parameter : Message {String} - сообщение, Secret key {String} - ключ
     * @return : Encrypted Message {String} - зашифрованное сообщение
     */
    public static String encryptMessage(String message, String secret_key) throws Exception {

        // Создание ключа и шифра
        SecretKeySpec aesKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        Cipher cipher;

        //AES шифр
        cipher = Cipher.getInstance("AES");

        // шифрование
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] encrypted;

        encrypted = cipher.doFinal(message.getBytes());

        Log.d("crypto", "Encrypted  in crypto (mine): " + Arrays.toString(encrypted) + "string: " + android.util.Base64.encodeToString(cipher.doFinal(message.getBytes()), 0));

        Log.d("crypto", "Encrypted  in crypto (theirs): " + Arrays.toString(cipher.doFinal(message.getBytes())) + "string : " + new String(encrypted));

        return android.util.Base64.encodeToString(cipher.doFinal(message.getBytes()), 0);
    }

    // Дешифрование
    /**
     * @parameter : encrypted_message {String} - Зашифрованное сообщение, secret_key {String} - Ключ
     * @return : decrypted {String} - Сообщение
     */
    public static String decryptMessage(String encrypted_message, String secret_key) throws Exception {

        Log.d("Decrypt", "message: + " + encrypted_message);
        // Создание ключа и шифра
        SecretKeySpec aesKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        Cipher cipher;

        //AES шифр
        cipher = Cipher.getInstance("AES");

        // дешифрование
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted;
        byte[] decoded;
        decoded = android.util.Base64.decode(encrypted_message.getBytes(), 0);
        decrypted = new String(cipher.doFinal(decoded));

        // возврат дешифрованного текста
        return decrypted;
    }

}
