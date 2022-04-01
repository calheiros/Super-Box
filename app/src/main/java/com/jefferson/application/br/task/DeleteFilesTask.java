package com.jefferson.application.br.task;

import android.content.*;
import android.os.*;
import android.widget.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.app.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;

public class DeleteFilesTask extends AsyncTask {

	public int progress;
	private List<String> items;
	public SimpleDialog dialog;
	private Context context;
	private int position;
	private File rootFile;
	private PathsData mData;
    private PathsData.Folder folderDatabase;

	public DeleteFilesTask(Context context, ArrayList<String> items, int position, File rootFile) {
		this.items = items;
		this.position = position;
		this.rootFile = rootFile;
		this.context = context;
		File file = new File(Storage.getDefaultStorage());
		this.mData = PathsData.getInstance(context, file.getAbsolutePath());
        folderDatabase = PathsData.Folder.getInstance(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new SimpleDialog(context);
		dialog.showProgressBar(!items.isEmpty())
			.setTitle("Excluindo")
			.setMax(items.size())
			.setProgress(0)
			.showPositiveButton(false)
			.setNegativeButton(context.getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){
				@Override
				public boolean onClick(SimpleDialog dialog) {
					cancel(true);
					return true;
				}
			}
        );
		dialog.show();
	}

	@Override
	protected void onCancelled(Object result) {
		Toast.makeText(context, "Cancelado!", 1).show();
		//synchronizeData();
	}

	@Override
	protected void onPostExecute(Object result) {

		dialog.dismiss();

		if (rootFile.list().length == 0) {
			deleteFolder(rootFile);
		}
		//synchronizeData();
	}

	@Override
	protected void onProgressUpdate(Object[] values) {
		super.onProgressUpdate(values);
		dialog.setProgress(progress);
		dialog.setMessage((String)values[1]);
		//mAdapter.removeItem(values[0]);
	}

	@Override
	protected Object doInBackground(Object[] p1) {

		try {
			for (String item : items) {
				if (isCancelled()) {
					break;
				}
				File file = new File(item);

                if (file.delete()) {
					progress++;
					String name = null;
					if ((name = mData.getPath(file.getName())) != null) {
						mData.deleteData(file.getName());
					}
					publishProgress(item, name);
				}
			}
		} catch (Exception e) {}
		return 0;
	}

	private void deleteFolder(File file) {

        if (file.delete()) {
			folderDatabase.delete(file.getName(), position == 0 ? FileModel.IMAGE_TYPE: FileModel.VIDEO_TYPE);
		}
		folderDatabase.close();
	}
}
