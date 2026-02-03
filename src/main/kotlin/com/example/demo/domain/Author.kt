package com.example.demo.domain

import com.fasterxml.uuid.Generators
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

data class Author(
    val id: UUID,
    val name: String,
    val dateOfBirth: LocalDate,
) {
    init {
        require(name.isNotBlank()) { "著者名は必須です" }
        require(name.length <= 200) { "著者名は200文字以内にしてください" }
    }

    companion object {
        fun create(
            name: String,
            dateOfBirth: LocalDate,
            clock: Clock = Clock.systemDefaultZone(),
            id: UUID = Generators.timeBasedEpochGenerator().generate(),
        ): Author {
            require(dateOfBirth <= LocalDate.now(clock)) { "生年月日は現在日以前にしてください" }
            return Author(
                id = id,
                name = name,
                dateOfBirth = dateOfBirth,
            )
        }
    }

    fun update(
        name: String,
        dateOfBirth: LocalDate,
        clock: Clock = Clock.systemDefaultZone(),
    ): Author {
        require(dateOfBirth <= LocalDate.now(clock)) { "生年月日は現在日以前にしてください" }
        return copy(
            name = name,
            dateOfBirth = dateOfBirth,
        )
    }
}
