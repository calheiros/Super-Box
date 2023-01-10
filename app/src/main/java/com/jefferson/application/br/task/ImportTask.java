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
import android.widget.Toast;

import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.database.PathsDatabase;
import com.jefferson.application.br.util.FileTransfer;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ImportTask extends JTask {

    public static final int PREPARATION_UPDATE = 1;
    public static final int PROGRESS_UPDATE = 2;
    private static final String TAG = "ImportTask";
    private Exception error = null;
    private final ArrayList<String> importedFilesPath = new ArrayList<>();
    private final int maxProgress;
    private final ArrayList<FileModel> models;
    private WatchTransference watchTransfer;
    private final StringBuilder errorMessage = new StringBuilder();
    private int failuresCount = 0;
    private final FileTransfer mTransfer;
    private boolean waiting = false;
    private final Listener listener;
    private final String no_left_space_error_message = "\nNão há espaço suficiente no dispositivo\n";
    private final Activity activity;

    public ImportTask(Activity activity, ArrayList<FileModel> models, Listener listener) {
        this.activity = activity;
        this.listener = listener;
        this.maxProgress = models.size();
        this.models = models;
        this.mTransfer = new FileTransfer();
    }

    @Override
    public void onException(Exception e) {
        error = e;
        failuresCount = 1;
        revokeFinish(false);
        errorMessage.append(e.getMessage());
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

    public Exception error() {
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
        Toast.makeText(activity, activity.getString(R.string.canceledo_usuario), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdated(Object[] values) {
    }

    @Override
    public void workingThread() {
        double max = 0;
        PathsDatabase database = PathsDatabase.getInstance(activity, Storage.getDefaultStoragePath());
        PathsDatabase folderDatabase = PathsDatabase.getInstance(activity);

        try {
            for (FileModel resource : models) {
                File file = new File(resource.getResource());
                max += file.length();
            }

            File target = new File(Storage.getDefaultStoragePath());

            if ((target.getFreeSpace() < max)) {
                sendUpdate(-2, activity.getString(R.string.sem_espaco_aviso));
                waitForResponse();
            }

            max /= 1024;
            sendUpdate(PROGRESS_UPDATE, null, null, max);

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
                    errorMessage.append("\n" + activity.getString(R.string.erro) + " " + failuresCount + ": O arquivo \"" + file.getName() + "\" não existe!\n");
                    continue;
                }

                sendUpdate(PROGRESS_UPDATE, file.getName(), null, null);

                String folderName = file.getParentFile().getName();
                String randomString = StringUtils.getRandomString(24);
                String randomString2 = StringUtils.getRandomString(24);
                String folderId = folderDatabase.getFolderIdFromName(folderName, model.getType());
                String str = folderId;

                if (folderId == null) {
                    folderDatabase.addFolderName(randomString2, folderName, model.getType());
                } else {
                    randomString2 = str;
                }

                String parentPath = model.getParentPath();

                if (parentPath == null) {
                    parentPath = Storage.getFolder(FileModel.IMAGE_TYPE.equals(model.getType()) ? Storage.IMAGE : Storage.VIDEO) + File.separator + randomString2;
                }

                File destFile = new File(parentPath, randomString);
                destFile.getParentFile().mkdirs();

                if (file.renameTo(destFile)) {
                    database.insertMediaData(randomString, model.getResource());
                    importedFilesPath.add(file.getAbsolutePath());
                    mTransfer.increment((double) destFile.length() / 1024d);
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
                            database.insertMediaData(randomString, model.getResource());
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
                            errorMessage.append("\n" + activity.getString(R.string.erro) + failuresCount + ": " + response + " when moving: " + file.getName() + "\n");
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
                waiting = false;
                break;
            }
        }
    }

    public void continueWork() {
        waiting = false;
    }

    public void stopWaiting() {
        this.waiting = false;
    }

    public interface Listener {
        void onBeingStarted();

        void onUserInteraction();

        void onInterrupted();

        void onFinished();
    }

    private class WatchTransference extends Thread {

        private final JTask task;
        private final FileTransfer transfer;

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
                }
                task.sendUpdate(PROGRESS_UPDATE, null, transfer.getTransferredKilobytes(), null);
            }
        }
    }
}
