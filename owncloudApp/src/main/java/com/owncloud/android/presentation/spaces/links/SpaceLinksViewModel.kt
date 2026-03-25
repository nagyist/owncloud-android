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

import androidx.lifecycle.ViewModel
import com.owncloud.android.domain.links.model.OCLinkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SpaceLinksViewModel: ViewModel() {

    private val _addPublicLinkUIState = MutableStateFlow<AddPublicLinkUIState?>(null)
    val addPublicLinkUIState: StateFlow<AddPublicLinkUIState?> = _addPublicLinkUIState

    init {
        _addPublicLinkUIState.value = AddPublicLinkUIState()
    }

    fun onPermissionSelected(permission: OCLinkType) {
        _addPublicLinkUIState.update { it?.copy(selectedPermission = permission) }
    }

    fun onExpirationDateSelected(expirationDate: String?) {
        _addPublicLinkUIState.update { it?.copy(selectedExpirationDate = expirationDate) }
    }

    fun onPasswordSelected(password: String?) {
        _addPublicLinkUIState.update { it?.copy(selectedPassword = password) }
    }

    data class AddPublicLinkUIState(
        val selectedPermission: OCLinkType? = null,
        val selectedExpirationDate: String? = null,
        val selectedPassword: String? = null
    )
}
