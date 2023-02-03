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
import com.jefferson.application.br.widget.LockCheck
import java.util.*

class LockFragment(mainActivity: MainActivity) : Fragment(), OnItemClickListener,
    SearchView.OnQueryTextListener {
    private var lastClickedParentView: View? = null
    private var lastClickedItemPosition = 0
    private var firstVisibleItem = -1
    private var lastVisibleItem = -1
    private var visibleCount = -1
    private var totalItemCount = -1
    private var mProgressBar: ProgressBar? = null
    private var mTextView: TextView? = null
    private var appModels: ArrayList<AppModel>? = null
    private var adapter: AppLockAdapter? = null
    private var intent: Intent? = null
    private var parentView: View? = null

    private var activity: MainActivity
    private var listView: ListView? = null
    private var mTask: LoadApplicationsTask? = null
    private var mySwipeRefreshLayout: SwipeRefreshLayout? = null
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
            mProgressBar = parentView!!.findViewById(R.id.progressApps)
            mTextView = parentView!!.findViewById(R.id.porcent)
            listView = parentView!!.findViewById(R.id.appList)
            mySwipeRefreshLayout = parentView!!.findViewById(R.id.swipe_refresh)
            listView?.itemsCanFocus = true
            listView?.clipToPadding = false
            setListViewPaddingBottom()
            val typedValue = TypedValue()
            val theme = activity.theme
            theme?.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true)
            val color = typedValue.data
            mySwipeRefreshLayout?.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)
            mySwipeRefreshLayout?.setProgressBackgroundColorSchemeColor(color) // .setProgressBackgroundColor(color);
            listView?.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(p1: AbsListView, p2: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    lastVisibleItem = firstVisibleItem + visibleItemCount
                    this@LockFragment.firstVisibleItem = firstVisibleItem
                    visibleCount = visibleItemCount
                    this@LockFragment.totalItemCount = totalItemCount
                }
            })
            if (mTask != null) {
                val status = mTask!!.getStatus()
                if (status == JTask.Status.FINISHED) {
                    doTaskFinalized()
                } else {
                    showProgressView()
                }
            }
            intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            listView?.onItemClickListener = this
            mySwipeRefreshLayout?.setOnRefreshListener {
                if (mTask?.getStatus() == JTask.Status.STARTED) {
                    mySwipeRefreshLayout?.isRefreshing = false
                } else {
                    adapter?.clear()
                    showProgressView()
                    startLoadPackagesTask()
                }
            }
        } else {
            if (mySwipeRefreshLayout?.isRefreshing == true) {
                mySwipeRefreshLayout?.isRefreshing = false
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
            val lowerName = model.packageName.lowercase(Locale.getDefault())
            val lowerInput = input.lowercase(Locale.getDefault())
            if (lowerName.startsWith(lowerInput)) {
                //JDebug.toast("match + " + models.get(x).appname);
                applicationFound(x)
                return true
            } else if (firstContains == -1) {
                if (lowerName.contains(lowerInput)) {
                    firstContains = x
                    //JDebug.toast(lowerName + " contains => " + input + " position " + firstContains);
                }
            }
        }
        if (firstContains != -1) {
            applicationFound(firstContains)
            return true
        }
        Toast.makeText(context, "No match found!", Toast.LENGTH_SHORT).show()
        //showInputMethod(searchView);
        return false
    }

    override fun onQueryTextChange(input: String): Boolean {
        return false
    }

    private fun showProgressView() {
        if (mProgressBar == null || mTextView == null) return
        mProgressBar?.progress = 0
        mProgressBar?.isIndeterminate = true
        mProgressBar?.visibility = View.VISIBLE
        mTextView?.text = ""
        mTextView?.visibility = View.VISIBLE
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View, position: Int, id: Long) {
        lastClickedItemPosition = position
        lastClickedParentView = view
        var noNeedOverlayPermission = false
        //Toast.makeText(getContext(), "position " + lastClickedItemPosition, Toast.LENGTH_SHORT);
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
                intent, REQUEST_OVERLAY_CODE
            ) //It will call onActivityResult Function After you press Yes/No and go Back after giving permission 
        } else {
            noNeedOverlayPermission = true
            Log.v("App", "We already have permission for it.")
            // disablePullNotificationTouch(); 
            // Do your stuff, we got permission captain 
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
        if (adapter != null && adapter!!.isMutable) {
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
        mTask = LoadApplicationsTask(activity)
        mTask?.start()
    }

    fun doTaskFinalized() {
        if (adapter == null) {
            adapter = AppLockAdapter(activity, appModels!!)
            listView?.adapter = adapter
        } else {
            adapter?.putDataSet(appModels!!)
        }
        mProgressBar?.visibility = View.GONE
        mTextView?.visibility = View.GONE
        if (mySwipeRefreshLayout?.isRefreshing == true) {
            mySwipeRefreshLayout?.isRefreshing = false
        }
        appModels = null
    }

    private fun needPermissionForBlocking(context: Context?): Boolean {
        return CodeManager.needPermissionForGetUsages(context)
    }

    override fun onDestroy() {
        if (mTask?.getStatus() != JTask.Status.FINISHED) {
            mTask?.revokeFinish(true)
            mTask?.interrupt()
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
            adapter!!.toggleSelection(lastClickedItemPosition)
            animateCheckView(lastClickedParentView)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_message_history, menu)

//      SearchManager searchManager =(SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        val searchView = menu.findItem(R.id.search).actionView as SearchView?
        //      searchView.setSearchableInfo(
//      searchManager.getSearchableInfo(getActivity().getComponentName()));
//
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
        paddingBottom = v.height
        setListViewPaddingBottom()
    }

    private fun setListViewPaddingBottom() {
        val left = listView?.paddingLeft
        val right = listView?.paddingRight
        val top = listView?.paddingTop
        if (left != null && top != null && right != null) {
            listView?.setPadding(left, top, right, paddingBottom)
        }
    }

    private inner class LoadApplicationsTask(private val context: Context) : JTask() {
        private var progress = 0.0

        init {
            appModels = ArrayList()
        }

        override fun workingThread() {
            val launch = Intent(Intent.ACTION_MAIN, null)
            launch.addCategory(Intent.CATEGORY_LAUNCHER)
            val pm = context.packageManager
            val apps = pm.queryIntentActivities(launch, 0)
            Collections.sort(apps, ResolveInfo.DisplayNameComparator(pm))
            for (i in apps.indices) {
                if (this.isInterrupted) {
                    break
                }
                val p = apps[i]
                if (p.activityInfo.packageName == context.packageName) {
                    continue
                }
                val newInfo = AppModel()
                newInfo.packageName = p.loadLabel(pm).toString()
                newInfo.name = p.activityInfo.packageName
                newInfo.icon = p.activityInfo.loadIcon(pm)
                appModels!!.add(newInfo)
                progress = 100.0 / apps.size * i
                sendUpdate()
            }
        }

        override fun onBeingStarted() {}
        override fun onFinished() {
            if (parentView != null) {
                doTaskFinalized()
            }
        }

        override fun onException(e: Exception) {}
        override fun onUpdated(get: Array<Any>) {
            if (mTextView != null) {
                mTextView?.text = progress.toInt().toString().plus("%")
            }
            if (mProgressBar != null) {
                if (mProgressBar?.isIndeterminate == true) {
                    mProgressBar?.isIndeterminate = false
                    mProgressBar?.max = 100
                }
                mProgressBar?.progress = progress.toInt()
            }
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_CODE = 9
    }
}