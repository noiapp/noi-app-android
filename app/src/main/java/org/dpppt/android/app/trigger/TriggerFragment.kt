/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.trigger

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import kotlinx.android.synthetic.main.fragment_trigger.*
import org.dpppt.android.app.MainApplication.Companion.getAppComponent
import org.dpppt.android.app.R
import org.dpppt.android.app.sdk.DP3T
import org.dpppt.android.app.trigger.views.ChainedEditText.ChainedEditTextListener
import org.dpppt.android.app.util.InfoDialog
import org.dpppt.android.sdk.internal.backend.ResponseException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.GregorianCalendar
import javax.inject.Inject

class TriggerFragment : Fragment(R.layout.fragment_trigger) {

    @Inject
    lateinit var DP3T: DP3T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent(context).inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trigger_fragment_input.addTextChangedListener(object : ChainedEditTextListener {
            override fun onTextChanged(input: String) {
                trigger_fragment_button_trigger.isEnabled = input.matches(REGEX_CODE_PATTERN)
            }

            override fun onEditorSendAction() {
                if (trigger_fragment_button_trigger.isEnabled) trigger_fragment_button_trigger.callOnClick()
            }
        })

        trigger_fragment_button_trigger.setOnClickListener {
            // TODO: HARDCODED ONSET DATE - NOT FINAL
            val calendarNow: Calendar = GregorianCalendar()
            calendarNow.add(Calendar.DAY_OF_YEAR, -14)
            val progressDialog = AlertDialog.Builder(requireContext())
                .setView(R.layout.dialog_loading)
                .show()
            val inputBase64 = String(Base64.encode(trigger_fragment_input.text.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP),
                StandardCharsets.UTF_8)


            viewLifecycleOwner.lifecycle.coroutineScope.launchWhenCreated {
                try {
                    DP3T.sendIWasExposed(inputBase64)
                    progressDialog.dismiss()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, ThankYouFragment.newInstance())
                        .addToBackStack(ThankYouFragment::class.java.canonicalName)
                        .commit()
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    val error: String = getString(R.string.unexpected_error_title).replace("{ERROR}",
                        if (e is ResponseException) e.message.orEmpty() else if (e is IOException) e.getLocalizedMessage() else "")
                    InfoDialog.newInstance(error)
                        .show(childFragmentManager, InfoDialog::class.java.canonicalName)
                    e.printStackTrace()
                }
            }
        }

        cancel_button.setOnClickListener { requireActivity().onBackPressed() }
        trigger_fragment_no_code_button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, NoCodeFragment.newInstance())
                .addToBackStack(NoCodeFragment::class.java.canonicalName)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        trigger_fragment_input.requestFocus()
    }

    companion object {
        private val REGEX_CODE_PATTERN = "\\d{6}".toRegex()

        @JvmStatic
        fun newInstance(): TriggerFragment {
            return TriggerFragment()
        }
    }
}