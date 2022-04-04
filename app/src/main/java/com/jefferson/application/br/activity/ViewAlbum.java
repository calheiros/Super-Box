package com.jefferson.application.br.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.MultiSelectRecyclerViewAdapter;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.ProgressThreadUpdate;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.task.DeleteFilesTask;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.util.FileTransfer;
import com.jefferson.application.br.util.Storage;
import com.stfalcon.frescoimageviewer.ImageViewer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.jefferson.application.br.util.JDebug;
import android.widget.RelativeLayout;
import android.animation.Animator;
import android.view.animation.Animation;

public class ViewAlbum extends MyCompatActivity implements MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener, OnClickListener, ImportTask.TaskListener {

    @Override
    public void onPostExecute() {
        updateRecyclerView();
        synchronizeMainActivity();
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onDialogDismiss() {
    }

    @Override
    public void OnCancelled() {

    }

	private PathsData database;
	private boolean selectionMode;
	private RelativeLayout mainLayout;
	private Toolbar mToolbar;
	private MultiSelectRecyclerViewAdapter mAdapter;
	private int position;
	private View menuLayout, mViewUnlock, mViewSelect, mViewDelete, mViewMove;
	private RecyclerView mRecyclerView ;
	private GridLayoutManager mLayoutManager;
	private String title;
	private File folder;
    private View emptyView;
    private FloatingActionButton fab;
    private static final int VIDEO_PLAY_CODE = 7;
    private MyThread myThread;
    private static final int CHANGE_DIRECTORY_CODE = 3;
    private static final int IMPORT_FROM_GALLLERY_CODE = 6;
    private String baseNameDirectory = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview_main);
        File file = new File(Storage.getDefaultStorage());
		database = PathsData.getInstance(this, file.getAbsolutePath());
		mainLayout = findViewById(R.id.main_linear_layout);
	    Intent intent = getIntent();
		position = intent.getIntExtra("position", -1);
	    title = intent.getStringExtra("name");
		folder = new File(intent.getStringExtra("folder"));
		ArrayList<MediaModel> mListItemsPath = (ArrayList<MediaModel>) intent.getParcelableArrayListExtra("data");
	    mRecyclerView = findViewById(R.id.my_recycler_view);
		mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MultiSelectRecyclerViewAdapter(ViewAlbum.this, mListItemsPath, this, position);
        mRecyclerView.setAdapter(mAdapter);
        fab = findViewById(R.id.view_album_fab_button);
		menuLayout = findViewById(R.id.lock_layout);
		mViewUnlock = findViewById(R.id.unlockView);
		mViewDelete = findViewById(R.id.deleteView);
		mViewSelect = findViewById(R.id.selectView);
		mViewMove = findViewById(R.id.moveView);
        emptyView = findViewById(R.id.view_album_empty_view);
		mViewUnlock.setOnClickListener(this);
		mViewDelete.setOnClickListener(this);
		mViewMove.setOnClickListener(this);
		mViewSelect.setOnClickListener(this);
        fab.setOnClickListener(this);

        initToolbar();

        if (mListItemsPath.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        }

