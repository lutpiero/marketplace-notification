package com.marketplace.notification.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.marketplace.notification.R
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.ActionType

class ActionDialogFragment : DialogFragment() {

    private var existingConfig: ActionConfig? = null
    private var onSave: ((ActionConfig) -> Unit)? = null

    private val tag = "ActionDialogFragment"

    // Form fields
    private lateinit var etName: EditText
    private lateinit var spType: MaterialAutoCompleteTextView
    private lateinit var layoutApi: LinearLayout
    private lateinit var layoutScp: LinearLayout
    private lateinit var layoutEmail: LinearLayout
    private lateinit var layoutWhatsapp: LinearLayout

    // API fields
    private lateinit var etApiUrl: EditText
    private lateinit var etApiMethod: EditText
    private lateinit var etApiHeaders: EditText
    private lateinit var etApiBodyTemplate: EditText

    // SCP fields
    private lateinit var etScpHost: EditText
    private lateinit var etScpPort: EditText
    private lateinit var etScpUsername: EditText
    private lateinit var etScpPassword: EditText
    private lateinit var etScpPath: EditText

    // Email fields
    private lateinit var etSmtpHost: EditText
    private lateinit var etSmtpPort: EditText
    private lateinit var etEmailUser: EditText
    private lateinit var etEmailPass: EditText
    private lateinit var etEmailFrom: EditText
    private lateinit var etEmailTo: EditText
    private lateinit var etEmailSubject: EditText

    // WhatsApp fields
    private lateinit var etWaRecipient: EditText
    private lateinit var etWaApiUrl: EditText
    private lateinit var etWaApiKey: EditText

    companion object {
        private val ACTION_TYPE_DISPLAY_NAMES = mapOf(
            ActionType.API_REQUEST to "API Request",
            ActionType.SCP_FILE to "SCP File",
            ActionType.EMAIL to "Email",
            ActionType.WHATSAPP to "WhatsApp"
        )

        private val DISPLAY_NAMES_TO_ACTION_TYPE = ACTION_TYPE_DISPLAY_NAMES
            .entries
            .associateBy({ it.value }, { it.key })

        fun newInstance(
            config: ActionConfig? = null,
            onSave: (ActionConfig) -> Unit
        ): ActionDialogFragment {
            return ActionDialogFragment().apply {
                this.existingConfig = config
                this.onSave = onSave
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_action_config, null)

        bindViews(view)
        setupTypeSpinner()
        existingConfig?.let { populateFields(it) }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (existingConfig == null) R.string.add_action else R.string.edit_action)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ -> saveAction() }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun bindViews(view: View) {
        etName = view.findViewById(R.id.et_action_name)
        spType = view.findViewById(R.id.sp_action_type)
        layoutApi = view.findViewById(R.id.layout_api)
        layoutScp = view.findViewById(R.id.layout_scp)
        layoutEmail = view.findViewById(R.id.layout_email)
        layoutWhatsapp = view.findViewById(R.id.layout_whatsapp)

        etApiUrl = view.findViewById(R.id.et_api_url)
        etApiMethod = view.findViewById(R.id.et_api_method)
        etApiHeaders = view.findViewById(R.id.et_api_headers)
        etApiBodyTemplate = view.findViewById(R.id.et_api_body)

        etScpHost = view.findViewById(R.id.et_scp_host)
        etScpPort = view.findViewById(R.id.et_scp_port)
        etScpUsername = view.findViewById(R.id.et_scp_username)
        etScpPassword = view.findViewById(R.id.et_scp_password)
        etScpPath = view.findViewById(R.id.et_scp_path)

        etSmtpHost = view.findViewById(R.id.et_smtp_host)
        etSmtpPort = view.findViewById(R.id.et_smtp_port)
        etEmailUser = view.findViewById(R.id.et_email_user)
        etEmailPass = view.findViewById(R.id.et_email_pass)
        etEmailFrom = view.findViewById(R.id.et_email_from)
        etEmailTo = view.findViewById(R.id.et_email_to)
        etEmailSubject = view.findViewById(R.id.et_email_subject)

        etWaRecipient = view.findViewById(R.id.et_wa_recipient)
        etWaApiUrl = view.findViewById(R.id.et_wa_api_url)
        etWaApiKey = view.findViewById(R.id.et_wa_api_key)
    }

