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
import android.net.*;
import android.os.*;
import android.widget.*;

import com.jefferson.application.br.model.FileModel;
import com.jefferson.application.br.task.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.util.JDebug;

import androidx.annotation.NonNull;
import com.jefferson.application.br.activity.ImportMediaActivity;

public class ReceiverMedia extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String action = intent.getAction();

		try {
			onReceive(action, intent);
		} catch (Exception e) {
			Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void onReceive(String action, Intent intent) throws Exception {

		if (action.equals(Intent.ACTION_SEND)) {
			Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            String path = Storage.getPath(uri, this);

            if (path == null) {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
            }

            File mFile = new File(path);
			ArrayList<FileModel> models = new ArrayList<>();
			FileModel model = getModel(mFile.getPath());

            if (model != null) {
				models.add(model);
                startImportActivity(models);
		    }
		} else if (action.equals(intent.ACTION_SEND_MULTIPLE)) {
			ArrayList<Uri> mediaUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			BuildModelsTast mTask = new BuildModelsTast(mediaUris, this);	
			mTask.start();
			//task here
		}
    }

    private void startImportActivity(ArrayList<FileModel> models) {
        Intent intent = new Intent(this, ImportMediaActivity.class);
        intent.putParcelableArrayListExtra(ImportMediaActivity.MODELS_KEY, models);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

	private FileModel getModel(String res) {
		FileModel model = new FileModel();
		model.setResource(res);

		if (MediaFilter.isImage(new File(res))) {
			//model.setDestination(Storage.getFolder(Storage.IMAGE).getAbsolutePath());
			model.setType(FileModel.IMAGE_TYPE);
		} else if (MediaFilter.isVideo(new File(res))) {
			//model.setDestination(Storage.getFolder(Storage.VIDEO).getAbsolutePath());
			model.setType(FileModel.VIDEO_TYPE);
		} else return null;

		return model;
	}

	private class BuildModelsTast extends JTask {

		private ArrayList<Uri> mediaUris;
		private Activity activity;
		private ProgressDialog mProgressDialog;
        ArrayList<FileModel> models = new ArrayList<>();

		public BuildModelsTast(ArrayList<Uri> mediaUris, Activity activity) {
			this.mediaUris = mediaUris;
			this.activity = activity;
		}

        @Override
        public void workingThread() {
            int index = 0;

            for (Uri uri : mediaUris) {
                sendUpdate(++index);
                String path = null;
                try {
                    path = Storage.getPath(uri, activity);
                } catch (Exception e) {
                    JDebug.toast("Trying uribto file " + uri.getPath());
                }

                if (path == null) {
                    continue;
                }
                FileModel model = getModel(path);
                if (model != null)
                    models.add(model);
			}
        }

        @Override
        public void onStarted() {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setTitle("Preparing...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        public void onFinished() {
            mProgressDialog.cancel();

            if (models.size() > 0) {
                startImportActivity(models);
            } else {
                Toast.makeText(activity, "Invalid data!", Toast.LENGTH_LONG).show();
                finish();
			}
            
            
        }

        @Override
        public void onException(Exception e) {
            mProgressDialog.cancel();
            Toast.makeText(ReceiverMedia.this, "Error", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onUpdated(Object[] get) {
            Integer index = (Integer)get[0];
            mProgressDialog.setMessage(index + " of " + mediaUris.size());

        }
	}
    public String gambiarra(@NonNull Uri uri) {
        //will not work :/
        return null;
    }
}
	

	
