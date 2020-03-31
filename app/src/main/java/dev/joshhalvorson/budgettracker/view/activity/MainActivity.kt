package dev.joshhalvorson.budgettracker.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import dev.joshhalvorson.budgettracker.adapter.ViewPagerFragmentAdapter
import dev.joshhalvorson.budgettracker.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val currentDate = getCurrentDate()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MM/yyyy")
        return sdf.format(Date())
    }

    private fun init() {
        binding.viewPager.adapter = ViewPagerFragmentAdapter(this)

        // attaching tab mediator
        TabLayoutMediator(binding.tabLayout, binding.viewPager,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = when (position) {
                    0 -> {
                        "$currentDate Budget"
                    }
                    else -> {
                        "All Budgets"
                    }
                }
            }
        ).attach()
    }
}
