package com.jefferson.application.br.fragment;

import android.content.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.view.ViewPager.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.activity.*;
import android.support.v7.widget.Toolbar;
import android.view.View.OnClickListener;
import com.jefferson.application.br.R;
import java.util.ArrayList;

public class MainFragment extends Fragment implements OnPageChangeListener, OnClickListener, OnLongClickListener {

	//private FABRevealLayout FABReveal;
	private ViewPager viewPager;
	private Toolbar toolbar;
	//private FloatingActionsMenu fabMenu;
	private View view = null;
	private pagerAdapter pagerAdapter;
    private TabLayout tabLayout;
	public static final String UNIT_TEST_ID="ca-app-pub-3940256099942544/6300978111";
	public static final String UNIT_ID="ca-app-pub-3062666120925607/7395488498";
	public static final int GET_FILE = 35;

	public static final enum ID {
		FIRST,
		SECOND,
		BOTH
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
		return view;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.fab:
				Intent intent = new Intent(getContext(), GalleryAlbum.class);
				getActivity().startActivityForResult(intent.putExtra("position", viewPager.getCurrentItem()), 23);
				break;
			case R.id.ad_view: // R.id.fab_create:
                int position = viewPager.getCurrentItem();
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
    public boolean onLongClick(View view) {
        
        AlbumFragment fragment = (AlbumFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
        fragment.inputFolderDialog(null, AlbumFragment.ACTION_CREATE_FOLDER);
        return true;
    }
	public void importFromGallery() {
        
		Intent intent = new Intent(getContext(), GalleryAlbum.class);
		intent.putExtra("position", viewPager.getCurrentItem());
		getActivity().startActivityForResult(intent, 23);
	}
    
	private void toogleTabIcon(int position) {
        MainFragment mainFragment = this;

        int id = position == 0 ? R.drawable.ic_videos : R.drawable.ic_pictures;
        mainFragment.tabLayout.getTabAt(position).setIcon(position == 0 ? R.drawable.ic_pictures_selected : R.drawable.ic_videos_selected);
        tabLayout.getTabAt(position == 0 ? 1 : 0).setIcon(id);
    }

	public void update(MainFragment.ID id) {

        if (id == ID.BOTH) {
            update(ID.FIRST);
            update(ID.SECOND);
        } else if (id == ID.FIRST || id == ID.SECOND) {
			int position = id == ID.FIRST ? 0: 1;
            if (pagerAdapter != null) {
                pagerAdapter.update(position);
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
		private Fragment[] fragments = new Fragment[SIZE];

        public pagerAdapter(FragmentManager fm) {

			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
            
            if (fragments[position] == null) {
			    fragments[position] = AlbumFragment.newInstance(position);
            }
            return fragments[position];
		}
        public void update(int position) {

			((AlbumFragment)fragments[position]).update();
			notifyDataSetChanged();
		}
		@Override
		public int getCount() {

			return SIZE;
		}
	}
}
