package de.lemke.nakbuch.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.sesl.tabs.TabLayoutMediator
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.dlyt.yanndroid.oneui.widget.TabLayout
import de.lemke.nakbuch.R

@AndroidEntryPoint
class MainActivityTabList : Fragment() {
    private lateinit var rootView: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tab_list, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val subTabs: TabLayout = rootView.findViewById(R.id.sub_tabs)
        subTabs.seslSetSubTabStyle()
        subTabs.tabMode = TabLayout.SESL_MODE_WEIGHT_AUTO
        val viewPager2: ViewPager2 = rootView.findViewById(R.id.viewPager2Lists)
        viewPager2.adapter = ViewPager2AdapterTabListSubtabs(this)
        viewPager2.registerOnPageChangeCallback(object : SeslViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        val tlm = TabLayoutMediator(subTabs, viewPager2) { tab, position ->
            val tabTitle = arrayOf(getString(R.string.numerical), getString(R.string.alphabetical), getString(R.string.rubric))
            tab.text = tabTitle[position]
        }
        tlm.attach()
    }
}

class ViewPager2AdapterTabListSubtabs(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return TabListSubtabNumeric()
            1 -> return TabListSubtabAlphabetic()
            2 -> return TabListSubtabRubric()
        }
        return TabListSubtabNumeric()
    }

    override fun getItemCount(): Int {
        return 3
    }
}