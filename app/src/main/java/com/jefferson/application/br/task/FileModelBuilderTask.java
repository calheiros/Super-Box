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

import android.content.Context;

import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.util.JDebug;
import java.util.ArrayList;

import android.widget.Toast;

public class FileModelBuilderTask extends JTask {

    private final ArrayList<String> paths;
    private final String type;
    private final ArrayList<FileModel> models = new ArrayList<>();
    private final String parentPath;
    private final Context context;

    public FileModelBuilderTask(Context context, ArrayList<String> paths, String type, String parent) {
        this.paths = paths;
        this.type = type;
        this.context = context;
        this.parentPath = parent;
    }

    public ArrayList<FileModel>  getData() {
        return models;
    }

    @Override
    public void workingThread() {
        
        for ( int i = 0; i < paths.size(); i++) {
            if (isCancelled()) {
                break;
            }
            String path = paths.get(i);
            FileModel model = new FileModel();
            model.setResource(path);
            model.setParentPath(parentPath);
            model.setType(type);
            models.add(model);
            sendUpdate(1, i + 1, paths.size());
        }
    }
    
    @Override
    public void onBeingStarted() {

    }

    @Override
    protected void onTaskCancelled() {
        super.onTaskCancelled();
        Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onFinished() {
    }
    
    @Override
    public void onException(Exception e) {
        JDebug.writeLog(e.getCause());
    }
    
    public void setOnLoopListener(onLoopListener listener) {

    }

    public void setDestination(String absolutePath) {

    }

    public static interface onLoopListener {
       void onLoop(String path);
    }
}
