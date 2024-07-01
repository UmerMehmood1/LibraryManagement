package com.appsqueeze.librarymanagement

import com.google.firebase.Timestamp

data class User(
    var Id: String = "",
    var name: String? = null,
    var email: String? = null,
    var book: MutableList<Int> = mutableListOf(),
    var fine: MutableList<Int> = mutableListOf(),
    var re: MutableList<Int> = mutableListOf(),
    var date: MutableList<Timestamp> = mutableListOf(),
    var varenroll: Int = 0,
    var card: Int = 0,
    var type: Int = 0,
    var fcmToken: String? = null,
    var left_fine: Int = 0
)