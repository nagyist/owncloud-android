/**
 * ownCloud Android client application
 *
 * @author Jorge Aguado Recio
 *
 * Copyright (C) 2026 ownCloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.presentation.spaces.links

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.owncloud.android.R
import com.owncloud.android.databinding.AddPublicLinkFragmentBinding
import com.owncloud.android.domain.capabilities.model.CapabilityBooleanType
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.links.model.OCLinkType
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.extensions.collectLatestLifecycleFlow
import com.owncloud.android.presentation.capabilities.CapabilityViewModel
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.utils.DisplayUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AddPublicLinkFragment: Fragment(), SetPasswordDialogFragment.SetPasswordListener {
    private var _binding: AddPublicLinkFragmentBinding? = null
    private val binding get() = _binding!!

    private val spaceLinksViewModel: SpaceLinksViewModel by viewModel {
        parametersOf(
            requireArguments().getString(ARG_ACCOUNT_NAME),
            requireArguments().getParcelable(ARG_CURRENT_SPACE)
        )
    }
    private val capabilityViewModel: CapabilityViewModel by viewModel {
        parametersOf(
            requireArguments().getString(ARG_ACCOUNT_NAME)
        )
    }

    private var capabilities: OCCapability? = null
    private var isPasswordEnforced = true
    private var hasPassword = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = AddPublicLinkFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setTitle(R.string.public_link_create_title)

        binding.publicLinkPermissions.apply {
            canViewPublicLinkRadioButton.tag = OCLinkType.CAN_VIEW
            canEditPublicLinkRadioButton.tag = OCLinkType.CAN_EDIT
            secretFileDropPublicLinkRadioButton.tag = OCLinkType.CREATE_ONLY
        }

        collectLatestLifecycleFlow(spaceLinksViewModel.addPublicLinkUIState) { uiState ->
            uiState?.let {
                it.selectedExpirationDate?.let { expirationDate ->
                    binding.expirationDateLayout.expirationDateValue.apply {
                        visibility = View.VISIBLE
                        text = DisplayUtils.displayDateToHumanReadable(expirationDate)
                    }
                }

                hasPassword = it.selectedPassword != null
                it.selectedPermission?.let { selectedPermission ->
                    binding.optionsLayout.isVisible = true
                    binding.passwordLayout.apply {
                        passwordValue.isVisible = hasPassword
                        setPasswordButton.isVisible = !hasPassword && isPasswordEnforced
                        removePasswordButton.isVisible = hasPassword && isPasswordEnforced
                        setPasswordSwitch.isVisible = !isPasswordEnforced
                        setPasswordSwitch.isChecked = hasPassword
                    }
                    binding.createPublicLinkButton.isEnabled = isPasswordEnforced && hasPassword || !isPasswordEnforced

                    binding.createPublicLinkButton.setOnClickListener {
                        spaceLinksViewModel.createPublicLink(
                            binding.publicLinkNameEditText.text.toString().ifEmpty { getString(R.string.public_link_default_display_name) },
                            selectedPermission,
                            uiState.selectedExpirationDate,
                            uiState.selectedPassword,
                        )
                    }
                }

                bindDatePickerDialog(uiState.selectedExpirationDate)

                binding.expirationDateLayout.apply {
                    expirationDateLayout.setOnClickListener {
                        if (uiState.selectedExpirationDate != null) {
                            openDatePickerDialog(uiState.selectedExpirationDate)
                        } else {
                            expirationDateSwitch.isChecked = true
                        }
                    }
                }

                binding.passwordLayout.apply {
                    passwordLayout.setOnClickListener {
                        if (!isPasswordEnforced){
                            setPasswordSwitch.isChecked = true
                        }
                        showPasswordDialog(uiState.selectedPassword)
                    }
                }
            }
        }

        capabilityViewModel.capabilities.observe(viewLifecycleOwner) { event->
            when (val uiResult = event.peekContent()) {
                is UIResult.Success -> {
                    capabilities = uiResult.data
                }
                is UIResult.Loading -> { }
                is UIResult.Error -> {
                    Timber.e(uiResult.error, "Failed to retrieve server capabilities")
                }
            }
        }

        binding.publicLinkPermissions.apply {
            canViewPublicLinkRadioButton.setOnClickListener { selectRadioButton(canViewPublicLinkRadioButton) }
            canViewPublicLinkLayout.setOnClickListener { selectRadioButton(canViewPublicLinkRadioButton) }
            canEditPublicLinkRadioButton.setOnClickListener { selectRadioButton(canEditPublicLinkRadioButton) }
            canEditPublicLinkLayout.setOnClickListener { selectRadioButton(canEditPublicLinkRadioButton) }
            secretFileDropPublicLinkRadioButton.setOnClickListener { selectRadioButton(secretFileDropPublicLinkRadioButton) }
            secretFileDropPublicLinkLayout.setOnClickListener { selectRadioButton(secretFileDropPublicLinkRadioButton) }
        }

        binding.passwordLayout.apply {
            setPasswordButton.setOnClickListener {
                showPasswordDialog()
            }
            removePasswordButton.setOnClickListener {
                removePassword()
            }
            setPasswordSwitch.setOnClickListener {
                if (setPasswordSwitch.isChecked) showPasswordDialog() else removePassword()
            }
        }
    }

    override fun onCancelPassword() {
        if (!isPasswordEnforced && !hasPassword) {
            binding.passwordLayout.setPasswordSwitch.isChecked = false
        }
    }

    override fun onSetPassword(password: String) {
        spaceLinksViewModel.onPasswordSelected(password)
    }

    private fun selectRadioButton(selectedRadioButton: RadioButton) {
        binding.publicLinkPermissions.apply {
            canViewPublicLinkRadioButton.isChecked = false
            canEditPublicLinkRadioButton.isChecked = false
            secretFileDropPublicLinkRadioButton.isChecked = false
            selectedRadioButton.isChecked = true
        }
        val selectedPermission = selectedRadioButton.tag as OCLinkType
        checkPasswordEnforced(selectedPermission)
        spaceLinksViewModel.onPermissionSelected(selectedPermission)
    }

    private fun checkPasswordEnforced(selectedPermission: OCLinkType) {
        isPasswordEnforced = when (selectedPermission) {
            OCLinkType.CAN_VIEW -> {
                capabilities?.filesSharingPublicPasswordEnforcedReadOnly == CapabilityBooleanType.TRUE
            }
            OCLinkType.CAN_EDIT -> {
                capabilities?.filesSharingPublicPasswordEnforcedReadWrite == CapabilityBooleanType.TRUE
            }
            OCLinkType.CREATE_ONLY -> {
                capabilities?.filesSharingPublicPasswordEnforcedUploadOnly == CapabilityBooleanType.TRUE
            }
            else -> {
                true
            }
        }
    }

    private fun bindDatePickerDialog(expirationDate: String?) {
        binding.expirationDateLayout.expirationDateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                openDatePickerDialog(expirationDate)
            } else {
                binding.expirationDateLayout.expirationDateValue.visibility = View.GONE
                spaceLinksViewModel.onExpirationDateSelected(null)
            }
        }
    }

    private fun openDatePickerDialog(expirationDate: String?) {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(DisplayUtils.DATE_FORMAT_ISO, Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        expirationDate?.let {
            calendar.time = formatter.parse(it)
        }

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val isoExpirationDate = formatter.format(calendar.time)
                spaceLinksViewModel.onExpirationDateSelected(isoExpirationDate)
                binding.expirationDateLayout.expirationDateValue.apply {
                    visibility = View.VISIBLE
                    text = DisplayUtils.displayDateToHumanReadable(isoExpirationDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = Calendar.getInstance().timeInMillis
            show()
            setOnCancelListener {
                if (expirationDate == null) {
                    binding.expirationDateLayout.expirationDateSwitch.isChecked = false
                }
            }
        }
    }

    private fun showPasswordDialog(password: String? = null) {
        val accountName = requireArguments().getString(ARG_ACCOUNT_NAME) ?: return
        val dialog = SetPasswordDialogFragment.newInstance(accountName, password, this)
        dialog.show(parentFragmentManager, DIALOG_SET_PASSWORD)
    }

    private fun removePassword() {
        spaceLinksViewModel.onPasswordSelected(null)
    }

    companion object {
        private const val DIALOG_SET_PASSWORD = "DIALOG_SET_PASSWORD"
        private const val ARG_ACCOUNT_NAME = "ARG_ACCOUNT_NAME"
        private const val ARG_CURRENT_SPACE = "ARG_CURRENT_SPACE"

        fun newInstance(
            accountName: String,
            currentSpace: OCSpace
        ): AddPublicLinkFragment {
            val args = Bundle().apply {
                putString(ARG_ACCOUNT_NAME, accountName)
                putParcelable(ARG_CURRENT_SPACE, currentSpace)
            }
            return AddPublicLinkFragment().apply {
                arguments = args
            }
        }
    }
}
