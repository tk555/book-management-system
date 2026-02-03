package com.example.demo.repository

import com.example.demo.domain.Author
import com.example.demo.jooq.tables.Authors.Companion.AUTHORS
import com.example.demo.jooq.tables.records.AuthorsRecord
import org.jooq.Record
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class AuthorWithTimestamps(
    val author: Author,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

internal fun AuthorsRecord.toDomain(): Author =
    Author(
        id = id!!,
        name = name,
        dateOfBirth = dateOfBirth,
    )

internal fun AuthorsRecord.toAuthorWithTimestamps(): AuthorWithTimestamps =
    AuthorWithTimestamps(
        author = toDomain(),
        createdAt = createdAt!!.atOffset(ZoneOffset.UTC),
        updatedAt = updatedAt!!.atOffset(ZoneOffset.UTC),
    )

internal fun Record.toAuthorWithTimestamps(): AuthorWithTimestamps =
    AuthorWithTimestamps(
        author =
            Author(
                id = get(AUTHORS.ID)!!,
                name = get(AUTHORS.NAME)!!,
                dateOfBirth = get(AUTHORS.DATE_OF_BIRTH)!!,
            ),
        createdAt = get(AUTHORS.CREATED_AT)!!.atOffset(ZoneOffset.UTC),
        updatedAt = get(AUTHORS.UPDATED_AT)!!.atOffset(ZoneOffset.UTC),
    )
