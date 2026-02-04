package com.example.demo.domain

enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
    ;

    fun canTransitionTo(newStatus: PublicationStatus): Boolean = this == UNPUBLISHED || newStatus == PUBLISHED
}
