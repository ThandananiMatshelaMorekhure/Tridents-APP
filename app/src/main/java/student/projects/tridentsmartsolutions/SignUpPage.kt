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

    // SharedPreferences for storing user data
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page)

        initializeViews()
        initializePreferences()
        setupClickListeners()
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

        // Check if user already exists
        if (userExists(email)) {
            Toast.makeText(this, "User with this email already exists", Toast.LENGTH_SHORT).show()
            return
        }

        // Create new user account
        if (createUserAccount(fullName, email, phone, address, password)) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

            // Auto-login after successful registration
            saveUserSession(email)
            navigateToMain()
        } else {
            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
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

    private fun userExists(email: String): Boolean {
        val existingEmail = sharedPreferences.getString("user_email", "")
        return email.equals(existingEmail, ignoreCase = true)
    }

    private fun createUserAccount(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): Boolean {
        try {
            // TODO: In production, send data to server API
            // For now, store locally in SharedPreferences
            with(sharedPreferences.edit()) {
                putString("user_full_name", fullName)
                putString("user_email", email)
                putString("user_phone", phone)
                putString("user_address", address)
                putString("user_password", password) // In production, hash this password!
                putLong("registration_date", System.currentTimeMillis())
                apply()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun saveUserSession(email: String) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
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