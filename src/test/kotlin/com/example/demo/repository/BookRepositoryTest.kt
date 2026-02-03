package com.example.demo.repository

import com.example.demo.domain.Author
import com.example.demo.domain.Book
import com.example.demo.domain.PublicationStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookRepositoryTest : RepositoryTestBase() {
    private lateinit var bookRepository: BookRepository
    private lateinit var authorRepository: AuthorRepository
    private lateinit var bookAuthorRepository: BookAuthorRepository

    private lateinit var testAuthor: Author

    @BeforeEach
    fun setUpRepository() {
        bookRepository = BookRepository()
        authorRepository = AuthorRepository()
        bookAuthorRepository = BookAuthorRepository()

        testAuthor =
            Author.create(
                name = "夏目漱石",
                dateOfBirth = LocalDate.of(1867, 2, 9),
            )
        authorRepository.insert(dsl, testAuthor)
    }

    @Test
    fun `書籍を登録するとタイムスタンプ付きで返される`() {
        val book =
            Book.create(
                title = "吾輩は猫である",
                price = 1500,
                publicationStatus = PublicationStatus.Unpublished,
                authorIds = setOf(testAuthor.id),
            )

        val result = bookRepository.insert(dsl, book)

        assertEquals(book.id, result.book.id)
        assertEquals("吾輩は猫である", result.book.title)
        assertEquals(1500, result.book.price)
        assertEquals(PublicationStatus.Unpublished, result.book.publicationStatus)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `存在する書籍を著者ID付きで取得できる`() {
        val book =
            Book.create(
                title = "坊っちゃん",
                price = 2000,
                publicationStatus = PublicationStatus.Published,
                authorIds = setOf(testAuthor.id),
            )
        bookRepository.insert(dsl, book)
        bookAuthorRepository.insertAssociations(dsl, book.id, setOf(testAuthor.id))

        val result = bookRepository.findById(dsl, book.id)

        assertNotNull(result)
        assertEquals(book.id, result.id)
        assertEquals("坊っちゃん", result.title)
        assertEquals(setOf(testAuthor.id), result.authorIds)
    }

    @Test
    fun `存在しないIDで検索するとnullが返る`() {
        val result = bookRepository.findById(dsl, UUID.randomUUID())

        assertNull(result)
    }

    @Test
    fun `書籍情報を更新できる`() {
        val book =
            Book.create(
                title = "旧タイトル",
                price = 1000,
                publicationStatus = PublicationStatus.Unpublished,
                authorIds = setOf(testAuthor.id),
            )
        bookRepository.insert(dsl, book)

        val updatedBook =
            book.update(
                title = "新タイトル",
                price = 1200,
                publicationStatus = PublicationStatus.Published,
                authorIds = setOf(testAuthor.id),
            )
        val result = bookRepository.update(dsl, updatedBook)

        assertEquals("新タイトル", result.book.title)
        assertEquals(1200, result.book.price)
        assertEquals(PublicationStatus.Published, result.book.publicationStatus)
    }

    @Test
    fun `存在する書籍の存在確認はtrueを返す`() {
        val book =
            Book.create(
                title = "こころ",
                price = 500,
                publicationStatus = PublicationStatus.Unpublished,
                authorIds = setOf(testAuthor.id),
            )
        bookRepository.insert(dsl, book)

        assertTrue(bookRepository.exists(dsl, book.id))
    }

    @Test
    fun `存在しない書籍の存在確認はfalseを返す`() {
        assertFalse(bookRepository.exists(dsl, UUID.randomUUID()))
    }
}
