package com.example.demo.domain

sealed interface PublicationStatus {
    data object Published : PublicationStatus

    data object Unpublished : PublicationStatus

    fun canTransitionTo(newStatus: PublicationStatus): Boolean =
        when (this) {
            Unpublished -> true
            Published -> newStatus == Published
        }
}
