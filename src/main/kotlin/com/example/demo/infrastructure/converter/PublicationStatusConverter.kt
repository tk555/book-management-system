package com.example.demo.infrastructure.converter

import com.example.demo.domain.PublicationStatus

object PublicationStatusConverter {
    fun toDbValue(status: PublicationStatus): String =
        when (status) {
            PublicationStatus.Unpublished -> "UNPUBLISHED"
            PublicationStatus.Published -> "PUBLISHED"
        }

    fun fromDbValue(value: String): PublicationStatus =
        when (value) {
            "UNPUBLISHED" -> PublicationStatus.Unpublished
            "PUBLISHED" -> PublicationStatus.Published
            else -> throw IllegalArgumentException("不明な出版ステータス: $value")
        }
}
