package com.jefferson.application.br.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.jefferson.application.br.R
import com.jefferson.application.br.fragment.PasswdPreviewFragment
import com.jefferson.application.br.fragment.PatternPreviewFragment
import com.jefferson.application.br.fragment.PinPreviewFragment
import com.jefferson.application.br.view.ViewPagerIndicator

class CustomLockScreen: MyCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_lock)
        val pagerIndicator = findViewById<ViewPagerIndicator>(R.id.view_pager_indicator)
        val viewPager: ViewPager2  = findViewById(R.id.view_pager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        pagerIndicator.setCount(adapter.itemCount)
        viewPager.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pagerIndicator.setCurrentPosition(position)
            }
        })
    }

    inner class ViewPagerAdapter(fa: FragmentActivity)

        : FragmentStateAdapter(fa) {
        private val selectListener: OnSelectLockTypeListener = OnSelectLockTypeListener()
        private val typeValues = LockType.values()

        override fun getItemCount(): Int {
            return typeValues.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (typeValues[position]) {
                LockType.PATTERN -> {
                    PatternPreviewFragment(selectListener)
                }
                LockType.PASSWORD -> {
                    PasswdPreviewFragment(selectListener)
                }
                LockType.PIN -> {
                    PinPreviewFragment(selectListener)
                }
            }
        }
    }

    enum class LockType {
        PATTERN,
        PASSWORD,
        PIN,
    }

    inner class OnSelectLockTypeListener: OnSelectLockTypeInterface {
        override fun onLockSelected(type: LockType) {
            Log.i("SelectedLockListener", "Selected: $type")
            Toast.makeText(this@CustomLockScreen, "Text off null", Toast.LENGTH_SHORT).show()
            val key = when (type) {
                LockType.PATTERN-> "pattern"
                LockType.PASSWORD -> "password"
                LockType.PIN -> "pin"
            }
        }
    }

    private interface OnSelectLockTypeInterface{
        fun onLockSelected(type: LockType)
    }
}