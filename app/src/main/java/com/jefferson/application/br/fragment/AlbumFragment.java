package com.jefferson.application.br.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.FolderModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.adapter.AlbumAdapter;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.task.DeleteFilesTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.DialogUtils;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumFragment extends Fragment {

    public static final String FOLDER_NAME_OKAY = "folder_name_okay";
    public final static int ACTION_CREATE_FOLDER = 122;
    public final static int ACTION_RENAME_FOLDER = 54;

	private String root;
	private int position;
	private AlbumAdapter albumAdapter;
	private View view;
	private SharedPreferences sharedPref;
    private JTask retrieveMedia;
    private RecyclerView recyclerView;
    private View progressBar;
    private View emptyView, storagePremissionView;
    private MainFragment mainFragment;
    private Handler corruptedWarnHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            SimpleDialog dialog = new SimpleDialog(getContext());
            dialog.setTitle("Database corrupted!!!");
            dialog.setMessage("The database has been corrupted!");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setPositiveButton("okay", null);
            dialog.show();
        }
    };

    public AlbumFragment() {

    }

    public void scrollTo(int position) {
        recyclerView.scrollToPosition(position);
    }

    public void putModels(ArrayList<FolderModel> models) {

        if (albumAdapter != null) {
            albumAdapter.setUpdatedData(models);
        } else {
            albumAdapter = new AlbumAdapter(AlbumFragment.this, models);
            notifyDataUpdated();
        }
    }

    public void removeFolder(int folderPosition) {
        albumAdapter.removeItem(folderPosition);
    }

    private void notifyDataUpdated() {

        if (recyclerView.getAdapter() == null) {
            recyclerView.setAdapter(albumAdapter);
        }

        int visibility = albumAdapter.getItemCount() == 0 ? View.VISIBLE: View.GONE;

        if (emptyView != null) {
            emptyView.setVisibility(visibility);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

	public AlbumFragment(int position, MainFragment mainFragment) {
        this.position = position;
        this.mainFragment = mainFragment;
	}

	public int getPagerPosition() {
		return position;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.main_gallery, container, false);
            progressBar = view.findViewById(R.id.main_galery_progressBar);
            emptyView = view.findViewById(R.id.empty_linearLayout);
            storagePremissionView = view.findViewById(R.id.storage_permission_layout);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            populateReciclerView();
        }
		return view;
	}

    public ArrayList<FolderModel> getModels(int position) {
        PathsData.Folder sqldb = null;
        ArrayList<FolderModel> models = new ArrayList<FolderModel>();
        File root = Storage.getFolder(position == 0 ? Storage.IMAGE: Storage.VIDEO);
        root.mkdirs();

        try {
            sqldb = PathsData.Folder.getInstance(getContext());
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException e ) {
            //do something
        }

        if (root.exists()) {
            String Files[] = root.list();
            for (int i = 0;i < Files.length;i++) {
                File file = new File(root, Files[i]);

                if (file.isDirectory()) {
                    File folder_list[] = file.listFiles();
                    String folder_name = null;
                    if (sqldb != null) {
                        folder_name = sqldb.getFolderName(Files[i], position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE);
                    }
                    FolderModel model = new FolderModel();

                    model.setName(folder_name == null ?  Files[i]: folder_name);
                    model.setPath(file.getAbsolutePath());

                    for (int j = 0; j < folder_list.length; j++) {
                        MediaModel mm = new MediaModel(folder_list[j].getAbsolutePath());
                        model.addItem(mm);
                    }
                    models.add(model);
                }
            }
        }
        Collections.sort(models, new Comparator<FolderModel>() {
                @Override public int compare(FolderModel o1, FolderModel o2) { 
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()); 
                } 
            }
        );
        if (sqldb != null)
            sqldb.close();

        return models;
    }

    private void populateReciclerView() {

        retrieveMedia = new JTask() {
            ArrayList<FolderModel> list;

            @Override
            public void workingThread() {
                this.list = getModels(position);
            }

            @Override
            public void onBeingStarted() {

            }

            @Override
            public void onFinished() {
                putModels(list);
                notifyDataUpdated();
            }

            @Override
            public void onException(Exception e) {
                revokeFinish(true);
                Toast.makeText(getContext(), "Unknown error occurred! " + e.getMessage(), 1).show();
                JDebug.writeLog(e.getCause());
            }
            public void  run() {

            }
        };
        retrieveMedia.setThreadPriority(Thread.MAX_PRIORITY);
        retrieveMedia.start();
    }

    private void warnDatabaseCorrupted() {
        corruptedWarnHandler.sendEmptyMessage(0);
    }

    public void inputFolderDialog(final FolderModel model, final int action) {

        Context context = getContext();
        View contentView = getActivity().getLayoutInflater().
            inflate(R.layout.dialog_edit_text, null);
        final EditText editText = contentView.findViewById(R.id.editTextInput);
        String title = null; 

        if (action == ACTION_RENAME_FOLDER) {
            String name = model.getName();
            title = getString(R.string.renomear_pasta);
            editText.setText(name);
            editText.setSelection(name.length());
        } else {
            title = getString(R.string.criar_pasta);
        }

        SimpleDialog dialog = new SimpleDialog(context, SimpleDialog.INPUT_STYLE);
        dialog.setTitle(title);
        dialog.setContentView(contentView);
        dialog.setPositiveButton(context.getString(R.string.concluir), new SimpleDialog.OnDialogClickListener(){

                @Override
                public boolean onClick(SimpleDialog dialog) {
                    String text = editText.getText().toString();
                    String result = validateFolderName(text, getContext());

                    if (!result.equals(FOLDER_NAME_OKAY)) {
                        Toast.makeText(getContext(), result, 1).show();
                        return false;
                    }
                    boolean success = true;
                    String message = null;

                    switch (action) {

                        case ACTION_RENAME_FOLDER:
                            if (success = renameFolder(getContext(), model, text, position)) {
                                message = "Folder renamed to \"" + text + "\".";
                                int index = albumAdapter.getItemPosition(model.getPath());
                                //FolderModel model = albumAdapter.getItem(index);
                                if (index != -1) {
                                    model.setName(text);
                                    albumAdapter.notifyItemChanged(index);
                                }
                            } else {
                                message = "Falied to rename folder! :(";
                            }
                            break;
                        case ACTION_CREATE_FOLDER:
                            FolderModel folder = createFolder(getContext(), text, position);                            
                            if (folder != null) {
                                message = "Folder \"" +  text + "\" created.";
                                albumAdapter.insertItem(folder);
                            } else {
                                message = "Falied to create folder! :(";
                            }
                    }
                    /* if (!success) {
                     //populateReciclerView();
                     }*/
                    ((MainActivity)getActivity()).showSnackBar(message, Snackbar.LENGTH_SHORT);
                    notifyDataUpdated();
                    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
                    return success;
                }
            }
        ).setNegativeButton(getString(R.string.cancelar), null)
            .show();
    }

    public static boolean renameFolder(Context context, FolderModel model, String newName, int position) {
        PathsData.Folder folderDatabase = null;

        try {
            String folderType = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
            folderDatabase = PathsData.Folder.getInstance(context);
            File file = new File(model.getPath());
            String id = file.getName();
            String folderName = folderDatabase.getFolderName(id, folderType);
            //JDebug.toast("ID => " + folderName + "\n NAME => " + model.getName());
            String newFolderId = folderDatabase.getFolderId(newName, folderType);

            if (folderName != null && folderName.equals(newName)) {
                Toast.makeText(context, context.getString(R.string.pasta_mesmo_nome), Toast.LENGTH_LONG).show();
                folderDatabase.close();
                return false;
            }

            if (newFolderId != null) {
                Toast.makeText(context, context.getString(R.string.pasta_existe), 1).show();
                folderDatabase.close();
                return false;
            }

            if (folderName == null) {
                folderDatabase.addName(id, newName, folderType);
            } else {
                folderDatabase.updateName(id, newName, folderType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (folderDatabase != null) {
                folderDatabase.close();
            }
        }
        return true;
    }

    public static FolderModel createFolder(Context context, @NonNull String name, int position) {
        PathsData.Folder folderDatabase = null;
        FolderModel folder = null;

        try {
            String type = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
            folderDatabase = PathsData.Folder.getInstance(context);
            String id = folderDatabase.getFolderId(name, type);
            String randomStr = StringUtils.getRandomString(24);

            if (id == null) {
                id = randomStr;
                int strType = position == 0 ? Storage.IMAGE: Storage.VIDEO;
                File file = new File(Storage.getFolder(strType), randomStr);

                if (file.mkdirs()) {
                    folder = new FolderModel();
                    folderDatabase.addName(id, name, type);
                    folder.setName(name);
                    folder.setPath(file.getAbsolutePath());
                }
            } else {
                Toast.makeText(context, context.getString(R.string.pasta_existe), 1).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally   {
            folderDatabase.close();
        }
        return folder;
    }

    public static String validateFolderName(@NonNull String name, Context context) {
        String nospace = name.replace(" ", "");

        if (nospace.isEmpty()) {
            return  context.getString(R.string.pasta_nome_vazio);
        } else if (name.length() > 50) {
            return context.getString(R.string.pasta_nome_muito_grande);
        } else {
            return FOLDER_NAME_OKAY;
        }
    }

    public void deleteFolder(final FolderModel model) {
        if (model == null) {
            return;
        }
		String name = model.getName();
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getString(R.string.apagar));
		builder.setMessage(String.format(getString(R.string.apagar_pasta_aviso), name));
		builder.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface inter, int p2) {
					final File root = new File(model.getPath());
					final DeleteFilesTask task = new DeleteFilesTask(getContext(), model.getItemsPath(), position, root);
                    task.setOnFinishedListener(new JTask.OnFinishedListener() {

                            @Override
                            public void onFinished() {
                                if (task.deletedAll()) {
                                    albumAdapter.removeItem(model);
                                    notifyDataUpdated();
                                } else {
                                    ((MainActivity)getActivity()).updateFragment(getPagerPosition());
                                }
                            }
                        }
                    );
                    task.start();
				}
            }
        );
		builder.setNegativeButton(getString(R.string.nao), null);
        DialogUtils.configureDialog(builder.show());
	}

	public void update() {
        populateReciclerView();
	}
}
