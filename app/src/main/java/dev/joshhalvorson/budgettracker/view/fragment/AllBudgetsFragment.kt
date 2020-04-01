package dev.joshhalvorson.budgettracker.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import dev.joshhalvorson.budgettracker.adapter.AllBudgetsRecyclerviewAdapter
import dev.joshhalvorson.budgettracker.database.AppDatabase
import dev.joshhalvorson.budgettracker.database.BudgetDao
import dev.joshhalvorson.budgettracker.databinding.FragmentAllBudgetsBinding
import dev.joshhalvorson.budgettracker.model.Budget
import kotlinx.coroutines.*

class AllBudgetsFragment : Fragment() {
    private var _binding: FragmentAllBudgetsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private lateinit var mContext: Context
    private lateinit var adapter: AllBudgetsRecyclerviewAdapter

    private val budgets = ArrayList<Budget>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllBudgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            mContext = it
        }

        adapter = AllBudgetsRecyclerviewAdapter(budgets)
        binding.allBudgetsRecyclerview.adapter = adapter
        binding.allBudgetsRecyclerview.layoutManager = LinearLayoutManager(context)

        db = Room.databaseBuilder(mContext, AppDatabase::class.java, "budget-db").build()

    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            val tempBudgets = db.budgetDao().getBudget()
            withContext(Dispatchers.Main) {
                budgets.clear()
                budgets.addAll(tempBudgets)
                adapter.notifyDataSetChanged()
                if (budgets.isEmpty()) {
                    //  should never be empty, but just in case
                    // TODO("display no budgets")
                }
            }
        }
    }
}
