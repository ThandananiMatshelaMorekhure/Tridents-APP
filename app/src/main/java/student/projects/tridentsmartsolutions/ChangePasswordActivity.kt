package student.projects.tridentsmartsolutions

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.FirebaseDatabase

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initializeViews()
        initializeFirebase()
        initializePreferences()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etCurrentPassword = findViewById(R.id.et_current_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnChangePassword = findViewById(R.id.btn_change_password)
        btnCancel = findViewById(R.id.btn_cancel)
        progressBar = findViewById(R.id.progress_bar)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Change Password"
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnChangePassword.setOnClickListener {
            changePassword()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validation
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Current password is required"
            etCurrentPassword.requestFocus()
            return
        }

        if (newPassword.isEmpty()) {
            etNewPassword.error = "New password is required"
            etNewPassword.requestFocus()
            return
        }

        if (newPassword.length < 6) {
            etNewPassword.error = "Password must be at least 6 characters"
            etNewPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
            etConfirmPassword.requestFocus()
            return
        }

        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val email = currentUser.email
        if (email == null) {
            Toast.makeText(this, "Unable to get user email", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Re-authenticate user before changing password
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                // Re-authentication successful, now update password
                updatePasswordInFirebase(currentUser.uid, newPassword)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                etCurrentPassword.error = "Current password is incorrect"
                etCurrentPassword.requestFocus()
                Toast.makeText(
                    this,
                    "Authentication failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updatePasswordInFirebase(userId: String, newPassword: String) {
        val currentUser = auth.currentUser ?: return

        // Update password in Firebase Authentication
        currentUser.updatePassword(newPassword)
            .addOnSuccessListener {
                // Update password hash in Realtime Database (optional - for backup)
                val userRef = database.getReference("users").child(userId)
                val passwordUpdate = hashMapOf<String, Any>(
                    "passwordUpdatedAt" to System.currentTimeMillis()
                )

                userRef.updateChildren(passwordUpdate)
                    .addOnSuccessListener {
                        // Save to SharedPreferences
                        with(sharedPreferences.edit()) {
                            putString("user_password", newPassword)
                            apply()
                        }

                        showLoading(false)
                        Toast.makeText(
                            this,
                            "Password changed successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        Toast.makeText(
                            this,
                            "Password updated in auth but failed to log in database: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // Still finish since password was changed in auth
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Failed to change password: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnChangePassword.isEnabled = !show
        btnCancel.isEnabled = !show
        etCurrentPassword.isEnabled = !show
        etNewPassword.isEnabled = !show
        etConfirmPassword.isEnabled = !show
    }
}