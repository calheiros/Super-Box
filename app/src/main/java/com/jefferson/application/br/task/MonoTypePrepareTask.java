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
import android.widget.Toast;

public class MonoTypePrepareTask extends JTask {

    private ArrayList<String> paths;
    private String type;
    private ArrayList<FileModel> models = new ArrayList<>();
    private SimpleDialog dialog;
    private String parentPath;
    private Context context;
    private ImportTask.Listener listener;
    private onLoopListener loopListener;
    private String destinationPath;
    
    public  MonoTypePrepareTask(Context context, ArrayList<String> paths, String type, String parent, ImportTask.Listener listener) {
        this.paths = paths;
        this.type = type;
        this.listener = listener;
        this.context = context;
        this.parentPath = parent;
    }

    @Override
    public void workingThread() {
        
        for (String path : paths) {
            if (loopListener != null) {
                loopListener.onLoop(path);
            }
            if (isCancelled()) {
                break;
            }
            FileModel model = new FileModel();
            model.setResource(path);
            model.setDestination(destinationPath);
            model.setParentPath(parentPath);
            model.setType(type);
            models.add(model);
        }
    }
    
    public void setDestination(String destination){
        this.destinationPath = destination;
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
    protected void onTaskCancelled() {
        super.onTaskCancelled();
        dialog.cancel();
        Toast.makeText(context, "Cancelled", 0).show();
    }
    
    @Override
    public void onFinished() {
        proceed();
    }
    
    public void proceed() {
        dialog.cancel();
        
        if (getStatus() == Status.FINISHED) {
            new ImportTask(context, models, listener).start();
        } else {
            revokeFinish(false);
        }
    }
    
    @Override
    public void onException(Exception e) {
        JDebug.writeLog(e.getCause());
    }
    
    public void setOnLoopListener(onLoopListener listener) {
        this.loopListener = listener;
    }
    
    public static interface onLoopListener{
       void onLoop(String path)
    }
}
