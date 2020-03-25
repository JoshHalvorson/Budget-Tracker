package dev.joshhalvorson.budgettracker.view.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import dev.joshhalvorson.budgettracker.databinding.FragmentLoginDialogBinding

class LoginDialog : DialogFragment() {
    var onResult: (() -> Unit)? = null
    private var _binding: FragmentLoginDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPrefs = context?.getSharedPreferences("shared-prefs", Context.MODE_PRIVATE)
        val password = sharedPrefs?.getString("password", "")
        if (password!!.isEmpty()) {
            newLogin(sharedPrefs)
        } else {
            loginWithExisting(password)
        }
    }

    private fun newLogin(sharedPrefs: SharedPreferences) {
        binding.loginButton.setOnClickListener {
            if (binding.loginPasswordEditText.text.toString().isNotBlank() && binding.loginPasswordConfirmEditText.text.toString().isNotBlank()) {
                if (binding.loginPasswordEditText.text.toString() == binding.loginPasswordConfirmEditText.text.toString()) {
                    sharedPrefs.edit().putString("password", binding.loginPasswordConfirmEditText.text.toString()).apply()
                    onResult?.invoke()
                    dismiss()
                } else {
                    binding.loginPasswordConfirmInputLayout.error = "Passwords do not match"
                }
            } else if (!binding.loginPasswordEditText.text.toString().isNotBlank()) {
                binding.loginPasswordInputLayout.error = "Enter password"
            } else {
                binding.loginPasswordConfirmInputLayout.error = "Enter password"
            }
        }
    }

    private fun loginWithExisting(password: String) {
        binding.loginPasswordConfirmInputLayout.visibility = View.GONE
        binding.loginButton.setOnClickListener {
            if (binding.loginPasswordEditText.text.toString().isNotBlank()) {
                if (binding.loginPasswordEditText.text.toString() == password) {
                    onResult?.invoke()
                    dismiss()
                } else {
                    binding.loginPasswordInputLayout.error = "Incorrect password"
                }
            } else if (!binding.loginPasswordEditText.text.toString().isNotBlank()) {
                binding.loginPasswordInputLayout.error = "Enter password"
            }
        }
    }
}
