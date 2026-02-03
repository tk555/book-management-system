package com.example.demo.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

data class Book(
    val id: UUID,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authorIds: Set<UUID>,
) {
    init {
        require(title.isNotBlank()) { "書籍タイトルは必須です" }
        require(title.length <= 400) { "書籍タイトルは400文字以内にしてください" }
        require(price >= 0) { "価格は0以上にしてください" }
        require(authorIds.isNotEmpty()) { "書籍には1人以上の著者が必要です" }
    }

    companion object {
        fun create(
            title: String,
            price: Int,
            publicationStatus: PublicationStatus,
            authorIds: Set<UUID>,
            id: UUID = Generators.timeBasedEpochGenerator().generate(),
        ): Book =
            Book(
                id = id,
                title = title,
                price = price,
                publicationStatus = publicationStatus,
                authorIds = authorIds,
            )
    }

    fun update(
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
        authorIds: Set<UUID>,
    ): Book {
        require(this.publicationStatus.canTransitionTo(publicationStatus)) {
            "出版ステータスを${this.publicationStatus}から${publicationStatus}に変更できません"
        }

        return copy(
            title = title,
            price = price,
            publicationStatus = publicationStatus,
            authorIds = authorIds,
        )
    }
}
