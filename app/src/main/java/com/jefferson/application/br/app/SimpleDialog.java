package com.jefferson.application.br.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.jefferson.application.br.R;
import com.jefferson.application.br.library.NumberProgressBar;
import com.jefferson.application.br.util.BlurViewUtils;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;

public class SimpleDialog {

    public static final int STYLE_PROGRESS = 123;
    public static final int STYLE_ALERT = 321;
    public static final int STYLE_INPUT = 444;
    public static final int STYLE_MENU = 4;
    private final String TAG = "SimpleDialog";

    private final Activity activity;
    private ViewGroup parentView;
    private NumberProgressBar progressBar;
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressBar.setProgress(msg.what);
        }
    };
    private TextView contentText;
    private TextView contentTitle;
    private SimpleDialog progressBarDialog;
    private long maxBytes;
    private long currentBytes;
    private int progress;
    private boolean dismissed = false;
    private Button positiveButton;
    private Button negativeButton;
    private JDialog jdialog;
    private View editTextLayout;
    private ImageView iconView;
    private ListView menuListView;

    public SimpleDialog(Activity activity, int style) {
        this.activity = activity;
        createView(style);
    }

    public SimpleDialog(Activity activity) {
        this.activity = activity;
        createView(0);
    }

    public static List<SimpleDialog.MenuItem> getMenuItems(String[] names, int[] icons) {
        List<SimpleDialog.MenuItem> menu = new ArrayList<MenuItem>();
        for (int i = 0; i < names.length; i++) {
            menu.add(new SimpleDialog.MenuItem(names[i], icons[i]));
        }
        return menu;
    }

    public void cancel() {
        jdialog.cancel();
    }

    public void setCanceledOnTouchOutside(boolean cancelable) {
        jdialog.setCanceledOnTouchOutside(cancelable);
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void registerBytes(long count) {
        this.currentBytes = count;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(long max) {
        this.maxBytes = max;
    }

    public int getMax() {
        return progressBar.getMax();
    }

    public SimpleDialog setMax(int value) {
        progressBar.setMax(value);
        return this;
    }

    public SimpleDialog setSingleLineMessage(boolean single) {
        TextUtils.TruncateAt ellipsize = single ? TextUtils.TruncateAt.MIDDLE : TextUtils.TruncateAt.END;
        int maxLines = single ? 1 : 256;

        contentText.setMaxLines(maxLines);
        contentText.setEllipsize(ellipsize);
        return this;
    }

    private void createView(int style) {
        progressBarDialog = this;
        parentView = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.dialog_main_layout, null);
        progressBar = parentView.findViewById(R.id.number_progress_bar);
        BlurView blurView = parentView.findViewById(R.id.blurView);
        contentTitle = parentView.findViewById(R.id.title_text_view);
        contentText = parentView.findViewById(R.id.message_text_view);
        positiveButton = parentView.findViewById(R.id.dialogPositiveButton);
        negativeButton = parentView.findViewById(R.id.dialogNegativebutton);
        editTextLayout = parentView.findViewById(R.id.dialog_edit_text_layout);
        iconView = parentView.findViewById(R.id.dialog_icon);

        this.jdialog = new JDialog(activity, style == STYLE_INPUT);
        int color = ContextCompat.getColor(activity, R.color.colorAccent);

        progressBar.setMax(100);
        progressBar.setReachedBarColor(color);
        progressBar.setProgressTextColor(color);
        configureBlur(activity, blurView);
        configureStyle(style);
    }

    private void configureBlur(Activity activity, BlurView blurView) {
        float radius = 20f;
        View decorView = activity.getWindow().getDecorView();
        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        blurView.setClipToOutline(true);
        blurView.setBlurAutoUpdate(true);
        BlurAlgorithm render = BlurViewUtils.getRenderAlgorithm(activity);
        blurView.setupWith(rootView, render) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);

    }

    public void resetDialog() {
        //reset proprieties
        showNegativeButton(false);
        showPositiveButton(false);
        showTextMessage(false);
        setSingleLineMessage(false);
        showProgressBar(false);
    }

    public void setMenuItems(List<MenuItem> items, AdapterView.OnItemClickListener listener) {
        if (menuListView == null) {
            createMenu();
            Log.w(TAG, "menuListView was not created early!");
        }
        menuListView.setAdapter(new DialogMenuAdapter(items, getContext()));
        menuListView.setOnItemClickListener(listener);
    }

    private void configureStyle(int style) {

        switch (style) {
            case STYLE_MENU:
                createMenu();
                break;
            case STYLE_PROGRESS:
                showProgressBar(true);
                break;
            case STYLE_INPUT:
                //showEditText(true);
                break;
        }
    }

    private void createMenu() {
        View menuView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_list_view_layout, parentView, false);
        menuListView = (ListView) menuView.findViewById(R.id.dialog_list_view);
        setContentView(menuView);
    }

    private void showTextMessage(boolean show) {
        contentText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEditText(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        if (editTextLayout != null)
            editTextLayout.setVisibility(visibility);
    }

    public SimpleDialog setContentView(View view) {
        ((ViewGroup) parentView.findViewById(R.id.dialog_layout_container)).addView(view);
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public SimpleDialog setProgress(int progress) {
        mHandler.sendEmptyMessage(progress);
        this.progress = progress;
        return this;
    }

    public SimpleDialog setCancelable(boolean cancelable) {
        this.jdialog.setCancelable(false);
        return this;
    }

    public SimpleDialog setPositiveButton(int stringId, OnDialogClickListener listener) {
        setPositiveButton(activity.getString(stringId), listener);
        return this;
    }

    public SimpleDialog setNegativeButton(int stringId, OnDialogClickListener listener) {
        setNegativeButton(activity.getString(stringId), listener);
        return this;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.jdialog.setOnDismissListener(listener);
    }

    public void dismiss() {
        if (jdialog != null) {
            this.jdialog.dismiss();
        }
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public SimpleDialog showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    public SimpleDialog showPositiveButton(boolean show) {
        positiveButton.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    public SimpleDialog showNegativeButton(boolean show) {
        negativeButton.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    public SimpleDialog setTitle(int titleId) {
        setTitle(activity.getString(titleId));
        return this;
    }

    public Context getContext() {
        return activity;
    }

    public SimpleDialog setTitle(String title) {
        if (contentTitle.getVisibility() != View.VISIBLE)
            contentTitle.setVisibility(View.VISIBLE);
        contentTitle.setText(title);
        return this;
    }

    public SimpleDialog setMessage(String text) {
        if (contentText.getVisibility() != View.VISIBLE)
            contentText.setVisibility(View.VISIBLE);
        contentText.setText(text);
        return this;
    }

    public SimpleDialog show() {
        this.jdialog.show();
        return this;
    }

    public void setMessage(CharSequence message) {
        setMessage(String.valueOf(message));
    }

    public void setTitle(CharSequence title) {
        setTitle(String.valueOf(title));
    }

    public SimpleDialog setPositiveButton(String buttonText, OnDialogClickListener listener) {
        if (positiveButton.getVisibility() != View.VISIBLE)
            positiveButton.setVisibility(View.VISIBLE);
        positiveButton.setText(buttonText);
        positiveButton.setOnClickListener(new OnClickListener(listener));
        return this;
    }

    public SimpleDialog setNegativeButton(String buttonText, OnDialogClickListener listener) {
        if (negativeButton.getVisibility() != View.VISIBLE)
            negativeButton.setVisibility(View.VISIBLE);
        negativeButton.setText(buttonText);
        negativeButton.setOnClickListener(new OnClickListener(listener));
        return this;
    }

    public void setIcon(int resId) {
        iconView.setVisibility(View.VISIBLE);
        iconView.setImageResource(resId);
    }

    public void setIconColor(int color) {
        iconView.setColorFilter(color);
    }

    abstract public static class OnDialogClickListener {
        public abstract boolean onClick(SimpleDialog dialog);
    }

    public static class MenuItem {

        String name;
        Integer icon;
        public MenuItem(String name, int iconRes) {
            this.name = name;
            this.icon = iconRes;
        }
    }

    public static class DialogMenuAdapter extends BaseAdapter {
        private final Context context;
        private final List<MenuItem> options;

        public DialogMenuAdapter(List<MenuItem> options, Context context) {
            this.options = options;
            this.context = context;
        }

        @Override
        public int getCount() {
            return options.size();
        }

        @Override
        public Object getItem(int position) {
            return options.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DialogHolder holder = null;
            MenuItem item = options.get(position);

            if (convertView == null) {
                convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.dialog_menu_item_layout, parent, false);
                holder = new DialogHolder();
                holder.textView = convertView.findViewById(R.id.dialog_item_text_view);
                holder.imageView = convertView.findViewById(R.id.dialog_item_image_view);
                convertView.setTag(holder);
            } else {
                holder = (DialogHolder) convertView.getTag();
            }

            holder.textView.setText(item.name);
            holder.imageView.setImageResource(item.icon);
            return convertView;
        }

        static class DialogHolder {
            ImageView imageView;
            TextView textView;
        }
    }

    private class OnClickListener implements View.OnClickListener {

        private final OnDialogClickListener listener;

        public OnClickListener(OnDialogClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                if (!listener.onClick(progressBarDialog)) return;
            }
            jdialog.dismiss();
        }
    }

    private class JDialog extends AlertDialog {
        private boolean requestKeyboard = false;

        public JDialog(Context context, boolean requestKeyboard) {
            super(context);
            this.requestKeyboard = requestKeyboard;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(parentView);

            Window window = getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setBackgroundDrawableResource(R.drawable.dialog_bg_inset);
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            if (requestKeyboard) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }

        }

        @Override
        public void dismiss() {
            super.dismiss();
            dismissed = true;
        }

        @Override
        public void cancel() {
            super.cancel();
            dismissed = true;
        }
    }
}
