package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.adapter.FilePickerAdapter;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsDatabase;
import com.jefferson.application.br.fragment.AlbumFragment;
import com.jefferson.application.br.model.PickerModel;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.Storage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.view.Menu;

public class FolderPickerActivity extends MyCompatActivity implements OnItemClickListener {

    private String currentPath;
    private FilePickerAdapter filePickerAdapter;
    private View myOverlay;
    private List<String> paths;
    private int position;
    private FloatingActionButton fab;

    public class MoveFilesTask extends JTask {

        @Override
        public void onException(Exception e) {
            Toast.makeText(FolderPickerActivity.this, "Error!", Toast.LENGTH_SHORT).show();
        }
        ArrayList<String> mMovedArray;
        String folder;
        SimpleDialog dialog;

        public MoveFilesTask(FolderPickerActivity filePicker, String str) {
            this.mMovedArray = new ArrayList<>();
            this.folder = str;
        }

        @Override
        public void onBeingStarted() {
            this.dialog = new SimpleDialog(FolderPickerActivity.this, SimpleDialog.STYLE_PROGRESS);
            this.dialog.setMax(paths.size());
            this.dialog.setTitle(getString(R.string.movendo));
            this.dialog.setProgress(0);
            this.dialog.show();
        }

        @Override
        protected void onUpdated(Object[] objArr) {
            this.dialog.setProgress((Integer) objArr[0]);
        }

        @Override
        public void onFinished() {
            this.dialog.dismiss();
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
                sendUpdate(dialog.getProgress() + 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.list_view_layout);
        ListView mListView = findViewById(R.id.androidList);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        this.myOverlay = findViewById(R.id.myOverlayLayout);
        this.position = getIntent().getIntExtra("position", -1);
        this.paths = getIntent().getStringArrayListExtra("selection");
        this.currentPath = getIntent().getStringExtra("current_path");
        // applyParentViewPadding(mListView);
        this.filePickerAdapter = new FilePickerAdapter(getModels(this.position), this, position);
        mListView.setAdapter(this.filePickerAdapter);
        mListView.setOnItemClickListener(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Move to");
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    new MoveFilesTask(FolderPickerActivity.this, (filePickerAdapter.models.get(filePickerAdapter.getSelectedItem())).getPath()).start();
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
            Animation slideUpAnimation = AnimationUtils.loadAnimation(FolderPickerActivity.this, R.anim.slide_up);
            slideUpAnimation.setInterpolator(new DecelerateInterpolator());
            fab.startAnimation(slideUpAnimation);
        }
        
        if (selectedItem != i) {
            View overlay = view.findViewById(R.id.folder_picker_check_overlay);
            Animation anim = AnimationUtils.loadAnimation(FolderPickerActivity.this, R.anim.fade_in);
            anim.setDuration(250);
            overlay.startAnimation(anim);
            filePickerAdapter.setSelectedItem(i);
        }
    }

    public void update() {
        this.filePickerAdapter.update(getModels(this.position));
        MainActivity mainActivity = MainActivity.getInstance();
        mainActivity.updateFragment(position);
    }

    public void createFolder() {
        View contentView = getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
        final EditText editText = contentView.findViewById(R.id.editTextInput);
        SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.STYLE_INPUT);

        dialog.setContentView(contentView);
        dialog.setTitle(getString(R.string.criar_pasta));
        dialog.setNegativeButton(getString(android.R.string.cancel), null);
        dialog.setPositiveButton(getString(android.R.string.ok), new SimpleDialog.OnDialogClickListener() {

                @Override
                public boolean onClick(SimpleDialog dialog) {
                    String name = editText.getText().toString();
                    String result = AlbumFragment.validateFolderName(name, FolderPickerActivity.this);
                    if (!result.equals(AlbumFragment.FOLDER_NAME_OKAY)) {
                        Toast.makeText(FolderPickerActivity.this, result, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    boolean success = AlbumFragment.createFolder(FolderPickerActivity.this, name, position) != null;
                    if (success) {
                        update();
                    }
                    return success;
                }
            }
        ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case android.R.id.home:   
                finish();
                break;
            case R.id.item_create_folder:
                createFolder();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public List<PickerModel> getModels(int i) {
        ArrayList<PickerModel> arrayList = new ArrayList<>();
        File storageAndFolder = Storage.getFolder(i == 0 ? Storage.IMAGE : Storage.VIDEO);
        PathsDatabase.Folder instance = PathsDatabase.Folder.getInstance(this);

        assert storageAndFolder != null;
        File[] listFiles = storageAndFolder.listFiles();
        assert listFiles != null;

        for (File file : listFiles) {

            if (file.isDirectory() && !file.getAbsolutePath().equals(currentPath)) {
                PickerModel pickerModel = new PickerModel();
                File[] dirList = file.listFiles();
                String folderName = instance.getFolderName(file.getName(), position == 0 ?
                        FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE);

                if (folderName == null) {
                    folderName = file.getName();
                }

                assert dirList != null;
                int length = dirList.length;

                if (length > 0) {
                    pickerModel.setTumbPath(dirList[0].getAbsolutePath());
                }

                pickerModel.setPath(file.getAbsolutePath());
                pickerModel.setName(folderName);
                pickerModel.setSize(length);
                arrayList.add(pickerModel);
            }
        }
        int visibility = arrayList.isEmpty() ? View.VISIBLE: View.GONE;
        if (myOverlay.getVisibility() != visibility) {
            myOverlay.setVisibility(visibility);
        }
        //sort files in alphabetically
        Collections.sort(arrayList, (model1, model2) -> model1.getName().toLowerCase().compareTo(
                model2.getName().toLowerCase()));
        return arrayList;
    }
}
