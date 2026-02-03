package com.example.demo.repository

import com.example.demo.domain.Book
import com.example.demo.infrastructure.converter.PublicationStatusConverter
import com.example.demo.jooq.tables.Books.Companion.BOOKS
import com.example.demo.jooq.tables.records.BooksRecord
import org.jooq.Record
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class BookWithTimestamps(
    val book: Book,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

internal fun BooksRecord.toDomain(authorIds: Set<UUID>): Book =
    Book(
        id = id!!,
        title = title,
        price = price,
        publicationStatus = PublicationStatusConverter.fromDbValue(publicationStatus),
        authorIds = authorIds,
    )

internal fun BooksRecord.toBookWithTimestamps(authorIds: Set<UUID>): BookWithTimestamps =
    BookWithTimestamps(
        book = toDomain(authorIds),
        createdAt = createdAt!!.atOffset(ZoneOffset.UTC),
        updatedAt = updatedAt!!.atOffset(ZoneOffset.UTC),
    )

internal fun Record.toBookWithTimestamps(authorIds: Set<UUID>): BookWithTimestamps =
    BookWithTimestamps(
        book =
            Book(
                id = get(BOOKS.ID)!!,
                title = get(BOOKS.TITLE)!!,
                price = get(BOOKS.PRICE)!!,
                publicationStatus = PublicationStatusConverter.fromDbValue(get(BOOKS.PUBLICATION_STATUS)!!),
                authorIds = authorIds,
            ),
        createdAt = get(BOOKS.CREATED_AT)!!.atOffset(ZoneOffset.UTC),
        updatedAt = get(BOOKS.UPDATED_AT)!!.atOffset(ZoneOffset.UTC),
    )
