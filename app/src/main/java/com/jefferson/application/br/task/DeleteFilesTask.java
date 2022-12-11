package com.jefferson.application.br.task;

import android.content.*;
import android.widget.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.app.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;

public class DeleteFilesTask extends JTask {

    public int progress;
    private List<String> items;
    public SimpleDialog dialog;
    private Context context;
    private int position;
    private File rootFile;
    private boolean deletedAll;
    private JTask.OnFinishedListener listener;

    public DeleteFilesTask(Context context, ArrayList<String> items, int position, File rootFile) {
        this.items = items;
        this.position = position;
        this.rootFile = rootFile;
        this.context = context;
    }

    public boolean deletedAll() {
        return deletedAll;
    }

    @Override
    public void workingThread() {
        PathsDatabase database = PathsDatabase.getInstance(context, Storage.getDefaultStoragePath());
        try {
            for (String path : items) {
                if (isInterrupted()) {
                    break;
                }

                File file = new File(path);
                if (file.delete()) {
                    progress++;
                    String name = null;
                    if ((name = database.getPath(file.getName())) != null) {
                        database.deleteData(file.getName());
                    }
                    sendUpdate(path, name);
                }
            }
        } finally {
            database.close();
        }
    }

    @Override
    public void onBeingStarted() {
        dialog = new SimpleDialog(context);
        dialog.showProgressBar(!items.isEmpty())
            .setTitle("Excluindo")
            .setMax(items.size())
            .setProgress(0)
            .showPositiveButton(false)
            .setNegativeButton(context.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){
                @Override
                public boolean onClick(SimpleDialog dialog) {
                    interrupt();
                    return true;
                }
            }
        );
        if (items.size() > 10) {
            dialog.show();
        }
    }

    @Override
    public void onFinished() {
        dialog.cancel();
        deletedAll = rootFile.list().length == 0; 

        if (deletedAll) {
            deleteFolder(rootFile);
        }

        if (listener != null) {
            listener.onFinished();
        }
    }

    @Override
    public void setOnFinishedListener(JTask.OnFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onException(Exception e) {
        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onInterrupted() {
        super.onInterrupted();
        Toast.makeText(context, context.getString(R.string.canceledo_usuario), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUpdated(Object[] get) {
        super.onUpdated(get);
        dialog.setProgress(progress);
		dialog.setMessage((String)get[1]);
    }

	private void deleteFolder(File file) {
        PathsDatabase.Folder database = PathsDatabase.Folder.getInstance(context);
        try {
            if (file.delete()) {
                database.delete(file.getName(), position == 0 ? FileModel.IMAGE_TYPE: FileModel.VIDEO_TYPE);
            }
        } finally {
            database.close();
        }
	}
}
