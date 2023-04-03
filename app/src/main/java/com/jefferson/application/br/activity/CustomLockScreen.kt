package com.jefferson.application.br.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jefferson.application.br.R
import com.jefferson.application.br.fragment.PatternPreviewFragment
import com.jefferson.application.br.fragment.PinPreviewFragment

class CustomLockScreen: MyCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_config)
        val viewPager: ViewPager2  = findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(this)

    }

    inner class ViewPagerAdapter(fa: FragmentActivity)
        : FragmentStateAdapter(fa) {
        private val typeValues = LockType.values()

        override fun getItemCount(): Int {
            return typeValues.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (typeValues[position]) {
                LockType.PATTERN -> {
                    PatternPreviewFragment()
                }
                LockType.PIN -> {
                    PinPreviewFragment()
                }
            }

        }

    }

    enum class LockType {
        PATTERN,
        PIN,
    }
}