package com.example.demo.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BookTest {
    @Test
    fun `正常に書籍を作成できる`() {
        val authorId = UUID.randomUUID()
        val book =
            Book.create(
                title = "吾輩は猫である",
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(authorId),
            )

        assertNotNull(book.id)
        assertEquals("吾輩は猫である", book.title)
        assertEquals(500, book.price)
        assertEquals(PublicationStatus.UNPUBLISHED, book.publicationStatus)
        assertEquals(setOf(authorId), book.authorIds)
    }

    @Test
    fun `複数の著者を持つ書籍を作成できる`() {
        val authorId1 = UUID.randomUUID()
        val authorId2 = UUID.randomUUID()
        val book =
            Book.create(
                title = "共著書",
                price = 1000,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = setOf(authorId1, authorId2),
            )

        assertEquals(2, book.authorIds.size)
    }

    @Test
    fun `タイトルが空白の場合はエラー`() {
        assertThrows<IllegalArgumentException> {
            Book.create(
                title = "",
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(UUID.randomUUID()),
            )
        }
    }

    @Test
    fun `タイトルが400文字を超える場合はエラー`() {
        val longTitle = "a".repeat(401)
        assertThrows<IllegalArgumentException> {
            Book.create(
                title = longTitle,
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(UUID.randomUUID()),
            )
        }
    }

    @Test
    fun `価格が負の場合はエラー`() {
        assertThrows<IllegalArgumentException> {
            Book.create(
                title = "Test Book",
                price = -1,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(UUID.randomUUID()),
            )
        }
    }

    @Test
    fun `価格が0の場合は許可される`() {
        val book =
            Book.create(
                title = "Free Book",
                price = 0,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(UUID.randomUUID()),
            )

        assertEquals(0, book.price)
    }

    @Test
    fun `著者が0人の場合はエラー`() {
        assertThrows<IllegalArgumentException> {
            Book.create(
                title = "Test Book",
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = emptySet(),
            )
        }
    }

    @Test
    fun `未出版の書籍を出版済みに更新できる`() {
        val book =
            Book.create(
                title = "Test Book",
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(UUID.randomUUID()),
            )

        val updated =
            book.update(
                title = "Test Book",
                price = 500,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = book.authorIds,
            )

        assertEquals(PublicationStatus.PUBLISHED, updated.publicationStatus)
    }

    @Test
    fun `出版済みの書籍を未出版に変更しようとするとエラー`() {
        val book =
            Book.create(
                title = "Test Book",
                price = 500,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = setOf(UUID.randomUUID()),
            )

        assertThrows<IllegalArgumentException> {
            book.update(
                title = "Test Book",
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = book.authorIds,
            )
        }
    }

    @Test
    fun `書籍情報を更新できる`() {
        val authorId1 = UUID.randomUUID()
        val authorId2 = UUID.randomUUID()
        val book =
            Book.create(
                title = "Original Title",
                price = 500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = setOf(authorId1),
            )

        val updated =
            book.update(
                title = "Updated Title",
                price = 1000,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = setOf(authorId1, authorId2),
            )

        assertEquals(book.id, updated.id)
        assertEquals("Updated Title", updated.title)
        assertEquals(1000, updated.price)
        assertEquals(PublicationStatus.PUBLISHED, updated.publicationStatus)
        assertEquals(setOf(authorId1, authorId2), updated.authorIds)
    }
}
