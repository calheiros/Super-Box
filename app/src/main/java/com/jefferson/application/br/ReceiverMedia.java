package com.jefferson.application.br;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v7.app.*;
import android.widget.*;
import com.jefferson.application.br.model.*;
import com.jefferson.application.br.task.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.task.*;
import android.support.v7.app.AlertDialog;
import com.jefferson.application.br.activity.MainActivity;

public class ReceiverMedia extends Activity implements ImportTask.TaskListener {

    @Override
    public void onPostExecute() {
        Intent intent = new Intent(MainActivity.ACTION_UPDATE);
        sendBroadcast(intent);
        JDebug.toast("send receiver!");
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onDialogDismiss() {
        finish();
    }

    @Override
    public void OnCancelled() {
    }

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
			File mFile = new File(Storage.getPathFromMediaUri(uri, this));

			ArrayList<FileModel> models = new ArrayList<>();
			FileModel model = getModel(mFile.getPath());
			
            if (model != null) {
				models.add(model);
				new ImportTask(this, models, ReceiverMedia.this).execute();
			} else {
				finish();
			}
            
		} else if (action.equals(intent.ACTION_SEND_MULTIPLE)) {
			ArrayList<Uri> mediaUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			BuildModelsTast mTask = new BuildModelsTast(mediaUris, this);	
			mTask.execute();
			//task here
		}
    }

	private FileModel getModel(String res) {
		FileModel model = new FileModel();
		model.setResource(res);

		if (MediaFilter.isImage(new File(res))) {
			model.setDestination(Storage.getFolder(Storage.IMAGE).getAbsolutePath());
			model.setType(FileModel.IMAGE_TYPE);
		} else if (MediaFilter.isVideo(new File(res))) {

			model.setDestination(Storage.getFolder(Storage.VIDEO).getAbsolutePath());
			model.setType(FileModel.VIDEO_TYPE);
		} else return null;

		return model;
	}

	private class BuildModelsTast extends AsyncTask<Void, Integer, ArrayList<FileModel>> {

		private ArrayList<Uri> mediaUris;
		private Activity activity;
		private ProgressDialog mProgressDialog;

		public BuildModelsTast(ArrayList<Uri> mediaUris, Activity activity) {
			this.mediaUris = mediaUris;
			this.activity = activity;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(activity);
			mProgressDialog.setTitle("Preparing...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer[] values) {
			super.onProgressUpdate(values);
			Integer index = values[0];
			mProgressDialog.setMessage(index + " of " + mediaUris.size());
		}

		@Override
		protected void onPostExecute(ArrayList<FileModel> result) {
			super.onPostExecute(result);
			mProgressDialog.dismiss();

			if (result.size() > 0) {
				new ImportTask(activity, result, ReceiverMedia.this).execute();
			} else {
				Toast.makeText(activity, "Arquivo(s) não suportado(s)", Toast.LENGTH_LONG).show();
				finish();
			}
		}

		@Override
		protected ArrayList<FileModel> doInBackground(Void[] p1) {

			ArrayList<FileModel> models = new ArrayList<>();
			int index = 0;

			for (Uri uri : mediaUris) {
				publishProgress(++index);
				String path = Storage.getPathFromMediaUri(uri, App.getInstance());
				FileModel model = getModel(path);
				
                if (model != null)
					models.add(model);
			}
			return models;
		}
	}
}
	

	
