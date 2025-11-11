package com.example.cyglobaltech.helpers

import android.content.Context

data class User(val email: String, val password: String, val name: String, val surname: String)

object AuthHelper {
    private const val PREFS_NAME = "UserPrefs"
    private const val USERS_KEY = "registered_users"
    private const val LOGGED_IN_USER = "logged_in_user"

    fun registerUser(context: Context, email: String, password: String, name: String, surname: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val users = prefs.getStringSet(USERS_KEY, mutableSetOf()) ?: mutableSetOf()

        if (users.any { it.startsWith("$email:") }) return false

        // Save user as "email:password:name:surname"
        users.add("$email:$password:$name:$surname")
        prefs.edit().putStringSet(USERS_KEY, users).apply()
        return true
    }

    fun loginUser(context: Context, email: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val users = prefs.getStringSet(USERS_KEY, emptySet()) ?: emptySet()

        val match = users.any { it.startsWith("$email:$password:") }
        if (match) {
            prefs.edit().putString(LOGGED_IN_USER, email).apply()
        }
        return match
    }

    fun logoutUser(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(LOGGED_IN_USER).apply()
    }

    fun getLoggedInUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(LOGGED_IN_USER, null) ?: return null

        val users = prefs.getStringSet(USERS_KEY, emptySet()) ?: emptySet()
        val userString = users.find { it.startsWith("$email:") } ?: return null

        val parts = userString.split(":")
        return if (parts.size == 4) {
            User(parts[0], parts[1], parts[2], parts[3])
        } else null
    }
}
