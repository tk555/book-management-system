package com.example.demo.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthorTest {
    @Test
    fun `正常に著者を作成できる`() {
        val author =
            Author.create(
                name = "夏目漱石",
                dateOfBirth = LocalDate.of(1867, 2, 9),
            )

        assertNotNull(author.id)
        assertEquals("夏目漱石", author.name)
        assertEquals(LocalDate.of(1867, 2, 9), author.dateOfBirth)
    }

    @Test
    fun `名前が空白の場合はエラー`() {
        assertThrows<IllegalArgumentException> {
            Author.create(
                name = "",
                dateOfBirth = LocalDate.of(2000, 1, 1),
            )
        }
    }

    @Test
    fun `名前が200文字を超える場合はエラー`() {
        val longName = "a".repeat(201)
        assertThrows<IllegalArgumentException> {
            Author.create(
                name = longName,
                dateOfBirth = LocalDate.of(2000, 1, 1),
            )
        }
    }

    @Test
    fun `著者情報を更新できる`() {
        val author =
            Author.create(
                name = "Original Name",
                dateOfBirth = LocalDate.of(2000, 1, 1),
            )

        val updated =
            author.update(
                name = "Updated Name",
                dateOfBirth = LocalDate.of(2000, 12, 31),
            )

        assertEquals(author.id, updated.id)
        assertEquals("Updated Name", updated.name)
        assertEquals(LocalDate.of(2000, 12, 31), updated.dateOfBirth)
    }

    @Test
    fun `生年月日が未来の場合はエラー`() {
        val fixedDate = LocalDate.of(2024, 6, 15)
        val clock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        assertThrows<IllegalArgumentException> {
            Author.create(
                name = "テスト著者",
                dateOfBirth = LocalDate.of(2024, 6, 16),
                clock = clock,
            )
        }.also {
            assertEquals("生年月日は現在日以前にしてください", it.message)
        }
    }

    @Test
    fun `生年月日が今日の場合は正常に作成できる`() {
        val fixedDate = LocalDate.of(2024, 6, 15)
        val clock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val author =
            Author.create(
                name = "テスト著者",
                dateOfBirth = fixedDate,
                clock = clock,
            )

        assertEquals(fixedDate, author.dateOfBirth)
    }

    @Test
    fun `更新時に生年月日が未来の場合はエラー`() {
        val fixedDate = LocalDate.of(2024, 6, 15)
        val clock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val author =
            Author.create(
                name = "テスト著者",
                dateOfBirth = LocalDate.of(2000, 1, 1),
                clock = clock,
            )

        assertThrows<IllegalArgumentException> {
            author.update(
                name = "更新後の名前",
                dateOfBirth = LocalDate.of(2024, 6, 16),
                clock = clock,
            )
        }.also {
            assertEquals("生年月日は現在日以前にしてください", it.message)
        }
    }
}
