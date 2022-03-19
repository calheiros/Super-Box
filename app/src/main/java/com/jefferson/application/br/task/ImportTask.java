package com.jefferson.application.br.task;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import com.google.android.gms.ads.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.activity.*;
import com.jefferson.application.br.app.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.fragment.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.R;

public class ImportTask extends AsyncTask {

    public static final int SESSION_OUTSIDE_APP = 1;
	public static final int SESSION_INSIDE_APP = 2;

    private ArrayList<String> importedFilesPath = new ArrayList<>();
	private int maxProgress;
	private PathsData db;
    private ArrayList<FileModel> models;
	private SimpleDialog myAlertDialog;
	private Activity myActivity;
	private int mode;
	private PathsData.Folder folderDatabase;
	private StringBuilder err_message = new StringBuilder();
	private int err_count = 0;
	private FileTransfer mTransfer;
	private ProgressThreadUpdate mUpdate;
	private boolean waiting = false;
	private String WARNING_ALERT = "warning_alert";
	private String no_left_space_error_message = "\nNão há espaço suficiente no dispositivo\n";

	public ImportTask(ArrayList<FileModel> models, Activity activity, int mode) {

		this.myActivity = activity;
		this.maxProgress = models.size();
		this.mode = mode;
		this.models = models;
		this.folderDatabase = PathsData.Folder.getInstance(activity);
        this.db = PathsData.getInstance(activity, Storage.getDefaultStorage());
		this.mTransfer = new FileTransfer();

	}

	@Override
	protected void onPreExecute() {

	    if (SESSION_INSIDE_APP == mode) {
			((MainActivity)myActivity).prepareAd();
		}
		myAlertDialog = new SimpleDialog(myActivity, SimpleDialog.PROGRESS_STYLE);
		myAlertDialog.setCancelable(false);
		myAlertDialog.setContentTitle(myActivity.getString(R.string.movendo))
			.setNegativeButton(myActivity.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){

				@Override
				public boolean onClick(SimpleDialog dialog) {
					mTransfer.cancel();
					cancel(true);
					return true;
				}
			})
			.show();

		mUpdate = new ProgressThreadUpdate(mTransfer, myAlertDialog);
	}
	
    @Override
	protected void onPostExecute(Object result) {

		synchronize();
		String message = err_count > 0 ? "Transferencia completada com " + err_count + " " + (err_count > 1 ? "erros": "erro") + ":\n"  + err_message.toString() : "Transferência concluída com sucesso.";
		myAlertDialog.setStyle(SimpleDialog.ALERT_STYLE);
		myAlertDialog.setContentTitle("Resultado");
		myAlertDialog.setContentText(message);
		myAlertDialog.setPositiveButton("Ok", new SimpleDialog.OnDialogClickListener(){

				@Override
				public boolean onClick(SimpleDialog progress) {
					if (mode == SESSION_OUTSIDE_APP)
						myActivity.finish();
					return true;
				}
			}).show();
		myAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (mode == SESSION_INSIDE_APP) {
						((MainActivity)myActivity).showAd();
					}
				}
			});
		folderDatabase.close();
		db.close();
	}

	private void synchronize() {

		mUpdate.die();
		myAlertDialog.dismiss();
		Storage.scanMediaFiles(importedFilesPath.toArray(new String[importedFilesPath.size()]));
		
        if (mode == SESSION_INSIDE_APP) {
		    ((MainActivity)myActivity)
				.update(MainFragment.ID.BOTH);
		} 
	}
	
    @Override
	protected void onCancelled(Object result) { 

		synchronize();
		if (mode == SESSION_OUTSIDE_APP) {
			myActivity.finish();
		}
		Toast.makeText(myActivity, "Cancelado pelo usuario!", 1).show();
	}

	@Override
	protected void onProgressUpdate(Object[] values) { 
		if (WARNING_ALERT.equals(values[0])) {
			warningAlert((String)values[1]);
		} else {
			String name = (String)values[1];
			myAlertDialog.setContentText(name);
		}
	}
	
    @Override
	protected Boolean doInBackground(Object[] v) {

		long max = 0;
        for (FileModel resource : models) {
            File file = new File(resource.getResource());
            max += file.length();
        }
		File target = new File(Storage.getDefaultStorage());

		if ((target.getFreeSpace() < max)) {
			publishProgress(WARNING_ALERT, "Pode não haver espaço no dispositivo para completar a tranfêrencia, quer tentar continuar mesmo assim?");
			waitForResponse();
		}
		max /= 1024;
		mUpdate.setMax(max);
		mUpdate.start();

		try {
			for (FileModel model: models) {
				if (isCancelled()) return null;

				File file = new File(model.getResource());
				if (!file.exists()) {
					err_count++;
					err_message.append("\n" + myActivity.getString(R.string.erro) + " " + err_count + ": O arquivo \"" + file.getName() + "\" não existe!\n");
					continue;
				}
				publishProgress(null, file.getName());

				String folderName = file.getParentFile().getName();
				String randomString = RandomString.getRandomString(24);
				String randomString2 = RandomString.getRandomString(24);
                String folderId = folderDatabase.getFolderId(folderName, model.getType());
				String str = folderId;

				if (folderId == null) {
					folderDatabase.addName(randomString2, folderName, model.getType());
				} else {
					randomString2 = str;
				}
                
				File destFile = new File(model.getDestination() + File.separator + randomString2 + File.separator + randomString);
				destFile.getParentFile().mkdirs();

				InputStream inputStream = new FileInputStream(file);
				OutputStream outputStream = new FileOutputStream(destFile);
				String response = mTransfer.transferStream(inputStream, outputStream);

				if (FileTransfer.OK.equals(response)) {
					if (Storage.deleteFile(file)) {
						db.insertData(randomString, model.getResource());
						importedFilesPath.add(file.getAbsolutePath());
					} else {
						destFile.delete();
					}
				} else {
					destFile.delete();
					err_count++;
					if (FileTransfer.Error.NO_LEFT_SPACE.equals(response)) {
						err_message.append(no_left_space_error_message);
						break;
					} else {
						err_message.append("\n" + myActivity.getString(R.string.erro) + err_count + ": " + response + " when moving: " + file.getName() + "\n");
					}
				}
			}
		} catch (Exception e) {
			err_message.append("Erro inesperado ocorrido!");
		}
        
		return true;
	}
	
    public void waitForResponse() {

		waiting = true;
		while (waiting) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
	}
	
    private void warningAlert(String msg) {

		SimpleDialog mDialog = new SimpleDialog(myActivity, SimpleDialog.ALERT_STYLE);
		mDialog.setContentTitle("Aviso");
		mDialog.setContentText(msg);
		mDialog.setCancelable(false);
		mDialog.setPositiveButton("Continuar", new SimpleDialog.OnDialogClickListener() {

				@Override
				public boolean onClick(SimpleDialog dialog) {
					waiting = false;
					return true;
				}
			});

		mDialog.setNegativeButton(myActivity.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){

				@Override
				public boolean onClick(SimpleDialog dialog) {
					cancel(true);
					waiting = false;
					return true;
				}
			});
		mDialog.show();
	}
}
