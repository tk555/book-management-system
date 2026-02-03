package com.example.demo.service

import com.example.demo.domain.Book
import com.example.demo.domain.PublicationStatus
import com.example.demo.repository.AuthorRepository
import com.example.demo.repository.AuthorWithTimestamps
import com.example.demo.repository.BookAuthorRepository
import com.example.demo.repository.BookRepository
import com.example.demo.repository.BookWithTimestamps
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BookService(
    private val dsl: DSLContext,
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
    private val bookAuthorRepository: BookAuthorRepository,
) {
    data class BookWithAuthors(
        val book: BookWithTimestamps,
        val authors: List<AuthorWithTimestamps>,
    )

    data class PagedBooksWithAuthors(
        val books: List<BookWithAuthors>,
        val totalCount: Long,
    )

    fun createBook(
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
        authorIds: Set<UUID>,
    ): BookWithAuthors =
        dsl.transactionResult { config ->
            val ctx = config.dsl()

            // authors → books の順でロック
            val lockedAuthors = authorRepository.findByIdsForUpdate(ctx, authorIds)
            if (lockedAuthors.size != authorIds.size) {
                throw IllegalArgumentException("指定された著者が見つかりません")
            }

            val book =
                Book.create(
                    title = title,
                    price = price,
                    publicationStatus = publicationStatus,
                    authorIds = authorIds,
                )

            val bookWithTimestamps = bookRepository.insert(ctx, book)
            bookAuthorRepository.insertAssociations(ctx, book.id, authorIds)

            val authors = authorRepository.findByIdsWithTimestamps(ctx, authorIds)
            BookWithAuthors(bookWithTimestamps, authors)
        }

    fun getBook(id: UUID): BookWithTimestamps? = bookRepository.findByIdWithTimestamps(dsl, id)

    fun updateBook(
        id: UUID,
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
        authorIds: Set<UUID>,
    ): BookWithAuthors? =
        dsl.transactionResult { config ->
            val ctx = config.dsl()

            // authors → books の順でロック
            val lockedAuthors = authorRepository.findByIdsForUpdate(ctx, authorIds)
            if (lockedAuthors.size != authorIds.size) {
                throw IllegalArgumentException("指定された著者が見つかりません")
            }

            val existingBook = bookRepository.findByIdForUpdate(ctx, id) ?: return@transactionResult null

            val updatedBook =
                existingBook.update(
                    title = title,
                    price = price,
                    publicationStatus = publicationStatus,
                    authorIds = authorIds,
                )

            val bookWithTimestamps = bookRepository.update(ctx, updatedBook)
            bookAuthorRepository.replaceAssociations(ctx, updatedBook.id, authorIds)

            val authors = authorRepository.findByIdsWithTimestamps(ctx, authorIds)
            BookWithAuthors(bookWithTimestamps, authors)
        }

    fun getBooksByAuthor(authorId: UUID): List<BookWithTimestamps>? {
        val books = bookAuthorRepository.findBooksWithTimestampsByAuthorId(dsl, authorId)
        // 書籍がある場合は著者存在確認不要（1クエリで済む）
        // 書籍が空の場合のみ著者存在確認を行う
        if (books.isEmpty() && !authorRepository.exists(dsl, authorId)) {
            return null
        }
        return books
    }

    fun getBookWithAuthors(id: UUID): BookWithAuthors? {
        val (book, authors) = bookRepository.findByIdWithAuthors(dsl, id) ?: return null
        return BookWithAuthors(book, authors)
    }

    fun getBooksWithAuthors(books: List<BookWithTimestamps>): List<BookWithAuthors> {
        if (books.isEmpty()) return emptyList()

        val bookIds = books.map { it.book.id }.toSet()
        val authorsMap = bookAuthorRepository.findAuthorsWithTimestampsByBookIds(dsl, bookIds)

        return books.map { book ->
            BookWithAuthors(
                book = book,
                authors = authorsMap[book.book.id] ?: emptyList(),
            )
        }
    }

    fun searchBooks(
        title: String?,
        authorName: String?,
        priceFrom: Int?,
        priceTo: Int?,
        publicationStatus: PublicationStatus?,
        page: Int,
        pageSize: Int,
    ): PagedBooksWithAuthors {
        val result =
            bookRepository.search(
                ctx = dsl,
                title = title,
                authorName = authorName,
                priceFrom = priceFrom,
                priceTo = priceTo,
                publicationStatus = publicationStatus,
                page = page,
                pageSize = pageSize,
            )
        return PagedBooksWithAuthors(
            books = getBooksWithAuthors(result.books),
            totalCount = result.totalCount,
        )
    }
}
