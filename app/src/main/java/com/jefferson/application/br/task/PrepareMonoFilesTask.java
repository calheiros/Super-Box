package com.jefferson.application.br.task;

import android.content.Context;
import android.view.View;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.ViewAlbum;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.util.JDebug;
import java.util.ArrayList;
import android.view.LayoutInflater;

public class PrepareMonoFilesTask extends JTask {

    private ArrayList<String> paths;
    private String type;
    private ArrayList<FileModel> models = new ArrayList<>();
    private SimpleDialog dialog;
    private String parentPath;
    private Context context;
    private ImportTask.Listener listener;
    
    public  PrepareMonoFilesTask(Context context, ArrayList<String> paths, String type, String parent, ImportTask.Listener listener) {
        this.paths = paths;
        this.type = type;
        this.listener = listener;
        this.context = context;
        this.parentPath = parent;
    }

    @Override
    public void workingThread() {
        for (String path : paths) {
            FileModel model = new FileModel();
            model.setResource(path);
            model.setParentPath(parentPath);
            model.setType(type);
            models.add(model);
        }
    }

    @Override
    public void onBeingStarted() {
        View view = ((LayoutInflater)App.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.inderteminate_progress_layout, null);
        dialog = new SimpleDialog(context);
        dialog.setContentView(view);
        dialog.setTitle("Preparing...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onFinished() {
        dialog.cancel();
        new ImportTask(context, models, listener).start();
    }

    @Override
    public void onException(Exception e) {
        JDebug.writeLog(e.getCause());
    }
}
