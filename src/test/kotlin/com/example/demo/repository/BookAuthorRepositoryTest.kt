package com.example.demo.repository

import com.example.demo.domain.Author
import com.example.demo.domain.Book
import com.example.demo.domain.PublicationStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookAuthorRepositoryTest : RepositoryTestBase() {
    private lateinit var bookRepository: BookRepository
    private lateinit var authorRepository: AuthorRepository
    private lateinit var bookAuthorRepository: BookAuthorRepository

    private lateinit var author1: Author
    private lateinit var author2: Author
    private lateinit var author3: Author

    @BeforeEach
    fun setUpRepository() {
        bookRepository = BookRepository()
        authorRepository = AuthorRepository()
        bookAuthorRepository = BookAuthorRepository()

        author1 = Author.create(name = "著者1", dateOfBirth = LocalDate.of(1980, 1, 1))
        author2 = Author.create(name = "著者2", dateOfBirth = LocalDate.of(1985, 5, 15))
        author3 = Author.create(name = "著者3", dateOfBirth = LocalDate.of(1990, 10, 20))
        authorRepository.insert(dsl, author1)
        authorRepository.insert(dsl, author2)
        authorRepository.insert(dsl, author3)
    }

    private fun createBook(
        title: String,
        authorIds: Set<java.util.UUID>,
    ): Book {
        val book =
            Book.create(
                title = title,
                price = 1000,
                publicationStatus = PublicationStatus.Unpublished,
                authorIds = authorIds,
            )
        bookRepository.insert(dsl, book)
        bookAuthorRepository.insertAssociations(dsl, book.id, authorIds)
        return book
    }

    @Test
    fun `書籍と著者の関連を登録できる`() {
        val book =
            Book.create(
                title = "共著本",
                price = 1000,
                publicationStatus = PublicationStatus.Unpublished,
                authorIds = setOf(author1.id, author2.id),
            )
        bookRepository.insert(dsl, book)

        bookAuthorRepository.insertAssociations(dsl, book.id, setOf(author1.id, author2.id))

        val authorIds = bookAuthorRepository.findAuthorIdsByBookId(dsl, book.id)
        assertEquals(setOf(author1.id, author2.id), authorIds)
    }

    @Test
    fun `書籍IDで関連を全削除できる`() {
        val book = createBook("テスト本", setOf(author1.id, author2.id))

        bookAuthorRepository.deleteByBookId(dsl, book.id)

        val authorIds = bookAuthorRepository.findAuthorIdsByBookId(dsl, book.id)
        assertTrue(authorIds.isEmpty())
    }

    @Test
    fun `書籍の著者を置換できる`() {
        val book = createBook("テスト本", setOf(author1.id))

        bookAuthorRepository.replaceAssociations(dsl, book.id, setOf(author2.id, author3.id))

        val authorIds = bookAuthorRepository.findAuthorIdsByBookId(dsl, book.id)
        assertEquals(setOf(author2.id, author3.id), authorIds)
    }

    @Test
    fun `書籍IDから著者の詳細を取得できる`() {
        val book = createBook("共著本", setOf(author1.id, author2.id))

        val authors = bookAuthorRepository.findAuthorsByBookId(dsl, book.id)

        assertEquals(2, authors.size)
        assertTrue(authors.any { it.id == author1.id && it.name == "著者1" })
        assertTrue(authors.any { it.id == author2.id && it.name == "著者2" })
    }

    @Test
    fun `著者IDから書籍一覧を取得できる`() {
        val book1 = createBook("本1", setOf(author1.id))
        val book2 = createBook("本2", setOf(author1.id, author2.id))
        createBook("本3", setOf(author2.id))

        val books = bookAuthorRepository.findBooksByAuthorId(dsl, author1.id)

        assertEquals(2, books.size)
        assertTrue(books.any { it.id == book1.id })
        assertTrue(books.any { it.id == book2.id })
    }

    @Test
    fun `著者IDから取得した書籍には全著者IDが含まれる`() {
        val book = createBook("共著本", setOf(author1.id, author2.id, author3.id))

        val books = bookAuthorRepository.findBooksByAuthorId(dsl, author1.id)

        assertEquals(1, books.size)
        assertEquals(setOf(author1.id, author2.id, author3.id), books[0].authorIds)
    }

    @Test
    fun `書籍を持たない著者で検索すると空リストが返る`() {
        val books = bookAuthorRepository.findBooksByAuthorId(dsl, author3.id)

        assertTrue(books.isEmpty())
    }
}
