package com.jefferson.application.br.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jefferson.application.br.R
import com.jefferson.application.br.util.PasswordManager

class PasswdPreviewFragment: Fragment() {
    private var rootView: View? = null
    private lateinit var passwdInput: EditText
    private lateinit var confirmButton: Button
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.main, container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        passwdInput = view.findViewById(R.id.passwordInput)
        confirmButton = view.findViewById(R.id.confirm_button)
        val password = PasswordManager.getTextPassword(requireContext())
        confirmButton.setOnClickListener{
            if (passwdInput.text.equals(password)) {
                //TODO passed
                Toast.makeText(requireContext(), "Passed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}