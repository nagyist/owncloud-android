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

package com.owncloud.android.data.links.repository

import com.owncloud.android.data.links.datasources.RemoteLinksDataSource
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_SPACE_PROJECT_WITH_IMAGE
import com.owncloud.android.testutil.SPACE_MEMBERS
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class OCLinksRepositoryTest {

    private val remoteLinksDataSource = mockk<RemoteLinksDataSource>()
    private val ocLinksRepository = OCLinksRepository(remoteLinksDataSource)

    private val password = "testPasswordForPublicLink"

    @Test
    fun `addLink adds a public link over a space correctly`() {
        every {
            remoteLinksDataSource.addLink(
                accountName = OC_ACCOUNT_NAME,
                spaceId = OC_SPACE_PROJECT_WITH_IMAGE.id,
                displayName = SPACE_MEMBERS.links[0].displayName,
                type = SPACE_MEMBERS.links[0].type,
                expirationDate = SPACE_MEMBERS.links[0].expirationDateTime,
                password = password
            )
        } returns Unit

        ocLinksRepository.addLink(
            accountName = OC_ACCOUNT_NAME,
            spaceId = OC_SPACE_PROJECT_WITH_IMAGE.id,
            displayName = SPACE_MEMBERS.links[0].displayName,
            type = SPACE_MEMBERS.links[0].type,
            expirationDate = SPACE_MEMBERS.links[0].expirationDateTime,
            password = password
        )

        verify(exactly = 1) {
            remoteLinksDataSource.addLink(
                accountName = OC_ACCOUNT_NAME,
                spaceId = OC_SPACE_PROJECT_WITH_IMAGE.id,
                displayName = SPACE_MEMBERS.links[0].displayName,
                type = SPACE_MEMBERS.links[0].type,
                expirationDate = SPACE_MEMBERS.links[0].expirationDateTime,
                password = password
            )
        }
    }
}
