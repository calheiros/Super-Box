package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.FolderModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.adapter.PhotosFolderAdapter;
import java.util.ArrayList;
import com.jefferson.application.br.util.Debug;

public class GalleryAlbum extends MyCompatActivity {

    private boolean boolean_folder;
    private PhotosFolderAdapter obj_adapter;
	private int position;
	private Toolbar toolbar;
    private GridView gv_folder;
	private SharedPreferences sharedPrefrs;
    private static final int REQUEST_PERMISSIONS = 100;
	public static final int GET_CODE = 5658;
	private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_album);

        gv_folder = (GridView)findViewById(R.id.gv_folder);
		sharedPrefrs = PreferenceManager.getDefaultSharedPreferences(this);
	    position = getIntent().getExtras().getInt("position");

		if (position == 0 || position == 1) {
			title = (position == 0 ? getString(R.string.importar_imagem) : getString(R.string.importar_video));
			new LoadItems().execute();
		}
		setupToolbar();
	}

    public  String getType() {
		switch (position) {
			case 0:
				return FileModel.IMAGE_TYPE;
			case 1:
				return FileModel.VIDEO_TYPE;
		    default: throw new IllegalArgumentException();
		}
	}

	private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(title);

    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

    public ArrayList<FolderModel> fn_imagespath() {
	    ArrayList<FolderModel> al_images = new ArrayList<FolderModel>();

        Uri uri = null;
        Cursor cursor;
		String orderBy = null;
		String index_fname = null;
		String Bucket = null;

        int column_index_data, column_index_folder_name;

		if (position == 0) {
			Bucket = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			index_fname = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
			orderBy = MediaStore.Images.Media.DATE_TAKEN;
		}
		if (position == 1) {
			Bucket = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
			uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			index_fname = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
			orderBy = MediaStore.Video.Media.DATE_TAKEN;
		}

        String absolutePathOfImage = "";
        String[] projection = {MediaStore.MediaColumns.DATA, Bucket};

        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(index_fname);
        
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            String folderName = cursor.getString(column_index_folder_name);
            int folderPosition = getFolderIndex(al_images, folderName);

            if (folderPosition == -1) {
                FolderModel model = new FolderModel();
                model.setName(cursor.getString(column_index_folder_name));
                model.addItem(absolutePathOfImage);
                al_images.add(model);
            } else {
                al_images.get(folderPosition).addItem(absolutePathOfImage);
            }
        }
        cursor.close();

        return al_images;
    }

    private int getFolderIndex(ArrayList<FolderModel> list, String name) {

        if (name == null) name = FolderModel.NO_FOLDER_NAME;

        for (int i = 0; i < list.size(); i++) {
            String folderName = list.get(i).getName();
            if (name.equals(folderName)) 
                return i;
        }
        return -1;
    }

    private void setAdapter(ArrayList<FolderModel> list) {
		obj_adapter = new PhotosFolderAdapter(GalleryAlbum.this, list, position);
		gv_folder.setAdapter(obj_adapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			ArrayList paths =  (ArrayList) data.getExtras().get("selection");
			Intent i = new Intent();
			i.putExtra("selection", paths);
			i.putExtra("type", getType());
			i.putExtra("position", position);
			setResult(RESULT_OK, (i));
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
					for (int i = 0; i < grantResults.length; i++) {
						if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							fn_imagespath();
						} else {
							Toast.makeText(GalleryAlbum.this, "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
						}
					}
				}
		}
	}

	private class LoadItems extends AsyncTask<Void, Void, ArrayList<FolderModel>>  {

		private ProgressBar myProgress;
        public LoadItems() {
			this.myProgress = (ProgressBar) findViewById(R.id.galleryalbumProgressBar);
		}

		@Override
		protected void onPreExecute() {
			myProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(ArrayList<FolderModel> result) {
			myProgress.setVisibility(View.GONE);
	        if (result != null) {
				setAdapter(result);
			}
			super.onPostExecute(result);
		}

		@Override
		protected ArrayList<FolderModel> doInBackground(Void[] object) {
			return fn_imagespath();
		}
	}
}
