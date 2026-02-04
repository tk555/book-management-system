package com.example.demo.repository

import com.example.demo.domain.Book
import com.example.demo.domain.PublicationStatus
import com.example.demo.jooq.tables.Authors.Companion.AUTHORS
import com.example.demo.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.demo.jooq.tables.Books.Companion.BOOKS
import com.example.demo.jooq.tables.records.BooksRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BookRepository {
    fun insert(
        ctx: DSLContext,
        book: Book,
    ): BookWithTimestamps {
        val record =
            ctx
                .insertInto(BOOKS)
                .set(BOOKS.ID, book.id)
                .set(BOOKS.TITLE, book.title)
                .set(BOOKS.PRICE, book.price)
                .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                .returning()
                .fetchSingle()

        return record.toBookWithTimestamps(book.authorIds)
    }

    fun findById(
        ctx: DSLContext,
        id: UUID,
    ): Book? {
        val bookRecord = ctx.selectFrom(BOOKS).where(BOOKS.ID.eq(id)).fetchOneInto(BooksRecord::class.java) ?: return null
        val authorIds = fetchAuthorIds(ctx, id)
        return bookRecord.toDomain(authorIds)
    }

    fun findByIdWithTimestamps(
        ctx: DSLContext,
        id: UUID,
    ): BookWithTimestamps? {
        val bookRecord = ctx.selectFrom(BOOKS).where(BOOKS.ID.eq(id)).fetchOne() ?: return null
        val authorIds = fetchAuthorIds(ctx, id)
        return bookRecord.toBookWithTimestamps(authorIds)
    }

    fun update(
        ctx: DSLContext,
        book: Book,
    ): BookWithTimestamps {
        val record =
            ctx
                .update(BOOKS)
                .set(BOOKS.TITLE, book.title)
                .set(BOOKS.PRICE, book.price)
                .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                .where(BOOKS.ID.eq(book.id))
                .returning()
                .fetchSingle()

        return record.toBookWithTimestamps(book.authorIds)
    }

    fun findByIdForUpdate(
        ctx: DSLContext,
        id: UUID,
    ): Book? {
        val bookRecord =
            ctx
                .selectFrom(BOOKS)
                .where(BOOKS.ID.eq(id))
                .forUpdate()
                .fetchOneInto(BooksRecord::class.java) ?: return null
        val authorIds = fetchAuthorIds(ctx, id)
        return bookRecord.toDomain(authorIds)
    }

    fun exists(
        ctx: DSLContext,
        id: UUID,
    ): Boolean =
        ctx.fetchExists(
            ctx
                .selectOne()
                .from(BOOKS)
                .where(BOOKS.ID.eq(id)),
        )

    fun findByIdWithAuthors(
        ctx: DSLContext,
        id: UUID,
    ): Pair<BookWithTimestamps, List<AuthorWithTimestamps>>? {
        val bookRecord = ctx.selectFrom(BOOKS).where(BOOKS.ID.eq(id)).fetchOne() ?: return null
        val authorIds = fetchAuthorIds(ctx, id)
        val authors = fetchAuthorsWithTimestamps(ctx, authorIds)

        return Pair(bookRecord.toBookWithTimestamps(authorIds), authors)
    }

    data class SearchResult(
        val books: List<BookWithTimestamps>,
        val totalCount: Long,
    )

    fun search(
        ctx: DSLContext,
        title: String?,
        authorName: String?,
        priceFrom: Int?,
        priceTo: Int?,
        publicationStatus: PublicationStatus?,
        page: Int,
        pageSize: Int,
    ): SearchResult {
        val conditions =
            listOfNotNull(
                title?.let { BOOKS.TITLE.containsIgnoreCase(it) },
                authorName?.let { AUTHORS.NAME.containsIgnoreCase(it) },
                priceFrom?.let { BOOKS.PRICE.greaterOrEqual(it) },
                priceTo?.let { BOOKS.PRICE.lessOrEqual(it) },
                publicationStatus?.let { BOOKS.PUBLICATION_STATUS.eq(it.name) },
            )

        val from =
            BOOKS
                .leftJoin(BOOK_AUTHORS)
                .on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .leftJoin(AUTHORS)
                .on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))

        val totalCount =
            ctx
                .selectCount()
                .from(
                    ctx
                        .selectDistinct(BOOKS.ID)
                        .from(from)
                        .where(conditions),
                ).fetchSingle()
                .value1()
                .toLong()

        val bookRecords =
            ctx
                .selectDistinct(BOOKS.asterisk())
                .from(from)
                .where(conditions)
                .orderBy(BOOKS.ID)
                .limit(pageSize)
                .offset(page * pageSize)
                .fetchInto(BooksRecord::class.java)

        if (bookRecords.isEmpty()) return SearchResult(emptyList(), totalCount)

        val bookIds = bookRecords.map { it.id!! }.toSet()
        val authorIdsByBookId =
            ctx
                .select(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
                .fetchGroups(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)

        val books =
            bookRecords.map { record ->
                val authorIds = authorIdsByBookId[record.id]?.map { it!! }?.toSet() ?: emptySet()
                record.toBookWithTimestamps(authorIds)
            }

        return SearchResult(books, totalCount)
    }

    private fun fetchAuthorIds(
        ctx: DSLContext,
        bookId: UUID,
    ): Set<UUID> =
        ctx
            .select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetch()
            .map { it.value1()!! }
            .toSet()

    private fun fetchAuthorsWithTimestamps(
        ctx: DSLContext,
        authorIds: Set<UUID>,
    ): List<AuthorWithTimestamps> {
        if (authorIds.isEmpty()) return emptyList()

        return ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(authorIds))
            .fetch()
            .map { it.toAuthorWithTimestamps() }
    }
}
