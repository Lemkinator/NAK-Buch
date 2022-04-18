package de.lemke.nakbuch

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.lemke.nakbuch.fragments.TextviewFragment
import de.lemke.nakbuch.utils.Constants

class TextviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        val mContext: Context = this
        val sp = mContext.getSharedPreferences(getString(R.string.preference_file_default), MODE_PRIVATE)
        val gesangbuchSelected = intent.getBooleanExtra("buchMode", sp.getBoolean("gesangbuchSelected", true))
        val nr = intent.getIntExtra("nr", -1)
        val boldText = intent.getStringExtra("boldText")
        if (nr < 1) {
            Toast.makeText(mContext, "Invalid Number...", Toast.LENGTH_LONG).show()
            finish()
        }
        setContentView(R.layout.activity_textview)
        val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2TextView)
        viewPager2.adapter = ViewPager2AdapterTextview(this, gesangbuchSelected, boldText)
        viewPager2.registerOnPageChangeCallback(object : SeslViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                /*if (position == 0) {
                    show fab
                } else {
                    hide fab
                }*/
            }

            override fun onPageSelected(position: Int) {
                /*if (position == 0) {
                    show fab
                } else {
                    hide fab
                }*/
            }

            override fun onPageScrollStateChanged(state: Int) {
                /*if (state == ViewPager2.SCROLL_STATE_IDLE && viewPager2.getCurrentItem() == 0) {
                    show fab
                } else {
                    hide fab
                }*/
            }
        })
        viewPager2.setCurrentItem(nr - 1, false)
        //new Handler(Looper.getMainLooper()).postDelayed(() -> viewPager2.setCurrentItem(nr, true), 100);
    }

    inner class ViewPager2AdapterTextview(
        fragmentActivity: FragmentActivity,
        private val gesangbuchSelected: Boolean,
        private val boldText: String?
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return TextviewFragment.newInstance(
                gesangbuchSelected,
                position + 1,
                boldText
            )
        }

        override fun getItemCount(): Int {
            return if (gesangbuchSelected) Constants.HYMNSGESANGBUCHCOUNT else Constants.HYMNSCHORBUCHCOUNT
        }
    }
}