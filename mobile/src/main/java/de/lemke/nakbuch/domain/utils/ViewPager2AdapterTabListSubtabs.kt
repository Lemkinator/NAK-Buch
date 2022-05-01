package de.lemke.nakbuch.domain.utils

import androidx.fragment.app.Fragment
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter
import de.lemke.nakbuch.ui.fragments.TabListSubtabAlphabetic
import de.lemke.nakbuch.ui.fragments.TabListSubtabNumeric
import de.lemke.nakbuch.ui.fragments.TabListSubtabRubric

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