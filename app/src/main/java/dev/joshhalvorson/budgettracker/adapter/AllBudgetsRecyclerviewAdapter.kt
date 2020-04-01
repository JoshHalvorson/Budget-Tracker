package dev.joshhalvorson.budgettracker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import dev.joshhalvorson.budgettracker.R
import dev.joshhalvorson.budgettracker.model.Budget
import kotlinx.android.synthetic.main.budgets_list_element.view.*

class AllBudgetsRecyclerviewAdapter(private val data: ArrayList<Budget>) :
    RecyclerView.Adapter<AllBudgetsRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.budgets_list_element,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(position, data[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val budgetTitle = itemView.budget_list_month
        private val budgetBudget = itemView.budget_list_budget
        private val budgetTotalSpent = itemView.budget_list_total_spent

        fun bindItem(position: Int, budget: Budget) {
            budgetTitle.text = "Budget of ${budget.dateStarted}"
            budgetBudget.text = "$${budget.budget} budget"
            budgetTotalSpent.text = "$${budget.spent} spent"
            initChart(budget)
        }

        private fun initChart(budget: Budget) {
            itemView.budget_list_pie_chart.setUsePercentValues(true)
            itemView.budget_list_pie_chart.isDrawHoleEnabled = true
            itemView.budget_list_pie_chart.setHoleColor(Color.TRANSPARENT)
            itemView.budget_list_pie_chart.setTransparentCircleColor(Color.TRANSPARENT)
            itemView.budget_list_pie_chart.setTransparentCircleAlpha(110)
            itemView.budget_list_pie_chart.holeRadius = 89f
//            itemView.budget_list_pie_chart.animateY(1400, Easing.EaseInOutQuad)
            itemView.budget_list_pie_chart.setCenterTextSize(18f)
            itemView.budget_list_pie_chart.legend.isEnabled = false
            itemView.budget_list_pie_chart.description.isEnabled = false
            setData(budget)
        }

        private fun setData(budget: Budget) {
            val entries: ArrayList<PieEntry> = ArrayList()
            entries.clear()
            entries.add(PieEntry(budget.bills))
            entries.add(PieEntry(budget.social))
            entries.add(PieEntry(budget.transportation))
            entries.add(PieEntry(budget.food))
            entries.add(PieEntry(budget.insurance))
            entries.add(PieEntry(budget.entertainment))
            entries.add(PieEntry(budget.other))

            val dataSet = PieDataSet(entries, "Expenses")
            dataSet.setDrawIcons(false)
            dataSet.sliceSpace = 0f
            dataSet.iconsOffset = MPPointF(0f, 40f)
            dataSet.selectionShift = 5f
            val colors: ArrayList<Int> = ArrayList()
            colors.add(Color.BLUE)
            colors.add(Color.RED)
            colors.add(Color.GREEN)
            colors.add(Color.YELLOW)
            colors.add(Color.MAGENTA)
            colors.add(Color.CYAN)
            colors.add(Color.LTGRAY)
            dataSet.colors = colors
            dataSet.setDrawValues(false)

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter(itemView.budget_list_pie_chart))
            data.setValueTextSize(11f)
            data.setValueTextColor(Color.WHITE)

            itemView.budget_list_pie_chart.data = data
            itemView.budget_list_pie_chart.highlightValues(null)
            itemView.budget_list_pie_chart.invalidate()
        }
    }
}
