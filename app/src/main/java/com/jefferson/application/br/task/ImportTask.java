package com.jefferson.application.br.task;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.app.ProgressThreadUpdate;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.fragment.MainFragment;
import com.jefferson.application.br.util.FileTransfer;
import com.jefferson.application.br.util.StringUtils;
import com.jefferson.application.br.util.Storage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import android.content.Context;
import java.io.FileNotFoundException;
import com.jefferson.application.br.util.JDebug;

public class ImportTask extends JTask {

    @Override
    public void onException(Exception e) {
        JDebug.writeLog(e.getCause());
        err_message.append("Erro inesperado ocorrido!");
        err_count = 1;
        revokeFinish(false);
        //synchronize();
    }

    public static final int SESSION_OUTSIDE_APP = 1;
	public static final int SESSION_INSIDE_APP = 2;

    private ArrayList<String> importedFilesPath = new ArrayList<>();
	private int maxProgress;
	private PathsData db;
    private ArrayList<FileModel> models;
	private SimpleDialog myAlertDialog;
	private PathsData.Folder folderDatabase;
	private StringBuilder err_message = new StringBuilder();
	private int err_count = 0;
	private FileTransfer mTransfer;
	private ProgressThreadUpdate mUpdate;
	private boolean waiting = false;
    private Listener listener;
	private String WARNING_ALERT = "warning_alert";
	private String no_left_space_error_message = "\nNão há espaço suficiente no dispositivo\n";
    private Context context;
    private static final String TAG = "ImportTask";

	public ImportTask(Context context, ArrayList<FileModel> models, Listener listener) {
        this.context = context;
		this.listener = listener;
		this.maxProgress = models.size();
		this.models = models;
		this.folderDatabase = PathsData.Folder.getInstance(context);
        this.db = PathsData.getInstance(context, Storage.getDefaultStorage());
		this.mTransfer = new FileTransfer();
	}

	@Override
	public void onBeingStarted() {
        if (listener != null) {
            listener.onBeingStarted();
        }

        myAlertDialog = new SimpleDialog(context, SimpleDialog.PROGRESS_STYLE);
		myAlertDialog.setCancelable(false);
        myAlertDialog.setSingleLineMessage(true);
		myAlertDialog.setTitle(context.getString(R.string.movendo))
			.setNegativeButton(context.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){

				@Override
				public boolean onClick(SimpleDialog dialog) {
					mTransfer.cancel();
					interrupt();

                    if (listener != null) {
                        listener.onInterrupted();
                    }

					return true;
				}
			}
        ).show();

		mUpdate = new ProgressThreadUpdate(mTransfer, myAlertDialog);
	}

    @Override
	public void onFinished() {
        synchronize();

        if (listener != null) {
            listener.onFinished();
        }

        if (isInterrupted()) {
            return;
        }

		String message = err_count > 0 ? "Transferencia completada com " + err_count + " " + (err_count > 1 ? "erros": "erro") + ":\n"  + err_message.toString() : context.getString(R.string.transferencia_sucesso);
		myAlertDialog.setStyle(SimpleDialog.ALERT_STYLE);
		myAlertDialog.setTitle(context.getString(R.string.resultado));
		myAlertDialog.setMessage(message);
        myAlertDialog.setCancelable(true);
		myAlertDialog.setPositiveButton("Ok", null).show();
		myAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
                    listener.onUserInteration();
				}
			}
        );
	}

	private void synchronize() {
		mUpdate.destroy();
		myAlertDialog.dismiss();
		Storage.scanMediaFiles(importedFilesPath.toArray(new String[importedFilesPath.size()]));
        folderDatabase.close();
		db.close();
	}

    @Override
    public void onInterrupted() {
        if (listener != null) {
            listener.onInterrupted();
        }
		Toast.makeText(context, context.getString(R.string.canceledo_usuario), 1).show();
	}

	@Override
	public void onUpdated(Object[] values) { 

		if (WARNING_ALERT.equals(values[0])) {
			warningAlert((String)values[1]);
		} else {
			String name = (String)values[1];
			myAlertDialog.setMessage(name);
		}
	}

    @Override
	public void workingThread() {
		long max = 0;

        for (FileModel resource : models) {
            File file = new File(resource.getResource());
            max += file.length();
        }

		File target = new File(Storage.getDefaultStorage());

		if ((target.getFreeSpace() < max)) {
			sendUpdate(WARNING_ALERT, context.getString(R.string.sem_espaco_aviso));
			waitForResponse();
		}

		max /= 1024;
		mUpdate.setMax(max);
		mUpdate.start();

        for (FileModel model: models) {
            if (isInterrupted()) return;

            File file = new File(model.getResource());
            if (!file.exists()) {
                err_count++;
                err_message.append("\n" + context.getString(R.string.erro) + " " + err_count + ": O arquivo \"" + file.getName() + "\" não existe!\n");
                continue;
            }

            sendUpdate(null, file.getName());

            String folderName = file.getParentFile().getName();
            String randomString = StringUtils.getRandomString(24);
            String randomString2 = StringUtils.getRandomString(24);
            String folderId = folderDatabase.getFolderId(folderName, model.getType());
            String str = folderId;

            if (folderId == null) {
                folderDatabase.addName(randomString2, folderName, model.getType());
            } else {
                randomString2 = str;
            }

            String parentPath = model.getParentPath();
            String root = parentPath == null ? model.getDestination() + File.separator + randomString2 : parentPath;
            File destFile = new File(root, randomString);
            destFile.getParentFile().mkdirs();

            if (file.renameTo(destFile)) {
                db.insertData(randomString, model.getResource());
                importedFilesPath.add(file.getAbsolutePath());
                mTransfer.increment(destFile.length() / 1024);
                Log.i(TAG, "Succesfully moved to: " + destFile);
            } else {
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    outputStream = new FileOutputStream(destFile);
                } catch (FileNotFoundException e) {
                    continue;
                }
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
                        err_message.append("\n" + context.getString(R.string.erro) + err_count + ": " + response + " when moving: " + file.getName() + "\n");
                    }
                }
            }
        }
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
		SimpleDialog dialog = new SimpleDialog(context, SimpleDialog.ALERT_STYLE);
		dialog.setTitle("Aviso");
		dialog.setMessage(msg);
		dialog.setCancelable(false);
		dialog.setPositiveButton("Continuar", new SimpleDialog.OnDialogClickListener() {

				@Override
				public boolean onClick(SimpleDialog dialog) {
					waiting = false;
					return true;
				}
			});

		dialog.setNegativeButton(context.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){

				@Override
				public boolean onClick(SimpleDialog dialog) {
					interrupt();
					waiting = false;
					return true;
				}
			});
		dialog.show();
	}

    public interface Listener {
        void onBeingStarted()
        void onUserInteration()
        void onInterrupted()
        void onFinished()
    }
}
