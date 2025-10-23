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

class EditProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        initializeFirebase()
        initializePreferences()
        loadCurrentData()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etFullName = findViewById(R.id.et_full_name)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        etAddress = findViewById(R.id.et_address)
        btnSaveProfile = findViewById(R.id.btn_save_profile)
        btnCancel = findViewById(R.id.btn_cancel)
        progressBar = findViewById(R.id.progress_bar)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profile"
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
    }

    private fun loadCurrentData() {
        etFullName.setText(sharedPreferences.getString("user_full_name", ""))
        etEmail.setText(sharedPreferences.getString("user_email", ""))
        etPhone.setText(sharedPreferences.getString("user_phone", ""))
        etAddress.setText(sharedPreferences.getString("user_address", ""))
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveProfile() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()

        // Validation
        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            etFullName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Get user ID
        val userId = currentUser.uid
        val currentEmail = sharedPreferences.getString("user_email", "") ?: ""

        // Check if email changed
        if (email != currentEmail) {
            // Need to reauthenticate before updating email
            updateEmailAndProfile(currentUser.email ?: "", email, fullName, phone, address, userId)
        } else {
            // Just update profile data in Firebase
            updateProfileInFirebase(fullName, email, phone, address, userId)
        }
    }

    private fun updateEmailAndProfile(
        oldEmail: String,
        newEmail: String,
        fullName: String,
        phone: String,
        address: String,
        userId: String
    ) {
        // For email update, we need current password - prompt user if needed
        // For now, we'll just update the profile data and skip email update in Firebase Auth
        // You can enhance this to prompt for password if email changes

        Toast.makeText(
            this,
            "Note: Email change in authentication requires re-login",
            Toast.LENGTH_LONG
        ).show()

        updateProfileInFirebase(fullName, newEmail, phone, address, userId)
    }

    private fun updateProfileInFirebase(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        userId: String
    ) {
        val userRef = database.getReference("users").child(userId)

        // Create user data map
        val userUpdates = hashMapOf<String, Any>(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "address" to address,
            "updatedAt" to System.currentTimeMillis()
        )

        // Update Firebase Realtime Database
        userRef.updateChildren(userUpdates)
            .addOnSuccessListener {
                // Save to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("user_full_name", fullName)
                    putString("user_email", email)
                    putString("user_phone", phone)
                    putString("user_address", address)
                    apply()
                }

                showLoading(false)
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Failed to update profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSaveProfile.isEnabled = !show
        btnCancel.isEnabled = !show
        etFullName.isEnabled = !show
        etEmail.isEnabled = !show
        etPhone.isEnabled = !show
        etAddress.isEnabled = !show
    }
}