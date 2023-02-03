/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import androidx.appcompat.app.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.LinearLayout.*;
import java.io.*;
import java.nio.channels.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.util.Base64;

public class CodeManager {
	
	public static final int UNLUCK_TO_ENTER = 87;
	public static final int UNLOCK_TO_FINISH = 53;

	public static void finishAllActivity(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			activity.finishAffinity();
		} else {
			//activity.startActivity(new Intent(activity, finishAll.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
			activity.overridePendingTransition(0, 0);
		}
	}
	
    public static boolean needPermissionForGetUsages(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				PackageManager packageManager = context.getPackageManager();
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
				AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
				int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
				return  (mode != AppOpsManager.MODE_ALLOWED);
			} catch (PackageManager.NameNotFoundException e) {
			}
		}
		return false;
	}
	
    public static void decrypteFile(SecretKey skey, File input, File output) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		File inpfile = input;
		int read;
		if (!inpfile.exists())
			return;
		File decfile = output;
		if (!decfile.exists())
			decfile.createNewFile();

		FileInputStream encfis = new FileInputStream(inpfile);
		FileOutputStream decfos = new FileOutputStream(decfile);

		Cipher decipher = Cipher.getInstance("ARC4");
		decipher.init(Cipher.DECRYPT_MODE, skey);
		CipherOutputStream cos = new CipherOutputStream(decfos, decipher);

		byte[] buff = new byte[1024];

		while ((read = encfis.read(buff)) != -1) {
			cos.write(buff, 0, read);
			cos.flush();
		}
		cos.close();
	}
	
    public static void encrypteFile(SecretKey skey, File input) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		FileInputStream fis = new FileInputStream(input);
		File outfile = new File(Environment.getExternalStorageDirectory(), "cripData");
		int read;
		if (!outfile.exists())
			outfile.createNewFile();

		FileOutputStream fos = new FileOutputStream(outfile);

		Cipher encipher = Cipher.getInstance("ARC4");

		encipher.init(Cipher.ENCRYPT_MODE, skey);
		CipherInputStream cis = new CipherInputStream(fis, encipher);

		byte[] buff = new byte[1024];
		while ((read = cis.read(buff)) != -1) {
			fos.write(buff, 0, read);
			fos.flush();
		}   
		fos.close();
	}
    
	public static SecretKey getKey(String key) {
		return new SecretKeySpec(key.getBytes(), "ARC4");
	}
	
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
											bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		//canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
						  bitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		Bitmap _bmp = Bitmap.createScaledBitmap(output, 100, 100, true);
		return _bmp;
	}

	public static boolean deleteContact(Context ctx, String phone, String name) {
		Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(phone));
		Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);

		return false;
	}

	public static String ReadText(File file) {
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
			}
			br.close();
		} catch (IOException e) {

		}

		return text.toString();
	}

	public static void WriterText(byte[] etTexto, File file) {
		try {
			FileOutputStream fosExt = new FileOutputStream(file);
			fosExt.write(etTexto);

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	public static String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

		return imageEncoded;
	}
	
    public static Bitmap decodeBase64(String input) {
		byte[] decodedByte = Base64.decode(input, 0);
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length); 
	}

	public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
	public static void updateTitle(String selecionados, String title, ActionBar action) {
		title = title + "(" + selecionados + ")";
		action.setTitle(title);

	}

	public static void criarAviso(String Root) {
		try {
			File aviso = new File(Root + "/.application", "DONT_REMOVE_THIS_FOLDER");
			if (!aviso.exists()) {
				aviso.createNewFile();
			}
		} catch (IOException e) {
		}
	}

	public static void isInBackground(int level, Activity mActivity) {
		if (level == mActivity.TRIM_MEMORY_UI_HIDDEN) {
			mActivity.finish();
		}
	}
	
    public static String gerarNome() {

		String letras = "abcdefghijklmnopqrstuvywxzABCDEFGHIJKLMNOPQRSTUVYWXZ0123456789";  

		Random random = new Random();  

		String armazenaChaves = "";  
		int index = -1;  
		for (int i = 0; i < 15; i++) {  
			index = random.nextInt(letras.length());  
			armazenaChaves += letras.substring(index, index + 1);  
		}  
		System.out.println(armazenaChaves);
		return armazenaChaves;
	}

	public static boolean moveFile(File sourceFile, File destFile, Context a, boolean lembrarCaminho) {
		try {
			if (!sourceFile.exists()) {
				return false;
			}

			FileChannel source = null;
			FileChannel destination = null;

			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();

			if (destination != null && source != null) {
				destination.transferFrom(source, 0, source.size());
				source.close();
				destination.close();
				if (lembrarCaminho)
					callBroadCast(sourceFile, a);
				else
					callBroadCast(destFile, a);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
    
	public static void callDialog(String Title, String message, int IdIcon, Context mContext) {
		AlertDialog.Builder mBuild = new AlertDialog.Builder(mContext);
		mBuild.setTitle(Title)
			.setMessage(message)
			.setIcon(IdIcon)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2) {
					p1.dismiss();
				}
			});
		mBuild.create().show();
	}

	public static void copyPath(File selecionada, String nome) {

		String caminhoPath="/.application/.cache/path";
		File filePath = new File(android.os.Environment.getExternalStorageDirectory() + caminhoPath, nome);
		filePath.getParentFile().mkdirs();

		FileOutputStream fosExt = null;

		try {
			fosExt = new FileOutputStream(filePath);
			fosExt.write(selecionada.getPath().getBytes());
			fosExt.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
    public static void callBroadCast(File file, Context Context) {

		if (Build.VERSION.SDK_INT >= 19) {
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri contentUri = Uri.fromFile(file);
			mediaScanIntent.setData(contentUri);
			Context.sendBroadcast(mediaScanIntent);
			Log.e("Scanner", "por senBroadcast");
		} else {
			ScannerMenor19(file, Context);
		}
	}

	public static void ScannerMenor19(File file, Context context) {
		Log.e("SDK", " Atual< 19");
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + file))); 
	}

	public static Bitmap getResizedBitmap(int maxSize, String filePhat) {

		Bitmap Bit = BitmapFactory.decodeFile(filePhat);
        int width = Bit.getWidth();
        int height = Bit.getHeight();


        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);

        }

		Bit = Bitmap.createScaledBitmap(Bit, width, height, true);


		return Bit;
	}
	public static boolean checkCameraFront(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
			return true;
		} else {
			return false;
		}
	}
	public static void onStop(Activity a) {
		boolean isScreenOn=true; 
		PowerManager pm = (PowerManager) 
			a.getSystemService(Context.POWER_SERVICE);
		if (Build.VERSION.SDK_INT >= 21) {
			isScreenOn = pm.isInteractive();
		} else {
			isScreenOn = pm.isScreenOn();
		}
		if (!isScreenOn) {
			a.finish();
		}

	}
	public static void expand(final View v) {
		v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final int targetHeight = v.getMeasuredHeight();

		// Older versions of android (pre API 21) cancel animations for views with a height of 0.
		v.getLayoutParams().height = 1;
		v.setVisibility(View.VISIBLE);
		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				v.getLayoutParams().height = interpolatedTime == 1
                    ? LayoutParams.WRAP_CONTENT
                    : (int)(targetHeight * interpolatedTime);
				v.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		// 1dp/ms
		a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 4);
		v.startAnimation(a);
	}

	public static void collapse(final View v) {
		final int initialHeight = v.getMeasuredHeight();

		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					v.setVisibility(View.GONE);
				} else {
					v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density) * 4);
		v.startAnimation(a);
	}
}
