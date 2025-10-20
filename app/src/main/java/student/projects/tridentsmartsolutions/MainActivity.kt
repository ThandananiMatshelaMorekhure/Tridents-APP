package student.projects.tridentsmartsolutions

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // Navigation Components
    private lateinit var navHome: LinearLayout
    private lateinit var navRequest: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navAccount: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Bottom Navigation Items
        navHome = findViewById(R.id.nav_home)
        navRequest = findViewById(R.id.nav_request)
        navNotifications = findViewById(R.id.nav_notifications)
        navAccount = findViewById(R.id.nav_account)
    }

    private fun setupClickListeners() {
        // Bottom Navigation Listeners
        navHome.setOnClickListener {
            // Already on home page - do nothing or refresh
        }

        navRequest.setOnClickListener {
            navigateToRequestPage()
        }

        navNotifications.setOnClickListener {
            // Placeholder for notifications
            // navigateToNotifications()
        }

        navAccount.setOnClickListener {
            navigateToProfile()
        }
    }

    private fun navigateToRequestPage() {
        val intent = Intent(this, RequestPage::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, CustomerPage::class.java)
        startActivity(intent)
    }
}