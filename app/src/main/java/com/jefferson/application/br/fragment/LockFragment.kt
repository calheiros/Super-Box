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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jefferson.application.br.App
import com.jefferson.application.br.CodeManager
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.adapter.AppLockAdapter
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.model.AppModel
import com.jefferson.application.br.service.AppLockService
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.ViewUtils
import com.jefferson.application.br.widget.LockCheck
import java.util.*

class LockFragment(mainActivity: MainActivity) : Fragment(), OnItemClickListener,
    SearchView.OnQueryTextListener {
    var firstVisibleItem = -1
    var lastVisibleItem = -1
    private var lastClickedParentView: View? = null
    private var lastClickedItemPosition = 0
    private var visibleCount = -1
    private var totalItemCount = -1
    private var progressBar: ProgressBar? = null
    private var loadingLabel: TextView? = null
    private var appModels: ArrayList<AppModel>? = null
    private var adapter: AppLockAdapter? = null
    private var intent: Intent? = null
    private var parentView: View? = null
    private var activity: MainActivity
    private var listView: ListView? = null
    private var loadApplicationsTask: LoadApplicationsTask? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    var mPreviousVisibleItem: Int = 0
    var paddingBottom = 0

    init {
        activity = mainActivity
        startLoadPackagesTask()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        if (parentView == null) {
            parentView = inflater.inflate(R.layout.list_view_app, container, false) as View
            progressBar = parentView!!.findViewById(R.id.progressApps)
            loadingLabel = parentView!!.findViewById(R.id.porcent)
            listView = parentView!!.findViewById(R.id.appList)
            swipeRefreshLayout = parentView!!.findViewById(R.id.swipe_refresh)
            listView?.itemsCanFocus = true
            listView?.clipToPadding = false
            ViewUtils.setViewPaddingBottom(listView, paddingBottom)
            val typedValue = TypedValue()
            val theme = activity.theme
            theme?.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true)
            val color = typedValue.data
            swipeRefreshLayout?.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)
            swipeRefreshLayout?.setProgressBackgroundColorSchemeColor(color) // .setProgressBackgroundColor(color);
            listView?.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(p1: AbsListView, p2: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    if (firstVisibleItem < mPreviousVisibleItem) {
                        adapter?.scrollState = AppLockAdapter.Companion.ScrollState.UP
                    } else if (firstVisibleItem > mPreviousVisibleItem) {
                        adapter?.scrollState = AppLockAdapter.Companion.ScrollState.DOWN
                    }
                    mPreviousVisibleItem = firstVisibleItem
                    lastVisibleItem = firstVisibleItem + visibleItemCount
                    this@LockFragment.firstVisibleItem = firstVisibleItem
                    visibleCount = visibleItemCount
                    this@LockFragment.totalItemCount = totalItemCount
                }
            })
            if (loadApplicationsTask?.getStatus() == JTask.Status.FINISHED)
                onPackagesLoaded() else showProgressView()

            intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            listView?.onItemClickListener = this
            swipeRefreshLayout?.setOnRefreshListener {
                if (loadApplicationsTask?.getStatus() == JTask.Status.STARTED) {
                    swipeRefreshLayout?.isRefreshing = false
                } else {
                    adapter?.clear()
                    showProgressView()
                    startLoadPackagesTask()
                }
            }
        } else {
            if (swipeRefreshLayout?.isRefreshing == true) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }
        val toolbar = parentView?.findViewById<Toolbar>(R.id.toolbar)
        activity.setupToolbar(toolbar, getString(R.string.bloquear_apps))
        activity.supportActionBar?.dispatchMenuVisibilityChanged(true)
        setHasOptionsMenu(true)
        return parentView
    }

    private fun applicationFound(x: Int) {
        listView?.smoothScrollToPositionFromTop(
            x, listView!!.height / 2 - adapter!!.itemHeight / 2
        )
        //mListView.smoothScrollToPosition(scrollPosition);
        hideInputMethod(requireActivity().window.currentFocus!!)
        if (x in firstVisibleItem until lastVisibleItem) {
            //Toast.makeText(getContext(), "It's a visible item!", 1).show();
            adapter!!.animateSearchedItem(x)
        } else {
            adapter!!.setSearchedItem(x)
        }
    }

    override fun onQueryTextSubmit(input: String): Boolean {
        if (adapter == null || input.isEmpty()) {
            return false
        }
        var model: AppModel
        val models = adapter!!.models
        var firstContains = -1
        for (x in models.indices) {
            model = models[x]
            val lowerName = model.packageName!!.lowercase(Locale.getDefault())
            val lowerInput = input.lowercase(Locale.getDefault())
            if (lowerName.startsWith(lowerInput)) {
                applicationFound(x)
                return true
            } else if (firstContains == -1) {
                if (lowerName.contains(lowerInput)) {
                    firstContains = x
                }
            }
        }
        if (firstContains != -1) {
            applicationFound(firstContains)
            return true
        }
        Toast.makeText(context, "Application not found!", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onQueryTextChange(input: String): Boolean {
        return false
    }

    private fun showProgressView() {
        if (progressBar == null || loadingLabel == null) return
        progressBar?.progress = 0
        progressBar?.isIndeterminate = true
        progressBar?.visibility = View.VISIBLE
        loadingLabel?.text = ""
        loadingLabel?.visibility = View.VISIBLE
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View, position: Int, id: Long) {
        lastClickedItemPosition = position
        lastClickedParentView = view
        var noNeedOverlayPermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                context
            )
        ) {
            Log.v(
                "App", "Requesting Permission" + Settings.canDrawOverlays(
                    context
                )
            ) // if not construct intent to request permission 
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                    "package:" + requireContext().packageName
                )
            ) // request permission via start activity for result 
            startActivityForResult(
                intent, REQUEST_OVERLAY_PERMISSION_CODE
            ) //It will call onActivityResult Function After you press Yes/No and go Back after giving permission 
        } else {
            noNeedOverlayPermission = true
            Log.v("App", "We already have permission for it.")
        }
        if (!needPermissionForBlocking(context)) {
            if (noNeedOverlayPermission) {
                this.adapter!!.toggleSelection(position)
                animateCheckView(lastClickedParentView)
            }
        } else {
            val alert = SimpleDialog(requireActivity())
            alert.setMessage(getString(R.string.usage_data_permission_message))
            alert.setPositiveButton(
                getString(android.R.string.ok),
                object : OnDialogClickListener() {
                    override fun onClick(dialog: SimpleDialog): Boolean {
                        startActivityForResult(intent, 0)
                        return true
                    }
                })
            alert.setNegativeButton(getString(android.R.string.cancel), null)
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
    }

    private fun animateCheckView(vi: View?) {
        if (vi == null) return
        val lockView = vi.findViewById<LockCheck>(R.id.check1)
        val animation = AnimationUtils.loadAnimation(context, R.anim.checked)
        lockView.startAnimation(animation)
        lockView.isChecked = !lockView.isChecked
    }

    override fun onPause() {
        super.onPause()
        adapter?.scrollState = AppLockAdapter.Companion.ScrollState.STOP
        if (adapter?.isMutable == true) {
            activity.startService(
                Intent(
                    context, AppLockService::class.java
                ).setAction(App.ACTION_APPLOCK_SERVICE_UPDATE_DATA)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        adapter?.isMutable = false
    }

    private fun startLoadPackagesTask() {
        loadApplicationsTask = LoadApplicationsTask(activity)
        loadApplicationsTask?.start()
    }

    fun onPackagesLoaded() {
        if (adapter == null) {
            adapter = AppLockAdapter(this, appModels!!)
            listView?.adapter = adapter
        } else {
            adapter?.setAppModels(appModels!!)
        }
        progressBar?.visibility = View.GONE
        loadingLabel?.visibility = View.GONE
        if (swipeRefreshLayout?.isRefreshing == true) {
            swipeRefreshLayout?.isRefreshing = false
        }
        appModels = null
    }

    private fun needPermissionForBlocking(context: Context?): Boolean {
        return CodeManager.needPermissionForGetUsages(context)
    }

    override fun onDestroy() {
        if (loadApplicationsTask?.getStatus() != JTask.Status.FINISHED) {
            loadApplicationsTask?.revokeFinish(true)
            loadApplicationsTask?.interrupt()
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!CodeManager.needPermissionForGetUsages(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                    context
                )
            ) {
                return
            }
            adapter?.toggleSelection(lastClickedItemPosition)
            animateCheckView(lastClickedParentView)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_message_history, menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView?
        searchView!!.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showInputMethod(view.findFocus())
            }
        }
        searchView.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showInputMethod(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    private fun hideInputMethod(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    fun notifyBottomLayoutChanged(v: View) {
        this.paddingBottom = v.height
        ViewUtils.setViewPaddingBottom(listView, paddingBottom)
    }

    private inner class LoadApplicationsTask(private val context: Context) : JTask() {
        private var progress = 0.0

        init {
            appModels = ArrayList()
        }

        override fun workingThread() {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pm = context.packageManager
            val infoMutableList =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    context.packageManager.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                    ) else @Suppress("DEPRECATION") context.packageManager.queryIntentActivities(
                    intent, 0
                )

            Collections.sort(infoMutableList, ResolveInfo.DisplayNameComparator(pm))
            for (i in infoMutableList.indices) {
                if (this.isInterrupted) {
                    break
                }
                val resolveInfo = infoMutableList[i]
                if (resolveInfo.activityInfo.packageName == context.packageName)
                    continue
                val model = AppModel()
                model.packageName = resolveInfo.loadLabel(pm).toString()
                model.name = resolveInfo.activityInfo.packageName
                model.icon = resolveInfo.activityInfo.loadIcon(pm)
                appModels?.add(model)
                progress = 100.0 / infoMutableList.size * i
                sendUpdate()
            }
        }

        override fun onStarted() {}
        override fun onFinished() {
            if (parentView != null) {
                onPackagesLoaded()
            }
        }

        override fun onException(e: Exception) {
            Toast.makeText(requireContext(), "exception: " + e.message, Toast.LENGTH_SHORT).show()
        }

        override fun onUpdated(get: Array<Any>) {
            if (loadingLabel != null) {
                loadingLabel?.text = progress.toInt().toString().plus("%")
            }
            if (progressBar != null) {
                if (progressBar?.isIndeterminate == true) {
                    progressBar?.isIndeterminate = false
                    progressBar?.max = 100
                }
                progressBar?.progress = progress.toInt()
            }
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION_CODE = 9
    }
}