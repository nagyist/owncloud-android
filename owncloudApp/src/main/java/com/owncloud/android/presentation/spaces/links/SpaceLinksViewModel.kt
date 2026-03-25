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
import com.owncloud.android.domain.links.usecases.AddLinkUseCase
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.ViewModelExt.runUseCaseWithResult
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SpaceLinksViewModel(
    private val addLinkUseCase: AddLinkUseCase,
    private val accountName: String,
    private val space: OCSpace,
    private val coroutineDispatcherProvider: CoroutinesDispatcherProvider
): ViewModel() {

    private val _addPublicLinkUIState = MutableStateFlow<AddPublicLinkUIState?>(null)
    val addPublicLinkUIState: StateFlow<AddPublicLinkUIState?> = _addPublicLinkUIState

    private val _addLinkResultFlow = MutableStateFlow<Event<UIResult<Unit>>?>(null)
    val addLinkResultFlow: StateFlow<Event<UIResult<Unit>>?> = _addLinkResultFlow

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

    fun createPublicLink(displayName: String, permission: OCLinkType, expirationDate: String?, password: String?) {
        runUseCaseWithResult(
            coroutineDispatcher = coroutineDispatcherProvider.io,
            flow = _addLinkResultFlow,
            useCase = addLinkUseCase,
            useCaseParams = AddLinkUseCase.Params(
                accountName = accountName,
                spaceId = space.id,
                displayName = displayName,
                type = OCLinkType.toString(permission),
                expirationDate = expirationDate,
                password = password
            )
        )
    }

    data class AddPublicLinkUIState(
        val selectedPermission: OCLinkType? = null,
        val selectedExpirationDate: String? = null,
        val selectedPassword: String? = null
    )
}
