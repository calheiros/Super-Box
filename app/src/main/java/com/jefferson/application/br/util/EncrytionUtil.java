package com.jefferson.application.br.util;
import android.util.Base64;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncrytionUtil {

	//public static String Key = "&MY_PASSWORD_IS_NOT_EASY$";

	public static String getEncryptedString(SecretKey key, String target) {
		String result = null;
		try {
			Cipher cipher = Cipher.getInstance("ARC4");
			cipher.init(cipher.ENCRYPT_MODE, key);
			byte[] encrypted = cipher.doFinal(target.getBytes());
			result = Base64.encodeToString(encrypted, Base64.DEFAULT);
		} catch (Exception e) {
            e.printStackTrace();
        }
		return result;
	}
    
    public static byte[] generateSalt() {
        Random r = new SecureRandom();
        byte[] salt = new byte[36];
        r.nextBytes(salt);
        return salt;
    }
    
//	private static Key getKey() {
//		Key key = new SecretKeySpec(Key.getBytes(), "ARC4");
//		return key;
//	}
    
    public static SecretKey getStoredKey(File file){
        byte[] raw = Storage.readFileToByte(file);
        SecretKey key = new SecretKeySpec(raw, "PBKDF2WithHmacSHA1");
        return key;
    }
	public static String getDecryptedString(SecretKey key, String target) {
		String result = null;
		try {
			Cipher cipher = Cipher.getInstance("ARC4");
			cipher.init(cipher.DECRYPT_MODE, key);

			byte[] encrypted = Base64.decode(target, Base64.DEFAULT);
		    byte[] data = cipher.doFinal(encrypted);
			result = new String(data);
		} catch (Exception e) {
            e.printStackTrace();
        }

		return result;
	}

    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase 
        // computation time. You should select a value that causes computation 
        // to take >100ms. final 
        int iterations = 1000; 
        // Generate a 256-bit key 
        final int outputKeyLength = 256;                           //  { PBKDF2WithHmacSHA1 }
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); 
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength); 
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey; 
    }
}
