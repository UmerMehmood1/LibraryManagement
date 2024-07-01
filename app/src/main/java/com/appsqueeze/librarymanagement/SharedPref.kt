package com.appsqueeze.librarymanagement

import android.content.Context
import android.content.SharedPreferences

class SharedPref private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun storeToken(token: String?): Boolean {
        val editor = sharedPreferences.edit()
        editor.putString(KEY, token)
        return editor.commit() // Use commit() instead of apply() to ensure immediate storage
    }

    val token: String?
        get() = sharedPreferences.getString(KEY, null)

    companion object {
        private const val NAME = "FCM"
        private const val KEY = "Key"

        @Volatile
        private var instance: SharedPref? = null

        fun getInstance(context: Context): SharedPref {
            return instance ?: synchronized(this) {
                instance ?: SharedPref(context).also { instance = it }
            }
        }
    }
}
