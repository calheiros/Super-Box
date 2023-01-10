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

package com.jefferson.application.br.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.activity.ViewAlbum;
import com.jefferson.application.br.adapter.AlbumAdapter;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsDatabase;
import com.jefferson.application.br.model.FolderModel;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.model.SimplifiedAlbum;
import com.jefferson.application.br.task.DeleteFilesTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class AlbumFragment extends Fragment {

    public static final String FOLDER_NAME_OKAY = "folder_name_okay";
    public final static int ACTION_CREATE_FOLDER = 122;
    public final static int ACTION_RENAME_FOLDER = 54;
    private final Handler corruptedWarnHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            SimpleDialog dialog = new SimpleDialog(requireActivity());
            dialog.setTitle("Database corrupted!!!");
            dialog.setMessage("The database has been corrupted!");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setPositiveButton("okay", null);
            dialog.show();
        }
    };

    private int position;
    private AlbumAdapter albumAdapter;
    private View view;
    private JTask retrieveMedia;
    private RecyclerView recyclerView;
    private View progressBar;
    private View emptyView;
    private int paddingBottom;

    public AlbumFragment() {

    }

    public AlbumFragment(int position, MainFragment mainFragment) {
        this.position = position;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.main_gallery, container, false);
            progressBar = view.findViewById(R.id.main_galery_progressBar);
            emptyView = view.findViewById(R.id.empty_linearLayout);
            View storagePermissionView = view.findViewById(R.id.storage_permission_layout);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            recyclerView = view.findViewById(R.id.recyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setClipToPadding(false);
            recyclerView.setPadding(0, 0, 0, paddingBottom);
            populateRecyclerView();
        }

        return view;
    }

    public void openAlbum(FolderModel f_model) {
        Intent intent = new Intent(getContext(), ViewAlbum.class);
        intent.putExtra("position", getPagerPosition());
        intent.putExtra("name", f_model.getName());
        intent.putExtra("data", f_model.getItems());
        intent.putExtra("folder", f_model.getPath());
        requireActivity().startActivity(intent);
    }

    public void openAlbum(String albumName) {
        int pos = albumAdapter.getItemPositionByName(albumName);
        if (pos != -1) {
            openAlbum(albumAdapter.getItem(pos));
        }
    }

    private static class BuildResult {
       ArrayList<SimplifiedAlbum> simplifiedAlbums;
       ArrayList<FolderModel> folderModels;

       public BuildResult() {
           simplifiedAlbums = new ArrayList<>();
           folderModels = new ArrayList<>();
       }
    }

    public BuildResult buildModels(int position) {
        BuildResult result = new BuildResult();
        PathsDatabase database = null;
        ArrayList<SimplifiedAlbum> simplifiedModels = new ArrayList<>();
        ArrayList<FolderModel> models = new ArrayList<FolderModel>();
        File root = Storage.getFolder(position == 0 ? Storage.IMAGE : Storage.VIDEO);
        root.mkdirs();

        try {
            database = PathsDatabase.getInstance(getContext());
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException e) {
            //do something
            return result;
        }
        Map<String, Boolean> bookmark = database.getFavoritesFolder();

        if (root.exists()) {
            String[] files = root.list();
            if (files != null)
                for (String s : files) {
                    File file = new File(root, s);

                    if (file.isDirectory()) {
                        File[] folder_list = file.listFiles();
                        String folderName = null;
                        boolean favorite = false;
                        folderName = database.getFolderName(s, position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE);

                        if (bookmark != null) {
                            favorite = Boolean.TRUE.equals(bookmark.get(file.getName()));
                        }
                        folderName = folderName == null ? s : folderName;
                        FolderModel model = new FolderModel();
                        model.setName(folderName);
                        model.setPath(file.getAbsolutePath());
                        model.setFavorite(favorite);

                        if (folder_list != null) for (File value : folder_list) {
                            MediaModel mm = new MediaModel(value.getAbsolutePath());
                            model.addItem(mm);
                        }

                        ArrayList<MediaModel> items = model.getItems();
                        String thumb = items.size() > 0 ? items.get(0).getPath() : null;
                        simplifiedModels.add(new SimplifiedAlbum(folderName, thumb));
                        models.add(model);
                    }
                }
        }
        FolderModel.sort(models);
        result.folderModels = models;
        result.simplifiedAlbums = simplifiedModels;
        database.close();
        return result;
    }

    private void populateRecyclerView() {
        retrieveMedia = new JTask() {
            private ArrayList<SimplifiedAlbum> simplifiedModels;
            private ArrayList<FolderModel> albumsModel;

            @Override
            public void workingThread() {
                BuildResult result = buildModels(position);
                this.albumsModel = result.folderModels;
                this.simplifiedModels = result.simplifiedAlbums;
            }

            @Override
            public void onBeingStarted() {
            }

            @Override
            public void onFinished() {
                putModels(albumsModel, simplifiedModels);
                notifyDataUpdated();
            }

            @Override
            public void onException(Exception e) {
                revokeFinish(true);
                Toast.makeText(getContext(), "Unknown error occurred! " + e.getMessage(), Toast.LENGTH_LONG).show();
                JDebug.writeLog(e.getCause());
            }

        };
        retrieveMedia.setThreadPriority(Thread.MAX_PRIORITY);
        retrieveMedia.start();
    }
    public static boolean renameFolder(Context context, FolderModel model, String newName, int position) {
        PathsDatabase folderDatabase = null;

        try {
            String folderType = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
            folderDatabase = PathsDatabase.getInstance(context);
            File file = new File(model.getPath());
            String id = file.getName();
            String folderName = folderDatabase.getFolderName(id, folderType);
            //JDebug.toast("ID => " + folderName + "\n NAME => " + model.getName());
            String newFolderId = folderDatabase.getFolderIdFromName(newName, folderType);

            if (folderName != null && folderName.equals(newName)) {
                Toast.makeText(context, context.getString(R.string.pasta_mesmo_nome), Toast.LENGTH_LONG).show();
                folderDatabase.close();
                return false;
            }

            if (newFolderId != null) {
                Toast.makeText(context, context.getString(R.string.pasta_existe), Toast.LENGTH_LONG).show();
                folderDatabase.close();
                return false;
            }

            if (folderName == null) {
                folderDatabase.addFolderName(id, newName, folderType);
            } else {
                folderDatabase.updateFolderName(id, newName, folderType);
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
        PathsDatabase database = null;
        FolderModel folder = null;

        try {
            String type = position == 0 ? FileModel.IMAGE_TYPE : FileModel.VIDEO_TYPE;
            database = PathsDatabase.getInstance(context);
            String id = database.getFolderIdFromName(name, type);
            String randomStr = StringUtils.getRandomString(24);

            if (id == null) {
                id = randomStr;
                int strType = position == 0 ? Storage.IMAGE : Storage.VIDEO;
                File file = new File(Storage.getFolder(strType), randomStr);

                if (file.mkdirs()) {
                    folder = new FolderModel();
                    database.addFolderName(id, name, type);
                    folder.setName(name);
                    folder.setPath(file.getAbsolutePath());
                }
            } else {
                Toast.makeText(context, context.getString(R.string.pasta_existe), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null)
                database.close();
        }
        return folder;
    }

    public static String validateFolderName(@NonNull String name, Context context) {
        String noSpace = name.replace(" ", "");

        if (noSpace.isEmpty()) {
            return context.getString(R.string.pasta_nome_vazio);
        } else if (name.length() > 50) {
            return context.getString(R.string.pasta_nome_muito_grande);
        } else {
            return FOLDER_NAME_OKAY;
        }
    }

    public void scrollTo(int position) {
        recyclerView.scrollToPosition(position);
    }

    public void putModels(ArrayList<FolderModel> models, ArrayList<SimplifiedAlbum> simplifiedModels) {

        if (albumAdapter != null) {
            albumAdapter.updateModels(models, simplifiedModels);
        } else {
            albumAdapter = new AlbumAdapter(AlbumFragment.this, models, simplifiedModels);
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

        int visibility = albumAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE;

        if (emptyView != null) {
            emptyView.setVisibility(visibility);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public int getPagerPosition() {
        return position;
    }

    private void warnDatabaseCorrupted() {
        corruptedWarnHandler.sendEmptyMessage(0);
    }

    public void inputFolderDialog(final FolderModel model, final int action) {
        Activity activity = requireActivity();
        View contentView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
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

        SimpleDialog dialog = new SimpleDialog(activity, SimpleDialog.STYLE_INPUT);
        dialog.setTitle(title);
        dialog.setContentView(contentView);
        dialog.setPositiveButton(activity.getString(R.string.concluir), new SimpleDialog.OnDialogClickListener() {

            @Override
            public boolean onClick(SimpleDialog dialog) {
                String text = editText.getText().toString();
                String result = validateFolderName(text, getContext());

                if (!result.equals(FOLDER_NAME_OKAY)) {
                    Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                    return false;
                }
                boolean success = true;
                String message = null;

                switch (action) {

                    case ACTION_RENAME_FOLDER:
                        if (success == renameFolder(getContext(), model, text, position)) {
                            message = "Folder renamed to \"" + text + "\".";
                            int index = albumAdapter.getItemPosition(model.getPath());
                            //FolderModel model = albumAdapter.getItem(index);
                            if (index != -1) {
                                SimplifiedAlbum simplifiedAlbum = albumAdapter
                                        .getSimplifiedAlbumByName(model.getName());
                                if (simplifiedAlbum != null) {
                                    simplifiedAlbum.setName(text);
                                }
                                model.setName(text);
                                albumAdapter.notifyItemChanged(index);
                            }
                        } else {
                            message = "Failed to rename folder! :(";
                        }
                        break;
                    case ACTION_CREATE_FOLDER:
                        FolderModel folder = createFolder(getContext(), text, position);
                        if (folder != null) {
                            message = "Folder \"" + text + "\" created.";
                            albumAdapter.insertItem(folder);
                        } else {
                            message = "Failed to create folder! :(";
                        }
                }
                notifyDataUpdated();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                return success;
            }
        }).setNegativeButton(getString(R.string.cancelar), null).show();
    }

    public void deleteFolder(final FolderModel model) {
        if (model == null) {
            return;
        }
        String name = model.getName();
        SimpleDialog simpleDialog = new SimpleDialog(requireActivity(), SimpleDialog.STYLE_ALERT_HIGH);
        simpleDialog.setTitle(getString(R.string.apagar));
        simpleDialog.setMessage(getString(R.string.apagar_pasta_aviso, name));
        simpleDialog.setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener() {
            @Override
            public boolean onClick(SimpleDialog dialog) {
                final File root = new File(model.getPath());
                final DeleteFilesTask task = new DeleteFilesTask(requireActivity(), model.getItemsPath(), position, root);
                task.setOnFinishedListener(new JTask.OnFinishedListener() {

                    @Override
                    public void onFinished() {
                        if (task.deletedAll()) {
                            albumAdapter.removeItem(model);
                            notifyDataUpdated();
                        } else {
                            ((MainActivity) requireActivity()).updateFragment(getPagerPosition());
                        }
                    }
                });
                task.start();
                return true;
            }
        });
        simpleDialog.setNegativeButton(getString(R.string.nao), null);
        simpleDialog.show();

    }

    public void update() {
        populateRecyclerView();
    }

    public void addToFavorites(FolderModel f_model) {
        PathsDatabase database = PathsDatabase.getInstance(requireContext());
        File file = new File(f_model.getPath());
        String name = file.getName();
        boolean success = database.setFavoriteFolder(name);
        database.close();

        if (!success) {
            Toast.makeText(requireContext(), "failed to ADD to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }
        f_model.setFavorite(true);
        Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_LONG).show();
    }

    public void reload() {
        if (emptyView != null && emptyView.getVisibility() == View.VISIBLE)
            emptyView.setVisibility(View.GONE);

        if (retrieveMedia != null && retrieveMedia.getStatus() == JTask.Status.STARTED)
            retrieveMedia.cancelTask();

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (albumAdapter != null) {
            albumAdapter.updateModels(new ArrayList<FolderModel>(), new ArrayList<SimplifiedAlbum>());
        }

        populateRecyclerView();
    }

    public void removeFromFavorites(FolderModel f_model) {
        PathsDatabase database = PathsDatabase.getInstance(requireContext());
        File file = new File(f_model.getPath());
        String name = file.getName();

        if (!database.removeFavoriteFolder(name)) {
            Toast.makeText(requireContext(), "failed to remove from bookmark", Toast.LENGTH_SHORT).show();
            return;
        }
        f_model.setFavorite(false);
    }

    public void setBottomPadding(int paddingBottom) {
        this.paddingBottom = paddingBottom;
        if (recyclerView != null) {
            recyclerView.setPadding(0, 0, 0, paddingBottom);
        }
    }

    public boolean isLoading() {
        return retrieveMedia.getStatus() == JTask.Status.STARTED;
    }

    public ArrayList<SimplifiedAlbum> getSimplifiedModels() {
        return albumAdapter.getSimplifiedModels();
    }

    public void scrollToAlbum(String albumName) {
       int position = albumAdapter.getItemPositionByName(albumName);
       if (position != -1) {
           RecyclerView.SmoothScroller smoothScroller = new
                   LinearSmoothScroller(requireContext()) {
                       @Override
                       protected int getVerticalSnapPreference() {
                           return LinearSmoothScroller.SNAP_TO_START;
                       }

                       @Override
                       protected void onStop() {
                           super.onStop();
                           albumAdapter.setItemToHighlight(position);
                           albumAdapter.notifyItemChanged(position);
                       }
                   };

           smoothScroller.setTargetPosition(position);
           Objects.requireNonNull(recyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);

       }
    }
}
