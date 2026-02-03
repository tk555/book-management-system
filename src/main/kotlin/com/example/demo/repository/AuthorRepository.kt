package com.example.demo.repository

import com.example.demo.domain.Author
import com.example.demo.domain.PublicationStatus
import com.example.demo.infrastructure.converter.PublicationStatusConverter
import com.example.demo.jooq.tables.Authors.Companion.AUTHORS
import com.example.demo.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.demo.jooq.tables.Books.Companion.BOOKS
import com.example.demo.jooq.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class AuthorRepository {
    fun insert(
        ctx: DSLContext,
        author: Author,
    ): AuthorWithTimestamps {
        val record =
            ctx
                .insertInto(AUTHORS)
                .set(AUTHORS.ID, author.id)
                .set(AUTHORS.NAME, author.name)
                .set(AUTHORS.DATE_OF_BIRTH, author.dateOfBirth)
                .returning()
                .fetchSingle()

        return record.toAuthorWithTimestamps()
    }

    fun findById(
        ctx: DSLContext,
        id: UUID,
    ): Author? =
        ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOneInto(AuthorsRecord::class.java)
            ?.toDomain()

    fun findByIdWithTimestamps(
        ctx: DSLContext,
        id: UUID,
    ): AuthorWithTimestamps? =
        ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
            ?.toAuthorWithTimestamps()

    fun update(
        ctx: DSLContext,
        author: Author,
    ): AuthorWithTimestamps {
        val record =
            ctx
                .update(AUTHORS)
                .set(AUTHORS.NAME, author.name)
                .set(AUTHORS.DATE_OF_BIRTH, author.dateOfBirth)
                .where(AUTHORS.ID.eq(author.id))
                .returning()
                .fetchSingle()

        return record.toAuthorWithTimestamps()
    }

    fun exists(
        ctx: DSLContext,
        id: UUID,
    ): Boolean =
        ctx.fetchExists(
            ctx
                .selectOne()
                .from(AUTHORS)
                .where(AUTHORS.ID.eq(id)),
        )

    fun existsAll(
        ctx: DSLContext,
        ids: Set<UUID>,
    ): Boolean {
        if (ids.isEmpty()) return true

        val count =
            ctx
                .selectCount()
                .from(AUTHORS)
                .where(AUTHORS.ID.`in`(ids))
                .fetchSingle()
                .value1()

        return count == ids.size
    }

    fun findByIdForUpdate(
        ctx: DSLContext,
        id: UUID,
    ): Author? =
        ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .forUpdate()
            .fetchOneInto(AuthorsRecord::class.java)
            ?.toDomain()

    fun findByIdsForUpdate(
        ctx: DSLContext,
        ids: Set<UUID>,
    ): List<Author> {
        if (ids.isEmpty()) return emptyList()

        return ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .orderBy(AUTHORS.ID)
            .forUpdate()
            .fetchInto(AuthorsRecord::class.java)
            .map { it.toDomain() }
    }

    fun findByIds(
        ctx: DSLContext,
        ids: Set<UUID>,
    ): List<Author> {
        if (ids.isEmpty()) return emptyList()

        return ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetchInto(AuthorsRecord::class.java)
            .map { it.toDomain() }
    }

    fun findByIdsWithTimestamps(
        ctx: DSLContext,
        ids: Set<UUID>,
    ): List<AuthorWithTimestamps> {
        if (ids.isEmpty()) return emptyList()

        return ctx
            .selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch()
            .map { it.toAuthorWithTimestamps() }
    }

    data class SearchResult(
        val authors: List<AuthorWithTimestamps>,
        val totalCount: Long,
    )

    fun search(
        ctx: DSLContext,
        name: String?,
        dateOfBirthFrom: LocalDate?,
        dateOfBirthTo: LocalDate?,
        bookTitle: String?,
        publicationStatus: PublicationStatus?,
        page: Int,
        pageSize: Int,
    ): SearchResult {
        val conditions =
            listOfNotNull(
                name?.let { AUTHORS.NAME.containsIgnoreCase(it) },
                dateOfBirthFrom?.let { AUTHORS.DATE_OF_BIRTH.greaterOrEqual(it) },
                dateOfBirthTo?.let { AUTHORS.DATE_OF_BIRTH.lessOrEqual(it) },
                bookTitle?.let { BOOKS.TITLE.containsIgnoreCase(it) },
                publicationStatus?.let { BOOKS.PUBLICATION_STATUS.eq(PublicationStatusConverter.toDbValue(it)) },
            )

        val from =
            AUTHORS
                .leftJoin(BOOK_AUTHORS)
                .on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
                .leftJoin(BOOKS)
                .on(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.ID))

        val totalCount =
            ctx
                .selectCount()
                .from(
                    ctx
                        .selectDistinct(AUTHORS.ID)
                        .from(from)
                        .where(conditions),
                ).fetchSingle()
                .value1()
                .toLong()

        val authors =
            ctx
                .selectDistinct(AUTHORS.asterisk())
                .from(from)
                .where(conditions)
                .orderBy(AUTHORS.ID)
                .limit(pageSize)
                .offset(page * pageSize)
                .fetchInto(AuthorsRecord::class.java)
                .map { it.toAuthorWithTimestamps() }

        return SearchResult(authors, totalCount)
    }
}
