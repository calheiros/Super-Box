package com.jefferson.application.br.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.ImportGalleryActivity;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.activity.SearchActivity;
import com.jefferson.application.br.model.FolderModel;
import com.jefferson.application.br.model.SimplifiedAlbum;

import java.util.ArrayList;

public class MainFragment extends Fragment implements OnPageChangeListener, OnClickListener, OnLongClickListener {

    public static final String UNIT_TEST_ID = "ca-app-pub-3940256099942544/6300978111";
    public static final int GET_FILE = 35;

    private ViewPager viewPager;
    private Toolbar toolbar;
    private View view = null;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private View fab;
    private int paddingBottom;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public MainFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity main = (MainActivity) getActivity();
        if (view == null) {
            view = inflater.inflate(R.layout.main_fragment, null);
            pagerAdapter = new PagerAdapter(requireActivity().getSupportFragmentManager());
            toolbar = view.findViewById(R.id.toolbar);
            viewPager = view.findViewById(R.id.mainViewPager);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOnPageChangeListener(this);
            tabLayout = view.findViewById(R.id.tab_layout);
            View searchView = view.findViewById(R.id.search_bar);
            int selected = getResources().getColor(R.color.tab_selected);
            int unselected = getResources().getColor(R.color.tab_unselected);
            tabLayout.setTabTextColors(unselected, selected);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.getTabAt(0).setText(getString(R.string.imagens));
            tabLayout.getTabAt(1).setText(getString(R.string.videos));
            tabLayout.setInlineLabel(true);

            fab = view.findViewById(R.id.fab);
            fab.setOnClickListener(this);
            fab.setOnLongClickListener(this);
            searchView.setOnClickListener(this);

            adjustViewsPadding();
            createActivityResultLauncher();
        }

        assert main != null;
        main.setupToolbar(toolbar, getToolbarName(viewPager.getCurrentItem()));

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab:
                Intent intent = new Intent(getContext(), ImportGalleryActivity.class);
                requireActivity().startActivityForResult(intent.putExtra("position",
                        viewPager.getCurrentItem()), MainActivity.IMPORT_FROM_GALLERY_CODE);
                break;
            case R.id.ad_view:
                int position = getPagerPosition();
                try {
                    startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_DEFAULT).setType(position == 0 ? "image/*" : "video/*"), GET_FILE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "Sem padrão", Toast.LENGTH_LONG).show();
                }
                //createFolder(viewPager.getCurrentItem(), getContext(), (FilePicker) null);
                break;
            case R.id.search_bar:
                openSearchView();
                break;
        }
    }
    private void createActivityResultLauncher() {
       activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            String albumName = data.getStringExtra("result");
                            scrollToAlbum(albumName);
                          //Toast.makeText(App.getAppContext(),"name: " + name, Toast.LENGTH_SHORT ).show();

                        }
                    }
                });
    }

    private void scrollToAlbum(String albumName) {
        int pos = viewPager.getCurrentItem();
        AlbumFragment fragment = pagerAdapter.getItem(pos);
        fragment.scrollToAlbum(albumName);
    }

    private void openSearchView() {
        int pos = viewPager.getCurrentItem();
        AlbumFragment fragment = pagerAdapter.getItem(pos);
        if (!fragment.isLoading()) {
            Intent intent = new Intent(requireActivity(), SearchActivity.class);
            intent.putParcelableArrayListExtra(SearchActivity.EXTRA_SIMPLE_MODELS, fragment.getSimplifiedModels());
            activityResultLauncher.launch(intent);
        }
    }

    public void notifyBottomLayoutChanged(@NonNull View view) {
        paddingBottom = view.getHeight();
        adjustViewsPadding();
    }

    private void adjustViewsPadding() {
        if (pagerAdapter != null) {
            AlbumFragment[] fragments = pagerAdapter.fragments;
            for (int i = 0; i < fragments.length; i++) {
                AlbumFragment album = pagerAdapter.getItem(i);
                album.setBottomPadding(paddingBottom);
            }
        }
        //change params and add the fab button
        if (fab != null) {
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    r.getDisplayMetrics()
            );
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            p.rightMargin = px;
            p.bottomMargin = paddingBottom + px;
            fab.setLayoutParams(p);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onLongClick(View view) {
        AlbumFragment fragment = pagerAdapter.getItem(viewPager.getCurrentItem());
        fragment.inputFolderDialog(null, AlbumFragment.ACTION_CREATE_FOLDER);
        return true;
    }

    public void showSnackBar(String message, int length) {
        Snackbar.make(fab, message, length).show();
    }

    public void removeFolder(int folderPosition, int pagerPosition) {
        if (pagerAdapter != null) {
            AlbumFragment fragment = pagerAdapter.getItem(pagerPosition);
            fragment.removeFolder(folderPosition);
        }
    }

    public int getPagerPosition() {
        return viewPager.getCurrentItem();
    }

    public void importFromGallery() {
        Intent intent = new Intent(getContext(), ImportGalleryActivity.class);
        intent.putExtra("position", viewPager.getCurrentItem());
        getActivity().startActivityForResult(intent, 23);
    }

   /* private void toogleTabIcon(int position) {
        MainFragment mainFragment = this;
        int id = position == 0 ? R.drawable.ic_videos : R.drawable.ic_pictures;
        Objects.requireNonNull(mainFragment.tabLayout.getTabAt(position)).setIcon(
                position == 0 ? R.drawable.ic_pictures_selected : R.drawable.ic_videos_selected);
        Objects.requireNonNull(tabLayout.getTabAt(position == 0 ? 1 : 0)).setIcon(id);
    }*/

    public void updateFragment(int id) {
        if (pagerAdapter != null) {
            pagerAdapter.update(id);
        }
    }

    public void updateAllFragments() {
        if (pagerAdapter != null) {
            for (int i = 0; i < pagerAdapter.getCount(); i++) {
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

    public void reloadFragments() {
        if (pagerAdapter != null) {
            for (int i = 0; i < pagerAdapter.getCount(); i++) {
                pagerAdapter.reload(i);
            }
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public static final int SIZE = 2;
        private final AlbumFragment[] fragments = new AlbumFragment[SIZE];

        public PagerAdapter(FragmentManager fm) {

            super(fm);
        }

        public void update(int position, ArrayList<FolderModel> models, ArrayList<SimplifiedAlbum> simplifiedModels) {
            getItem(position).putModels(models, simplifiedModels);
        }

        @NonNull
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

        public void reload(int i) {
            fragments[i].reload();
        }
    }
}
