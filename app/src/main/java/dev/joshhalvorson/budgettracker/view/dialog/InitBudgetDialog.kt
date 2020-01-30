package dev.joshhalvorson.budgettracker.view.dialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

import dev.joshhalvorson.budgettracker.R
import kotlinx.android.synthetic.main.fragment_init_budget_dialog.*

class InitBudgetDialog : DialogFragment() {
    var onResult: ((amount: Float) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_init_budget_dialog, container, false)
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init_budget_dialog_add_button.setOnClickListener {
            onResult?.invoke(init_budget_dialog_amount_edit_text.text.toString().toFloat())
            dismiss()
        }
    }
}
