package com.jefferson.application.br.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AlbumUtilsTest {

    @Test
    fun `empty album name return false`() {
        val result = AlbumUtils.validateName("", null)
        assertThat(result.ok).isFalse()
    }

    @Test
    fun `album name less than 50 return true`() {
        //text with 49 characters
        val name =  "Lorem ipsum dolor sit amet, consectetur accumsan"
        val result = AlbumUtils.validateName(name, null)
        assertThat(result.ok).isTrue()
    }

    @Test
    fun `album name less than 50 return false`() {
        //text with 51 characters
        val name = "Lorem ipsum dolor sit amet, consectetur vestibulum."
        val result = AlbumUtils.validateName(name, null)
        assertThat(result.ok).isFalse()
    }
}