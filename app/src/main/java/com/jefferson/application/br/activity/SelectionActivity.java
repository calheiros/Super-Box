package com.jefferson.application.br.activity;

import android.content.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.jefferson.application.br.activity.MyCompatActivity;
import java.util.*;
import android.support.v7.widget.Toolbar;
import com.jefferson.application.br.*;
import com.jefferson.application.br.model.MediaModel;
import android.support.annotation.NonNull;

public class SelectionActivity extends MyCompatActivity implements MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener, OnClickListener {

	private String name;
	private Toolbar toolbar;
	private ImageView selectAllImageView;
	private ArrayList<MediaModel> data;
    private RecyclerView mRecyclerView;
	private MultiSelectRecyclerViewAdapter  mAdapter;
    private String baseTitle;
    private TextView selectAllTextView;
    private View selectAllLayout, importLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_selection_layout);
		importLayout = findViewById(R.id.importView);
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        selectAllTextView = findViewById(R.id.options_selectTextView);

        Intent intent = getIntent();
		name = intent.getStringExtra("name");
		data = (ArrayList<MediaModel>)intent.getSerializableExtra("data");
		int position = intent.getIntExtra("position", 0);
		mRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MultiSelectRecyclerViewAdapter(SelectionActivity.this, data, this, position);
        mRecyclerView.setAdapter(mAdapter);
        selectAllLayout = findViewById(R.id.selectView);
	    selectAllImageView = findViewById(R.id.selectImageView);

		importLayout.setOnClickListener(this);
		selectAllLayout.setOnClickListener(this);

        setupToolbar();
        updateActionBarTitle();
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.selectView:
                if (mAdapter.getSelectedItemCount() == data.size()) {
                    mAdapter.clearSelection();
                } else {
                    for (int i= 0; i < data.size();i++) {
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
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private List<Integer> selectedItemsPosition() {
		List<Integer> integer = mAdapter.getSelectedItems();
		return integer;
	}

	private ArrayList<String> getSelectedItems() {
        /*
         ArrayList<String> selectedItems = new ArrayList<String>();

         for (int i : selectedItemsPosition()) {
         selectedItems.add(data.get(i).getPath());
         }
         */
		return mAdapter.getSelectedItemsPath();
	}

	private void updateActionBarTitle() {
        if (baseTitle == null)
            baseTitle = name.length() >= 20 ? name.substring(0, 20) + "... ( %d )" : name + " ( %d )";
	    int count = mAdapter.getSelectedItemCount();
        getSupportActionBar().setTitle(String.format(baseTitle, count));
	}

	private void toogleSelectViewIcon() {
        boolean allSelected = mAdapter.getSelectedItemCount() == data.size();
        String text = allSelected ? "Unselect all" : "Select all";
		int resId = (allSelected) ? R.drawable.ic_select : R.drawable.ic_select_all;
        
	    selectAllImageView.setImageResource(resId);
        selectAllTextView.setText(text);
        
	}
}
