package com.appsqueeze.librarymanagement

data class Admin (
    var type: Int = 0,
    var name: String? = null,
    var email: String? = null,
    var fcmToken: String? = null
)
