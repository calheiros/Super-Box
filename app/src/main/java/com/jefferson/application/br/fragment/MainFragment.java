package com.jefferson.application.br.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.FolderModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.ImportGalleryActivity;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.database.PathsData;
import com.jefferson.application.br.model.MediaModel;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.Storage;
import java.io.File;
import java.util.ArrayList;
import android.view.MenuInflater;
import android.view.Menu;

public class MainFragment extends Fragment implements OnPageChangeListener, OnClickListener, OnLongClickListener {

	private ViewPager viewPager;
	private Toolbar toolbar;
	private View view = null;
	private pagerAdapter pagerAdapter;
    private TabLayout tabLayout;
	public static final String UNIT_TEST_ID="ca-app-pub-3940256099942544/6300978111";
	public static final String UNIT_ID="ca-app-pub-3062666120925607/7395488498";
	public static final int GET_FILE = 35;
    private JTask retriveTask;
    private boolean corruptedDatabase;
    
    public int getPagerPosition() {
        return viewPager.getCurrentItem();
    }

	public MainFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity main = (MainActivity)getActivity();

		if (view == null) {
			view = inflater.inflate(R.layout.main_fragment, null);
			pagerAdapter = new pagerAdapter(getActivity().getSupportFragmentManager());
			toolbar = view.findViewById(R.id.toolbar);
			viewPager = view.findViewById(R.id.mainViewPager);
			viewPager.setAdapter(pagerAdapter);
			viewPager.setOnPageChangeListener(this);
			tabLayout = view.findViewById(R.id.tabLayoutPedido);
			//tabLayout.setSelectedTabIndicatorColor(getRe
			int selected = getResources().getColor(R.color.tab_selected);
			int unselected = getResources().getColor(R.color.tab_unsected);
			tabLayout.setTabTextColors(unselected, selected);
			tabLayout.setupWithViewPager(viewPager);

			//fabMenu = (FloatingActionsMenu) view.findViewById(R.id.mFloatingActionsMenu);
		    View fab = view.findViewById(R.id.fab);
            fab.setOnClickListener(this);
            fab.setOnLongClickListener(this);

			//view.findViewById(R.id.fab_create).setOnClickListener(this);
			toogleTabIcon(0);
		}
        
		main.setupToolbar(toolbar, getToolbarName(viewPager.getCurrentItem()));
        //setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.fab:
				Intent intent = new Intent(getContext(), ImportGalleryActivity.class);
				getActivity().startActivityForResult(intent.putExtra("position", viewPager.getCurrentItem()), 23);
				break;
			case R.id.ad_view: // R.id.fab_create:
                int position = getPagerPosition();

                try {
					startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_DEFAULT).setType(position == 0 ? "image/*" : "video/*"), GET_FILE);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(getContext(), "Sem padrão", 1).show();
				}
				//createFolder(viewPager.getCurrentItem(), getContext(), (FilePicker) null);
				break;
		} 
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_main_album, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
   
    @Override
    public boolean onLongClick(View view) {
        AlbumFragment fragment = (AlbumFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
        fragment.inputFolderDialog(null, AlbumFragment.ACTION_CREATE_FOLDER);
        return true;
    }

	public void importFromGallery() {
		Intent intent = new Intent(getContext(), ImportGalleryActivity.class);
		intent.putExtra("position", viewPager.getCurrentItem());
		getActivity().startActivityForResult(intent, 23);
	}

	private void toogleTabIcon(int position) {
        MainFragment mainFragment = this;
        int id = position == 0 ? R.drawable.ic_videos : R.drawable.ic_pictures;
        mainFragment.tabLayout.getTabAt(position).setIcon(position == 0 ? R.drawable.ic_pictures_selected : R.drawable.ic_videos_selected);
        tabLayout.getTabAt(position == 0 ? 1 : 0).setIcon(id);
    }

	public void updateFragment(int id) {
        if (pagerAdapter != null) {
            pagerAdapter.update(id);
        }
    }

    public void updateAllFragments() {
        
        if (pagerAdapter != null){
            for (int i =0; i< pagerAdapter.getCount(); i++) {
                pagerAdapter.update(i);
            }
        }
    }
   
	@Override
	public void onPageScrolled(int p1, float p2, int p3) {

	}

	@Override
	public void onPageScrollStateChanged(int p1) {

	}

	@Override
    public void onPageSelected(int i) {
        toogleTabIcon(i);
        ((MainActivity) getActivity()).setupToolbar(this.toolbar, getToolbarName(i));
    }

	private CharSequence getToolbarName(int i) {
        return i == 0 ? getString(R.string.imagens) : getString(R.string.videos);
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class pagerAdapter extends FragmentPagerAdapter {

        public static final int SIZE = 2;
		private AlbumFragment[] fragments = new AlbumFragment[SIZE];

        public pagerAdapter(FragmentManager fm) {

			super(fm);
		}

        public void update(int position, ArrayList<FolderModel> models) {
            getItem(position).putModels(models);
        }

		@Override
		public AlbumFragment getItem(int position) {

            if (fragments[position] == null) {
			    fragments[position] = new AlbumFragment(position, MainFragment.this);
            }
            return fragments[position];
		}

        public void update(int position) {
			AlbumFragment fragment = fragments[position];

            if (fragment != null) {
                fragment.update();
			    //notifyDataSetChanged();
            }
		}

		@Override
		public int getCount() {

			return SIZE;
		}
	}
}
