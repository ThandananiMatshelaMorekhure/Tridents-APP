package student.projects.tridentsmartsolutions


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class ProfilePage : AppCompatActivity() {

    // UI Components
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button

    // Navigation
    private lateinit var navHome: MaterialCardView
    private lateinit var navRequest: MaterialCardView
    private lateinit var navAccount: MaterialCardView

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        initializeViews()
        initializePreferences()
        setupClickListeners()
        loadUserData()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvUserPhone = findViewById(R.id.tv_user_phone)
        tvUserAddress = findViewById(R.id.tv_user_address)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnChangePassword = findViewById(R.id.btn_change_password)
        btnLogout = findViewById(R.id.btn_logout)

        // Navigation
        navHome = findViewById(R.id.nav_home)
        navRequest = findViewById(R.id.nav_request)
        navAccount = findViewById(R.id.nav_account)
    }

    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
            Toast.makeText(this, "Edit profile feature coming soon", Toast.LENGTH_SHORT).show()
        }

        btnChangePassword.setOnClickListener {
            // TODO: Implement change password functionality
            Toast.makeText(this, "Change password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        // Navigation listeners
        navHome.setOnClickListener {
            navigateToHome()
        }

        navRequest.setOnClickListener {
            navigateToRequestPage()
        }

        navAccount.setOnClickListener {
            // Already on profile page
        }
    }

    private fun loadUserData() {
        val fullName = sharedPreferences.getString("user_full_name", "User Name") ?: "User Name"
        val email = sharedPreferences.getString("user_email", "user@example.com") ?: "user@example.com"
        val phone = sharedPreferences.getString("user_phone", "Not provided") ?: "Not provided"
        val address = sharedPreferences.getString("user_address", "Not provided") ?: "Not provided"

        tvUserName.text = fullName
        tvUserEmail.text = email
        tvUserPhone.text = phone
        tvUserAddress.text = address
    }

    private fun logoutUser() {
        // Clear user session
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", false)
            remove("current_user_email")
            apply()
        }

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRequestPage() {
        val intent = Intent(this, RequestPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginPage::class.java)
        startActivity(intent)
        finishAffinity() // Clear all activities
    }
}