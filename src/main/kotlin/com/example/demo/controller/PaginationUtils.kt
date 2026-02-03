package com.example.demo.controller

fun calculateTotalPages(
    totalCount: Long,
    pageSize: Int,
): Int = if (totalCount == 0L) 0 else ((totalCount - 1) / pageSize + 1).toInt()
