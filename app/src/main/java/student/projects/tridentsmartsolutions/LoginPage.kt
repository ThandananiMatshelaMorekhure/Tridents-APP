package student.projects.tridentsmartsolutions

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginPage : AppCompatActivity() {

    // UI Components
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnEmergencyAccess: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGotoSignup: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        initializeViews()
        initializePreferences()
        setupClickListeners()
        checkExistingSession()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.et_login_email)
        etPassword = findViewById(R.id.et_login_password)
        btnLogin = findViewById(R.id.btn_login)
        btnEmergencyAccess = findViewById(R.id.btn_emergency_access)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        tvGotoSignup = findViewById(R.id.tv_goto_signup)
    }

    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        btnEmergencyAccess.setOnClickListener {
            handleEmergencyAccess()
        }

        tvForgotPassword.setOnClickListener {
            handleForgotPassword()
        }

        tvGotoSignup.setOnClickListener {
            navigateToSignup()
        }
    }

    private fun checkExistingSession() {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
            navigateToMain()
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Input validation
        if (!validateLoginInputs(email, password)) {
            return
        }


                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                } else {
                }
            }
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        when {
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
            else -> return true
        }
    }

        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            putString("current_user_email", email)
            putLong("login_timestamp", System.currentTimeMillis())
            apply()
        }
    }

    private fun handleEmergencyAccess() {
        // Navigate directly to emergency request form
        val intent = Intent(this, RequestPage::class.java)
        intent.putExtra(RequestPage.EXTRA_SERVICE_TYPE, RequestPage.SERVICE_TYPE_EMERGENCY)
        intent.putExtra("is_emergency_access", true)
        startActivity(intent)
    }

    private fun handleForgotPassword() {
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignupPage::class.java)
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}