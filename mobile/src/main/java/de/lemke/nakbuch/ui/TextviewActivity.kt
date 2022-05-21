package de.lemke.nakbuch.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymnUseCases.GetHymnCountUseCase
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.ui.fragments.TextviewFragment
import javax.inject.Inject

@AndroidEntryPoint
class TextviewActivity : AppCompatActivity() {

    @Inject
    lateinit var getHymnCount: GetHymnCountUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        setContentView(R.layout.activity_textview)
        val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2TextView)
        val hymnId = HymnId.create(intent.getIntExtra("hymnId", -1))
        Log.d("test", "tvActivity got: $hymnId")
        if (hymnId == null) finish()
        else {
            viewPager2.adapter = ViewPager2AdapterTextview(this, hymnId, intent.getStringExtra("boldText"))
            viewPager2.setCurrentItem(hymnId.number - 1, true) //TODO smooth?
            viewPager2.registerOnPageChangeCallback(object : SeslViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {}

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    inner class ViewPager2AdapterTextview(
        fragmentActivity: FragmentActivity, private val hymnId: HymnId, private val boldText: String?
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return TextviewFragment.newInstance(position + 1 + hymnId.buchMode.toInt(), boldText)
        }

        override fun getItemCount(): Int {
            return getHymnCount(hymnId.buchMode)
        }
    }
}