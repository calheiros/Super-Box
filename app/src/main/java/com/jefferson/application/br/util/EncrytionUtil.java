package com.jefferson.application.br.util;
import javax.crypto.*;
import java.security.*;
import javax.crypto.spec.*;
import android.util.*;

public class EncrytionUtil {
    
	public static String Key = "I love you:)";

	public static String getEncryptedString(String target) {
		String result = null;
		try {
			Cipher cipher = Cipher.getInstance("ARC4");
			cipher.init(cipher.ENCRYPT_MODE, getKey());
			byte[] encrypted = cipher.doFinal(target.getBytes());
			result = Base64.encodeToString(encrypted, Base64.DEFAULT);
		} catch (Exception e){
            e.printStackTrace();
        }
		return result;
	}

	private static Key getKey() {
		Key key = new SecretKeySpec(Key.getBytes(), "ARC4");
		return key;
	}
    
	public static String getDecryptedString(String target) {
        
		String result = null;
		try {
			Cipher cipher = Cipher.getInstance("ARC4");
			cipher.init(cipher.DECRYPT_MODE, getKey());
			
			byte[] encrypted = Base64.decode(target, Base64.DEFAULT);
		    byte[] data = cipher.doFinal(encrypted);
			result = new String(data);
		}
		catch (Exception e) {
            e.printStackTrace();
        }

		return result;
	}
}
