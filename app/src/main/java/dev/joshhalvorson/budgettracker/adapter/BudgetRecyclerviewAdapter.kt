package dev.joshhalvorson.budgettracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.joshhalvorson.budgettracker.R
import dev.joshhalvorson.budgettracker.model.Budget
import dev.joshhalvorson.budgettracker.util.ProgressBarAnimation
import kotlinx.android.synthetic.main.budget_list_element_layout.view.*
import kotlin.math.roundToInt

class BudgetRecyclerviewAdapter(
    private val data: ArrayList<Pair<String, Float>>,
    private val budget: Budget
) : RecyclerView.Adapter<BudgetRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.budget_list_element_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data.sortWith(compareBy { it.second })
        data.reverse()
        holder.bindItem(position, data[position], budget)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val expenseImage = itemView.expense_imageview
        private val expenseName = itemView.expense_name_textview
        private val expensePercent = itemView.expense_percent_textview
        private val expenseTotal = itemView.expense_total_textview
        private val expensePercentBar = itemView.expense_percent_bar

        fun bindItem(position: Int, expensePair: Pair<String, Float>, budget: Budget) {
            val percent = ((expensePair.second / budget.spent) * 100).roundToInt()

            expenseName.text = expensePair.first
            expenseTotal.text = expensePair.second.toString()
            expensePercent.text = "$percent%"

            val anim = ProgressBarAnimation(expensePercentBar, 0f, percent.toFloat())
            anim.duration = 500
            expensePercentBar.startAnimation(anim)

            when (expensePair.first) {
                "Bills" -> expenseImage.setImageDrawable(itemView.context.resources.getDrawable(R.drawable.ic_bills))
                "Social" -> expenseImage.setImageDrawable(itemView.context.resources.getDrawable(R.drawable.ic_social))
                "Transportation" -> expenseImage.setImageDrawable(
                    itemView.context.resources.getDrawable(
                        R.drawable.ic_transportation
                    )
                )
                "Food" -> expenseImage.setImageDrawable(itemView.context.resources.getDrawable(R.drawable.ic_food))
                "Insurance" -> expenseImage.setImageDrawable(
                    itemView.context.resources.getDrawable(
                        R.drawable.ic_insurance
                    )
                )
                "Entertainment" -> expenseImage.setImageDrawable(
                    itemView.context.resources.getDrawable(
                        R.drawable.ic_entertainment
                    )
                )
                "Other" -> expenseImage.setImageDrawable(itemView.context.resources.getDrawable(R.drawable.ic_other))
            }
        }
    }
}
