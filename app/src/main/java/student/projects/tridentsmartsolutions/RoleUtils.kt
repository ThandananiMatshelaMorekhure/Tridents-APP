package student.projects.tridentsmartsolutions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

object RoleUtils {
    const val ROLE_CLIENT = "client"
    const val ROLE_ADMIN = "admin"

    // Activities that clients can access
    private val clientActivities = setOf(
        "MainActivity",
        "RequestPage",
        "RequestFormActivity",
        "ProfilePage",
        "EditProfileActivity",
        "ChangePasswordActivity"
    )

    // Get current user's role from Realtime Database
    fun getCurrentUserRole(callback: (String) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").getValue(String::class.java) ?: ROLE_CLIENT
                        callback(role)
                    } else {
                        callback(ROLE_CLIENT)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(ROLE_CLIENT) // Default to client on error
                }
            })
        } ?: callback(ROLE_CLIENT)
    }

    // Check if current user is admin
    fun isUserAdmin(callback: (Boolean) -> Unit) {
        getCurrentUserRole { role ->
            callback(role == ROLE_ADMIN)
        }
    }

    // Validate activity access
    fun canAccessActivity(activityName: String, userRole: String): Boolean {
        return when (userRole) {
            ROLE_ADMIN -> true // Admin can access everything
            ROLE_CLIENT -> clientActivities.contains(activityName)
            else -> false
        }
    }
}