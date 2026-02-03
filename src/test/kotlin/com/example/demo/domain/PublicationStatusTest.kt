package com.example.demo.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicationStatusTest {
    @Test
    fun `未出版から未出版への遷移が可能`() {
        val status = PublicationStatus.Unpublished
        assertTrue(status.canTransitionTo(PublicationStatus.Unpublished))
    }

    @Test
    fun `未出版から出版済みへの遷移が可能`() {
        val status = PublicationStatus.Unpublished
        assertTrue(status.canTransitionTo(PublicationStatus.Published))
    }

    @Test
    fun `出版済みから出版済みへの遷移が可能`() {
        val status = PublicationStatus.Published
        assertTrue(status.canTransitionTo(PublicationStatus.Published))
    }

    @Test
    fun `出版済みから未出版への遷移が不可能`() {
        val status = PublicationStatus.Published
        assertFalse(status.canTransitionTo(PublicationStatus.Unpublished))
    }
}
