package com.jefferson.application.br.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.MultiSelectRecyclerViewAdapter;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.ProgressThreadUpdate;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsDatabase;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.task.DeleteFilesTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.BlurUtils;
import com.jefferson.application.br.util.FileTransfer;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;

public class ViewAlbum extends MyCompatActivity implements MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener, OnClickListener {

    private static final int IMPORT_FROM_VIEW_ALBUM_CODE = 9;
    private static final int VIDEO_PLAY_CODE = 7;
    private static final int CHANGE_DIRECTORY_CODE = 3;
    private static final int IMPORT_FROM_GALLERY_CODE = 6;
    private boolean selectionMode;
    private Toolbar mToolbar;
    private MultiSelectRecyclerViewAdapter mAdapter;
    private int position;
    private View menuLayout;
    private RecyclerView mRecyclerView;
    private String title;
    private File folder;
    private View emptyView;
    private FloatingActionButton fab;
    private RetrieverDataTask myThread;
    private String baseNameDirectory = null;
    private TextView selectAllTextView;
    private ImageView selectImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_album_layout);
        RelativeLayout mainLayout = findViewById(R.id.main_linear_layout);
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        title = intent.getStringExtra("name");
        folder = new File(intent.getStringExtra("folder"));
        ArrayList<MediaModel> mListItemsPath = intent.getParcelableArrayListExtra("data");
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MultiSelectRecyclerViewAdapter(ViewAlbum.this, mListItemsPath, this, position);
        mRecyclerView.setAdapter(mAdapter);

        fab = findViewById(R.id.view_album_fab_button);
        menuLayout = findViewById(R.id.lock_layout);

        View mViewUnlock = findViewById(R.id.unlockView);
        View mViewDelete = findViewById(R.id.deleteView);
        View mViewSelect = findViewById(R.id.selectView);
        selectAllTextView = findViewById(R.id.options_album_selectTextView);
        selectImageView = findViewById(R.id.selectImageView);
        View mViewMove = findViewById(R.id.moveView);
        emptyView = findViewById(R.id.view_album_empty_view);

        mViewUnlock.setOnClickListener(this);
        mViewDelete.setOnClickListener(this);
        mViewMove.setOnClickListener(this);
        mViewSelect.setOnClickListener(this);
        fab.setOnClickListener(this);

        initToolbar();
        configureBlurView(mRecyclerView);

        if (mListItemsPath.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        }

        if (position == 1) {
            updateDatabase(mListItemsPath, mAdapter);
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (selectionMode) {
                    return;
                }

                if (dy > 0) {
                    // Scroll Down
                    if (fab.isShown()) {

                        fab.hide();
                    }
                } else if (dy < 0) { // Scroll Up

                    if (!fab.isShown()) {

                        fab.show();
                    }
                }
            }
        });
    }
    private void configureBlurView(ViewGroup view) {
        BlurView blurView = findViewById(R.id.blurView);
        float radius = 20f;
        View decorView = getWindow().getDecorView();
        blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        blurView.setClipToOutline(true);

        BlurAlgorithm render = BlurUtils.getRenderAlgorithm(this);
        blurView.setupWith(view, render) // or RenderEffectBlur
                .setBlurRadius(radius);

    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.deleteView:
                deleteFilesDialog();
                break;
            case R.id.moveView:
                changeFilesDirectory();
                break;
            case R.id.unlockView:
                exportToGallery();
                break;
            case R.id.selectView:
                toggleSelection();
                break;
            case R.id.view_album_fab_button:
                importFromGallery();
        }
    }

    private void importFromGallery() {
        Intent intent = new Intent(this, ImportGalleryActivity.class);
        intent.putExtra("position", position);
        startActivityForResult(intent, IMPORT_FROM_GALLERY_CODE);
    }

    private void updateDatabase(final ArrayList<MediaModel> list, final MultiSelectRecyclerViewAdapter adapter) {
        myThread = new RetrieverDataTask(list, adapter);
        myThread.setPriority(Thread.MAX_PRIORITY);
        myThread.start();
    }

    private void toggleSelection() {
        if (mAdapter.getSelectedItemCount() == mAdapter.mListItemsModels.size()) {
            mAdapter.clearSelection();
        } else {
            for (int i = 0; i < mAdapter.mListItemsModels.size(); i++) {
                if (!mAdapter.isSelected(i)) {
                    mAdapter.toggleSelection(i);
                }
            }
        }
        invalidateOptionsMenu();
        switchSelectButtonIcon();
    }

    private void exportToGallery() {

        if (mAdapter.getSelectedItemCount() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.selecionar_um), Toast.LENGTH_LONG).show();
        } else {
            int count = mAdapter.getSelectedItemCount();
            String item = count + " " + getItemName(count);
            String message = String.format(getString(R.string.mover_galeria), item);

            SimpleDialog dialog = new SimpleDialog(ViewAlbum.this, SimpleDialog.STYLE_ALERT);
            dialog.setIcon(R.drawable.ic_info_twotone);
            dialog.setTitle(getString(R.string.mover)).setMessage(message).setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener() {

                @Override
                public boolean onClick(SimpleDialog dialog) {

                    ViewAlbum.ExportTask task = new ExportTask(getSelectedItemsPath(), dialog);
                    exitSelectionMode();
                    task.start();
                    return false;
                }
            }).setNegativeButton(getString(R.string.cancelar), null).show();
        }
    }

    private void deleteFilesDialog() {
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            Toast.makeText(ViewAlbum.this, getString(R.string.selecionar_um), Toast.LENGTH_SHORT).show();
            return;
        }

        String item = count + " " + getItemName(count);
        String message = String.format(getString(R.string.apagar_mensagem), item);

        SimpleDialog dialog = new SimpleDialog(ViewAlbum.this);
        dialog.setIcon(R.drawable.ic_warning_twotone);
        dialog.showProgressBar(false);
        dialog.setTitle(getString(R.string.excluir));
        dialog.setMessage(message);
        dialog.setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener() {

            @Override
            public boolean onClick(SimpleDialog dialog) {
                dialog.dismiss();
                ViewAlbum.DeleteFiles task = new DeleteFiles(ViewAlbum.this, getSelectedItemsPath(), position, folder);
                task.start();
                return true;
            }
        });
        dialog.setNegativeButton(getString(R.string.cancelar), null);
        dialog.show();
    }

    private void changeFilesDirectory() {

        if (mAdapter.getSelectedItemCount() == 0) {
            Toast.makeText(ViewAlbum.this, getString(R.string.selecionar_um), Toast.LENGTH_SHORT).show();
            return;
        }

        final Intent intent = new Intent(ViewAlbum.this, FolderPickerActivity.class);
        intent.putExtra("selection", getSelectedItemsPath());
        intent.putExtra("position", position);
        intent.putExtra("current_path", folder.getAbsolutePath());
        startActivityForResult(intent, CHANGE_DIRECTORY_CODE);
    }

    private String getItemName(int count) {
        return (position == 0 ? count > 1 ? getString(R.string.imagens) : getString(R.string.imagem) : count > 1 ? getString(R.string.videos) : getString(R.string.video)).toLowerCase();
    }

    private void showFilesInfo() {
        ArrayList<String> files = mAdapter.getSelectedItemsPath();
        int size = files.size();
        int resId = 0;
        View view = null;
        PathsDatabase database = PathsDatabase.getInstance(this, Storage.getDefaultStoragePath());

        if (size == 1) {
            resId = R.layout.files_info_layout;
            view = getLayoutInflater().inflate(resId, null);

            File file = new File(files.get(0));
            TextView nameText = view.findViewById(R.id.file_name_info);
            TextView sizeText = view.findViewById(R.id.file_size_info);
            TextView originText = view.findViewById(R.id.file_origin_text_view);
            String filePath = database.getPath(file.getName());
            String name = new File(filePath).getName();

            nameText.setText(name);
            sizeText.setText(StringUtils.getFormattedFileSize(file.length()));
            originText.setText(filePath);
        } else return;
        new SimpleDialog(this).setTitle(getString(R.string.information)).setContentView(view).setPositiveButton(android.R.string.ok, null).show();

        database.close();
    }

    public String getType() {
        switch (position) {
            case 0:
                return FileModel.IMAGE_TYPE;
            case 1:
                return FileModel.VIDEO_TYPE;
            default:
                return null;
        }
    }

    private void synchronizeMainActivity() {
        int visibility = (mAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE;
        MainActivity mActivity = MainActivity.getInstance();
        emptyView.setVisibility(visibility);

        if (mActivity != null) {
            mActivity.updateFragment(position);
        } else {
            Toast.makeText(this, "Can't synchronize MainActivity!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHANGE_DIRECTORY_CODE:
                    exitSelectionMode();
                    ArrayList<String> list = data.getStringArrayListExtra("moved_files");
                    Toast.makeText(this, list.size() + " file(s) moved", Toast.LENGTH_SHORT).show();
                    mAdapter.removeAll(list);
                    synchronizeMainActivity();
                    break;
                case VIDEO_PLAY_CODE:
                    final int index = data.getIntExtra("index", 0);
                    mRecyclerView.post(new Runnable() {

                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollToPosition(index);
                        }
                    });
                    break;
                case IMPORT_FROM_GALLERY_CODE:
                    ArrayList<String> paths = data.getStringArrayListExtra("selection");
                    String type = data.getStringExtra("type");

                    Intent intent = new Intent(this, ImportMediaActivity.class);
                    intent.putStringArrayListExtra(ImportMediaActivity.MEDIA_LIST_KEY, paths);
                    intent.putExtra(ImportMediaActivity.TYPE_KEY, type);
                    //intent.putExtra(ImportMediaActivity.POSITION_KEY, position);
                    intent.putExtra(ImportMediaActivity.PARENT_KEY, folder.getAbsolutePath());
                    startActivityForResult(intent, IMPORT_FROM_VIEW_ALBUM_CODE);
                    break;
                case IMPORT_FROM_VIEW_ALBUM_CODE:
                    updateRecyclerView();
                    synchronizeMainActivity();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateRecyclerView() {
        ArrayList<MediaModel> mListItemsPath = new ArrayList<MediaModel>();

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            mListItemsPath.add(new MediaModel(file.getAbsolutePath()));
        }

        mAdapter.mListItemsModels = mListItemsPath;
        mAdapter.notifyDataSetChanged();

        if (position == 1) {
            updateDatabase(mListItemsPath, mAdapter);
        }
    }

    private void retrieveDataAndUpdate() {
        if (mAdapter != null) {
            ArrayList<MediaModel> list = mAdapter.mListItemsModels;
            if (list != null) {
                updateDatabase(list, mAdapter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (selectionMode) {
            getMenuInflater().inflate(R.menu.view_album_menu, menu);
            if (baseNameDirectory == null) {
                baseNameDirectory = (title.length() <= 20) ? title + " ( %s )" : title.substring(0, 20) + "... ( %s )";
            }
            String count = String.valueOf(mAdapter.getSelectedItemCount());
            mToolbar.setTitle(String.format(baseNameDirectory, count));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (selectionMode) {
                exitSelectionMode();
            } else {
                finish();
            }
        } else if (item.getItemId() == R.id.info_item_menu) {
            showFilesInfo();
        }
        return false;
    }

    @Override
    public void onItemClicked(int item_position, View v) {
        if (!selectionMode) {
            Class<?> mClass = null;
            switch (position) {
                case 0:
                    ArrayList<String> path = new ArrayList<>();
                    for (MediaModel model : mAdapter.mListItemsModels) {
                        path.add(model.getPath());
                    }
                    startPreviewActivity(ImagePreviewActivity.class, item_position, v);
                    break;
                case 1:
                    mClass = VideoPlayerActivity.class;
                    startPreviewActivity(mClass, item_position, v);
                    break;
            }
            return;
        }
        toggleSelection(item_position);
        invalidateOptionsMenu();
        switchSelectButtonIcon();
    }

    private void startPreviewActivity(Class<?> previewActivity, int position, View v) {
        Intent intent = new Intent(this, previewActivity);
        intent.putExtra("position", position);
        intent.putExtra("filepath", mAdapter.getListItemsPath());
        ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()); // Request the activity be started, using the custom animation options.
        startActivityForResult(intent, VIDEO_PLAY_CODE, opts.toBundle());
    }

    @Override
    public boolean onItemLongClicked(int position) {
        toggleSelection(position);
        invalidateOptionsMenu();
        switchSelectButtonIcon();

        if (!selectionMode) {
            enterSelectionMode();
        }
        return true;
    }

    private ArrayList<String> getSelectedItemsPath() {
        ArrayList<String> selectedItems = new ArrayList<String>();

        for (int i : mAdapter.getSelectedItems()) {
            selectedItems.add(mAdapter.mListItemsModels.get(i).getPath());
        }
        return selectedItems;
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
    }

    private void switchSelectButtonIcon() {
        boolean allSelected = mAdapter.getSelectedItemCount() == mAdapter.mListItemsModels.size();
        String text = allSelected ? "Unselect all" : "Select all";
        selectAllTextView.setText(text);
        selectImageView.setImageResource(allSelected ? R.drawable.ic_select : R.drawable.ic_select_all);
    }

    public void enterSelectionMode() {
        selectionMode = true;
        invalidateOptionsMenu();
        menuLayout.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation anim) {

            }

            @Override
            public void onAnimationEnd(Animation anim) {
                int base = (int) getResources().getDimension(R.dimen.recycler_view_padding);
                int button = menuLayout.getHeight();
                mRecyclerView.setPadding(base, base, base, button);
                mRecyclerView.setClipToPadding(false);
            }

            @Override
            public void onAnimationRepeat(Animation p1) {

            }
        });
        menuLayout.setAnimation(anim);
        fab.hide();
    }

    public void exitSelectionMode() {
        selectionMode = false;
        invalidateOptionsMenu();

        if (!mAdapter.mListItemsModels.isEmpty()) {
            mAdapter.clearSelection();
        }

        menuLayout.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_bottom));
        menuLayout.setVisibility(View.GONE);
        int dimen = (int) getResources().getDimension(R.dimen.recycler_view_padding);
        mRecyclerView.setPadding(dimen, dimen, dimen, dimen);
        mToolbar.setTitle(title);
        fab.show();
    }

    @Override
    public void onBackPressed() {
        if (selectionMode) {
            exitSelectionMode();
            return;
        }
        super.onBackPressed();
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public class DeleteFiles extends DeleteFilesTask {
        private boolean threadInterrupted;

        public DeleteFiles(Activity activity, ArrayList<String> p1, int p3, File p4) {
            super(activity, p1, p3, p4);
        }

        @Override
        public void onBeingStarted() {
            super.onBeingStarted();

            if (myThread != null && myThread.isWorking()) {
                myThread.stopWork();
                threadInterrupted = true;
            }
            exitSelectionMode();
        }

        @Override
        protected void onInterrupted() {
            super.onInterrupted();

            if (threadInterrupted && !mAdapter.mListItemsModels.isEmpty()) {
                updateRecyclerView();
            }
            synchronizeMainActivity();
        }

        @Override
        public void onFinished() {
            super.onFinished();

            if (mAdapter.mListItemsModels.isEmpty()) {
                finish();
            } else if (threadInterrupted) {
                updateRecyclerView();
            }
            synchronizeMainActivity();
        }

        @Override
        protected void onUpdated(Object[] get) {
            super.onUpdated(get);
            mAdapter.removeItem((String) get[0]);
        }
    }

    public class RetrieverDataTask extends Thread {

        private boolean running;
        private boolean cancelled;
        private final ArrayList<MediaModel> list;
        private final MultiSelectRecyclerViewAdapter adapter;

        public RetrieverDataTask(ArrayList<MediaModel> list, MultiSelectRecyclerViewAdapter adapter) {
            this.list = list;
            this.adapter = adapter;
            this.running = true;
            this.cancelled = false;
        }

        @Override
        public void run() {
            try (PathsDatabase database = PathsDatabase.getInstance(ViewAlbum.this, Storage.getDefaultStoragePath())) {
                for (final MediaModel model : list) {
                    if (!running) break;

                    try {
                        File file = new File(model.getPath());
                        int duration = database.getDuration(file.getName());

                        if (duration == -1 || duration == 0) {
                            try {
                                Uri uri = Uri.parse(model.getPath());
                                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                mmr.setDataSource(App.getAppContext(), uri);
                                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                duration = Integer.parseInt(durationStr);
                            } catch (RuntimeException e) {
                                duration = -2;
                            }
                            database.updateFileDuration(file.getName(), duration);
                        }
                        final String time = StringUtils.getFormattedVideoDuration(String.valueOf(duration));
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                adapter.updateItemDuration(model.getPath(), time);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        JDebug.writeLog(e.getCause());
                    }
                }
            } finally {
                running = false;
            }
        }

        public boolean cancelled() {
            return cancelled;
        }

        public void stopWork() {
            this.cancelled = true;
            this.running = false;
        }

        public boolean isWorking() {
            return running;
        }
    }

    public class ExportTask extends JTask {

        private static final String ACTION_UPDATE_ADAPTER = "action_update";
        private boolean allowListModification = true;
        private final SimpleDialog mySimpleDialog;
        private final List<String> selectedItems;
        private final ArrayList<String> mArrayPath = new ArrayList<>();
        private final FileTransfer mTransfer = new FileTransfer();
        private final ProgressThreadUpdate mUpdate;
        private final ArrayList<String> junkList = new ArrayList<>();
        private final PathsDatabase database;
        private final String TAG = "ExportTask";

        public ExportTask(List<String> items, SimpleDialog progress) {
            this.mySimpleDialog = progress;
            this.selectedItems = items;
            this.mUpdate = new ProgressThreadUpdate(mTransfer, mySimpleDialog);
            this.database = PathsDatabase.getInstance(ViewAlbum.this, Storage.getDefaultStoragePath());
        }

        @Override
        public void workingThread() {
            try {
                long max = 0;

                for (String item : selectedItems) {
                    File file = new File(item);
                    max += file.length();
                }

                max /= 1024;
                mUpdate.setMax(max);
                mUpdate.start();

                long start = System.currentTimeMillis();
                for (String item : selectedItems) {
                    try {
                        if (this.isInterrupted()) {
                            break;
                        }

                        File file = new File(item);
                        String path = database.getPath(file.getName());

                        if (path == null) {
                            //need something 0.o
                            continue;
                        }

                        File fileOut = new File(path);

                        if (fileOut.exists()) fileOut = new File(getNewFileName(fileOut));

                        fileOut.getParentFile().mkdirs();
                        sendUpdate(null, fileOut.getName());

                        if (file.renameTo(fileOut)) {
                            mArrayPath.add(fileOut.getAbsolutePath());
                            database.deleteData(file.getName());
                            addJunkItem(item, Thread.currentThread());
                            //sendUpdate(ACTION_ADD_JUNK, item);
                            mTransfer.increment(fileOut.length() / 1024);

                        } else {
                            OutputStream output = getOutputStream(fileOut);
                            InputStream input = new FileInputStream(file);
                            String response = mTransfer.transferStream(input, output);

                            if (FileTransfer.OK.equals(response)) {
                                if (file.delete()) {
                                    mArrayPath.add(fileOut.getAbsolutePath());
                                    database.deleteData(file.getName());
                                    addJunkItem(item, Thread.currentThread());
                                    //sendUpdate(ACTION_ADD_JUNK, item);
                                }
                            } else {
                                Storage.deleteFile(fileOut);
                            }
                        }

                        if (System.currentTimeMillis() - start >= 600 && junkList.size() > 0) {
                            sendUpdate(ACTION_UPDATE_ADAPTER);
                            start = System.currentTimeMillis();
                        }
                    } catch (Exception e) {

                    }
                }
                if (!junkList.isEmpty()) {
                    sendUpdate(ACTION_UPDATE_ADAPTER);
                }
            } finally {
                mUpdate.destroy();
                database.close();
            }
        }

        @Override
        public void onBeingStarted() {
            if (myThread != null && myThread.isWorking()) {
                myThread.stopWork();
            }
            mySimpleDialog.resetDialog();
            mySimpleDialog.showProgressBar(true);
            mySimpleDialog.setTitle(getString(R.string.mover));
            mySimpleDialog.setMessage("");
            mySimpleDialog.setSingleLineMessage(true);
            mySimpleDialog.setCancelable(false);
            mySimpleDialog.setNegativeButton(getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener() {
                @Override
                public boolean onClick(SimpleDialog dialog) {
                    mTransfer.cancel();
                    interrupt();
                    return true;
                }
            });
        }

        @Override
        protected void onUpdated(Object[] get) {
            if (ACTION_UPDATE_ADAPTER.equals(get[0])) {
                updateAdapter();
            } else {
                String name = (String) get[1];
                mySimpleDialog.setMessage(name);
            }
        }

        @Override
        public void onFinished() {
            retrieveInfo();
            kill();
        }

        private void retrieveInfo() {
            if (myThread != null && myThread.cancelled() && mAdapter.getItemCount() > 0) {
                retrieveDataAndUpdate();
            }
        }

        private String getAlternativePath(int type) {
            File file = new File(Environment.getExternalStoragePublicDirectory(type == 0 ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES), StringUtils.getFormattedDate("yyyy.MM.dd 'at' HH:mm:ss z") + (type == 0 ? ".jpeg" : ".mp4"));
            if (file.exists()) {
                file = new File(getNewFileName(file));
            }
            return file.getAbsolutePath();
        }

        @Override
        public void onException(Exception e) {
            Toast.makeText(ViewAlbum.this, "Finished with error!", Toast.LENGTH_SHORT).show();
        }

        private void kill() {
            Storage.scanMediaFiles(mArrayPath.toArray(new String[mArrayPath.size()]));
            mySimpleDialog.dismiss();

            if (mAdapter.mListItemsModels.isEmpty()) {
                deleteFolder();
                finish();
            }
            synchronizeMainActivity();
        }

        private void updateAdapter() {
            allowListModification = false;

            if (!junkList.isEmpty()) {
                for (String s : junkList) {
                    mAdapter.removeItem(s);
                }
                junkList.clear();
            }
            allowListModification = true;
        }

        public void deleteFolder() {
            PathsDatabase.Folder database = PathsDatabase.Folder.getInstance(App.getInstance());

            if (folder.delete()) {
                database.delete(folder.getName(), position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE);
            }
            database.close();
        }

        private void addJunkItem(String item, Thread t) {
            while (!allowListModification) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
            junkList.add(item);
        }

        private String getNewFileName(File file) {
            String path = file.getAbsolutePath();
            int lasIndexOf = path.lastIndexOf(".");
            return lasIndexOf != -1 ? concatenateParts(path.substring(0, lasIndexOf), path.substring(lasIndexOf), 1) : concatenateParts(path, "", 1);
        }

        private String concatenateParts(String part1, String part2, int time) {
            File file = new File(part1 + "(" + time + ")" + part2);
            return file.exists() ? concatenateParts(part1, part2, time + 1) : file.getAbsolutePath();
        }

        public OutputStream getOutputStream(File file) throws FileNotFoundException {

            if (Build.VERSION.SDK_INT >= 21) if (Environment.isExternalStorageRemovable(file)) {
                return App.getAppContext().getContentResolver().openOutputStream(Storage.getDocumentFile(file, true).getUri());
            }

            file.getParentFile().mkdirs();
            return new FileOutputStream(file);
        }
    }
}
	
