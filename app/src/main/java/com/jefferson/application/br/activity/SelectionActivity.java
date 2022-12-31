package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jefferson.application.br.MultiSelectRecyclerViewAdapter;
import com.jefferson.application.br.R;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.util.BlurUtils;

import java.util.ArrayList;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class SelectionActivity extends MyCompatActivity implements MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener, OnClickListener {

    private String name;
    private ImageView selectAllImageView;
    private ArrayList<MediaModel> models;
    private MultiSelectRecyclerViewAdapter mAdapter;
    private String baseTitle;
    private TextView selectAllTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_selection_layout);
        View importLayout = findViewById(R.id.importView);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        selectAllTextView = (TextView) findViewById(R.id.options_selectTextView);
        BlurView blurView = findViewById(R.id.blurView);
        BlurUtils.setupWith(blurView, this, 13f);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        models = intent.getParcelableArrayListExtra("data");
        int position = intent.getIntExtra("position", 0);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MultiSelectRecyclerViewAdapter(SelectionActivity.this, models, this, position);
        mRecyclerView.setAdapter(mAdapter);
        View selectAllLayout = findViewById(R.id.selectView);
        selectAllImageView = (ImageView) findViewById(R.id.selectImageView);

        importLayout.setOnClickListener(this);
        selectAllLayout.setOnClickListener(this);

        setupToolbar();
        updateActionBarTitle();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.selectView:
                if (mAdapter.getSelectedItemCount() == models.size()) {
                    mAdapter.clearSelection();
                } else {
                    for (int i = 0; i < models.size(); i++) {
                        if (!mAdapter.isSelected(i)) {
                            mAdapter.toggleSelection(i);
                        }
                    }
                }
                updateActionBarTitle();
                toogleSelectViewIcon();
                break;
            case R.id.importView:
                ArrayList<String> listPaths = getSelectedItems();
                if (listPaths.size() > 0) {
                    Intent i = new Intent();
                    i.putStringArrayListExtra("selection", listPaths);
                    setResult(RESULT_OK, (i));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.selecionar_um), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void updateItem(int position) {
        mAdapter.toggleSelection(position);
        updateActionBarTitle();
        toogleSelectViewIcon();
    }

    @Override
    public void onItemClicked(int position, View v) {
        updateItem(position);
    }

    @Override
    public boolean onItemLongClicked(int position) {
        updateItem(position);
        return true;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private ArrayList<String> getSelectedItems() {
        return mAdapter.getSelectedItemsPath();
    }

    private void updateActionBarTitle() {
        if (baseTitle == null)
            baseTitle = name.length() >= 20 ? name.substring(0, 20) + "... ( %d )" : name + " ( %d )";
        int count = mAdapter.getSelectedItemCount();
        Objects.requireNonNull(getSupportActionBar()).setTitle(String.format(baseTitle, count));
    }

    private void toogleSelectViewIcon() {
        boolean allSelected = mAdapter.getSelectedItemCount() == models.size();
        String text = allSelected ? "Unselect all" : "Select all";
        int resId = (allSelected) ? R.drawable.ic_select : R.drawable.ic_select_all;

        selectAllImageView.setImageResource(resId);
        selectAllTextView.setText(text);

    }
}
