package com.jefferson.application.br.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.ThemeConfig;

import java.io.File;

public class ImagePreviewFragment extends Fragment implements View.OnClickListener {

    private View parentView;
    private final String path;
    private View optionLayout;

    public ImagePreviewFragment(String path) {
        this.path = path;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null)  {
            parentView = inflater.inflate(R.layout.image_preview_layout, null);
            SubsamplingScaleImageView imageView = parentView.findViewById(R.id.imageView);
            ImageButton exportButton = parentView.findViewById(R.id.image_preview_move_gallery_button);
            ImageButton deleteButton = parentView.findViewById(R.id.image_preview_delete_button);
            ImageView gifView = parentView.findViewById(R.id.gif_view);
            optionLayout = parentView.findViewById(R.id.image_preview_options_layout);
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
            imageView.setSoundEffectsEnabled(false);
            imageView.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            exportButton.setOnClickListener(this);
        }
        return parentView;
    }
    
    public void switchOptionsVisibility() {
        int visibility = optionLayout.getVisibility() == View.VISIBLE ? View.GONE: View.VISIBLE;
        optionLayout.setVisibility(visibility);
    }
    
    public static String getMimeType(String url) { 
        String type = null; 
        String extension = MimeTypeMap.getFileExtensionFromUrl(url); 
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type; 
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
                switchOptionsVisibility();
                break;
            case R.id.image_preview_delete_button:
                dialogDeletionConfirmation();
                break;
            case R.id.image_preview_move_gallery_button:
                exportImage();
                break;
        }
    }

    private void exportImage() {

    }

    private void  dialogDeletionConfirmation() {
        SimpleDialog builder = new SimpleDialog(getContext(), ThemeConfig.getTheme(getContext()));
        builder.setIconColor(Color.argb(255, 255, 0, 0));
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(getString(R.string.apagar));
        builder.setMessage(getString(R.string.apagar_image_mensagem));
        builder.setPositiveButton(getString(android.R.string.yes), new SimpleDialog.OnDialogClickListener() {

            @Override
            public boolean onClick(SimpleDialog dialog) {
                return true;
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.show();

    }
}
