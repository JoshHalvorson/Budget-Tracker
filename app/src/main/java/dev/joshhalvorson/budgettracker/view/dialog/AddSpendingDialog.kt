package dev.joshhalvorson.budgettracker.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import dev.joshhalvorson.budgettracker.databinding.FragmentAddSpendingDialogBinding

class AddSpendingDialog : DialogFragment() {
    var onResult: ((category: String, amount: Float) -> Unit)? = null
    private var selectedItem = "Bills"

    private var _binding: FragmentAddSpendingDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddSpendingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.addSpendingDialogCategorySpinner.setItems(
            "Bills",
            "Social",
            "Transportation",
            "Food",
            "Insurance",
            "Entertainment",
            "Other"
        )

        binding.addSpendingDialogCategorySpinner.setOnItemSelectedListener { view, position, id, item ->
            selectedItem = view.getItems<String>()[position]
        }

        binding.addSpendingDialogAddButton.setOnClickListener {
            onResult?.invoke(
                selectedItem,
                binding.addSpendingDialogAmountEditText.text.toString().toFloat()
            )
            dismiss()
        }
    }
}
