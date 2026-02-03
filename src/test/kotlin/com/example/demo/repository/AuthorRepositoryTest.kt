package com.example.demo.repository

import com.example.demo.domain.Author
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthorRepositoryTest : RepositoryTestBase() {
    private lateinit var authorRepository: AuthorRepository

    @BeforeEach
    fun setUpRepository() {
        authorRepository = AuthorRepository()
    }

    @Test
    fun `著者を登録するとタイムスタンプ付きで返される`() {
        val author =
            Author.create(
                name = "夏目漱石",
                dateOfBirth = LocalDate.of(1867, 2, 9),
            )

        val result = authorRepository.insert(dsl, author)

        assertEquals(author.id, result.author.id)
        assertEquals("夏目漱石", result.author.name)
        assertEquals(LocalDate.of(1867, 2, 9), result.author.dateOfBirth)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `存在する著者をIDで取得できる`() {
        val author =
            Author.create(
                name = "芥川龍之介",
                dateOfBirth = LocalDate.of(1892, 3, 1),
            )
        authorRepository.insert(dsl, author)

        val result = authorRepository.findById(dsl, author.id)

        assertNotNull(result)
        assertEquals(author.id, result.id)
        assertEquals("芥川龍之介", result.name)
    }

    @Test
    fun `存在しないIDで検索するとnullが返る`() {
        val result = authorRepository.findById(dsl, UUID.randomUUID())

        assertNull(result)
    }

    @Test
    fun `著者情報を更新できる`() {
        val author =
            Author.create(
                name = "旧名",
                dateOfBirth = LocalDate.of(1980, 3, 10),
            )
        authorRepository.insert(dsl, author)

        val updatedAuthor =
            author.update(
                name = "新名",
                dateOfBirth = LocalDate.of(1980, 3, 11),
            )
        val result = authorRepository.update(dsl, updatedAuthor)

        assertEquals("新名", result.author.name)
        assertEquals(LocalDate.of(1980, 3, 11), result.author.dateOfBirth)
    }

    @Test
    fun `存在する著者の存在確認はtrueを返す`() {
        val author =
            Author.create(
                name = "太宰治",
                dateOfBirth = LocalDate.of(1909, 6, 19),
            )
        authorRepository.insert(dsl, author)

        assertTrue(authorRepository.exists(dsl, author.id))
    }

    @Test
    fun `存在しない著者の存在確認はfalseを返す`() {
        assertFalse(authorRepository.exists(dsl, UUID.randomUUID()))
    }

    @Test
    fun `全員存在する場合existsAllはtrueを返す`() {
        val author1 = Author.create(name = "著者1", dateOfBirth = LocalDate.of(1990, 1, 1))
        val author2 = Author.create(name = "著者2", dateOfBirth = LocalDate.of(1991, 2, 2))
        authorRepository.insert(dsl, author1)
        authorRepository.insert(dsl, author2)

        assertTrue(authorRepository.existsAll(dsl, setOf(author1.id, author2.id)))
    }

    @Test
    fun `一部が存在しない場合existsAllはfalseを返す`() {
        val author = Author.create(name = "著者", dateOfBirth = LocalDate.of(1990, 1, 1))
        authorRepository.insert(dsl, author)

        assertFalse(authorRepository.existsAll(dsl, setOf(author.id, UUID.randomUUID())))
    }

    @Test
    fun `空のセットに対してexistsAllはtrueを返す`() {
        assertTrue(authorRepository.existsAll(dsl, emptySet()))
    }

    @Test
    fun `複数IDで著者を一括取得できる`() {
        val author1 = Author.create(name = "著者1", dateOfBirth = LocalDate.of(1990, 1, 1))
        val author2 = Author.create(name = "著者2", dateOfBirth = LocalDate.of(1991, 2, 2))
        val author3 = Author.create(name = "著者3", dateOfBirth = LocalDate.of(1992, 3, 3))
        authorRepository.insert(dsl, author1)
        authorRepository.insert(dsl, author2)
        authorRepository.insert(dsl, author3)

        val result = authorRepository.findByIds(dsl, setOf(author1.id, author3.id))

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == author1.id })
        assertTrue(result.any { it.id == author3.id })
    }

    @Test
    fun `空のセットで検索すると空リストが返る`() {
        val result = authorRepository.findByIds(dsl, emptySet())

        assertTrue(result.isEmpty())
    }
}