        if (position == 1) {
            updateDatabase(mListItemsPath, mAdapter);

        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() { 
                @Override 
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) { 
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
            }
        );
	}

    @Override
    protected void onDestroy() {

        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }

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
                exportGallery();
                break;
            case R.id.selectView:
                toggleSelection();
                break;
            case R.id.view_album_fab_button:
                importFromGallery();
        }
    }

    private void importFromGallery() {
        Intent intent = new Intent(this, GalleryAlbum.class);
        intent.putExtra("position", position);
        startActivityForResult(intent, IMPORT_FROM_GALLLERY_CODE);
    }

    private void updateDatabase(final ArrayList<MediaModel> list, final MultiSelectRecyclerViewAdapter adapter) {
        myThread =  new MyThread(list, adapter);
        myThread.setPriority(Thread.MAX_PRIORITY);
        myThread.start();
    }

    private void toggleSelection() {

        if (mAdapter.getSelectedItemCount() == mAdapter.mListItemsModels.size()) {
            mAdapter.clearSelection();
        } else {
            for (int i = 0; i < mAdapter.mListItemsModels.size();i++) {
                if (!mAdapter.isSelected(i)) {
                    mAdapter.toggleSelection(i);
                }
            }
        }
        invalidateOptionsMenu();
        switchIcon();
    }

    private void exportGallery() {

        if (mAdapter.getSelectedItemCount() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.selecionar_um), Toast.LENGTH_LONG).show();
        } else {

            int count = mAdapter.getSelectedItemCount();
            String item = count + " " + getItemName(count);
            String message = String.format(getString(R.string.mover_galeria), item);

            SimpleDialog dDialog = new SimpleDialog(ViewAlbum.this, SimpleDialog.ALERT_STYLE);
            dDialog
                .setTitle(getString(R.string.mover))
                .setMessage(message)
                .setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener(){

                    @Override
                    public boolean onClick(SimpleDialog dialog) {

                        ViewAlbum.ExportMedia task = new ExportMedia(getSelectedItensPath(), dialog);
                        exitSelectionMode();
                        task.execute();
                        return false;
                    }
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
        }
    }

    private void deleteFilesDialog() {
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            Toast.makeText(ViewAlbum.this, getString(R.string.selecionar_um), 1).show();
            return;
        }

        String item = count + " " + getItemName(count);
        String message = String.format(getString(R.string.apagar_mensagem), item);

        SimpleDialog dialog = new SimpleDialog(ViewAlbum.this);
        dialog.showProgressBar(false);
        dialog.setTitle(getString(R.string.excluir));
        dialog.setMessage(message);
        dialog.setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener(){

                @Override
                public boolean onClick(SimpleDialog dialog) {
                    dialog.dismiss();
                    new DeleteFiles(ViewAlbum.this, getSelectedItensPath(), position, folder).execute();
                    return true;
                }
            });
        dialog.setNegativeButton(getString(R.string.cancelar), null);
        dialog.show();
    }

    private void changeFilesDirectory() {

        if (mAdapter.getSelectedItemCount() == 0) {
            Toast.makeText(ViewAlbum.this, getString(R.string.selecionar_um), 1).show();
            return;
        }

        final Intent intent = new Intent(ViewAlbum.this, FilePicker.class);
        intent.putExtra("selection", getSelectedItensPath());
        intent.putExtra("position", position);
        intent.putExtra("current_path", folder.getAbsolutePath());
        startActivityForResult(intent, CHANGE_DIRECTORY_CODE);
    }

	private String getItemName(int count) {
		return (position == 0 ? count > 1 ? getString(R.string.imagens) : getString(R.string.imagem) : count > 1 ? getString(R.string.videos) : getString(R.string.video)).toLowerCase();
	}

	public String getType() {
		switch (position) {
			case 0:
				return FileModel.IMAGE_TYPE;
			case 1:
				return FileModel.VIDEO_TYPE;
			default:return null;
		}
	}

	private void synchronizeMainActivity() {

        int visibility = (mAdapter.getItemCount() == 0) ? View.VISIBLE: View.GONE;
		MainActivity mActivity = MainActivity.getInstance();
        emptyView.setVisibility(visibility);

		if (mActivity != null) {
			mActivity.updateFragment(position);
		} else {
            Toast.makeText(this, "Can't synchronize MainActivity!", 1).show();
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) { 
            if (requestCode == CHANGE_DIRECTORY_CODE) {
                exitSelectionMode();
                ArrayList<String> list = data.getStringArrayListExtra("moved_files");
                Snackbar.make(mRecyclerView, list.size() + " file(s) moved", Snackbar.LENGTH_SHORT).show();
                mAdapter.removeAll(list);
                synchronizeMainActivity();
            } else if (requestCode == VIDEO_PLAY_CODE) {

                final int index = data.getIntExtra("index", 0);
                mRecyclerView.post(new Runnable() { 

                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollToPosition(index);
                        }
                    }
                );
            } else if (requestCode == IMPORT_FROM_GALLLERY_CODE) {
                ArrayList<FileModel> models = new ArrayList<>();
                ArrayList<String> paths = data.getStringArrayListExtra("selection");

                for (String path : paths) {
                    FileModel model = new FileModel();
                    model.setResource(path);
                    model.setParentPath(folder.getAbsolutePath());
                    model.setType(data.getStringExtra("type"));
                    models.add(model);
                }
                new ImportTask(this, models, this).execute();
                /*
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

                 if(hasExternalFile(paths) && (Storage.getExternalUri(this) == null || getContentResolver().getPersistedUriPermissions().isEmpty())) {
                 getSdCardUri(GET_URI_CODE_TASK);
                 return;
                 }
                 */
            }
        }

		super.onActivityResult(requestCode, resultCode, data);
	}

    public void updateRecyclerView() {
        ArrayList<MediaModel> mListItemsPath = new ArrayList<MediaModel>();

        for (File file : folder.listFiles()) {
            mListItemsPath.add(new MediaModel(file.getAbsolutePath()));
        }

        mAdapter.mListItemsModels = mListItemsPath;
        mAdapter.notifyDataSetChanged();

        if (position == 1) {
            updateDatabase(mListItemsPath, mAdapter);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (selectionMode) {

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
        }
		return false;
	}

	@Override
	public void onItemClicked(int item_position) {
		
        if (!selectionMode) {
			Class<?> mClass = null;

			switch (position) {
				case 0:
                    ArrayList<String> path = new ArrayList<>();
                    for (MediaModel model : mAdapter.mListItemsModels) {
                        path.add(model.getPath());
                    }
					new ImageViewer.Builder(this, Storage.toArrayString(path, true))
						.setStartPosition(item_position)
						.show();
					break;
				case 1:
					mClass = VideoPlayerActivity.class;
					Intent intent = new Intent(getApplicationContext(), mClass);
					intent.putExtra("position", item_position);
					intent.putExtra("filepath", mAdapter.getListItemsPath());
					startActivityForResult(intent, VIDEO_PLAY_CODE);
					break;
			}
			return;
		}

		toggleSelection(item_position);
		invalidateOptionsMenu();
		switchIcon();
	}

	@Override
	public boolean onItemLongClicked(int position) {
		toggleSelection(position);
		invalidateOptionsMenu();
		switchIcon();

		if (!selectionMode) {
			enterSeletionMode();
		}
		return true;
	}

	private ArrayList<String> getSelectedItensPath() {
		ArrayList<String> selectedItens = new ArrayList<String>();

		for (int i : mAdapter.getSelectedItems()) {
			selectedItens.add(mAdapter.mListItemsModels.get(i).getPath());
		}
		return selectedItens;
	}

	private void toggleSelection(int position) {
		mAdapter.toggleSelection(position);
	}

	private void switchIcon() {
		((ImageView)mViewSelect).setImageResource(mAdapter.getSelectedItemCount() == mAdapter.mListItemsModels.size() ? R.drawable.ic_unselect_all : R.drawable.ic_select_all);
	}

    public void enterSeletionMode() {
        selectionMode = true;
        menuLayout.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        anim.setAnimationListener(new Animation.AnimationListener(){

                @Override
                public void onAnimationStart(Animation anim) {

                }

                @Override
                public void onAnimationEnd(Animation anim) {
                    int base = (int) getResources().getDimension(R.dimen.recycler_view_padding);
                    int botton = menuLayout.getHeight();
                    mRecyclerView.setPadding(base, base, base, botton);
                }

                @Override
                public void onAnimationRepeat(Animation p1) {
                }
            }
        );

        menuLayout.setAnimation(anim);
        fab.hide();
    }

	public void exitSelectionMode() {
		selectionMode = false;

        if (!mAdapter.mListItemsModels.isEmpty()) {
			mAdapter.clearSelection();
        }

        menuLayout.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
		menuLayout.setVisibility(View.GONE);
        int dimen = (int)getResources().getDimension(R.dimen.recycler_view_padding);
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
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
		getSupportActionBar().setTitle(title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public class DeleteFiles extends DeleteFilesTask {

        private boolean threadInterrupted;

		public DeleteFiles(Context context, ArrayList<String> p1, int p3, File p4) {
			super(context, p1, p3, p4);
		}

		@Override
		protected void onPreExecute() {

            if (myThread != null && myThread.isWorking()) {
                myThread.stopWork();
                threadInterrupted = true;
            }

			exitSelectionMode();
			super.onPreExecute();
		}

		@Override
		protected void onCancelled(Object result) {
			super.onCancelled(result);

            if (threadInterrupted  && !mAdapter.mListItemsModels.isEmpty()) {
                updateRecyclerView();
            }

			synchronizeMainActivity();
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

            if (mAdapter.mListItemsModels.isEmpty()) {
				finish();
			} else if (threadInterrupted) {
                updateRecyclerView();
            }

			synchronizeMainActivity();
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			mAdapter.removeItem((String)values[0]);
		}

		@Override
		protected Object doInBackground(Object[] p1) {
			return super.doInBackground(p1);
		}
	}

    public class MyThread extends Thread {

        private boolean running;
        private ArrayList<MediaModel> list;
        //private PathsData data;
        private MultiSelectRecyclerViewAdapter adapter;

        public MyThread(ArrayList<MediaModel> list, MultiSelectRecyclerViewAdapter adapter) {
            this.list = list;
            this.adapter = adapter;
            this.running = true;
        }

        @Override
        public void run() {

            for (final MediaModel model: list) {
                if (!running) break;

                try {
                    File file = new File(model.getPath());
                    int duration = database.getDuration(file.getName());

                    if (duration == 0) {
                        Uri uri = Uri.parse(model.getPath()); 
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever(); 
                        mmr.setDataSource(App.getAppContext(), uri); 
                        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); 
                        duration = Integer.parseInt(durationStr);
                        database.updateFileDuration(file.getName(), duration);
                    }

                    long secunds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
                    final String time = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), secunds);

                    runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                adapter.updateItemDuration(model.getPath(), time);
                            }
                        }
                    );
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    JDebug.toast(e.getMessage());
                }

            } 
            running = false;
        }

        public void stopWork() {
            this.running = false;
        }

        public boolean isWorking() {
            return running;
        }
    }

	public class ExportMedia extends AsyncTask {

        private boolean allowListModification = true;
		private SimpleDialog mySimpleDialog;
		private List<String> selectedItens;
		private ArrayList<String> mArrayPath = new ArrayList<>();
		private FileTransfer mTransfer = new FileTransfer();
		private ProgressThreadUpdate mUpdate;
		private ArrayList<String> list2Delete= new ArrayList<>();
		private String ACTION_UPDATE = "ACTION_UPDATE";
        private boolean threadInterrupted;

		public ExportMedia(List<String> itens, SimpleDialog progress) {
			this.mySimpleDialog = progress;
			this.selectedItens = itens;
			this.mUpdate = new ProgressThreadUpdate(mTransfer, mySimpleDialog);
		}

		@Override
		protected void onPreExecute() {

            if (myThread != null && myThread.isWorking()) {
                myThread.stopWork();
                threadInterrupted = true;
            }

            MainActivity.getInstance().prepareAd();
			mySimpleDialog.setStyle(SimpleDialog.PROGRESS_STYLE);
			mySimpleDialog.setTitle(getString(R.string.mover));
            mySimpleDialog.setMessage("");
			mySimpleDialog.setCancelable(false);
			mySimpleDialog.setNegativeButton(getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){
					@Override
					public boolean onClick(SimpleDialog dialog) {
						mTransfer.cancel();
						cancel(true);
						return true;
					}
				});
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Object result) {
			kill();

            if (threadInterrupted) {
                updateRecyclerView();
            }
		}

		private void kill() {

			MainActivity.getInstance().showAd();
			Storage.scanMediaFiles(mArrayPath.toArray(new String[mArrayPath.size()]));

			mySimpleDialog.dismiss();
			mUpdate.die();

			if (mAdapter.mListItemsModels.isEmpty()) {
				deleteFolder();
				finish();
			}

			synchronizeMainActivity();
		}

		public void deleteFolder() {
			PathsData.Folder folderDatabase = PathsData.Folder.getInstance(App.getInstance());

            if (folder.delete()) {
				folderDatabase.delete(folder.getName(), position == 0 ? FileModel.IMAGE_TYPE: FileModel.VIDEO_TYPE);
			}

			folderDatabase.close();
		}

		@Override
		protected void onCancelled(Object result) {
            updateRecyclerView();
			kill();
		}

        private void addItemToDelete(String item, Thread t) {

            while (allowListModification != true) {
                try {
                    t.sleep(10);
                } catch (InterruptedException e) {

                }
            }
            list2Delete.add(item);
        }

		@Override
		protected void onProgressUpdate(Object[] values) {
            //I love 
			if (ACTION_UPDATE.equals(values[0])) {
                allowListModification = false;

                if (!list2Delete.isEmpty()) {

                    Iterator<String> iterator = list2Delete.iterator();
                    while (iterator.hasNext()) { 
                        mAdapter.removeItem(iterator.next());
                    }
                    list2Delete.clear();
                }
                allowListModification = true;
			} else {
				String name = (String)values[1];
				mySimpleDialog.setMessage(name);
			}
		}

		@Override
		protected Void doInBackground(Object[] p1) {

            try {
				long max = 0;
				for (String item : selectedItens) {
					File file = new File(item);
					max += file.length();
				}
				max /= 1024;
				mUpdate.setMax(max);
				mUpdate.start();

				long start = System.currentTimeMillis();
				for (String item :selectedItens) {

                    if (isCancelled()) {
						break;
					}

                    File file = new File(item);
					String path = database.getPath(file.getName());
					if (path == null) {
						continue;
					}

					File fileOut = new File(path);

				    if (fileOut.exists())
						fileOut = new File(getNewFileName(fileOut));

					fileOut.getParentFile().mkdirs();
					publishProgress(null, fileOut.getName());

                    if (file.renameTo(fileOut)) {
                        mArrayPath.add(fileOut.getAbsolutePath());
                        database.deleteData(file.getName());
                        addItemToDelete(item, Thread.currentThread());
                        mTransfer.increment(fileOut.length() / 1024);

                    } else {
                        OutputStream output = getOutputStream(fileOut);
                        InputStream input = new FileInputStream(file);
                        String response = mTransfer.transferStream(input, output);

                        if (FileTransfer.OK.equals(response)) {
                            if (file.delete()) {
                                mArrayPath.add(fileOut.getAbsolutePath());
                                database.deleteData(file.getName());
                                addItemToDelete(item, Thread.currentThread());
                            }
                        } else {
                            Storage.deleteFile(fileOut);
                            if (FileTransfer.Error.NO_LEFT_SPACE.equals(response))
                                break;
                        }

                    }

                    if (System.currentTimeMillis() - start >= 500 && list2Delete.size() > 0) {
                        publishProgress(ACTION_UPDATE);
                        start = System.currentTimeMillis();
                    }
                }

				if (list2Delete.size() > 0) {
					publishProgress(ACTION_UPDATE);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		private String getNewFileName(File file) {
			String path = file.getAbsolutePath();
			int lasIndexOf = path.lastIndexOf(".");

			return lasIndexOf != -1 ? concateParts(path.substring(0, lasIndexOf), path.substring(lasIndexOf), 1) : concateParts(path, "", 1);
		}

		private String concateParts(String part1, String part2, int time) {
			File file = new File(part1 + "(" + time + ")" + part2);
			return file.exists() ? concateParts(part1, part2, time + 1) : file.getAbsolutePath();
		}

		public OutputStream getOutputStream(File file) throws FileNotFoundException {

            if (Build.VERSION.SDK_INT >= 21) 
				if (Environment.isExternalStorageRemovable(file)) {
					return App.getAppContext().getContentResolver().openOutputStream(Storage.getDocumentFile(file, true).getUri());
				}
			file.getParentFile().mkdirs();

            OutputStream fileOutputStream = new FileOutputStream(file);
            return fileOutputStream;
        }
	}
}
	
