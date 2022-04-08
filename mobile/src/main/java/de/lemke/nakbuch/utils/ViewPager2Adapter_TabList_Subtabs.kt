package de.lemke.nakbuch.utils

import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter
import de.lemke.nakbuch.fragments.TabList_SubtabNumeric
import de.lemke.nakbuch.fragments.TabList_SubtabAlphabetic
import de.lemke.nakbuch.fragments.TabList_SubtabRubric
import java.util.*

class ViewPager2Adapter_TabList_Subtabs(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return TabList_SubtabNumeric()
            1 -> return TabList_SubtabAlphabetic()
            2 -> return TabList_SubtabRubric()
        }
        return TabList_SubtabNumeric()
    }

    override fun getItemCount(): Int {
        return 3
    }
}