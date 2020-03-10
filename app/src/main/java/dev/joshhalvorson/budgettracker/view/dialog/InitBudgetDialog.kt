package dev.joshhalvorson.budgettracker.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import dev.joshhalvorson.budgettracker.databinding.FragmentInitBudgetDialogBinding

class InitBudgetDialog : DialogFragment() {
    var onResult: ((amount: Float) -> Unit)? = null
    private var _binding: FragmentInitBudgetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInitBudgetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.initBudgetDialogAddButton.setOnClickListener {
            onResult?.invoke(binding.initBudgetDialogAmountEditText.text.toString().toFloat())
            dismiss()
        }
    }
}
