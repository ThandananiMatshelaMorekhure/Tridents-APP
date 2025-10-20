package student.projects.tridentsmartsolutions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class RequestPage : AppCompatActivity() {

    // UI Components
    private lateinit var btnRequestPlumbing: Button
    private lateinit var btnRequestSecurity: Button
    private lateinit var btnEmergencyRequest: Button

    // Navigation Components
    private lateinit var navHome: LinearLayout
    private lateinit var navRequest: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navAccount: LinearLayout

    companion object {
        const val EXTRA_SERVICE_TYPE = "service_type"
        const val SERVICE_TYPE_PLUMBING = "plumbing"
        const val SERVICE_TYPE_SECURITY = "security"
        const val SERVICE_TYPE_EMERGENCY = "emergency"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_page)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Service Request Buttons
        btnRequestPlumbing = findViewById(R.id.btn_request_plumbing)
        btnRequestSecurity = findViewById(R.id.btn_request_security)
        btnEmergencyRequest = findViewById(R.id.btn_emergency_request)

        // Navigation Items
        navHome = findViewById(R.id.nav_home)
        navRequest = findViewById(R.id.nav_request)
        navNotifications = findViewById(R.id.nav_notifications)
        navAccount = findViewById(R.id.nav_account)
    }

    private fun setupClickListeners() {
        // Service Request Button Listeners
        btnRequestPlumbing.setOnClickListener {
            navigateToRequestForm(SERVICE_TYPE_PLUMBING)
        }

        btnRequestSecurity.setOnClickListener {
            navigateToRequestForm(SERVICE_TYPE_SECURITY)
        }

        btnEmergencyRequest.setOnClickListener {
            navigateToRequestForm(SERVICE_TYPE_EMERGENCY)
        }

        // Bottom Navigation Listeners
        navHome.setOnClickListener {
            navigateToHome()
        }

        navRequest.setOnClickListener {
            // Already on request page - do nothing or refresh
        }

//        navNotifications.setOnClickListener {
//            navigateToNotifications()
//        }

//        navAccount.setOnClickListener {
//            navigateToAccount()
//        }
    }

    private fun navigateToRequestForm(serviceType: String) {
        val intent = Intent(this, RequestPage::class.java)
        intent.putExtra(EXTRA_SERVICE_TYPE, serviceType)
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

//    private fun navigateToNotifications() {
//        val intent = Intent(this, NotificationsActivity::class.java)
//        startActivity(intent)
//    }

//    private fun navigateToAccount() {
//        val intent = Intent(this, AccountActivity::class.java)
//        startActivity(intent)
//    }
}