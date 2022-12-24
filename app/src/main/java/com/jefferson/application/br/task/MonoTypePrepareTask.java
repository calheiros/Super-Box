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
    
    private String parentPath;
    private Context context;
    private onLoopListener loopListener;
    private String destinationPath;
    
    public  MonoTypePrepareTask(Context context, ArrayList<String> paths, String type, String parent) {
        this.paths = paths;
        this.type = type;
        this.context = context;
        this.parentPath = parent;
    }

    public ArrayList<FileModel>  getData() {
        return models;
    }

    @Override
    public void workingThread() {
        
        for ( int i = 0; i < paths.size(); i++) {
            if (isCancelled()) {
                break;
            }
            String path = paths.get(i);
            FileModel model = new FileModel();
            model.setResource(path);
            model.setParentPath(parentPath);
            model.setType(type);
            models.add(model);
            sendUpdate(1, i + 1, paths.size());
        }
    }
    
    public void setDestination(String destination){
        this.destinationPath = destination;
    }
    
    @Override
    public void onBeingStarted() {

    }

    @Override
    protected void onTaskCancelled() {
        super.onTaskCancelled();
        Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onFinished() {
        //proceed();
    }
    
//    public void proceed() {
//        dialog.cancel();
//        
//        if (getStatus() == Status.FINISHED) {
//            new ImportTask(context, models, listener).start();
//        } else {
//            revokeFinish(false);
//        }
//    }
    
    @Override
    public void onException(Exception e) {
        JDebug.writeLog(e.getCause());
    }
    
    public void setOnLoopListener(onLoopListener listener) {
        this.loopListener = listener;
    }
    
    public static interface onLoopListener{
       void onLoop(String path);
    }
}
