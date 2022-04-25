package de.lemke.nakbuch.fragments

import android.view.View
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.utils.ViewPager2AdapterTabListSubtabs
import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.sesl.tabs.TabLayoutMediator
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2
import de.dlyt.yanndroid.oneui.view.ViewPager2
import de.dlyt.yanndroid.oneui.widget.TabLayout

class MainActivityTabList : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mRootView = inflater.inflate(R.layout.fragment_tab_list, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val subTabs: TabLayout = mRootView.findViewById(R.id.sub_tabs)
        subTabs.seslSetSubTabStyle()
        subTabs.tabMode = TabLayout.SESL_MODE_WEIGHT_AUTO
        val viewPager2: ViewPager2 = mRootView.findViewById(R.id.viewPager2Lists)
        viewPager2.adapter = ViewPager2AdapterTabListSubtabs(this)
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
        val tlm = TabLayoutMediator(subTabs, viewPager2) { tab, position ->
            val tabTitle = arrayOf(
                getString(R.string.numerical),
                getString(R.string.alphabetical),
                getString(R.string.rubric)
            )
            tab.text = tabTitle[position]
        }
        tlm.attach()
    }
}