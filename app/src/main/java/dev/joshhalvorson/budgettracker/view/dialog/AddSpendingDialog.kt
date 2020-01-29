package dev.joshhalvorson.budgettracker.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import dev.joshhalvorson.budgettracker.R
import kotlinx.android.synthetic.main.fragment_add_spending_dialog.*

class AddSpendingDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_spending_dialog, container, false)
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        add_spending_dialog_category_spinner.setItems(
            "Bills",
            "Social",
            "Transportation",
            "Food",
            "Insurance",
            "Entertainment",
            "Other"
        )
    }
}
