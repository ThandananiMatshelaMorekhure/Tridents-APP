package student.projects.tridentsmartsolutions

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginPage : AppCompatActivity() {

    // UI Components
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnEmergencyAccess: Button
    private lateinit var btnGoogleSignIn: Button // Add this for Google SSO
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGotoSignup: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    // Request codes
    companion object {
        private const val RC_GOOGLE_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        initializeFirebase()
        initializeGoogleSignIn()
        initializeViews()
        initializePreferences()
        setupClickListeners()
        checkExistingSession()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initializeGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // You'll need to add this to your strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.et_login_email)
        etPassword = findViewById(R.id.et_login_password)
        btnLogin = findViewById(R.id.btn_login)
        btnEmergencyAccess = findViewById(R.id.btn_emergency_access)
        btnGoogleSignIn = findViewById(R.id.btn_google_signin) // Make sure to add this button in your XML
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

        btnGoogleSignIn.setOnClickListener {
            performGoogleSignIn()
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

        // Show loading state
        btnLogin.isEnabled = false
        btnLogin.text = "Signing in..."

        // Authenticate user with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success
                    val user = auth.currentUser
                    user?.let {
                        // Save session
                        saveUserSession(it.uid, email)

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                } else {
                    // Login failed
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun performGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    user?.let {
                        // Check if user exists in database, if not create profile
                        checkAndCreateUserProfile(it)

                        // Save session
                        saveUserSession(it.uid, it.email ?: "")

                        Toast.makeText(this, "Google sign in successful!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                } else {
                    // Sign in failed
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndCreateUserProfile(user: com.google.firebase.auth.FirebaseUser) {
        val userRef = database.getReference("users").child(user.uid)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.value == null) {
                // User doesn't exist in database, create profile
                val userData = mapOf<String, Any>(
                    "fullName" to (user.displayName ?: "Google User"),
                    "email" to (user.email ?: ""),
                    "phone" to (user.phoneNumber ?: ""),
                    "address" to "",
                    "registrationDate" to System.currentTimeMillis(),
                    "isGoogleUser" to true
                )

                userRef.setValue(userData).addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to create user profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

    private fun saveUserSession(uid: String, email: String) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            putString("current_user_uid", uid)
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
        // TODO: Implement forgot password functionality
        Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignupPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, RequestPage::class.java)
        startActivity(intent)
        finish()
    }
}