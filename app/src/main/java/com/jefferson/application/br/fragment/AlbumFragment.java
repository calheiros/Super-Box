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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.jefferson.application.br.App;
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

public class AlbumFragment extends Fragment {

	private String root;
	private int position;
	private AlbumAdapter mAdapter;
	private View view;
	private SharedPreferences sharedPref;

    public final static int ACTION_CREATE_FOLDER = 122;
    public final static int ACTION_RENAME_FOLDER = 54;

    private RecyclerView recyclerView;
    private View progressBar;
    private View emptyView;

	public AlbumFragment() {

	}

	public static Fragment newInstance(int position) {
        AlbumFragment frament = new AlbumFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		frament.setArguments(bundle);
		return frament;
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
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
            position = getArguments() != null ? getArguments().getInt("position", 0): 0;
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            populateReciclerView();
        }
		return view;
	}

    private void setEmptyViewVisibility(int visibility) {

        if (emptyView != null && emptyView.getVisibility() != visibility) {
            emptyView.setVisibility(visibility);
        }
    }

    private void hideProgressBar() {

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

    }

    private void populateReciclerView() {

        final Handler handler =  new Handler(){

            @Override
            public void  handleMessage(Message m) {
                ArrayList<FolderModel> list = (ArrayList<FolderModel>) m.getData().getParcelableArrayList("list");
                setEmptyViewVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                hideProgressBar();

                mAdapter = new AlbumAdapter(AlbumFragment.this, list);
                if (recyclerView != null) {
                    recyclerView.setAdapter(mAdapter);
                }
            }
        };

        Thread thread = new Thread() {

            @Override
            public void  run() {
                ArrayList<FolderModel> list = getLocalList();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("list", list);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

	private ArrayList<FolderModel> getLocalList() {

		ArrayList<FolderModel> models = new ArrayList<FolderModel>();
        File root = Storage.getFolder(position == 0 ? Storage.IMAGE: Storage.VIDEO);
		root.mkdirs();
		PathsData.Folder sqldb = PathsData.Folder.getInstance(getContext());

        if (root.exists()) {
			String Files[] = root.list();
			for (int i = 0;i < Files.length;i++) {
				File file = new File(root, Files[i]);

				if (file.isDirectory()) {
					File folder_list[] = file.listFiles();
					String folder_name = sqldb.getFolderName(Files[i], position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE);
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
		sqldb.close();
		return models;
	}

    public void inputFolderDialog(final FolderModel model, final int action) {

        Context context = getContext();
        View contentView = getActivity().getLayoutInflater().
            inflate(R.layout.dialog_edit_text, null);
        final EditText editText = contentView.findViewById(R.id.editTextInput);
        
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        
        String title = null; 

        if (action == ACTION_RENAME_FOLDER) {
            String name = model.getName();
            title = getString(R.string.renomear_pasta);
            editText.setText(name);
            editText.setSelection(name.length());
        } else {
            title = getString(R.string.criar_pasta);
        }

        SimpleDialog dialog = new SimpleDialog(context);
        dialog.setTitle(title);
        dialog.setContentView(contentView);
        dialog.setPositiveButton(context.getString(R.string.concluir), new SimpleDialog.OnDialogClickListener(){

                @Override
                public boolean onClick(SimpleDialog dialog) {
                    String text = editText.getText().toString();
                    AlbumFragment.ValidateResult result = validateFolderName(text);

                    if (!result.passed) {
                        Toast.makeText(getContext(), result.message, 1).show();
                        return false;
                    }

                    switch (action) {
                        case ACTION_RENAME_FOLDER:
                            return renameFolder(model, text);
                        case ACTION_CREATE_FOLDER:
                            return createFolder(text);
                    }
                    return true;
                }
            }
        ).setNegativeButton(getString(R.string.cancelar), null).show();
    }

    public boolean renameFolder(FolderModel model, String newName) {

        Context activity = App.getAppContext();
        String folderType = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
        PathsData.Folder folderDatabase = PathsData.Folder.getInstance(activity);
        File file = new File(model.getPath());
        String id = file.getName();
        String folderName = folderDatabase.getFolderName(id, folderType);
        JDebug.toast("ID => " + folderName + "\n NAME => " + model.getName());
        String newFolderId = folderDatabase.getFolderId(newName, folderType);

        if (folderName != null && folderName.equals(newName)) {
            Toast.makeText(getContext(), getString(R.string.pasta_mesmo_nome), Toast.LENGTH_LONG).show();
            folderDatabase.close();
            return false;
        }

        if (newFolderId != null) {
            Toast.makeText(getContext(), getString(R.string.pasta_existe), 1).show();
            folderDatabase.close();
            return false;
        }

        if (folderName == null) {
            folderDatabase.addName(id, newName, folderType);
        } else {
            folderDatabase.updateName(id, newName, folderType);
        }

        Snackbar.make(view, "Folder renamed to \"" + newName + "\"", Snackbar.LENGTH_SHORT).show();
        folderDatabase.close();
        update();
        return true;
    }

    public boolean createFolder(@NonNull String name) {

        String type = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
        PathsData.Folder folderDatabase = PathsData.Folder.getInstance(getContext());
        String id = folderDatabase.getFolderId(name, type);
        String randomStr = StringUtils.getRandomString(24);
        if (id == null) {
            id = randomStr;
            int strType = position == 0 ? Storage.IMAGE: Storage.VIDEO;
            File file = new File(Storage.getFolder(strType), randomStr);

            if (file.mkdirs()) {
                folderDatabase.addName(id, name, type);
                Snackbar.make(view, "Created folder \"" + name + "\"", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.pasta_existe), 1).show();
            return false;
        }

        folderDatabase.close();
        update();
        return true;
    }

    public ValidateResult validateFolderName(@NonNull String name) {
        ValidateResult result = new ValidateResult();
        String nospace = name.replace(" ", "");

        if (nospace.isEmpty()) {
            result.message = "Name is empty!";
        } else if (name.length() > 50) {
            result.message = "Many characters!";
        } else {
            result.passed = true;
        }

        return result;
    }

    public void deleteAlbum(final FolderModel model) {
		String name = "\"" + model.getName() + "\"";
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getString(R.string.apagar));
		builder.setMessage(String.format(getString(R.string.apagar_pasta_aviso), name));
		builder.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface inter, int p2) {
					File root = new File(model.getPath());
					DeleteFilesTask task = new DeleteFilesTask(getContext(), model.getItemsPath(), position, root);
                    task.setOnFinishedListener(new JTask.OnFinishedListener() {

                            @Override
                            public void onFinished() {
                                ((MainActivity)getActivity()).updateFragment(getPagerPosition());
                            }
                        }
                    );
                    task.start();
				}
            }
        );

		builder.setNegativeButton(getString(R.string.nao), null);
        DialogUtils.configureRoudedDialog(builder.show());
	}

	public void update() {
        populateReciclerView();
	}

    class ValidateResult {
        public boolean passed = false;
        public String message = "";
    }
}
