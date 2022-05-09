package com.jefferson.application.br.fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.util.Storage;
import java.io.File;

public class ImagePreviewFragment extends Fragment {

    private View parentView;
    private String path;
    private SubsamplingScaleImageView imageView;

    public ImagePreviewFragment(String path) {
        this.path = path;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null)  {
            parentView = inflater.inflate(R.layout.image_preview_layout, null);
            imageView = parentView.findViewById(R.id.imageView);
            imageView.setImage(ImageSource.uri(path));
        }
        PathsData database = PathsData.getInstance(getContext(), Storage.getDefaultStoragePath());
        String originPath = database.getPath(new File(path).getName());
        if (originPath != null) {
            Toast.makeText(getContext(), "Memi type " + getMimeType(originPath), 1).show();
        }
        return parentView;
    }

    public static String getMimeType(String url) { 
        String type = null; String extension = MimeTypeMap.getFileExtensionFromUrl(url); 
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type; 
    }
}
