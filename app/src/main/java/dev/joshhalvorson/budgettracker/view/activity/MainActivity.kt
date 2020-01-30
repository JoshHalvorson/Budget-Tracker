package dev.joshhalvorson.budgettracker.view.activity

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.facebook.stetho.Stetho
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import dev.joshhalvorson.budgettracker.R
import dev.joshhalvorson.budgettracker.adapter.BudgetRecyclerviewAdapter
import dev.joshhalvorson.budgettracker.database.AppDatabase
import dev.joshhalvorson.budgettracker.model.Budget
import dev.joshhalvorson.budgettracker.view.dialog.AddSpendingDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.expense_breakdown_card_content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private val expenseTypes =
        listOf("Bills", "Social", "Transportation", "Food", "Insurance", "Entertainment", "Other")
    private var chartColors = listOf<Int>()
    private var chartData = listOf<PieEntry>()

    private lateinit var budget: Budget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Stetho.initializeWithDefaults(applicationContext)

        val db =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "budget-db").build()
        GlobalScope.launch {
//            db.budgetDao().insert(
//                Budget(
//                    1,
//                    6000f,
//                    2630f,
//                    6000f - 2630f,
//                    1500f,
//                    100f,
//                    50f,
//                    500f,
//                    400f,
//                    25f,
//                    55f
//                )
//            )
//            db.budgetDao().update(
//                Budget(
//                    1,
//                    6000f,
//                    2630f,
//                    6000f - 2630f,
//                    1500f,
//                    100f,
//                    50f,
//                    500f,
//                    400f,
//                    25f,
//                    55f
//                )
//            )
            val currentBudget = db.budgetDao().getBudget()
            budget = currentBudget[0]
            val spent = db.budgetDao().getTotalSpent()
            Log.i("testBudget", currentBudget.toString())
            Log.i("testBudget", spent.toString())
            withContext(Dispatchers.Main) {
                initChart(currentBudget[0])
            }
        }

        edit_budget_button.setOnClickListener {
            val dialog = AddSpendingDialog()
            dialog.onResult = { category, amount ->
                //update db
                Log.i("dialogTest", "$category, $amount")
                GlobalScope.launch {
                    db.budgetDao().update(updateBudgetValues(budget, category, amount))
                    withContext(Dispatchers.Main) {
                        resetChart()
                        initChart(budget)
                    }
                }
            }
            dialog.show(supportFragmentManager, "add_spending")
        }
    }

    private fun initChart(budget: Budget) {
        expense_card_total_spent_textview.text = "Total: ${budget.spent}"
        budget_pie_chart.setUsePercentValues(true)
        budget_pie_chart.isDrawHoleEnabled = true
        budget_pie_chart.setHoleColor(Color.WHITE)
        budget_pie_chart.setTransparentCircleColor(Color.WHITE)
        budget_pie_chart.setTransparentCircleAlpha(110)
        budget_pie_chart.holeRadius = 89f
        budget_pie_chart.animateY(1400, Easing.EaseInOutQuad)
        budget_pie_chart.centerText = "Expenses: \n ${budget.spent}"
        budget_pie_chart.setCenterTextSize(18f)
        budget_pie_chart.legend.isEnabled = false
        budget_pie_chart.description.isEnabled = false
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
        chartData = entries

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
        chartColors = colors
        dataSet.colors = colors
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(budget_pie_chart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)

        budget_pie_chart.data = data
        budget_pie_chart.highlightValues(null)
        budget_pie_chart.invalidate()
        setLegend(budget)
        initRecyclerView(budget)
    }

    private fun setLegend(budget: Budget) {
        budget_chart_legend.removeAllViews()
        chartData.forEachIndexed { index, pieEntry ->
            budget_chart_legend.addView(createLegendView(pieEntry, index, budget))
        }
    }

    private fun createLegendView(pieEntry: PieEntry, index: Int, budget: Budget): LinearLayout {
        val ll = LinearLayout(applicationContext)
        val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        layoutParams.topMargin = 5
        ll.layoutParams = layoutParams
        ll.orientation = HORIZONTAL

        val expenseTitle = TextView(applicationContext)
        expenseTitle.text = "${expenseTypes[index]}"
        val expenseTitleLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        expenseTitleLayoutParams.weight = 1.0f
        expenseTitle.layoutParams = expenseTitleLayoutParams

        val expensePercent = TextView(applicationContext)
        expensePercent.text = "${((pieEntry.value / budget.spent) * 100).roundToInt()}%"
        expensePercent.setTextColor(Color.BLACK)
        val expensePercentLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        expensePercentLayoutParams.weight = 0.0f
        expensePercent.layoutParams = expensePercentLayoutParams

        val legendColor = ImageView(applicationContext)
        val legendColorLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        legendColorLayoutParams.weight = 0.0f
        legendColorLayoutParams.topMargin = 8
        legendColorLayoutParams.rightMargin = 16
        legendColor.layoutParams = legendColorLayoutParams
        legendColor.setImageDrawable(resources.getDrawable(R.drawable.ic_budget_chart_legend_icon))
        val drawable = legendColor.drawable as GradientDrawable
        drawable.setStroke(6, chartColors[index])

        ll.addView(legendColor)
        ll.addView(expenseTitle)
        ll.addView(expensePercent)

        return ll
    }

    private fun initRecyclerView(budget: Budget) {
        val data = ArrayList<Pair<String, Float>>()

        expenseTypes.forEachIndexed { i, s ->
            data.add(Pair(s, chartData[i].value))
        }

        expense_breakdown_budget_list.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = BudgetRecyclerviewAdapter(data, budget)
        expense_breakdown_budget_list.adapter = adapter
    }

    private fun updateBudgetValues(budget: Budget, category: String, amount: Float) : Budget {
        when (category) {
            "Bills" -> budget.bills += amount
            "Social" -> budget.social += amount
            "Transportation" -> budget.transportation += amount
            "Food" -> budget.food += amount
            "Insurance" -> budget.insurance += amount
            "Entertainment" -> budget.entertainment += amount
            "Other" -> budget.other += amount
        }
        budget.spent += amount
        budget.balance -= amount
        return budget
    }

    private fun resetChart() {
        budget_pie_chart.data.clearValues()
        budget_pie_chart.clear()
        budget_pie_chart.invalidate()
    }
}
