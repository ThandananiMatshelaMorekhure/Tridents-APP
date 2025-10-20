package student.projects.tridentsmartsolutions

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RequestFormActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvServiceType: TextView
    private lateinit var etProblemDescription: TextInputEditText
    private lateinit var spinnerUrgency: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var spinnerContactPreference: Spinner
    private lateinit var btnSubmitRequest: Button

    // Navigation
    private lateinit var navHome: MaterialCardView
    private lateinit var navRequest: MaterialCardView
    private lateinit var navAccount: MaterialCardView

    // Selected date
    private var selectedDate: Date? = null
    private var serviceType: String? = null

    // Firebase
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_form)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        initializeViews()
        setupSpinners()
        setupClickListeners()
        setServiceTypeFromIntent()
    }

    private fun initializeViews() {
        tvServiceType = findViewById(R.id.tv_service_type)
        etProblemDescription = findViewById(R.id.et_problem_description)
        spinnerUrgency = findViewById(R.id.spinner_urgency)
        btnSelectDate = findViewById(R.id.btn_select_date)
        spinnerContactPreference = findViewById(R.id.spinner_contact_preference)
        btnSubmitRequest = findViewById(R.id.btn_submit_request)

        // Navigation
        navHome = findViewById(R.id.nav_home)
        navRequest = findViewById(R.id.nav_request)
        navAccount = findViewById(R.id.nav_account)
    }

    private fun setupSpinners() {
        // Urgency options
        val urgencyOptions = arrayOf(
            "Low - Within 1 week",
            "Medium - Within 3 days",
            "High - Within 24 hours",
            "Emergency - ASAP"
        )
        val urgencyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            urgencyOptions
        )
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUrgency.adapter = urgencyAdapter

        // Contact preference options
        val contactOptions = arrayOf("Phone Call", "SMS", "Email", "WhatsApp")
        val contactAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            contactOptions
        )
        contactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerContactPreference.adapter = contactAdapter
    }

    private fun setupClickListeners() {
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnSubmitRequest.setOnClickListener {
            submitServiceRequest()
        }

        // Navigation
        navHome.setOnClickListener { navigateToHome() }
        navRequest.setOnClickListener { navigateToRequestPage() }
        navAccount.setOnClickListener { navigateToProfile() }
    }

    private fun setServiceTypeFromIntent() {
        serviceType = intent.getStringExtra(RequestPage.EXTRA_SERVICE_TYPE)
        val serviceName = when (serviceType) {
            RequestPage.SERVICE_TYPE_PLUMBING -> "Plumbing"
            RequestPage.SERVICE_TYPE_SECURITY -> "Security"
            RequestPage.SERVICE_TYPE_EMERGENCY -> "Emergency"
            else -> "Service"
        }
        tvServiceType.text = "$serviceName Request"

        // Auto-select urgency for emergency
        if (serviceType == RequestPage.SERVICE_TYPE_EMERGENCY) {
            spinnerUrgency.setSelection(3)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                btnSelectDate.text = dateFormat.format(selectedDate!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun submitServiceRequest() {
        val problemDescription = etProblemDescription.text.toString().trim()
        val urgency = spinnerUrgency.selectedItem.toString()
        val contactPreference = spinnerContactPreference.selectedItem.toString()

        if (problemDescription.isEmpty()) {
            etProblemDescription.error = "Please describe the problem"
            etProblemDescription.requestFocus()
            return
        }

        if (problemDescription.length < 10) {
            etProblemDescription.error = "Please provide more details (at least 10 characters)"
            etProblemDescription.requestFocus()
            return
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a preferred date", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("current_user_email", "") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Please login to submit a request", Toast.LENGTH_SHORT).show()
            return
        }

        val requestId = database.child("service_requests").push().key ?: return
        val serviceRequest = ServiceRequest(
            id = requestId,
            serviceType = serviceType ?: "general",
            problemDescription = problemDescription,
            urgency = urgency,
            preferredDate = selectedDate!!.time,
            contactPreference = contactPreference,
            timestamp = System.currentTimeMillis(),
            status = "pending",
            userId = userEmail
        )

        database.child("service_requests").child(requestId)
            .setValue(serviceRequest)
            .addOnSuccessListener {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = selectedDate?.let { dateFormat.format(it) } ?: "N/A"
                Toast.makeText(
                    this,
                    "Service request submitted successfully!\nService: ${tvServiceType.text}\nDate: $formattedDate",
                    Toast.LENGTH_LONG
                ).show()
                navigateToHistory()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    private fun navigateToRequestPage() {
        startActivity(Intent(this, RequestPage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    private fun navigateToHistory() {
        startActivity(Intent(this, RequestHistoryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfilePage::class.java))
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToRequestPage()
    }
}
