package dev.joshhalvorson.budgettracker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.joshhalvorson.budgettracker.view.fragment.AllBudgetsFragment
import dev.joshhalvorson.budgettracker.view.fragment.MonthBudgetFragment

class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return MonthBudgetFragment()
            1 -> return AllBudgetsFragment()
        }
        return MonthBudgetFragment()
    }

}