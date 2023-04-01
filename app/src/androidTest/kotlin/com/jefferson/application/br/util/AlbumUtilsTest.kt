package com.jefferson.application.br.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.jefferson.application.br.database.AlbumDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AlbumUtilsTest {
    private lateinit var database: AlbumDatabase;

    @Before
    fun setup() {
        database = AlbumDatabase.getInstance(
            context = ApplicationProvider.getApplicationContext()
        )
    }

    @Test
    fun testInsertAnDeleteMedia() {
        val id = StringUtils.getRandomString(9)
        val insertResult = database.insertMediaData(id, "Test Name", 100)
        assertWithMessage("Assert INSERT with: result != -1")
            .that(insertResult).isNotEqualTo(-1)
        assertWithMessage("Assert INSERT with database.mediaExists == true")
            .that(database.mediaIdExists(id)).isTrue()

        val deleteResult = database.deleteMediaData(id)
        assertWithMessage("Assert DELETE with result == true").that(deleteResult).isTrue()
        assertWithMessage("Assert DELETE with database.mediaIdExists == false")
            .that(database.mediaIdExists(id)).isFalse()
    }

    @After
    fun tearDown() {
        database.close()
    }
}