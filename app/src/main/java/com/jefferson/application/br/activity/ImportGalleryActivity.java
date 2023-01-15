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

package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.adapter.PhotosFolderAdapter;
import com.jefferson.application.br.model.FolderModel;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.FileUtils;
import com.jefferson.application.br.util.StringUtils;
import java.util.ArrayList;

public class ImportGalleryActivity extends MyCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final int GET_CODE = 5658;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final String TAG = "ImportGalleryActivity";
    private static final int PICK_CONTENT_FROM_EXTERNAL_APP = 1;
    private PhotosFolderAdapter obj_adapter;
    private int position;
    private GridView myGridView;
    private String title;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ImportGalleryActivity.RetrieveMediaTask retrieveMediaTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_gallery);
        myGridView = findViewById(R.id.gv_folder);
        position = getIntent().getExtras().getInt("position");
        mySwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mySwipeRefreshLayout.setOnRefreshListener(this);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

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
            default:
                throw new IllegalArgumentException("can not find type for position: " + position);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.item_from_gallery) {
            getContentFromExternalApp();
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getContentFromExternalApp() {
        Intent intent = new Intent();
        intent.setType(getIntentType());
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_CONTENT_FROM_EXTERNAL_APP);
    }

    private String getIntentType() {
        switch (position) {
            case 0:
                return "image/*";
            case 1:
                return "video/*";
            default:
                throw new RuntimeException("can not find intent type for position " + position);
        }
    }

    private void notImplemented() {
        Toast.makeText(this, "Not implemented!", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<FolderModel> getGalleryItems() {
        ArrayList<FolderModel> galleryItems = new ArrayList<>();
        Uri uri;
        Cursor cursor;
        String orderBy;
        String bucketName;
        int column_index_data, column_index_folder_name;

        if (position == 0) {
            bucketName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            orderBy = MediaStore.Images.Media.DATE_TAKEN;
        } else if (position == 1) {
            bucketName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            orderBy = MediaStore.Video.Media.DATE_TAKEN;
        } else {
            return null;
        }

        String absolutePathOfImage;
        String[] projection = {MediaStore.MediaColumns.DATA, bucketName};

        if (position == 1) {
            projection = new String[]{MediaStore.Video.VideoColumns.DURATION, MediaStore.MediaColumns.DATA, bucketName};
        }

        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(bucketName);
        int column_index_duration = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

        while (cursor.moveToNext()) {
            String duration = null;

            if (position == 1) {
                duration = cursor.getString(column_index_duration);
            }

            absolutePathOfImage = cursor.getString(column_index_data);
            String folderName = cursor.getString(column_index_folder_name);
            int folderPosition = getFolderIndex(galleryItems, folderName);

            if (folderPosition == -1) {
                FolderModel model = new FolderModel();
                MediaModel mm = new MediaModel(absolutePathOfImage);

                if (position == 1)
                    mm.setDuration(StringUtils.getFormattedVideoDuration(duration));

                model.setName(cursor.getString(column_index_folder_name));
                model.addItem(mm);
                galleryItems.add(model);
            } else {
                MediaModel mm = new MediaModel(absolutePathOfImage);

                if (position == 1) {
                    String formattedTime = StringUtils.getFormattedVideoDuration(duration);
                    mm.setDuration(formattedTime);
                }

                galleryItems.get(folderPosition).addItem(mm);
            }
        }
        cursor.close();
        FolderModel.sort(galleryItems);
        return galleryItems;
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
            ArrayList<String> mediaList = new ArrayList<>();

            if (requestCode == PICK_CONTENT_FROM_EXTERNAL_APP) {
                FileUtils fileUtils = new FileUtils(this);

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri(); //do what do you want to do
                        String path = fileUtils.getPath(imageUri);
                        mediaList.add(path);
                    }
                } else if (data.getData() != null) {
                    Uri selectedImageUri = data.getData(); //do what do you want to do
                    String path = fileUtils.getPath(selectedImageUri);
                    mediaList.add(path);
                } else {
                    return;
                }
            } else {
                mediaList = data.getExtras().getStringArrayList("selection");
            }
            if (mediaList.isEmpty()) {
                return;
            }
            Intent i = new Intent();
            i.putExtra("selection", mediaList);
            i.putExtra("type", getType());
            i.putExtra("position", position);
            setResult(RESULT_OK, (i));
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    getGalleryItems();
                } else {
                    Toast.makeText(ImportGalleryActivity.this, "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class RetrieveMediaTask extends JTask {

        private ArrayList<FolderModel> result;
        private ProgressBar myProgress;

        @Override
        public void workingThread() {
            result = getGalleryItems();
        }

        @Override
        public void onBeingStarted() {
            this.myProgress = findViewById(R.id.galleryalbumProgressBar);
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
