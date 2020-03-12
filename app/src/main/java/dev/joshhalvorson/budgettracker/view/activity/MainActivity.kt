package dev.joshhalvorson.budgettracker.view.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import dev.joshhalvorson.budgettracker.databinding.ActivityMainBinding
import dev.joshhalvorson.budgettracker.model.Budget
import dev.joshhalvorson.budgettracker.view.dialog.AddSpendingDialog
import dev.joshhalvorson.budgettracker.view.dialog.InitBudgetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val expenseTypes =
        listOf("Bills", "Social", "Transportation", "Food", "Insurance", "Entertainment", "Other")
    private var chartColors = listOf<Int>()
    private var chartData = listOf<PieEntry>()

    private val listData = ArrayList<Pair<String, Float>>()

    private lateinit var adapter: BudgetRecyclerviewAdapter
    private lateinit var budget: Budget
    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideViews()

        Stetho.initializeWithDefaults(applicationContext)

        binding.expenseBreakdown.expenseBreakdownBudgetList.layoutManager =
            LinearLayoutManager(applicationContext)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "budget-db").build()

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("BioAuth", "App can authenticate using biometrics.")
                authenticateWithBio(db)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("BioAuth", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("BioAuth", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e(
                    "BioAuth", "The user hasn't associated " +
                            "any biometric credentials with their account."
                )
        }

        binding.editBudgetButton.setOnClickListener {
            val dialog = AddSpendingDialog()
            dialog.onResult = { category, amount ->
                //update db
                Log.i("dialogTest", "$category, $amount")
                GlobalScope.launch {
                    val newBudget = updateBudgetValues(budget, category, amount)
                    db.budgetDao().update(newBudget)
                    withContext(Dispatchers.Main) {
                        listData.clear()
                        resetChart()
                        initChart(newBudget, db)
                    }
                }
            }
            dialog.show(supportFragmentManager, "add_spending")
        }
    }

    private fun initChart(budget: Budget, db: AppDatabase) {
        binding.expenseBreakdown.expenseCardTotalSpentTextview.text = "Total: ${budget.spent}"
        binding.budgetPieChart.setUsePercentValues(true)
        binding.budgetPieChart.isDrawHoleEnabled = true
        binding.budgetPieChart.setHoleColor(Color.WHITE)
        binding.budgetPieChart.setTransparentCircleColor(Color.WHITE)
        binding.budgetPieChart.setTransparentCircleAlpha(110)
        binding.budgetPieChart.holeRadius = 89f
        binding.budgetPieChart.animateY(1400, Easing.EaseInOutQuad)
        binding.budgetPieChart.centerText = "Expenses: \n ${budget.spent}"
        binding.budgetPieChart.setCenterTextSize(18f)
        binding.budgetPieChart.legend.isEnabled = false
        binding.budgetPieChart.description.isEnabled = false

        adapter = BudgetRecyclerviewAdapter(listData, budget)
        binding.expenseBreakdown.expenseBreakdownBudgetList.adapter = adapter

        setData(budget, db)
    }

    private fun setData(budget: Budget, db: AppDatabase) {
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
        data.setValueFormatter(PercentFormatter(binding.budgetPieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)

        binding.budgetPieChart.data = data
        binding.budgetPieChart.highlightValues(null)
        binding.budgetPieChart.invalidate()
        setLegend(budget)

        listData.clear()
        GlobalScope.launch {
            expenseTypes.forEachIndexed { i, s ->
                val amount = when (s) {
                    "Bills" -> db.budgetDao().getBills()
                    "Social" -> db.budgetDao().getSocial()
                    "Transportation" -> db.budgetDao().getTransportation()
                    "Food" -> db.budgetDao().getFood()
                    "Insurance" -> db.budgetDao().getInsurance()
                    "Entertainment" -> db.budgetDao().getEntertainment()
                    "Other" -> db.budgetDao().getOther()
                    else -> 0.0f
                }
                listData.add(Pair(s, amount))
            }
            withContext(Dispatchers.Main) {
                listData.sortWith(compareBy { it.second })
                listData.reverse()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setLegend(budget: Budget) {
        binding.budgetChartLegend.removeAllViews()
        chartData.forEachIndexed { index, pieEntry ->
            binding.budgetChartLegend.addView(createLegendView(pieEntry, index, budget))
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
        if (pieEntry.value != 0.0f) {
            expensePercent.text = "${((pieEntry.value / budget.spent) * 100).roundToInt()}%"
        } else {
            expensePercent.text = "0%"
        }
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

    private fun updateBudgetValues(budget: Budget, category: String, amount: Float): Budget {
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
        binding.budgetPieChart.data.clearValues()
        binding.budgetPieChart.clear()
        binding.budgetPieChart.invalidate()
    }

    private fun initBudget(db: AppDatabase) {
        val dialog = InitBudgetDialog()
        dialog.onResult = { amount ->
            GlobalScope.launch {
                val newBudget = Budget(
                    1,
                    amount,
                    0f,
                    amount,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f
                )
                budget = newBudget
                db.budgetDao().insert(
                    newBudget
                )
                withContext(Dispatchers.Main) {
                    showViews()
                    initChart(newBudget, db)
                }
            }

        }
        dialog.show(supportFragmentManager, "init_budget")
    }

    private fun hideViews() {
        binding.expenseBreakdownCard.visibility = View.GONE
        binding.budgetPieChart.visibility = View.GONE
        binding.budgetChartLegend.visibility = View.GONE
        binding.editBudgetButton.visibility = View.GONE
    }

    private fun showViews() {
        binding.expenseBreakdownCard.visibility = View.VISIBLE
        binding.budgetPieChart.visibility = View.VISIBLE
        binding.budgetChartLegend.visibility = View.VISIBLE
        binding.editBudgetButton.visibility = View.VISIBLE
    }

    private fun authenticateWithBio(db: AppDatabase) {
        hideViews()
        executor = ContextCompat.getMainExecutor(applicationContext)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                            applicationContext,
                            "$errString", Toast.LENGTH_SHORT
                        )
                        .show()
                    finishAffinity()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                            applicationContext,
                            "Authentication succeeded!", Toast.LENGTH_SHORT
                        )
                        .show()
                    GlobalScope.launch {
                        val currentBudget = db.budgetDao().getBudget()
                        if (currentBudget.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                initBudget(db)
                                hideViews()
                            }
                        } else {
                            budget = currentBudget[0]
                            val spent = db.budgetDao().getTotalSpent()
                            Log.i("testBudget", currentBudget.toString())
                            Log.i("testBudget", spent.toString())
                            withContext(Dispatchers.Main) {
                                showViews()
                                initChart(currentBudget[0], db)
                            }
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                            applicationContext, "Authentication failed",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    finishAffinity()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify indentity")
            .setSubtitle("Log in using your biometric credential, or device credential")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun exportData() {
        GlobalScope.launch {
            var csvString = ""
            val cursor = db.budgetDao().getAllCursor()
            val success = cursor.moveToFirst()
            if (success) {
                while (!cursor.isAfterLast) {
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("budget")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("spent")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("balance")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("bills")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("social")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("transportation")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("food")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("insurance")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("entertainment")))
                    Log.i("exportData", cursor.getString(cursor.getColumnIndex("other")))

                    val dataList = mutableListOf<String>()

                    dataList.add("Budget: ${cursor.getString(cursor.getColumnIndex("budget"))}")
                    dataList.add("Spent: ${cursor.getString(cursor.getColumnIndex("spent"))}")
                    dataList.add("Balance: ${cursor.getString(cursor.getColumnIndex("balance"))}")
                    dataList.add("Bills: ${cursor.getString(cursor.getColumnIndex("bills"))}")
                    dataList.add("Social: ${cursor.getString(cursor.getColumnIndex("social"))}")
                    dataList.add("Transportation: ${cursor.getString(cursor.getColumnIndex("transportation"))}")
                    dataList.add("Food: ${cursor.getString(cursor.getColumnIndex("food"))}")
                    dataList.add("Insurance: ${cursor.getString(cursor.getColumnIndex("insurance"))}")
                    dataList.add("Entertainment: ${cursor.getString(cursor.getColumnIndex("entertainment"))}")
                    dataList.add("Other: ${cursor.getString(cursor.getColumnIndex("other"))}")

                    csvString = dataList.joinToString { it }
                    Log.i("exportData", csvString)

                    cursor.moveToNext()
                }
            } else {
                //empty
            }
            cursor.close()

            withContext(Dispatchers.Main) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, csvString)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, "send_data")
                if (sendIntent.resolveActivity(packageManager) != null) {
                    startActivity(shareIntent)
                }
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share_data -> {
                exportData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
