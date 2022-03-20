package com.jefferson.application.br.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.activity.*;
import com.jefferson.application.br.adapter.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.util.Debug;
import android.support.v4.app.Fragment;
import com.jefferson.application.br.task.*;
import com.jefferson.application.br.database.PathsData.Folder;
import android.widget.EditText;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

public class AlbumFragment extends Fragment {

	private String root;
	private int position;
	private AlbumAdapter mAdapter;
	private View view;
	private SharedPreferences sharedPref;

    public final static int ACTION_CREATE_FOLDER = 122;
    public final static int ACTION_RENAME_FOLDER = 54;
    
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

        view = inflater.inflate(R.layout.main_gallery, container, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		root = Environment.getExternalStorageDirectory().getAbsolutePath();
		position = getArguments() != null ? getArguments().getInt("position", 0): 0;

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
	    GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

		mRecyclerView.setLayoutManager(layoutManager);
		mAdapter = new AlbumAdapter(this, getLocalList());
        mRecyclerView.setAdapter(mAdapter);
		return view;
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
						model.addItem(folder_list[j].getAbsolutePath());
					}
					models.add(model);
					/*if (folder_list.length > 0)
                     models.add(model);
                     else file.delete();*/
				}
			}
		}

		View noticeView = view.findViewById(R.id.empty_linearLayout);
		noticeView.setVisibility(models.isEmpty() ? View.VISIBLE : View.GONE);
		sqldb.close();
		return models;
	}

	final class DeleteAlbumTask extends DeleteFilesTask  {

        public DeleteAlbumTask(Context p1, ArrayList<String> p2, int p3, File p4) {
			super(p1, p2, p3, p4);
		}

        @Override
		protected void onPreExecute() {
           
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			((MainActivity)getActivity()).update(getPagerPosition() == 0 ? MainFragment.ID.FIRST: MainFragment.ID.SECOND);
		}
	}

    public void inputFolderDialog(final FolderModel model, final int action) {

        Context context = getContext();
        View contentView = getActivity().getLayoutInflater().
            inflate(R.layout.dialog_edit_text, null);
        final EditText editText = contentView.findViewById(R.id.editTextInput);
        String title = action == ACTION_RENAME_FOLDER ? "Renomear pasta" : "Criar pasta";

        if (model != null)
            editText.setText(model.getName());

        new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(contentView)
            .setPositiveButton(context.getString(R.string.renomear), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dInterface, int p) {
                    String text = editText.getText().toString();
                    switch (action) {
                        case ACTION_RENAME_FOLDER:
                            renameFolder(model, text);
                            break;
                        case ACTION_CREATE_FOLDER:
                            createFolder(text);
                            break;
                    }
                }
            }
        ).show();
    }

    public void renameFolder(FolderModel model, String newName) {

        Context activity = App.getAppContext();
        String folderType = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
        PathsData.Folder folderDatabase = PathsData.Folder.getInstance(activity);
        File file = new File(model.getPath());
        String id = file.getName();
        String folderName = folderDatabase.getFolderName(id, folderType);
        Debug.toast("ID => " + folderName + "\n NAME => " + model.getName());

        if (folderName == null) {
            folderDatabase.addName(id, newName, folderType);
        } else if (folderName.equals(newName)) {
            Toast.makeText(getContext(), "The new name can not be the same!", Toast.LENGTH_LONG).show();
            folderDatabase.close();
            return;
        } else {
            folderDatabase.updateName(id, newName, folderType);
        }
        Snackbar.make(view, "Folder renamed: " + newName, Snackbar.LENGTH_SHORT).show();
        folderDatabase.close();
        update();
    }

    public void createFolder(String name) {

        String type = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
        PathsData.Folder folderDatabase = PathsData.Folder.getInstance(getContext());
        String id = folderDatabase.getFolderId(name, type);
        String randomStr = RandomString.getRandomString(24);

        if (id == null) {
            
            id = randomStr;
            int strType = position == 0 ? Storage.IMAGE: Storage.VIDEO;
            File file = new File(Storage.getFolder(strType), randomStr);
            
            if (file.mkdirs()) {
                folderDatabase.addName(id, name, type);
                Snackbar.make(view, "Creating folder \"" + name + "\"", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Folder alreay exists!", 1).show();
        }
        folderDatabase.close();
        update();
    }
    public void deleteAlbum(final FolderModel model) {

		String name = model.getName();
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getString(R.string.apagar));
		builder.setMessage("Tem certeza que deseja apagar a pasta \"" + name + "\" e todo o seu conteudo permanentimente?");
		builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface inter, int p2) {
					File root = new File(model.getPath());
					new DeleteAlbumTask(getContext(), model.getItems(), position, root).execute();
				}
            });
		builder.setNegativeButton("Não", null);
		builder.create().show();
	}

	public void update() {
		if (mAdapter != null) {
			mAdapter.setUpdatedData(getLocalList());
		}
	}
}
