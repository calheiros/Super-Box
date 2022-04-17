package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.adapter.FilePickerAdapter;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.model.PickerModel;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.Storage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilePicker extends MyCompatActivity implements OnItemClickListener {

    private String currentPath;
    private FilePickerAdapter filePickerAdapter;
    private ListView mListView;
    private View myOverlay;
    private Toolbar mToolbar;
    private List<String> paths;
    private int position;

    private FloatingActionButton fab;

    public class MoveFilesTask extends JTask {

        @Override
        public void onException(Exception e) {
            Toast.makeText(FilePicker.this, "Error!", 1).show();
        }


        ArrayList<String> mMovedArray;
        String folder;
        SimpleDialog progress;

        public MoveFilesTask(FilePicker filePicker, String str) {
            this.mMovedArray = new ArrayList<>();
            this.folder = str;
        }

        @Override
        public void onBeingStarted() {
            this.progress = new SimpleDialog(FilePicker.this);
            this.progress.setMax(paths.size());
            this.progress.setTitle("Movendo...");
			this.progress.setProgress(0);
            this.progress.show();

        }

        @Override
        protected void onUpdated(Object[] objArr) {
            this.progress.setProgress(((Integer) objArr[0]).intValue());

        }

        @Override
        public void onFinished() {
            this.progress.dismiss();
			Intent intent = new Intent();
			intent.putExtra("moved_files", mMovedArray);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void workingThread() {

            for (String str : paths) {

                File file = new File(str);
                File newFile = new File(folder, file.getName());

                if (file.renameTo(newFile)) {
					mMovedArray.add(str);
                }

                sendUpdate(progress.getProgress() + 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.list_view_layout);
        this.mListView = (ListView) findViewById(R.id.androidList);
        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        this.myOverlay = findViewById(R.id.myOverlayLayout);
        this.position = getIntent().getIntExtra("position", -1);
        this.paths = getIntent().getStringArrayListExtra("selection");
        this.currentPath = getIntent().getStringExtra("current_path");

        this.filePickerAdapter = new FilePickerAdapter(getModels(this.position), this);
        this.mListView.setAdapter(this.filePickerAdapter);
        this.mListView.setOnItemClickListener(this);
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("Move to");
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View p1) {
                    new MoveFilesTask(FilePicker.this, (filePickerAdapter.models.get(filePickerAdapter.getSelectedItem())).getPath()).start();
                    fab.hide();
                }
            }
        );
        fab.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        int selectedItem = this.filePickerAdapter.getSelectedItem();

        if (selectedItem == -1) {
            fab.setVisibility(View.VISIBLE);
            Animation slideUpAnimation = AnimationUtils.loadAnimation(FilePicker.this, R.anim.slide_up);
            slideUpAnimation.setInterpolator(new DecelerateInterpolator());
            fab.startAnimation(slideUpAnimation);
        } 
        Animation anim = AnimationUtils.loadAnimation(FilePicker.this, R.anim.fade_in);
        anim.setDuration(250);
        view.startAnimation(anim);
        filePickerAdapter.setSelectedItem(i);
    }

    public void update() {
        this.filePickerAdapter.update(getModels(this.position));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public List<PickerModel> getModels(int i) {
        ArrayList<PickerModel> arrayList = new ArrayList<>();
        File storageAndFolder = Storage.getFolder(i == 0 ? Storage.IMAGE : Storage.VIDEO);
        PathsData.Folder instance = PathsData.Folder.getInstance(this);
        File[] listFiles = storageAndFolder.listFiles();

        for (File file : listFiles) {

            if (file.isDirectory() && !file.getAbsolutePath().equals(currentPath)) {
                PickerModel pickerModel = new PickerModel();
                File[] listFiles2 = file.listFiles();
                String folderName = instance.getFolderName(file.getName(), position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE);

                if (folderName == null) {
                    folderName = file.getName();
                }

                int length = listFiles2.length;

                if (length > 0) {
                    pickerModel.setTumbPath(listFiles2[0].getAbsolutePath());
                }

                pickerModel.setPath(file.getAbsolutePath());
                pickerModel.setName(folderName);
                pickerModel.setSize(length);
                arrayList.add(pickerModel);
            }
        }
        if (arrayList.isEmpty()) {
            myOverlay.setVisibility(View.VISIBLE);
        }
        return arrayList;
    }
}
