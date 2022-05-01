package com.jefferson.application.br.task;

import android.content.Context;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.util.FileTransfer;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ImportTask extends JTask {

    private Exception error = null;

    @Override
    public void onException(Exception e) {
        error = e;
        failuresCount = 1;
        revokeFinish(false);
        errorMessage.append(e.getMessage());
    }
    
    public static final int PREPARATION_UPDATE = 1;
    public static final int PROGRESS_UPDATE = 2;
    private ArrayList<String> importedFilesPath = new ArrayList<>();
	private int maxProgress;
    private ArrayList<FileModel> models;
    private WatchTransference watchTransfer;
	private StringBuilder errorMessage = new StringBuilder();
	private int failuresCount = 0;
	private FileTransfer mTransfer;
	private boolean waiting = false;
    private Listener listener;
	private String no_left_space_error_message = "\nNão há espaço suficiente no dispositivo\n";
    private Context context;
    private static final String TAG = "ImportTask";

	public ImportTask(Context context, ArrayList<FileModel> models, Listener listener) {
        this.context = context;
		this.listener = listener;
		this.maxProgress = models.size();
		this.models = models;
		this.mTransfer = new FileTransfer();
	}
    
    public int getFailuresCount() {
        return failuresCount;
    }

    public boolean isWaiting() {
        return waiting;
    }
    
	@Override
	public void onBeingStarted() {
        if (listener != null) {
            listener.onBeingStarted();
        }

    }
    public Exception error(){
        return error;
    }
    @Override
    protected void onTaskCancelled() {
        super.onTaskCancelled();
        Toast.makeText(App.getAppContext(), "Task cancelled!", Toast.LENGTH_SHORT).show();
    }

    @Override
	public void onFinished() {
        synchronize();

        if (listener != null) {
            listener.onFinished();
        }
	}

	private void synchronize() {
		Storage.scanMediaFiles(importedFilesPath.toArray(new String[importedFilesPath.size()]));
	}

    @Override
    public void onInterrupted() {
        if (listener != null) {
            listener.onInterrupted();
        }
		Toast.makeText(context, context.getString(R.string.canceledo_usuario), 1).show();
	}

	@Override
	public void onUpdated(Object[] values) {}

    @Override
	public void workingThread() {
		long max = 0;
        PathsData database = PathsData.getInstance(context, Storage.getDefaultStorage());
        PathsData.Folder folderDatabase = PathsData.Folder.getInstance(context);

        try {
            for (FileModel resource : models) {
                File file = new File(resource.getResource());
                max += file.length();
            }
            
            File target = new File(Storage.getDefaultStorage());

            if ((target.getFreeSpace() < max)) {
                sendUpdate(-2, context.getString(R.string.sem_espaco_aviso));
                waitForResponse();
            }

            max /= 1024;
            sendUpdate(PROGRESS_UPDATE, null, null, max);
            //mUpdate.setMax(max);
            //mUpdate.start();
            watchTransfer = new WatchTransference(this, mTransfer);
            watchTransfer.start();

            for (int i = 0; i < models.size(); i++) {
                if (isInterrupted()) {
                    break;
                }
 
                FileModel model = models.get(i);
                File file = new File(model.getResource());
                
                if (!file.exists()) {
                    failuresCount++;
                    errorMessage.append("\n" + context.getString(R.string.erro) + " " + failuresCount + ": O arquivo \"" + file.getName() + "\" não existe!\n");
                    continue;
                }

                sendUpdate(PROGRESS_UPDATE, file.getName(), null, null);

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

                if (parentPath == null) {
                    parentPath = Storage.getFolder(FileModel.IMAGE_TYPE.equals(model.getType()) ? Storage.IMAGE: Storage.VIDEO) + File.separator + randomString2;
                }

                File destFile = new File(parentPath, randomString);
                destFile.getParentFile().mkdirs();

                if (file.renameTo(destFile)) {
                    database.insertData(randomString, model.getResource());
                    importedFilesPath.add(file.getAbsolutePath());
                    mTransfer.increment(destFile.length() / 1024);
                    //Log.i(TAG, "Succesfully moved to: " + destFile);
                } else {
                    InputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                        outputStream = new FileOutputStream(destFile);
                    } catch (FileNotFoundException e) {
                        failuresCount++;
                        continue;
                    }
                    String response = mTransfer.transferStream(inputStream, outputStream);

                    if (FileTransfer.OK.equals(response)) {
                        if (Storage.deleteFile(file)) {
                            database.insertData(randomString, model.getResource());
                            importedFilesPath.add(file.getAbsolutePath());
                        } else {
                            destFile.delete();
                        }
                    } else {
                        destFile.delete();
                        failuresCount++;
                        if (FileTransfer.Error.NO_LEFT_SPACE.equals(response)) {
                            errorMessage.append(no_left_space_error_message);
                        } else {
                            errorMessage.append("\n" + context.getString(R.string.erro) + failuresCount + ": " + response + " when moving: " + file.getName() + "\n");
                        }
                    }
                }
                sendUpdate(PREPARATION_UPDATE, (i + 1) - failuresCount, models.size());
            }
            
        } finally {
           database.close();
        }
	}

    public void waitForResponse() {
		waiting = true;
		while (waiting) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
                break;
            }
		}
	}

    public void continueWork() {
        waiting = false;
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

    private class WatchTransference extends Thread {

        private JTask task;
        private FileTransfer transfer;

        public WatchTransference(JTask task, FileTransfer transfer) {
            this.task = task;
            this.transfer = transfer;
        }

        @Override
        public void run() {
            super.run();

            while (task.status == JTask.Status.STARTED) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
                task.sendUpdate(PROGRESS_UPDATE, null, transfer.getTransferedKbs(), null);
            }
        }
    }
}
