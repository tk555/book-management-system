package com.example.demo.repository

import com.example.demo.domain.Author
import com.example.demo.domain.Book
import com.example.demo.jooq.tables.Authors.Companion.AUTHORS
import com.example.demo.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.demo.jooq.tables.Books.Companion.BOOKS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BookAuthorRepository {
    fun insertAssociations(
        ctx: DSLContext,
        bookId: UUID,
        authorIds: Set<UUID>,
    ) {
        if (authorIds.isEmpty()) return

        val batch =
            authorIds.map { authorId ->
                ctx
                    .insertInto(BOOK_AUTHORS)
                    .set(BOOK_AUTHORS.BOOK_ID, bookId)
                    .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
            }
        ctx.batch(batch).execute()
    }

    fun deleteByBookId(
        ctx: DSLContext,
        bookId: UUID,
    ) {
        ctx
            .deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
    }

    fun replaceAssociations(
        ctx: DSLContext,
        bookId: UUID,
        authorIds: Set<UUID>,
    ) {
        deleteByBookId(ctx, bookId)
        insertAssociations(ctx, bookId, authorIds)
    }

    fun findAuthorIdsByBookId(
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

    fun findAuthorsByBookId(
        ctx: DSLContext,
        bookId: UUID,
    ): List<Author> = findAuthorsWithTimestampsByBookId(ctx, bookId).map { it.author }

    fun findAuthorsWithTimestampsByBookId(
        ctx: DSLContext,
        bookId: UUID,
    ): List<AuthorWithTimestamps> =
        ctx
            .select(AUTHORS.asterisk())
            .from(AUTHORS)
            .join(BOOK_AUTHORS)
            .on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetch()
            .map { it.toAuthorWithTimestamps() }

    fun findAuthorsWithTimestampsByBookIds(
        ctx: DSLContext,
        bookIds: Set<UUID>,
    ): Map<UUID, List<AuthorWithTimestamps>> {
        if (bookIds.isEmpty()) return emptyMap()

        return ctx
            .select(AUTHORS.asterisk(), BOOK_AUTHORS.BOOK_ID)
            .from(AUTHORS)
            .join(BOOK_AUTHORS)
            .on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
            .fetch()
            .map { it.get(BOOK_AUTHORS.BOOK_ID)!! to it.toAuthorWithTimestamps() }
            .groupBy({ it.first }, { it.second })
    }

    fun findBooksByAuthorId(
        ctx: DSLContext,
        authorId: UUID,
    ): List<Book> = findBooksWithTimestampsByAuthorId(ctx, authorId).map { it.book }

    fun findBooksWithTimestampsByAuthorId(
        ctx: DSLContext,
        authorId: UUID,
    ): List<BookWithTimestamps> {
        val bookIds =
            ctx
                .select(BOOK_AUTHORS.BOOK_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
                .fetch()
                .map { it.value1()!! }
                .toSet()

        if (bookIds.isEmpty()) return emptyList()

        val bookRecords =
            ctx
                .selectFrom(BOOKS)
                .where(BOOKS.ID.`in`(bookIds))
                .fetch()

        val authorIdsByBookId =
            ctx
                .select(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
                .fetchGroups(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)

        return bookRecords.map { record ->
            val authorIds = authorIdsByBookId[record.id]?.map { it!! }?.toSet() ?: emptySet()
            record.toBookWithTimestamps(authorIds)
        }
    }
}
