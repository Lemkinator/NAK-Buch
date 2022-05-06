package de.lemke.nakbuch.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymns.GetHymnCountUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.settings.GetBuchModeUseCase
import de.lemke.nakbuch.ui.fragments.TextviewFragment

class TextviewActivity : AppCompatActivity() {
    private lateinit var buchMode: BuchMode
    private var nr = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        setContentView(R.layout.activity_textview)
        nr = intent.getIntExtra("nr", -1)
        if (nr < 1) finish()
        val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2TextView)
        buchMode = GetBuchModeUseCase()()
        viewPager2.adapter = ViewPager2AdapterTextview(this, buchMode, intent.getStringExtra("boldText"))
        viewPager2.registerOnPageChangeCallback(object : SeslViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })
        viewPager2.setCurrentItem(nr - 1, false)
    }

    inner class ViewPager2AdapterTextview(
        fragmentActivity: FragmentActivity, private val buchMode: BuchMode, private val boldText: String?
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return TextviewFragment.newInstance(buchMode, position + 1, boldText)
        }

        override fun getItemCount(): Int {
            return GetHymnCountUseCase()(buchMode)
        }
    }
}