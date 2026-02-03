package com.example.demo.controller

import com.example.demo.api.AuthorsApi
import com.example.demo.controller.mapper.toApiAuthor
import com.example.demo.controller.mapper.toApiBook
import com.example.demo.controller.mapper.toDomain
import com.example.demo.model.AuthorRequest
import com.example.demo.model.Book
import com.example.demo.model.PageMeta
import com.example.demo.model.PagedAuthors
import com.example.demo.model.PublicationStatus
import com.example.demo.service.AuthorService
import com.example.demo.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID
import com.example.demo.model.Author as ApiAuthor

@RestController
class AuthorsApiController(
    private val authorService: AuthorService,
    private val bookService: BookService,
) : AuthorsApi {
    override fun createAuthor(authorRequest: AuthorRequest): ResponseEntity<ApiAuthor> {
        val authorWithTimestamps =
            authorService.createAuthor(
                name = authorRequest.name,
                dateOfBirth = authorRequest.dateOfBirth,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(authorWithTimestamps.toApiAuthor())
    }

    override fun getAuthor(id: UUID): ResponseEntity<ApiAuthor> {
        val authorWithTimestamps = authorService.getAuthor(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(authorWithTimestamps.toApiAuthor())
    }

    override fun updateAuthor(
        id: UUID,
        authorRequest: AuthorRequest,
    ): ResponseEntity<ApiAuthor> {
        val authorWithTimestamps =
            authorService.updateAuthor(
                id = id,
                name = authorRequest.name,
                dateOfBirth = authorRequest.dateOfBirth,
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(authorWithTimestamps.toApiAuthor())
    }

    override fun getBooksByAuthor(id: UUID): ResponseEntity<List<Book>> {
        val booksWithTimestamps = bookService.getBooksByAuthor(id) ?: return ResponseEntity.notFound().build()
        val booksWithAuthors = bookService.getBooksWithAuthors(booksWithTimestamps)
        return ResponseEntity.ok(booksWithAuthors.map { it.toApiBook() })
    }

    override fun searchAuthors(
        name: String?,
        dateOfBirthFrom: LocalDate?,
        dateOfBirthTo: LocalDate?,
        bookTitle: String?,
        publicationStatus: PublicationStatus?,
        page: Int,
        pageSize: Int,
    ): ResponseEntity<PagedAuthors> {
        val result =
            authorService.searchAuthors(
                name = name,
                dateOfBirthFrom = dateOfBirthFrom,
                dateOfBirthTo = dateOfBirthTo,
                bookTitle = bookTitle,
                publicationStatus = publicationStatus?.toDomain(),
                page = page,
                pageSize = pageSize,
            )

        val totalPages = calculateTotalPages(result.totalCount, pageSize)

        return ResponseEntity.ok(
            PagedAuthors(
                content = result.authors.map { it.toApiAuthor() },
                meta =
                    PageMeta(
                        page = page,
                        pageSize = pageSize,
                        totalElements = result.totalCount,
                        totalPages = totalPages,
                    ),
            ),
        )
    }
}
