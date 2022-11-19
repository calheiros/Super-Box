package com.jefferson.application.br.util;

import android.util.*;
import com.jefferson.application.br.app.*;
import java.io.*;
import java.nio.channels.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class FileTransfer {

	private boolean running = true;
    private double kilobytes = 0;
    public static final String OK = "_Ok";
    
    public void increment(double length) {
        this.kilobytes += length;
    }

	public double getTransferedKilobytes() {
		return kilobytes;
	}

	public static final class Error {
		public final static String NO_LEFT_SPACE = "java.io.IOException: write failed: ENOSPC (No space left on device)";
	}
    
	public String transferStream(InputStream inputStream, OutputStream outputStream) {

        try {
            byte[] bArr = new byte[4096];
            while (running) {
                int read = inputStream.read(bArr);

                if (read == -1) {
                    outputStream.close();
					inputStream.close();
                    return OK;
                }
                outputStream.write(bArr, 0, read);
                kilobytes += (double)read / 1024d;
            }
        } catch (IOException e) {
			return e.toString();
        }
		return "Interrupted";
    }
    
	public void cancel() {
		this.running = false;
	}

	public boolean moveFile(File source, File dest) {
		FileChannel sourceChannel = null;
		FileChannel destChannel = null;
	    try {
			sourceChannel = new FileInputStream(source).getChannel();
			destChannel = new FileOutputStream(dest).getChannel();

            if (sourceChannel != null && destChannel != null) {
			    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
			} else return false;

			sourceChannel.close();
			destChannel.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static class encrypetion {

		public boolean encryptFile(File res, File dest) {
			try {
				FileInputStream input = new FileInputStream(res);
				FileOutputStream output = new FileOutputStream(dest);

				SecretKey key = new SecretKeySpec("0x200 & 0xff".getBytes(), "ARC4");

				Cipher cipher = Cipher.getInstance("ARC4");
				cipher.init(Cipher.ENCRYPT_MODE, key);

				CipherOutputStream outCipher = new CipherOutputStream(output, cipher);
				byte[] buffer = new byte[1024];
				int count;

				while ((count = input.read(buffer)) != -1) {
					outCipher.write(buffer, 0, count);
				}
				outCipher.close();
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		public boolean decryptFile(File res, File dest) {
			return true;
		}
	}
}
