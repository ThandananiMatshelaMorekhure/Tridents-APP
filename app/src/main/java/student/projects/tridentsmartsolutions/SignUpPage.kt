package student.projects.tridentsmartsolutions

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupPage : AppCompatActivity() {

    // UI Components
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignup: Button
    private lateinit var tvGotoLogin: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page)

        initializeFirebase()
        initializeViews()
        initializePreferences()
        setupClickListeners()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.et_signup_fullname)
        etEmail = findViewById(R.id.et_signup_email)
        etPhone = findViewById(R.id.et_signup_phone)
        etAddress = findViewById(R.id.et_signup_address)
        etPassword = findViewById(R.id.et_signup_password)
        etConfirmPassword = findViewById(R.id.et_signup_confirm_password)
        cbTerms = findViewById(R.id.cb_terms)
        btnSignup = findViewById(R.id.btn_signup)
        tvGotoLogin = findViewById(R.id.tv_goto_login)
    }

    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        btnSignup.setOnClickListener {
            performSignup()
        }

        tvGotoLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun performSignup() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Input validation
        if (!validateSignupInputs(fullName, email, phone, address, password, confirmPassword)) {
            return
        }

        // Show loading state
        btnSignup.isEnabled = false
        btnSignup.text = "Creating account..."

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    val user = auth.currentUser
                    user?.let {
                        // Save user data to Realtime Database with role
                        saveUserDataToDatabase(it.uid, fullName, email, phone, address)
                    }
                } else {
                    // Sign up failed
                    btnSignup.isEnabled = true
                    btnSignup.text = "Create Account"
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserDataToDatabase(
        uid: String,
        fullName: String,
        email: String,
        phone: String,
        address: String
    ) {
        val userRef = database.getReference("users").child(uid)

        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "address" to address,
            "registrationDate" to System.currentTimeMillis(),
            "role" to "client" // ADDED: Default role for new users
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                // Save session
                saveUserSession(uid, email)

                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            .addOnFailureListener { e ->
                btnSignup.isEnabled = true
                btnSignup.text = "Create Account"
                Toast.makeText(
                    this,
                    "Failed to save user data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateSignupInputs(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        when {
            fullName.isEmpty() -> {
                etFullName.error = "Full name is required"
                etFullName.requestFocus()
                return false
            }
            fullName.length < 2 -> {
                etFullName.error = "Please enter a valid name"
                etFullName.requestFocus()
                return false
            }
            email.isEmpty() -> {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return false
            }
            phone.isEmpty() -> {
                etPhone.error = "Phone number is required"
                etPhone.requestFocus()
                return false
            }
            phone.length < 10 -> {
                etPhone.error = "Please enter a valid phone number"
                etPhone.requestFocus()
                return false
            }
            address.isEmpty() -> {
                etAddress.error = "Address is required"
                etAddress.requestFocus()
                return false
            }
            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return false
            }
            confirmPassword != password -> {
                etConfirmPassword.error = "Passwords do not match"
                etConfirmPassword.requestFocus()
                return false
            }
            !cbTerms.isChecked -> {
                Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

    private fun saveUserSession(uid: String, email: String) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            putString("current_user_uid", uid)
            putString("current_user_email", email)
            putLong("login_timestamp", System.currentTimeMillis())
            apply()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}