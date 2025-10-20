package student.projects.tridentsmartsolutions

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginPage : AppCompatActivity() {

    // UI Components
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnEmergencyAccess: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGotoSignup: TextView
    private lateinit var btnGoogleSignIn: Button

    // SharedPreferences for storing user session
    private lateinit var sharedPreferences: SharedPreferences

    // Google Sign-In
    private lateinit var googleSignInUtils: GoogleSignInUtils
    private lateinit var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    // Coroutine scope
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        initializeViews()
        initializePreferences()
        setupGoogleSignIn()
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
        btnGoogleSignIn = findViewById(R.id.btn_google_signin) // Make sure this exists in your layout
    }

    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
    }

    private fun setupGoogleSignIn() {
        googleSignInUtils = GoogleSignInUtils()

        // Initialize the launcher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle the result if needed after Google Sign-In intent
            if (result.resultCode == RESULT_OK) {
                // You can handle additional logic here if needed
                Toast.makeText(this, "Google Sign-In completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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

        btnGoogleSignIn.setOnClickListener {
            performGoogleSignIn()
        }
    }

    private fun checkExistingSession() {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
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

        // TODO: Replace with actual authentication logic
        // This is a simple demonstration - in production, use proper authentication
        if (authenticateUser(email, password)) {
            // Save login session
            saveUserSession(email)

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun performGoogleSignIn() {
        googleSignInUtils.doGoogleSignIn(
            activity = this,
            scope = coroutineScope,
            launcher = googleSignInLauncher as ManagedActivityResultLauncher<Intent, ActivityResult>?,
            loginPage = {
                // This lambda will be called when Google Sign-In is successful
                handleSuccessfulGoogleSignIn()
            }
        )
    }

    private fun handleSuccessfulGoogleSignIn() {
        // Save Google Sign-In session
        saveGoogleSignInSession()

        Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    private fun saveGoogleSignInSession() {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            putString("current_user_email", "google_user") // You can get actual email from Firebase auth if needed
            putBoolean("is_google_signin", true)
            putLong("login_timestamp", System.currentTimeMillis())
            apply()
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

    private fun authenticateUser(email: String, password: String): Boolean {
        // TODO: Implement actual authentication logic
        // For now, check against stored user data or demo credentials
        val storedEmail = sharedPreferences.getString("user_email", "")
        val storedPassword = sharedPreferences.getString("user_password", "")

        // Demo credentials for testing
        return (email == storedEmail && password == storedPassword) ||
                (email == "demo@trident.com" && password == "demo123")
    }

    private fun saveUserSession(email: String) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            putString("current_user_email", email)
            putBoolean("is_google_signin", false)
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
        // TODO: Implement forgot password functionality
        Toast.makeText(this, "Please contact support: 012 345 6789", Toast.LENGTH_LONG).show()
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