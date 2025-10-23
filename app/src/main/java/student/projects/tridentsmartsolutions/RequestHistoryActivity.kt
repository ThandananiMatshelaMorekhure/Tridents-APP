package student.projects.tridentsmartsolutions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RequestHistoryActivity : AppCompatActivity() {

    private lateinit var rvRequestHistory: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var requestAdapter: RequestHistoryAdapter


    // Navigation
    private lateinit var navHome: MaterialCardView
    private lateinit var navRequest: MaterialCardView
    private lateinit var navHistory: MaterialCardView
    private lateinit var navAccount: MaterialCardView

    // Firebase
    private lateinit var database: DatabaseReference
    private var requestsList = mutableListOf<ServiceRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_history)

        initializeViews()
        setupRecyclerView()
        setupNavigation()
        loadRequestHistory()
    }

    private fun initializeViews() {
        rvRequestHistory = findViewById(R.id.rv_request_history)
        emptyState = findViewById(R.id.empty_state)

        navHome = findViewById(R.id.nav_home)
        navRequest = findViewById(R.id.nav_request)
        navHistory = findViewById(R.id.nav_history)
        navAccount = findViewById(R.id.nav_account)
    }

    private fun setupRecyclerView() {
        requestAdapter = RequestHistoryAdapter(requestsList) { request ->
            showRequestDetails(request)
        }
        rvRequestHistory.layoutManager = LinearLayoutManager(this)
        rvRequestHistory.adapter = requestAdapter
    }

    private fun setupNavigation() {
        navHome.setOnClickListener { navigateToHome() }
        navRequest.setOnClickListener { navigateToRequest() }
        navHistory.setOnClickListener { /* Already here */ }
        navAccount.setOnClickListener { navigateToProfile() }
    }

    private fun loadRequestHistory() {
        val sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("current_user_email", "") ?: ""

        if (userEmail.isEmpty()) {
            showEmptyState()
            return
        }

        // Initialize Firebase database reference
        database = Firebase.database.reference

        database.child("service_requests")
            .orderByChild("userId")
            .equalTo(userEmail)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestsList.clear()

                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(ServiceRequest::class.java)
                        request?.let {
                            it.id = requestSnapshot.key ?: ""
                            requestsList.add(it)
                        }
                    }

                    requestsList.sortByDescending { it.timestamp }

                    if (requestsList.isEmpty()) {
                        showEmptyState()
                    } else {
                        showRequestList()
                    }

                    requestAdapter.updateRequests(requestsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@RequestHistoryActivity,
                        "Failed to load requests: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
            })
    }

    private fun showRequestDetails(request: ServiceRequest) {
        val message = """
            Service: ${request.serviceType}
            Status: ${request.status}
            
            Description:
            ${request.problemDescription}
            
            Urgency: ${request.urgency}
            Preferred Date: ${request.getFormattedPreferredDate()}
            Contact Via: ${request.contactPreference}
            
            Submitted: ${request.getFormattedTimestamp()}
        """.trimIndent()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Request Details")
            .setMessage(message)
            .setPositiveButton("OK", null)

        // Only show cancel button if request is not completed or already cancelled
        if (request.status != "completed" && request.status != "cancelled") {
            dialog.setNeutralButton("Cancel Request") { _, _ ->
                cancelRequest(request)
            }
        }

        dialog.show()
    }

    private fun cancelRequest(request: ServiceRequest) {
        if (request.status == "completed" || request.status == "cancelled") {
            Toast.makeText(this, "Cannot cancel this request", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Cancel Request")
            .setMessage("Are you sure you want to cancel this service request?")
            .setPositiveButton("Yes") { _, _ ->
                database.child("service_requests")
                    .child(request.id)
                    .child("status")
                    .setValue("cancelled")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to cancel: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        rvRequestHistory.visibility = View.GONE
    }

    private fun showRequestList() {
        emptyState.visibility = View.GONE
        rvRequestHistory.visibility = View.VISIBLE
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    private fun navigateToRequest() {
        startActivity(Intent(this, RequestPage::class.java))
        finish()
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfilePage::class.java))
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHome()
    }
}