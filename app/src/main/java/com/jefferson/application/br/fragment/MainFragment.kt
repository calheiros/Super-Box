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
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.ImportGalleryActivity
import com.jefferson.application.br.activity.ImportMediaActivity
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.activity.SearchActivity

class MainFragment : Fragment(), OnPageChangeListener, View.OnClickListener, OnLongClickListener {
    private lateinit var viewPager: ViewPager2
    private var toolbar: Toolbar? = null
    private var view: View? = null
    private var pagerAdapter: PagerAdapter? = null
    private var tabLayout: TabLayout? = null
    private var floatingButton: View? = null
    private var paddingBottom = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val main = activity as MainActivity?
        if (view == null) {
            view = inflater.inflate(R.layout.main_fragment, container, false)
            pagerAdapter = PagerAdapter(requireActivity())
            toolbar = view?.findViewById(R.id.toolbar)
            viewPager = view?.findViewById(R.id.mainViewPager) as ViewPager2
            viewPager.adapter = pagerAdapter
            viewPager.isSaveEnabled = false
            viewPager.offscreenPageLimit = 2
            tabLayout = view?.findViewById(R.id.tab_layout)

            val searchView = view?.findViewById<View>(R.id.search_bar)
            val selected = ContextCompat.getColor(requireContext(), R.color.tab_selected)
            val unselected = ContextCompat.getColor(requireContext(), R.color.tab_unselected)

            tabLayout?.setTabTextColors(unselected, selected)
            TabLayoutMediator(tabLayout!!, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.imagens)
                    1 -> getString(R.string.videos)
                    else -> ""
                }
            }.attach()
            floatingButton = view?.findViewById(R.id.fab)
            floatingButton?.setOnClickListener(this)
            floatingButton?.setOnLongClickListener(this)
            searchView?.setOnClickListener(this)
            adjustViewsPadding()
        }
        main?.setupToolbar(toolbar, getToolbarName(viewPager.currentItem))
        return view
    }

    private val importMediaResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            updateFragment(viewPager.currentItem)
        }
    }

    private val gallerySelectionResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val position = data!!.getIntExtra("position", -1)
            val type = data.getStringExtra("type")
            val paths = data.getStringArrayListExtra("selection")
            val intent = Intent(context, ImportMediaActivity::class.java)
            intent.putExtra(ImportMediaActivity.KEY_TYPE, type)
            intent.putExtra(ImportMediaActivity.KEY_MEDIA_LIST, paths)
            intent.putExtra(ImportMediaActivity.KEY_POSITION, position)
            importMediaResult.launch(intent)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab -> importFromGallery()
            R.id.search_bar -> openSearchView()
        }
    }

    private val searchResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val albumName = data.getStringExtra("result")
                val action = data.action
                if (action == SearchActivity.ACTION_GO_TO_ALBUM) {
                    currentFragment?.scrollToAlbum(albumName)
                    return@registerForActivityResult
                }
                if (action == SearchActivity.ACTION_OPEN_ALBUM) {
                    currentFragment?.openAlbum(albumName!!)
                }
            }
        }
    }

    private val currentFragment: AlbumFragment?
        get() {
            val pos = viewPager.currentItem
            return pagerAdapter?.createFragment(pos)
        }

    private fun openSearchView() {
        val pos = viewPager.currentItem
        val fragment = pagerAdapter!!.createFragment(pos)
        if (!fragment.isLoading) {
            val intent = Intent(requireActivity(), SearchActivity::class.java)
            intent.putParcelableArrayListExtra(
                SearchActivity.EXTRA_SIMPLE_MODELS,
                fragment.simplifiedModels
            )
            searchResult.launch(intent)
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
        if (floatingButton != null) {
            val r = resources
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                r.displayMetrics
            ).toInt()
            val p = floatingButton!!.layoutParams as MarginLayoutParams
            p.rightMargin = px
            p.bottomMargin = paddingBottom + px
            floatingButton!!.layoutParams = p
        }
    }

    override fun onLongClick(view: View): Boolean {
        val fragment = pagerAdapter?.createFragment(viewPager.currentItem)
        fragment?.inputFolderDialog(null, AlbumFragment.ACTION_CREATE_FOLDER)
        return true
    }

    fun removeAlbum(albumPosition: Int, pagerPosition: Int) {
        if (pagerAdapter != null) {
            val fragment = pagerAdapter!!.createFragment(pagerPosition)
            fragment.removeFolder(albumPosition)
        }
    }

    val pagerPosition: Int
        get() = viewPager.currentItem

    private fun importFromGallery() {
        val intent = Intent(context, ImportGalleryActivity::class.java)
        intent.putExtra("position", pagerPosition)
        gallerySelectionResult.launch(intent)
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

    fun reloadFragments() {
        if (pagerAdapter != null) {
            for (i in 0 until pagerAdapter!!.itemCount) {
                pagerAdapter!!.reload(i)
            }
        }
    }

    fun notifyItemChanged(fragmentPosition: Int, itemPosition: Int) {
        val fragment = pagerAdapter?.getFragmentOrNull(fragmentPosition) ?: return
        fragment.notifyItemChanged(itemPosition)
    }

    private inner class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(
        fa
    ) {
        val fragments = arrayOfNulls<AlbumFragment>(Companion.SIZE)

        fun update(position: Int) {
            val fragment = getFragmentOrNull(position)
            fragment?.update()
        }

        fun reload(i: Int) {
            fragments[i]?.reload()
        }

        override fun getItemCount(): Int {
            return Companion.SIZE
        }

        fun getFragmentOrNull(position: Int): AlbumFragment? {
           return if (position in 0 until itemCount)
               createFragment(position) else null
        }

        override fun createFragment(position: Int): AlbumFragment {
            if (fragments[position] == null) {
                fragments[position] = AlbumFragment(position)
            }
            return fragments[position]!!
        }
    }

    companion object {
        const val GET_FILE = 35
        const val SIZE = 2
    }
}