package com.example.demo.service

import com.example.demo.domain.Author
import com.example.demo.domain.PublicationStatus
import com.example.demo.repository.AuthorRepository
import com.example.demo.repository.AuthorWithTimestamps
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class AuthorService(
    private val dsl: DSLContext,
    private val authorRepository: AuthorRepository,
) {
    data class PagedAuthors(
        val authors: List<AuthorWithTimestamps>,
        val totalCount: Long,
    )

    fun createAuthor(
        name: String,
        dateOfBirth: LocalDate,
    ): AuthorWithTimestamps =
        dsl.transactionResult { config ->
            val author =
                Author.create(
                    name = name,
                    dateOfBirth = dateOfBirth,
                )

            authorRepository.insert(config.dsl(), author)
        }

    fun getAuthor(id: UUID): AuthorWithTimestamps? = authorRepository.findByIdWithTimestamps(dsl, id)

    fun updateAuthor(
        id: UUID,
        name: String,
        dateOfBirth: LocalDate,
    ): AuthorWithTimestamps? =
        dsl.transactionResult { config ->
            val ctx = config.dsl()
            val existingAuthor = authorRepository.findByIdForUpdate(ctx, id) ?: return@transactionResult null

            val updatedAuthor =
                existingAuthor.update(
                    name = name,
                    dateOfBirth = dateOfBirth,
                )

            authorRepository.update(ctx, updatedAuthor)
        }

    fun searchAuthors(
        name: String?,
        dateOfBirthFrom: LocalDate?,
        dateOfBirthTo: LocalDate?,
        bookTitle: String?,
        publicationStatus: PublicationStatus?,
        page: Int,
        pageSize: Int,
    ): PagedAuthors {
        val result =
            authorRepository.search(
                ctx = dsl,
                name = name,
                dateOfBirthFrom = dateOfBirthFrom,
                dateOfBirthTo = dateOfBirthTo,
                bookTitle = bookTitle,
                publicationStatus = publicationStatus,
                page = page,
                pageSize = pageSize,
            )
        return PagedAuthors(
            authors = result.authors,
            totalCount = result.totalCount,
        )
    }
}
