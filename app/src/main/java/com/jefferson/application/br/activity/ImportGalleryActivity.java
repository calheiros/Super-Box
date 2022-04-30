package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.FolderModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.ImportGalleryActivity;
import com.jefferson.application.br.adapter.PhotosFolderAdapter;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ImportGalleryActivity extends MyCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private PhotosFolderAdapter obj_adapter;
	private int position;
	private Toolbar toolbar;
    private GridView myGridView;
	private SharedPreferences sharedPrefrs;
    private static final int REQUEST_PERMISSIONS = 100;
	public static final int GET_CODE = 5658;
    private static final String TAG = "ImportGalleryActivity";

    private String title;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ImportGalleryActivity.RetrieveMediaTask retrieveMediaTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_gallery);
        myGridView = (GridView)findViewById(R.id.gv_folder);
		sharedPrefrs = MyPreferences.getSharedPreferences(this);
	    position = getIntent().getExtras().getInt("position");
        mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
	    mySwipeRefreshLayout.setOnRefreshListener(this);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        applyParentViewPadding(myGridView);
        
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true);
        int color = typedValue.data;
        
        mySwipeRefreshLayout.setProgressBackgroundColorSchemeColor(color);// .setProgressBackgroundColor(color);

        title = (position == 0 ? getString(R.string.importar_imagem) : getString(R.string.importar_video));
        retrieveMediaTask = new RetrieveMediaTask();
        retrieveMediaTask.start();
		setupToolbar();
	}
    
    @Override
    public void onRefresh() {
        if (retrieveMediaTask.getStatus() == JTask.Status.FINISHED) {
            obj_adapter.clear();
            retrieveMediaTask = new RetrieveMediaTask();
            retrieveMediaTask.start();
        } else {
            mySwipeRefreshLayout.setRefreshing(false);
        }
    }

    public String getType() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_from_gallery:
                notImplemented();
                break;
            case R.id.item_from_camera:
                notImplemented();
                break;
            case android.R.id.home:
                finish();
                break;
        }
		return super.onOptionsItemSelected(item);
	}

    private void notImplemented() {
        Toast.makeText(this, "Not implemented!", 1).show();
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
		} else if (position == 1) {
			Bucket = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
			uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			index_fname = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
			orderBy = MediaStore.Video.Media.DATE_TAKEN;
		} else {
            return null;
        }

        String absolutePathOfImage = "";
        String[] projection = { MediaStore.MediaColumns.DATA, Bucket };

        if (position == 1) {
            projection = new String[] {MediaStore.Video.VideoColumns.DURATION, MediaStore.MediaColumns.DATA, Bucket};
        }

        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(index_fname);
        int column_index_duration  = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

        while (cursor.moveToNext()) {
            String duration = null;

            if (position == 1) {
                duration = cursor.getString(column_index_duration);
            }

            absolutePathOfImage = cursor.getString(column_index_data);
            String folderName = cursor.getString(column_index_folder_name);
            int folderPosition = getFolderIndex(al_images, folderName);

            if (folderPosition == -1) {
                FolderModel model = new FolderModel();
                MediaModel mm = new MediaModel(absolutePathOfImage);

                if (position == 1)
                    mm.setDuration(StringUtils.getFormatedVideoDuration(duration));

                model.setName(cursor.getString(column_index_folder_name));
                model.addItem(mm);
                al_images.add(model);
            } else {
                MediaModel mm = new MediaModel(absolutePathOfImage);

                if (position == 1) {
                    String formatedTime = StringUtils.getFormatedVideoDuration(duration);
                    mm.setDuration(formatedTime);
                }

                al_images.get(folderPosition).addItem(mm);
            }
        }
        cursor.close();

        Collections.sort(al_images, new Comparator<FolderModel>() {
                @Override public int compare(FolderModel o1, FolderModel o2) { 
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()); 
                } 
            }
        );
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
        if (list.isEmpty()) {
            findViewById(R.id.gallery_album_empty_layout).setVisibility(View.VISIBLE);
        }
		obj_adapter = new PhotosFolderAdapter(ImportGalleryActivity.this, list, position);
		myGridView.setAdapter(obj_adapter);
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
							Toast.makeText(ImportGalleryActivity.this, "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
						}
					}
				}
		}
	}

	private class RetrieveMediaTask extends JTask  {

        private ArrayList<FolderModel> result;
        private ProgressBar myProgress;

        @Override
        public void workingThread() {
            result = fn_imagespath();
        }

        @Override
        public void onBeingStarted() {
            this.myProgress = (ProgressBar) findViewById(R.id.galleryalbumProgressBar);
            myProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFinished() {
            myProgress.setVisibility(View.GONE);

            if (result != null) {
                setAdapter(result);
			}
            mySwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onException(Exception e) {

        }
	}
}
