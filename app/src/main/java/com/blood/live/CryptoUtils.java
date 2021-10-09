package com.blood.live;

import android.util.Log;

import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    //static String secret_message = "Testing sreenivas";
    static String encrypt_key = "BCA2164F-0D1B-4185-81DA-4185-81D";
    static byte key_to_bytes[] = encrypt_key.getBytes();
    static byte iv[] =  "BILPL9Success@9*".getBytes();//new byte[16];
    public static void main(String secret_message) {
//        SecureRandom my_rand = new SecureRandom(); // Use the Java secure PRNG
//        my_rand.nextBytes(iv); // Generate a new IV every time!
        try {
            byte[] cipher = encrypt(secret_message, key_to_bytes);

            System.out.print("IV:\t\t\t");
            for (int i=0; i<iv.length; i++) System.out.print(new Integer(iv[i])+" ");

            System.out.print("\ncipher text:\t\t");
            for (int i=0; i<cipher.length; i++) System.out.print(new Integer(cipher[i])+" ");

            String decrypted = decrypt(cipher, key_to_bytes);
            System.out.println("\ndecrypted plain text:\t" + decrypted);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String plainText, byte[] enc_key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(enc_key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
        String encryptedValue = Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
        return cipher.doFinal(plainText.getBytes());
    }

    public static String decrypt(byte[] cipherText, byte[] enk_key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(enk_key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv));
        return new String(cipher.doFinal(cipherText));
    }

    public static String getencryptedString(String plainText){
        String encryptedValue = "";
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(key_to_bytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
            encryptedValue = Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
            encryptedValue = URLEncoder.encode(encryptedValue, "UTF-8");
        }catch (Exception ex){
            Log.e("encryptString: ", ex.getMessage());
        }
        return  encryptedValue;
    }
}