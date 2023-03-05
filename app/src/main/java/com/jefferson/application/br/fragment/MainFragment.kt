/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.jefferson.application.br.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.View.OnLongClickListener
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.ImportGalleryActivity
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.activity.SearchActivity
import com.jefferson.application.br.model.AlbumModel

class MainFragment : Fragment(), OnPageChangeListener, View.OnClickListener, OnLongClickListener {
    private var viewPager: ViewPager2? = null
    private var toolbar: Toolbar? = null
    private var view: View? = null
    private var pagerAdapter: PagerAdapter? = null
    private var tabLayout: TabLayout? = null
    private var fab: View? = null
    private var paddingBottom = 0
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val main = activity as MainActivity?
        if (view == null) {
            view = inflater.inflate(R.layout.main_fragment, null)
            pagerAdapter = PagerAdapter(requireActivity())
            toolbar = view?.findViewById(R.id.toolbar)
            viewPager = view?.findViewById(R.id.mainViewPager)
            viewPager?.adapter = pagerAdapter
            viewPager?.isSaveEnabled = false
            //viewPager?.setOnPageChangeListener(this)
            tabLayout = view?.findViewById(R.id.tab_layout)

            val searchView = view?.findViewById<View>(R.id.search_bar)
            val selected = resources.getColor(R.color.tab_selected)
            val unselected = resources.getColor(R.color.tab_unselected)

            tabLayout?.setTabTextColors(unselected, selected)
            TabLayoutMediator(tabLayout!!, viewPager!!) { tab, position ->
                tab.text = when(position) {
                    0 -> getString(R.string.imagens)
                    1 -> getString(R.string.videos)
                    else -> ""
                }
            }.attach()
            fab = view?.findViewById(R.id.fab)
            fab?.setOnClickListener(this)
            fab?.setOnLongClickListener(this)

            searchView?.setOnClickListener(this)
            adjustViewsPadding()
            createActivityResultLauncher()
        }
        main?.setupToolbar(toolbar, getToolbarName(viewPager!!.currentItem))
        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab -> {
                val intent = Intent(context, ImportGalleryActivity::class.java)
                requireActivity().startActivityForResult(
                    intent.putExtra(
                        "position",
                        viewPager!!.currentItem
                    ), MainActivity.IMPORT_FROM_GALLERY_CODE
                )
            }
            R.id.ad_view -> {
                val position = pagerPosition
                try {
                    startActivityForResult(
                        Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_DEFAULT)
                            .setType(if (position == 0) "image/*" else "video/*"), GET_FILE
                    )
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "Sem padrão", Toast.LENGTH_LONG).show()
                }
            }
            R.id.search_bar -> openSearchView()
        }
    }

    private fun createActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult (
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val albumName = data.getStringExtra("result")
                    val action = data.action
                    if (action == SearchActivity.ACTION_GO_TO_ALBUM) {
                        scrollToAlbum(albumName)
                        return@registerForActivityResult
                    }
                    if (action == SearchActivity.ACTION_OPEN_ALBUM) {
                        openAlbum(albumName)
                    }
                }
            }
        }
    }

    private fun openAlbum(albumName: String?) {
        currentFragment.openAlbum(albumName)
    }

    private fun scrollToAlbum(albumName: String?) {
        currentFragment.scrollToAlbum(albumName)
    }

    private val currentFragment: AlbumFragment
        get() {
            val pos = viewPager!!.currentItem
            return pagerAdapter!!.createFragment(pos)
        }

    private fun openSearchView() {
        val pos = viewPager!!.currentItem
        val fragment = pagerAdapter!!.createFragment(pos)
        if (!fragment.isLoading) {
            val intent = Intent(requireActivity(), SearchActivity::class.java)
            intent.putParcelableArrayListExtra(
                SearchActivity.EXTRA_SIMPLE_MODELS,
                fragment.simplifiedModels
            )
            activityResultLauncher?.launch(intent)
        }
    }

    fun notifyBottomLayoutChanged(view: View) {
        paddingBottom = view.height
        adjustViewsPadding()
    }

    private fun adjustViewsPadding() {
        if (pagerAdapter != null) {
            val fragments = pagerAdapter!!.fragments
            for (i in fragments.indices) {
                val album = pagerAdapter!!.createFragment(i)
                album.setBottomPadding(paddingBottom)
            }
        }
        //change params and add the fab button
        if (fab != null) {
            val r = resources
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                r.displayMetrics
            ).toInt()
            val p = fab!!.layoutParams as MarginLayoutParams
            p.rightMargin = px
            p.bottomMargin = paddingBottom + px
            fab!!.layoutParams = p
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onLongClick(view: View): Boolean {
        val fragment = pagerAdapter?.createFragment(viewPager!!.currentItem)
        fragment?.inputFolderDialog(null, AlbumFragment.ACTION_CREATE_FOLDER)
        return true
    }

    fun showSnackBar(message: String?, length: Int) {
        Snackbar.make(fab!!, message!!, length).show()
    }

    fun removeFolder(folderPosition: Int, pagerPosition: Int) {
        if (pagerAdapter != null) {
            val fragment = pagerAdapter!!.createFragment(pagerPosition)
            fragment.removeFolder(folderPosition)
        }
    }

    val pagerPosition: Int
        get() = viewPager!!.currentItem

    fun importFromGallery() {
        val intent = Intent(context, ImportGalleryActivity::class.java)
        intent.putExtra("position", viewPager!!.currentItem)
        activity?.startActivityForResult(intent, 23)
    }

    fun updateFragment(id: Int) {
        pagerAdapter?.update(id)
    }

    fun updateAllFragments() {
        if (pagerAdapter != null) {
            for (i in 0 until pagerAdapter!!.itemCount) {
                pagerAdapter?.update(i)
            }
        }
    }

    override fun onPageScrolled(p1: Int, p2: Float, p3: Int) {}
    override fun onPageScrollStateChanged(p1: Int) {}
    override fun onPageSelected(i: Int) {}
    private fun getToolbarName(i: Int): CharSequence {
        return if (i == 0) getString(R.string.imagens) else getString(R.string.videos)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun reloadFragments() {
        if (pagerAdapter != null) {
            for (i in 0 until pagerAdapter!!.itemCount) {
                pagerAdapter!!.reload(i)
            }
        }
    }
    private inner class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter (
        fa
    ) {
        val fragments = arrayOfNulls<AlbumFragment>(Companion.SIZE)
        fun update(
            position: Int,
            models: ArrayList<AlbumModel>?
        ) {
            createFragment(position).putModels(models)
        }

        fun update(position: Int) {
            val fragment = fragments[position]
            fragment?.update()
        }

        fun reload(i: Int) {
            fragments[i]?.reload()
        }

        override fun getItemCount(): Int {
            return Companion.SIZE
        }

        override fun createFragment(position: Int): AlbumFragment {
            if (fragments[position] == null) {
                fragments[position] = AlbumFragment(position, this@MainFragment)
            }
            return fragments[position]!!
        }
    }

    companion object {
        const val UNIT_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
        const val GET_FILE = 35
        const val SIZE = 2
    }
}