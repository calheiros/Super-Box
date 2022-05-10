package com.jefferson.application.br.fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
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
    private ImageView gifView;

    public ImagePreviewFragment(String path) {
        this.path = path;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null)  {
            parentView = inflater.inflate(R.layout.image_preview_layout, null);
            imageView = parentView.findViewById(R.id.imageView);
            gifView = parentView.findViewById(R.id.gif_view);
            PathsData database = PathsData.getInstance(getContext(), Storage.getDefaultStoragePath());
            String originPath = originPath = database.getPath(new File(path).getName());
            database.close();

            if (originPath != null) {
                String mimeType = getMimeType(originPath);
                if (mimeType != null && mimeType.endsWith("/gif")) {
                    Glide.with(getContext()).load("file://" + path).skipMemoryCache(true).into(gifView);
                    return parentView;
                } 
            }
            imageView.setImage(ImageSource.uri(path));
        }
        return parentView;
    }

    public static String getMimeType(String url) { 
        String type = null; 
        String extension = MimeTypeMap.getFileExtensionFromUrl(url); 
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type; 
    }
}
