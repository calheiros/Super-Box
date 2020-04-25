package com.jefferson.superbox.br.activity;

import android.content.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import com.chartboost.sdk.*;
import com.google.android.gms.ads.*;
import com.jefferson.superbox.br.*;
import com.jefferson.superbox.br.activity.*;
import com.jefferson.superbox.br.app.*;
import com.jefferson.superbox.br.database.*;
import com.jefferson.superbox.br.fragment.*;
import com.jefferson.superbox.br.util.*;
import com.stfalcon.frescoimageviewer.*;
import java.io.*;
import java.util.*;

import android.support.v7.widget.Toolbar;
import android.view.animation.AnimationUtils;
import com.jefferson.superbox.br.R;
import com.jefferson.superbox.br.database.PathsData.*;
import com.jefferson.superbox.br.task.*;

public class ViewAlbum extends MyCompatActivity implements MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener {

	private PathsData db;
	private boolean selectionMode;
	private LinearLayout mainLayout;
	private Toolbar mToolbar;
	private MultiSelectRecyclerViewAdapter mAdapter;
	private int position;
	private View lockButton, mViewUnlock, mViewSelect, mViewDelete, mViewMove;
	private RecyclerView mRecyclerView ;
	private GridLayoutManager mLayoutManager;
	private String title;
	private File folder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview_main);
		File file = new File(Storage.getDefaultStorage());
		db = PathsData.getInstance(this, file.getAbsolutePath());
		mainLayout = (LinearLayout)findViewById(R.id.main_linear_layout);

	    final Intent intent = getIntent();
		position = intent.getIntExtra("position", -1);
	    title = intent.getStringExtra("name");
		folder = new File(intent.getStringExtra("folder"));
		ArrayList<String> mListItemsPath = intent.getStringArrayListExtra("data");

		initToolbar();

	    mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
		mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MultiSelectRecyclerViewAdapter(ViewAlbum.this, mListItemsPath, this, position);
        mRecyclerView.setAdapter(mAdapter);
		lockButton = findViewById(R.id.lock_layout);
		mViewUnlock = findViewById(R.id.unlockView);
		mViewDelete = findViewById(R.id.deleteView);
		mViewSelect = findViewById(R.id.selectView);
		mViewMove = findViewById(R.id.moveView);

		mViewUnlock.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {

                    if (mAdapter.getSelectedItemCount() == 0) {
						Toast.makeText(getApplicationContext(), getString(R.string.selecionar_um), Toast.LENGTH_LONG).show();
					} else {

						int count = mAdapter.getSelectedItemCount();
						String item = count + " " + getItemName(count);
						String message = String.format(getString(R.string.mover_galeria), item);

						SimpleDialog dDialog = new SimpleDialog(ViewAlbum.this, SimpleDialog.ALERT_STYLE);
						dDialog
							.setContentTitle(getString(R.string.mover))
							.setContentText(message)
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
					}}
			});
		mViewDelete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {

					int count = mAdapter.getSelectedItemCount();
					if (count == 0) {
						Toast.makeText(ViewAlbum.this, getString(R.string.selecionar_um), 1).show();
						return;
					}
					String item = count + " " + getItemName(count);
					String message = String.format(getString(R.string.apagar_mensagem), item);

					SimpleDialog dDialog = new SimpleDialog(ViewAlbum.this);
					dDialog.showProgressBar(false);
					dDialog.setContentTitle(getString(R.string.excluir));
					dDialog.setContentText(message);
					dDialog.setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener(){

							@Override
							public boolean onClick(SimpleDialog dialog) {
								dialog.dismiss();
								new DeleteFiles(ViewAlbum.this, getSelectedItensPath(), position, folder).execute();
								return true;
							}
						});
					dDialog.setNegativeButton(getString(R.string.cancelar), null);
					dDialog.show();
				}
			});
		mViewMove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					if (mAdapter.getSelectedItemCount() == 0) {
						Toast.makeText(ViewAlbum.this, getString(R.string.selecionar_um), 1).show();
						return;
					}

					final Intent intent = new Intent(ViewAlbum.this, FilePicker.class);
					intent.putExtra("selection", getSelectedItensPath());
					intent.putExtra("position", position);
					intent.putExtra("current_path", folder.getAbsolutePath());
					startActivityForResult(intent, 10);
				}
			});
		mViewSelect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mAdapter.getSelectedItemCount() == mAdapter.mListItemsPath.size()) {
						mAdapter.clearSelection();
					} else {
						for (int i = 0; i < mAdapter.mListItemsPath.size();i++) {
							if (!mAdapter.isSelected(i)) {
								mAdapter.toggleSelection(i);
							}
						}
					}
					invalidateOptionsMenu();
					switchIcon();
				}
			});
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

	private void synchronizeData() {

		MainActivity mActivity = null;
		MainFragment.ID id = position == 0 ? MainFragment.ID.FIRST : MainFragment.ID.SECOND;
		if ((mActivity = MainActivity.getInstance()) != null) {
			mActivity.update(id);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			ArrayList<String> list = data.getStringArrayListExtra("moved_files");
			Toast.makeText(this, "Moved " + list.size() + " file(s)", 1).show();
			mAdapter.removeAll(list);
			synchronizeData();
			if (mAdapter.getItemCount() == 0) {
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (selectionMode) {
			getMenuInflater().inflate(R.menu.menu_ok, menu);
			menu.getItem(0).setTitle(String.valueOf(mAdapter.getSelectedItemCount()));
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() != R.id.count_show) {
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onItemClicked(int item_position) {
		if (!selectionMode) {
			Class<?> mClass= null;

			switch (position) {
				case 0:
					new ImageViewer.Builder(this, Storage.toArrayString(mAdapter.mListItemsPath, true))
						.setStartPosition(item_position)
						.show();

					break;
				case 1:
					mClass = VideoPlayerActivity.class;
					Intent intent = new Intent(getApplicationContext(), mClass);
					intent.putExtra("position", item_position);
					intent.putExtra("filepath", mAdapter.mListItemsPath);
					startActivity(intent);
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
			selectionMode = true;
			lockButton.setVisibility(View.VISIBLE);
			lockButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
			invalidateOptionsMenu();
		}
		return true;
	}

	private ArrayList<String> getSelectedItensPath() {
		ArrayList<String> selectedItens = new ArrayList<String>();

		for (int i : mAdapter.getSelectedItems()) {
			selectedItens.add(mAdapter.mListItemsPath.get(i));
		}
		return selectedItens;
	}
	private void toggleSelection(int position) {
		mAdapter.toggleSelection(position);
	}

	private void switchIcon() {
		((ImageView)mViewSelect).setImageResource(mAdapter.getSelectedItemCount() == mAdapter.mListItemsPath.size() ? R.drawable.ic_unselect_all : R.drawable.ic_select_all);
	}

	public void exitSelectionMode() {
		selectionMode = false;
		if (!mAdapter.mListItemsPath.isEmpty())
			mAdapter.clearSelection();
		lockButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
		lockButton.setVisibility(View.GONE);

		invalidateOptionsMenu();
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

		public DeleteFiles(Context context, ArrayList<String> p1, int p3, File p4) {
			super(context,p1, p3, p4);
		}
		@Override
		protected void onPreExecute() {
			exitSelectionMode();
			super.onPreExecute();
		}

		@Override
		protected void onCancelled(Object result) {
			super.onCancelled(result);
			synchronizeData();
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (mAdapter.mListItemsPath.isEmpty()) {
				finish();
			}
			synchronizeData();
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			mAdapter.removeItem(values[0]);
		}

		@Override
		protected Object doInBackground(Object[] p1) {
			return super.doInBackground(p1);
		}
		
	}
	public class ExportMedia extends AsyncTask {

		private SimpleDialog mDialog;
		private List<String> selectedItens;
		private ArrayList<String> mArrayPath = new ArrayList<>();
		private FileTransfer mTransfer = new FileTransfer();
		private ProgressThreadUpdate mUpdate;
		private List<String> mListTemp = new ArrayList<>();
		private String ACTION_UPDATE = "_UPDATE";

		public ExportMedia(List<String> itens, SimpleDialog progress) {
			this.mDialog = progress;
			this.selectedItens = itens;
			this.mUpdate = new ProgressThreadUpdate(mTransfer, mDialog);
		}
		@Override
		protected void onPreExecute() {

            MainActivity.getInstance().prepareAd();
			mDialog.setStyle(SimpleDialog.PROGRESS_STYLE);
			mDialog.setContentTitle(getString(R.string.mover));
			mDialog.setCancelable(false);
			mDialog.setNegativeButton(getString(R.string.cancelar), new SimpleDialog.OnDialogClickListener(){
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
			onFinish();

		}
		private void onFinish() {

			MainActivity.getInstance().showAd();
			Storage.scanMediaFiles(mArrayPath.toArray(new String[mArrayPath.size()]));

			mDialog.dismiss();
			mUpdate.die();

			if (mAdapter.mListItemsPath.isEmpty()) {
				deleteFolder();
				finish();
			}
			synchronizeData();
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
			onFinish();
		}

		@Override
		protected void onProgressUpdate(Object[] values) {

			if (ACTION_UPDATE.equals(values[0])) {
				for (String item: mListTemp) {
					mAdapter.removeItem(item);
				}
				mListTemp.clear();
			} else {
				String name = (String)values[1];
				mDialog.setContentText(name);
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
					String path = db.getPath(file.getName());
					if (path == null) {
						continue;
					}
					File fileOut = new File(path);
				    if (fileOut.exists())
						fileOut = new File(getNewFileName(fileOut));

					fileOut.getParentFile().mkdirs();
					publishProgress(null, fileOut.getName());

					OutputStream output = getOutputStream(fileOut);
					InputStream input = new FileInputStream(file);
					String response = mTransfer.transferStream(input, output);

					if (FileTransfer.OK.equals(response)) {
						if (file.delete()) {
							mArrayPath.add(fileOut.getAbsolutePath());
							db.deleteData(file.getName());
							mListTemp.add(item);
						}
					} else {
						Storage.deleteFile(fileOut);
						if (FileTransfer.Error.NO_LEFT_SPACE.equals(response))
							break;
					}
					if (System.currentTimeMillis() - start >= 1000 && mListTemp.size() > 0) {
						publishProgress(ACTION_UPDATE);
						start = System.currentTimeMillis();
					}
				}
				if (mListTemp.size() > 0) {
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
	
