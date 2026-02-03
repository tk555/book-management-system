package com.example.demo.controller

import com.example.demo.api.BooksApi
import com.example.demo.controller.mapper.toApiBook
import com.example.demo.controller.mapper.toDomain
import com.example.demo.model.Book
import com.example.demo.model.BookRequest
import com.example.demo.model.PageMeta
import com.example.demo.model.PagedBooks
import com.example.demo.model.PublicationStatus
import com.example.demo.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class BooksApiController(
    private val bookService: BookService,
) : BooksApi {
    override fun createBook(bookRequest: BookRequest): ResponseEntity<Book> {
        val bookWithAuthors =
            bookService.createBook(
                title = bookRequest.title,
                price = bookRequest.price,
                publicationStatus = bookRequest.publicationStatus.toDomain(),
                authorIds = bookRequest.authorIds.toSet(),
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(bookWithAuthors.toApiBook())
    }

    override fun getBook(id: UUID): ResponseEntity<Book> {
        val bookWithAuthors = bookService.getBookWithAuthors(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(bookWithAuthors.toApiBook())
    }

    override fun updateBook(
        id: UUID,
        bookRequest: BookRequest,
    ): ResponseEntity<Book> {
        val bookWithAuthors =
            bookService.updateBook(
                id = id,
                title = bookRequest.title,
                price = bookRequest.price,
                publicationStatus = bookRequest.publicationStatus.toDomain(),
                authorIds = bookRequest.authorIds.toSet(),
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(bookWithAuthors.toApiBook())
    }

    override fun searchBooks(
        title: String?,
        authorName: String?,
        priceFrom: Int?,
        priceTo: Int?,
        publicationStatus: PublicationStatus?,
        page: Int,
        pageSize: Int,
    ): ResponseEntity<PagedBooks> {
        val result =
            bookService.searchBooks(
                title = title,
                authorName = authorName,
                priceFrom = priceFrom,
                priceTo = priceTo,
                publicationStatus = publicationStatus?.toDomain(),
                page = page,
                pageSize = pageSize,
            )

        val totalPages = calculateTotalPages(result.totalCount, pageSize)

        return ResponseEntity.ok(
            PagedBooks(
                content = result.books.map { it.toApiBook() },
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