    private fun setupTypeSpinner() {
        val types = ACTION_TYPE_DISPLAY_NAMES.values.toList()
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            types
        )
        spType.setAdapter(adapter)

        spType.setOnItemClickListener { _, _, _, _ ->
            val selectedType = getActionTypeFromString(spType.text.toString())
            showFieldsForType(selectedType)
        }
    }

    private fun getActionTypeFromString(typeString: String): ActionType {
        val result = DISPLAY_NAMES_TO_ACTION_TYPE[typeString]

        if (result == null) {
            Log.w(tag, "Unknown action type: '$typeString', defaulting to API_REQUEST")
        }

        return result ?: ActionType.API_REQUEST
    }

    private fun getActionTypeDisplayName(type: ActionType): String {
        return ACTION_TYPE_DISPLAY_NAMES[type] ?: "API Request"
    }

    private fun showFieldsForType(type: ActionType) {
        layoutApi.visibility = if (type == ActionType.API_REQUEST) View.VISIBLE else View.GONE
        layoutScp.visibility = if (type == ActionType.SCP_FILE) View.VISIBLE else View.GONE
        layoutEmail.visibility = if (type == ActionType.EMAIL) View.VISIBLE else View.GONE
        layoutWhatsapp.visibility = if (type == ActionType.WHATSAPP) View.VISIBLE else View.GONE
    }

    private fun populateFields(config: ActionConfig) {
        etName.setText(config.name)
        spType.setText(getActionTypeDisplayName(config.type), false)
        showFieldsForType(config.type)

        etApiUrl.setText(config.apiUrl)
        etApiMethod.setText(config.apiMethod)
        etApiHeaders.setText(config.apiHeaders)
        etApiBodyTemplate.setText(config.apiBodyTemplate)

        etScpHost.setText(config.scpHost)
        etScpPort.setText(config.scpPort.toString())
        etScpUsername.setText(config.scpUsername)
        etScpPassword.setText(config.scpPassword)
        etScpPath.setText(config.scpRemotePath)

        etSmtpHost.setText(config.emailSmtpHost)
        etSmtpPort.setText(config.emailSmtpPort.toString())
        etEmailUser.setText(config.emailUsername)
        etEmailPass.setText(config.emailPassword)
        etEmailFrom.setText(config.emailFrom)
        etEmailTo.setText(config.emailTo)
        etEmailSubject.setText(config.emailSubject)

        etWaRecipient.setText(config.whatsappRecipient)
        etWaApiUrl.setText(config.whatsappApiUrl)
        etWaApiKey.setText(config.whatsappApiKey)
    }

    private fun saveAction() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) return

        val type = getActionTypeFromString(spType.text.toString())

        val config = ActionConfig(
            id = existingConfig?.id ?: 0L,
            name = name,
            type = type,
            enabled = existingConfig?.enabled ?: true,
            apiUrl = etApiUrl.text.toString().trim(),
            apiMethod = etApiMethod.text.toString().trim().ifEmpty { "POST" },
            apiHeaders = etApiHeaders.text.toString().trim().ifEmpty { "{}" },
            apiBodyTemplate = etApiBodyTemplate.text.toString().trim(),
            scpHost = etScpHost.text.toString().trim(),
            scpPort = etScpPort.text.toString().toIntOrNull() ?: 22,
            scpUsername = etScpUsername.text.toString().trim(),
            scpPassword = etScpPassword.text.toString(),
            scpRemotePath = etScpPath.text.toString().trim().ifEmpty { "/tmp/notifications" },
            emailSmtpHost = etSmtpHost.text.toString().trim(),
            emailSmtpPort = etSmtpPort.text.toString().toIntOrNull() ?: 587,
            emailUsername = etEmailUser.text.toString().trim(),
            emailPassword = etEmailPass.text.toString(),
            emailFrom = etEmailFrom.text.toString().trim(),
            emailTo = etEmailTo.text.toString().trim(),
            emailSubject = etEmailSubject.text.toString().trim()
                .ifEmpty { "Marketplace Notification Alert" },
            whatsappRecipient = etWaRecipient.text.toString().trim(),
            whatsappApiUrl = etWaApiUrl.text.toString().trim(),
            whatsappApiKey = etWaApiKey.text.toString().trim()
        )

        onSave?.invoke(config)
    }
}
