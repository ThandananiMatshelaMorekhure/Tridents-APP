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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.concurrent.Executor

class LoginPage : AppCompatActivity() {

    // UI Components
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnEmergencyAccess: Button
    private lateinit var btnGoogleSignIn: Button
    private lateinit var btnBiometricSignIn: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGotoSignup: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    // Biometric prefs
    private val BIO_PREFS = "biometric_prefs"
    private val KEY_BIO_ENABLED = "biometric_enabled"
    private val KEY_BIO_EMAIL = "bio_email"
    private val KEY_BIO_PASSWORD = "bio_password"

    private lateinit var biometricExecutor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

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
        setupBiometricPrompt()
        setupClickListeners()
        checkExistingSession()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.et_login_email)
        etPassword = findViewById(R.id.et_login_password)
        btnLogin = findViewById(R.id.btn_login)
        btnEmergencyAccess = findViewById(R.id.btn_emergency_access)
        btnGoogleSignIn = findViewById(R.id.btn_google_signin)
        btnBiometricSignIn = findViewById(R.id.btn_biometric_signin)
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

        btnBiometricSignIn.setOnClickListener {
            if (!isBiometricAvailable()) {
                Toast.makeText(this, "Biometric not available on this device.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isBiometricEnabled()) {
                Toast.makeText(this, "Biometric sign-in not enabled yet.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            biometricPrompt.authenticate(getBiometricPromptInfo())
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

        if (!validateLoginInputs(email, password)) return

        btnLogin.isEnabled = false
        btnLogin.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnLogin.isEnabled = true
                btnLogin.text = "Login"

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserSession(it.uid, email)
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        // CHANGED: Use role-based navigation
                        redirectUserBasedOnRole()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun performGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        checkAndCreateUserProfile(it)
                        saveUserSession(it.uid, it.email ?: "")
                        Toast.makeText(this, "Google sign in successful!", Toast.LENGTH_SHORT).show()
                        // CHANGED: Use role-based navigation
                        redirectUserBasedOnRole()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndCreateUserProfile(user: com.google.firebase.auth.FirebaseUser) {
        val userRef = database.getReference("users").child(user.uid)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.value == null) {
                val userData = mapOf<String, Any>(
                    "fullName" to (user.displayName ?: "Google User"),
                    "email" to (user.email ?: ""),
                    "phone" to (user.phoneNumber ?: ""),
                    "address" to "",
                    "registrationDate" to System.currentTimeMillis(),
                    "role" to "client", // ADDED: Default role for Google users
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
        val intent = Intent(this, RequestPage::class.java)
        intent.putExtra(RequestPage.EXTRA_SERVICE_TYPE, RequestPage.SERVICE_TYPE_EMERGENCY)
        intent.putExtra("is_emergency_access", true)
        startActivity(intent)
    }

    private fun handleForgotPassword() {
        Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignupPage::class.java)
        startActivity(intent)
        finish()
    }

    // CHANGED: Role-based navigation
    private fun redirectUserBasedOnRole() {
        RoleUtils.getCurrentUserRole { role ->
            val intent = when (role) {
                RoleUtils.ROLE_ADMIN -> Intent(this, RequestHistoryActivity::class.java)
                else -> Intent(this, RequestPage::class.java) // Default for clients
            }
            startActivity(intent)
            finish()
        }
    }

    // KEPT: Existing method for session check compatibility
    private fun navigateToMain() {
        val intent = Intent(this, RequestPage::class.java)
        startActivity(intent)
        finish()
    }

    // ---------- Biometric Helpers ----------

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        val can = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return can == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun isBiometricEnabled(): Boolean {
        return try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                BIO_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encPrefs.getBoolean(KEY_BIO_ENABLED, false)
        } catch (e: Exception) {
            false
        }
    }

    private fun setupBiometricPrompt() {
        biometricExecutor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val creds = readCredentialsFromEncryptedPrefs()
                    if (creds != null) {
                        val (email, password) = creds
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this@LoginPage) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    user?.let {
                                        saveUserSession(it.uid, email)
                                        Toast.makeText(applicationContext, "Biometric sign-in successful", Toast.LENGTH_SHORT).show()
                                        // CHANGED: Use role-based navigation for biometric login too
                                        redirectUserBasedOnRole()
                                    }
                                } else {
                                    Toast.makeText(applicationContext, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(applicationContext, "No stored credentials found.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun getBiometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sign in with biometrics")
            .setSubtitle("Authenticate to sign in")
            .setNegativeButtonText("Use password")
            .build()
    }

    private fun readCredentialsFromEncryptedPrefs(): Pair<String, String>? {
        return try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                BIO_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val enabled = encPrefs.getBoolean(KEY_BIO_ENABLED, false)
            if (!enabled) return null
            val email = encPrefs.getString(KEY_BIO_EMAIL, null)
            val password = encPrefs.getString(KEY_BIO_PASSWORD, null)
            if (email != null && password != null) Pair(email, password) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}