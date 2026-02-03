package com.example.demo.controller.mapper

import com.example.demo.model.Book
import com.example.demo.model.PublicationStatus
import com.example.demo.repository.AuthorWithTimestamps
import com.example.demo.service.BookService
import com.example.demo.domain.PublicationStatus as DomainPublicationStatus
import com.example.demo.model.Author as ApiAuthor

fun PublicationStatus.toDomain(): DomainPublicationStatus =
    when (this) {
        PublicationStatus.Unpublished -> DomainPublicationStatus.Unpublished
        PublicationStatus.Published -> DomainPublicationStatus.Published
    }

fun DomainPublicationStatus.toApi(): PublicationStatus =
    when (this) {
        DomainPublicationStatus.Unpublished -> PublicationStatus.Unpublished
        DomainPublicationStatus.Published -> PublicationStatus.Published
    }

fun AuthorWithTimestamps.toApiAuthor(): ApiAuthor =
    ApiAuthor(
        id = author.id,
        name = author.name,
        dateOfBirth = author.dateOfBirth,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun BookService.BookWithAuthors.toApiBook(): Book =
    Book(
        id = book.book.id,
        title = book.book.title,
        price = book.book.price,
        publicationStatus = book.book.publicationStatus.toApi(),
        authors = authors.map { it.toApiAuthor() },
        createdAt = book.createdAt,
        updatedAt = book.updatedAt,
    )
