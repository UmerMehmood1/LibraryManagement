package com.appsqueeze.librarymanagement

data class Book(
    var id: Int = 0,
    var title: String? = null,
    var author: String? = null,
    var available: Int = 0,
    var units: MutableList<Int> = mutableListOf(),
    var type: String="",
    var total: Int=1,
)
