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

package com.jefferson.application.br.task;

import android.app.Activity;
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
    private Activity activity;
    private int position;
    private File rootFile;
    private boolean deletedAll;
    private JTask.OnFinishedListener listener;

    public DeleteFilesTask(Activity activity, ArrayList<String> items, int position, File rootFile) {
        this.items = items;
        this.position = position;
        this.rootFile = rootFile;
        this.activity = activity;
    }

    public boolean deletedAll() {
        return deletedAll;
    }

    @Override
    public void workingThread() {
        PathsDatabase database = PathsDatabase.getInstance(activity, Storage.getDefaultStoragePath());
        try {
            for (String path : items) {
                if (isInterrupted()) {
                    break;
                }

                File file = new File(path);
                if (file.delete()) {
                    progress++;
                    String name = null;
                    if ((name = database.getMediaPath(file.getName())) != null) {
                        database.deleteMediaData(file.getName());
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
        dialog = new SimpleDialog(activity);
        dialog.showProgressBar(!items.isEmpty())
            .setTitle("Excluindo")
            .setMax(items.size())
            .setProgress(0)
            .showPositiveButton(false)
            .setNegativeButton(activity.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){
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
        Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onInterrupted() {
        super.onInterrupted();
        Toast.makeText(activity, activity.getString(R.string.canceledo_usuario), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUpdated(Object[] get) {
        super.onUpdated(get);
        dialog.setProgress(progress);
        dialog.setMessage((String)get[1]);
    }

	private void deleteFolder(File file) {
        PathsDatabase database = PathsDatabase.getInstance(activity);
        try {
            if (file.delete()) {
                database.deleteFolder(file.getName(), position == 0 ? FileModel.IMAGE_TYPE: FileModel.VIDEO_TYPE);
            }
        } finally {
            database.close();
        }
	}
}
