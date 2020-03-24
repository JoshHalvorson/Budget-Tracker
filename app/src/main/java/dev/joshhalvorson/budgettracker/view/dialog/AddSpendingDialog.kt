package dev.joshhalvorson.budgettracker.view.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import dev.joshhalvorson.budgettracker.R
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
        val items = listOf(
            "Bills",
            "Social",
            "Transportation",
            "Food",
            "Insurance",
            "Entertainment",
            "Other"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_layout, items)
        binding.textField.setAdapter(adapter)

        binding.textField.setOnItemClickListener { parent, view, position, id ->
            selectedItem = parent.getItemAtPosition(position).toString()
            Log.i("aijwdpoia", selectedItem)
        }

        binding.addSpendingDialogAddButton.setOnClickListener {
            if (items.contains(binding.textField.text.toString()) && binding.addSpendingDialogAmountEditText.text.toString().isNotBlank()) {
                onResult?.invoke(
                    selectedItem,
                    binding.addSpendingDialogAmountEditText.text.toString().toFloat()
                )
                dismiss()
            } else if (!items.contains(binding.textField.text.toString()) && !binding.addSpendingDialogAmountEditText.text.toString().isNotBlank()) {
                binding.addSpendingDialogAmountInputLayout.error = "Enter valid amount"
                binding.addSpendingDialogCategorySpinner.error = "Enter valid category"
            } else if (!binding.addSpendingDialogAmountEditText.text.toString().isNotBlank()) {
                binding.addSpendingDialogAmountInputLayout.error = "Enter valid amount"
            } else {
                binding.addSpendingDialogCategorySpinner.error = "Enter valid category"
            }
        }
    }
}
