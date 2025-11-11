package com.example.cyglobaltech

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val profileUri: String = "",
    val role: String = "user",
    val isEmailVerified: Boolean = false
)

object UserManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    suspend fun registerUser(context: Context, name: String, surname: String, email: String, password: String): Pair<Boolean, String?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid

            if (uid != null) {
                val userProfile = UserProfile(
                    uid = uid,
                    email = email,
                    name = name,
                    surname = surname,
                    role = "user"
                )
                db.collection("users").document(uid).set(userProfile).await()
                result.user?.sendEmailVerification()
                Pair(true, null)
            } else {
                Pair(false, "Registration failed: User UID is null.")
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage)
        }
    }

    suspend fun validateLogin(context: Context, email: String, password: String): Pair<Boolean, String?> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage)
        }
    }

    suspend fun changePassword(email: String): Pair<Boolean, String?> {
        return try {
            val querySnapshot = db.collection("users").whereEqualTo("email", email).get().await()
            if (querySnapshot.isEmpty) {
                return Pair(false, "No account found with that email.")
            }

            auth.sendPasswordResetEmail(email).await()
            Pair(true, "Password reset email sent. Please check your inbox.")

        } catch (e: Exception) {
            Pair(false, e.localizedMessage)
        }
    }


    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(uid: String, name: String, surname: String, phone: String): Pair<Boolean, String?> {
        return try {
            val updates = mapOf(
                "name" to name,
                "surname" to surname,
                "phone" to phone
            )
            db.collection("users").document(uid).update(updates).await()
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage)
        }
    }


    fun logout(context: Context) {
        auth.signOut()
    }

    fun getLoggedInEmail(): String? {
        return auth.currentUser?.email
    }

    fun getLoggedInUid(): String? {
        return auth.currentUser?.uid
    }

    suspend fun isAdmin(): Boolean {
        val uid = getLoggedInUid() ?: return false
        val profile = getUserProfile(uid)
        return profile?.role == "admin"
    }
}