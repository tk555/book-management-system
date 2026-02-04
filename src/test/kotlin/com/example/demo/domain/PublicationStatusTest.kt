package com.example.demo.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicationStatusTest {
    @Test
    fun `未出版から未出版への遷移が可能`() {
        val status = PublicationStatus.UNPUBLISHED
        assertTrue(status.canTransitionTo(PublicationStatus.UNPUBLISHED))
    }

    @Test
    fun `未出版から出版済みへの遷移が可能`() {
        val status = PublicationStatus.UNPUBLISHED
        assertTrue(status.canTransitionTo(PublicationStatus.PUBLISHED))
    }

    @Test
    fun `出版済みから出版済みへの遷移が可能`() {
        val status = PublicationStatus.PUBLISHED
        assertTrue(status.canTransitionTo(PublicationStatus.PUBLISHED))
    }

    @Test
    fun `出版済みから未出版への遷移が不可能`() {
        val status = PublicationStatus.PUBLISHED
        assertFalse(status.canTransitionTo(PublicationStatus.UNPUBLISHED))
    }
}
