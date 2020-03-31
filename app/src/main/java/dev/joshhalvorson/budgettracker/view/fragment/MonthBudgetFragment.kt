package dev.joshhalvorson.budgettracker.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import dev.joshhalvorson.budgettracker.databinding.FragmentMonthBudgetBinding
import dev.joshhalvorson.budgettracker.model.Budget
import dev.joshhalvorson.budgettracker.view.dialog.AddSpendingDialog
import dev.joshhalvorson.budgettracker.view.dialog.InitBudgetDialog
import dev.joshhalvorson.budgettracker.view.dialog.LoginDialog
import kotlinx.android.synthetic.main.budget_pie_chart_legend_element.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MonthBudgetFragment : Fragment() {
    private var _binding: FragmentMonthBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context

    private val expenseTypes =
        listOf("Bills", "Social", "Transportation", "Food", "Insurance", "Entertainment", "Other")
    private var chartColors = listOf<Int>()
    private var chartData = listOf<PieEntry>()

    private val listData = ArrayList<Pair<String, Float>>()
    private val currentDate = getCurrentDate()

    private lateinit var adapter: BudgetRecyclerviewAdapter
    private lateinit var budget: Budget
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonthBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            mContext = it
        }

        hideViews()

        Stetho.initializeWithDefaults(mContext)

        binding.expenseBreakdown.expenseBreakdownBudgetList.layoutManager =
            LinearLayoutManager(mContext)

        db = Room.databaseBuilder(mContext, AppDatabase::class.java, "budget-db").build()

        val biometricManager = BiometricManager.from(mContext)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("BioAuth", "App can authenticate using biometrics.")
                authenticateWithBio(db)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("BioAuth", "No biometric features available on this device.")
                launchLoginDialog()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("BioAuth", "Biometric features are currently unavailable.")
                launchLoginDialog()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(
                    "BioAuth", "The user hasn't associated " +
                            "any biometric credentials with their account."
                )
                launchLoginDialog()
            }
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
            dialog.show(childFragmentManager, "add_spending")
        }
    }

    private fun launchLoginDialog() {
        val dialog = LoginDialog()
        dialog.onResult = {
            GlobalScope.launch {
                val currentBudget = db.budgetDao().getBudget()
                if (currentBudget.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        initBudget(db)
                        hideViews()
                    }
                } else {
                    budget = currentBudget[currentBudget.size - 1]
                    checkIfNewMonth(budget)
                    val spent = db.budgetDao().getTotalSpent(budget.id)
                    Log.i("testBudget", currentBudget.toString())
                    Log.i("testBudget", spent.toString())
                    withContext(Dispatchers.Main) {
                        showViews()
                        initChart(budget, db)
                    }
                }
            }
        }
        dialog.show(childFragmentManager, "login_dialog")
    }

    private fun initChart(budget: Budget, db: AppDatabase) {
        binding.expenseBreakdown.expenseCardTotalSpentTextview.text = "Total: ${budget.spent}"
        binding.budgetPieChart.setUsePercentValues(true)
        binding.budgetPieChart.isDrawHoleEnabled = true
        binding.budgetPieChart.setHoleColor(Color.TRANSPARENT)
        binding.budgetPieChart.setTransparentCircleColor(Color.TRANSPARENT)
        binding.budgetPieChart.setTransparentCircleAlpha(110)
        binding.budgetPieChart.holeRadius = 89f
        binding.budgetPieChart.animateY(1400, Easing.EaseInOutQuad)
        binding.budgetPieChartText.text = "Expenses: \n ${budget.spent}"
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
                    "Bills" -> db.budgetDao().getBills(budget.id)
                    "Social" -> db.budgetDao().getSocial(budget.id)
                    "Transportation" -> db.budgetDao().getTransportation(budget.id)
                    "Food" -> db.budgetDao().getFood(budget.id)
                    "Insurance" -> db.budgetDao().getInsurance(budget.id)
                    "Entertainment" -> db.budgetDao().getEntertainment(budget.id)
                    "Other" -> db.budgetDao().getOther(budget.id)
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

    private fun createLegendView(pieEntry: PieEntry, index: Int, budget: Budget): View {
        val legendElement =
            LayoutInflater.from(mContext)
                .inflate(R.layout.budget_pie_chart_legend_element, null, false)

        legendElement.legend_element_text.text = expenseTypes[index]

        legendElement.legend_element_percent.text =
            if (pieEntry.value == 0.0f) "0%" else "${((pieEntry.value / budget.spent) * 100).roundToInt()}%"

        val drawable = legendElement.legend_element_icon.drawable as GradientDrawable
        drawable.setStroke(6, chartColors[index])

        return legendElement
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
                    currentDate,
                    currentDate,
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
        dialog.show(childFragmentManager, "init_budget")
    }

    private fun hideViews() {
        binding.expenseBreakdownCard.visibility = View.GONE
        binding.budgetPieChart.visibility = View.GONE
        binding.budgetChartLegend.visibility = View.GONE
        binding.editBudgetButton.visibility = View.GONE
        binding.budgetPieChartText.visibility = View.GONE
    }

    private fun showViews() {
        binding.expenseBreakdownCard.visibility = View.VISIBLE
        binding.budgetPieChart.visibility = View.VISIBLE
        binding.budgetChartLegend.visibility = View.VISIBLE
        binding.editBudgetButton.visibility = View.VISIBLE
        binding.budgetPieChartText.visibility = View.VISIBLE
    }

    private fun authenticateWithBio(db: AppDatabase) {
        hideViews()
        executor = ContextCompat.getMainExecutor(mContext)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        mContext,
                        "$errString", Toast.LENGTH_SHORT
                    )
                        .show()
                    activity?.finishAffinity()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        mContext,
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
                            budget = currentBudget[currentBudget.size - 1]
                            checkIfNewMonth(budget)
                            val spent = db.budgetDao().getTotalSpent(budget.id)
                            Log.i("testBudget", currentBudget.toString())
                            Log.i("testBudget", spent.toString())
                            withContext(Dispatchers.Main) {
                                showViews()
                                initChart(budget, db)
                            }
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        mContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    activity?.finishAffinity()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify identity")
            .setSubtitle("Log in using your biometric credential, or device credential")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun exportData() {
        GlobalScope.launch {
            var csvString = ""
            val cursor = db.budgetDao().getAllCursor(budget.id)
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
                if (sendIntent.resolveActivity(activity?.packageManager!!) != null) {
                    startActivity(shareIntent)
                }
            }

        }

    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MM/yyyy")
        return sdf.format(Date())
    }

    private fun checkIfNewMonth(budget: Budget) {
        // get last budget, if its last month, init new budget
        val format = SimpleDateFormat("MM/yyyy")
        val oldDate = format.parse(budget.dateStarted)
        val currentDate = format.parse(getCurrentDate())

        when {
            oldDate.before(currentDate) -> {
                Log.i("dateCompare", "before")
                hideViews()
                initBudget(db)
            }
            oldDate == currentDate -> {
                Log.i("dateCompare", "equals")
            }
            else -> {
                Log.i("dateCompare", "after")
            }
        }

    }

}
