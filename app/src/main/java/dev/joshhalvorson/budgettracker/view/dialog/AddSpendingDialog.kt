package dev.joshhalvorson.budgettracker.view.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
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
        binding.addSpendingDialogCategorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    selectedItem = parent.getItemAtPosition(position).toString()
                    Log.i("aijwdpoia", selectedItem)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
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
