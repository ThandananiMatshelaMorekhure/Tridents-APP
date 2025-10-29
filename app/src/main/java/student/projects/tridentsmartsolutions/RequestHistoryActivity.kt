package student.projects.tridentsmartsolutions

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var auth: FirebaseAuth
    private var requestsList = mutableListOf<ServiceRequest>()
    private var currentUserRole = RoleUtils.ROLE_CLIENT
    private var currentUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_history)

        initializeViews()
        setupFirebase()
        setupNavigation()

        // ONE-TIME DATA FIX - ADDED THIS
        oneTimeDataFix()

        // Debug and test functions
        checkAuthentication()
        testAdminPermissions()
        debugRoleCheck()

        checkUserRoleAndSetupUI()
    }

    // ADD THIS FUNCTION FOR ONE-TIME DATA FIX
    private fun oneTimeDataFix() {
        val sharedPreferences = getSharedPreferences("TridentPrefs", MODE_PRIVATE)
        val alreadyFixed = sharedPreferences.getBoolean("data_fixed", false)

        if (!alreadyFixed) {
            Log.d("DataFix", "🔄 Running one-time data fix...")
            fixAllUserIds()
            sharedPreferences.edit().putBoolean("data_fixed", true).apply()
        } else {
            Log.d("DataFix", "✅ Data fix already completed")
        }
    }

    // ADD THIS FUNCTION TO FIX USER IDs
    private fun fixAllUserIds() {
        // Map of email -> UID for all your users
        // UPDATE THESE WITH YOUR ACTUAL USER UIDs FROM FIREBASE AUTHENTICATION
        val emailToUidMap = mapOf(
            "Thanda12@gmail.com" to "um69YVtK0EePSknQPUXPSoARwgn2",
            "bob1@gmail.com" to "cxbdPFCdS6Zj1wBWG2vCMkAWs7h1", // Replace with actual UID
            "Admin@TridentSSThanda.com" to "6YlpOqe0sPPnxxS9XWg9vtFCd173",
            "thanda10icloud.com" to "5TcihcfM6sTo4JrtPFGH8PUhl6s1",
            "ndu101@gmail.com" to "0wRAEelVT8O8jTr3euUX8fuRC3P2"
            // Add all other users from your users node
        )

        Log.d("DataFix", "🔄 Starting userId fix...")

        database.child("service_requests").get().addOnSuccessListener { snapshot ->
            var fixedCount = 0
            val updates = mutableMapOf<String, Any>()

            snapshot.children.forEach { requestSnapshot ->
                val requestId = requestSnapshot.key ?: ""
                val currentUserId = requestSnapshot.child("userId").value as? String

                currentUserId?.let { email ->
                    val correctUid = emailToUidMap[email]
                    correctUid?.let { uid ->
                        // Add to batch update
                        updates["service_requests/$requestId/userId"] = uid
                        fixedCount++
                        Log.d("DataFix", "📝 Updating request $requestId: $email -> $uid")
                    } ?: run {
                        Log.w("DataFix", "⚠️ No UID found for email: $email")
                    }
                }
            }

            // Execute all updates at once
            if (updates.isNotEmpty()) {
                database.updateChildren(updates)
                    .addOnSuccessListener {
                        Log.d("DataFix", "✅ Successfully fixed $fixedCount requests")
                        Toast.makeText(this, "Fixed $fixedCount requests", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Log.e("DataFix", "❌ Failed to fix requests: ${error.message}")
                        Toast.makeText(this, "Fix failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d("DataFix", "ℹ️ No requests need fixing")
            }
        }.addOnFailureListener { error ->
            Log.e("DataFix", "💥 Failed to read service_requests: ${error.message}")
        }
    }

    // REST OF YOUR EXISTING CODE REMAINS THE SAME...
    private fun initializeViews() {
        rvRequestHistory = findViewById(R.id.rv_request_history)
        emptyState = findViewById(R.id.empty_state)

        navHome = findViewById(R.id.nav_home)
        navRequest = findViewById(R.id.nav_request)
        navHistory = findViewById(R.id.nav_history)
        navAccount = findViewById(R.id.nav_account)
    }

    private fun setupFirebase() {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""
        Log.d("RequestHistory", "🔐 Current User UID: $currentUserId")
        Log.d("RequestHistory", "📧 Current User Email: ${auth.currentUser?.email}")
    }

    private fun setupNavigation() {
        navHome.setOnClickListener { navigateToHome() }
        navRequest.setOnClickListener { navigateToRequest() }
        navHistory.setOnClickListener {
            // Already on history page, do nothing or refresh
        }
        navAccount.setOnClickListener { navigateToProfile() }
    }

    private fun checkAuthentication() {
        val currentUser = auth.currentUser

        Log.d("AuthCheck", "=== CURRENT AUTHENTICATION ===")
        Log.d("AuthCheck", "UID: ${currentUser?.uid}")
        Log.d("AuthCheck", "Email: ${currentUser?.email}")

        // Just log the info without hardcoded UID check
        Log.d("AuthCheck", "User is authenticated: ${currentUser != null}")
    }

    private fun testAdminPermissions() {
        Log.d("PermissionTest", "🧪 Testing admin permissions...")

        // Test 1: Read service_requests (should work for admin)
        database.child("service_requests").get()
            .addOnSuccessListener { snapshot ->
                Log.d("PermissionTest", "✅ SUCCESS: Can read service_requests")
                Log.d("PermissionTest", "📊 Found ${snapshot.childrenCount} requests")
                Toast.makeText(this, "Admin access working! Found ${snapshot.childrenCount} requests", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { error ->
                Log.e("PermissionTest", "❌ FAILED to read service_requests: ${error.message}")
                Toast.makeText(this, "Permission denied: ${error.message}", Toast.LENGTH_LONG).show()
            }

        // Test 2: Read all users (should work for admin)
        database.child("users").get()
            .addOnSuccessListener { snapshot ->
                Log.d("PermissionTest", "✅ SUCCESS: Can read users")
                Log.d("PermissionTest", "👥 Found ${snapshot.childrenCount} users")
            }
            .addOnFailureListener { error ->
                Log.e("PermissionTest", "❌ FAILED to read users: ${error.message}")
            }
    }

    private fun debugRoleCheck() {
        val currentUser = auth.currentUser
        val database = Firebase.database.reference

        Log.d("DebugRole", "=== ROLE CHECK DEBUG ===")
        Log.d("DebugRole", "Current UID: ${currentUser?.uid}")

        if (currentUser != null) {
            // Check what's actually in the database
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Log.d("DebugRole", "✅ User data found in database")
                        Log.d("DebugRole", "Full user data: ${snapshot.value}")
                        Log.d("DebugRole", "Role value: ${snapshot.child("role").value}")
                        Log.d("DebugRole", "Role type: ${snapshot.child("role").value?.javaClass?.simpleName}")
                    } else {
                        Log.e("DebugRole", "❌ No user data found at users/${currentUser.uid}")
                    }
                }
                .addOnFailureListener { error ->
                    Log.e("DebugRole", "💥 Failed to read user data: ${error.message}")
                }
        }
    }

    private fun checkUserRoleAndSetupUI() {
        Log.d("RequestHistory", "🔍 Checking user role...")

        RoleUtils.getCurrentUserRole { role ->
            currentUserRole = role
            Log.d("RequestHistory", "🎯 User role: $role")

            if (role == RoleUtils.ROLE_ADMIN) {
                setupAdminUI()
                loadAllRequests()
            } else {
                setupClientUI()
                loadUserRequests()
            }
        }
    }

    private fun setupAdminUI() {
        supportActionBar?.title = "Manage All Requests"
        setupRecyclerView(true)
        Log.d("RequestHistory", "👑 Admin UI setup complete")
    }

    private fun setupClientUI() {
        supportActionBar?.title = "My Requests"
        setupRecyclerView(false)
        Log.d("RequestHistory", "👤 Client UI setup complete")
    }

    private fun setupRecyclerView(isAdmin: Boolean) {
        requestAdapter = RequestHistoryAdapter(requestsList, isAdmin) { request ->
            showRequestDetails(request, isAdmin)
        }
        rvRequestHistory.layoutManager = LinearLayoutManager(this)
        rvRequestHistory.adapter = requestAdapter
    }

    private fun loadAllRequests() {
        Log.d("RequestHistory", "🔄 Loading ALL requests for admin...")

        database.child("service_requests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestsList.clear()
                    Log.d("RequestHistory", "📊 Total requests found: ${snapshot.childrenCount}")

                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(ServiceRequest::class.java)
                        request?.let {
                            it.id = requestSnapshot.key ?: ""
                            requestsList.add(it)
                            Log.d("RequestHistory", "📝 Request: ${it.serviceType} - ${it.status} - User: ${it.userId}")
                        }
                    }

                    requestsList.sortByDescending { it.timestamp }

                    if (requestsList.isEmpty()) {
                        showEmptyState()
                        Log.d("RequestHistory", "📭 No requests found")
                    } else {
                        showRequestList()
                        Log.d("RequestHistory", "✅ Displaying ${requestsList.size} requests")
                    }

                    requestAdapter.updateRequests(requestsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RequestHistory", "💥 Failed to load all requests: ${error.message}")
                    Log.e("RequestHistory", "💥 Error details: ${error.details}")
                    Toast.makeText(
                        this@RequestHistoryActivity,
                        "Failed to load requests: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showEmptyState()
                }
            })
    }

    private fun loadUserRequests() {
        if (currentUserId.isEmpty()) {
            Log.e("RequestHistory", "❌ No user UID found")
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show()
            showEmptyState()
            return
        }

        Log.d("RequestHistory", "🔄 Loading requests for user UID: $currentUserId")

        database.child("service_requests")
            .orderByChild("userId")
            .equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestsList.clear()
                    Log.d("RequestHistory", "📊 User requests found: ${snapshot.childrenCount}")

                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(ServiceRequest::class.java)
                        request?.let {
                            it.id = requestSnapshot.key ?: ""
                            requestsList.add(it)
                            Log.d("RequestHistory", "✅ Loaded request: ${it.serviceType}")
                        }
                    }

                    requestsList.sortByDescending { it.timestamp }

                    if (requestsList.isEmpty()) {
                        showEmptyState()
                        Log.d("RequestHistory", "📭 No requests found for user")
                    } else {
                        showRequestList()
                        Log.d("RequestHistory", "✅ Displaying ${requestsList.size} user requests")
                    }

                    requestAdapter.updateRequests(requestsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RequestHistory", "💥 Failed to load user requests: ${error.message}")
                    Log.d("RequestHistory", "💥 Error code: ${error.code}")
                    Toast.makeText(
                        this@RequestHistoryActivity,
                        "Failed to load requests: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showEmptyState()
                }
            })
    }

    private fun showRequestDetails(request: ServiceRequest, isAdmin: Boolean) {
        val message = buildString {
            append("Service: ${request.serviceType}\n")
            append("Status: ${request.status}\n")
            if (isAdmin) {
                append("User ID: ${request.userId}\n")
            }
            append("\nDescription:\n${request.problemDescription}\n\n")
            append("Urgency: ${request.urgency}\n")
            append("Preferred Date: ${request.getFormattedPreferredDate()}\n")
            append("Contact Via: ${request.contactPreference}\n\n")
            append("Submitted: ${request.getFormattedTimestamp()}")
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(if (isAdmin) "Manage Request" else "Request Details")
            .setMessage(message)
            .setPositiveButton("OK", null)

        // ADMIN: Show management buttons
        if (isAdmin && request.status != "completed" && request.status != "cancelled") {
            dialogBuilder.setNeutralButton("Approve") { _, _ ->
                updateRequestStatus(request.id, "approved", "Request approved by admin")
            }
            dialogBuilder.setNegativeButton("Decline") { _, _ ->
                updateRequestStatus(request.id, "declined", "Request declined by admin")
            }
        }
        // CLIENT: Show cancel button for their own requests
        else if (!isAdmin && request.status != "completed" && request.status != "cancelled") {
            dialogBuilder.setNeutralButton("Cancel Request") { _, _ ->
                cancelRequest(request)
            }
        }

        dialogBuilder.show()
    }

    private fun updateRequestStatus(requestId: String, newStatus: String, adminNote: String = "") {
        Log.d("RequestHistory", "🔄 Updating request $requestId to $newStatus")

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "lastUpdated" to System.currentTimeMillis(),
            "updatedBy" to currentUserId
        )

        // Add admin note if provided
        if (adminNote.isNotEmpty()) {
            updates["adminNotes"] = adminNote
        }

        database.child("service_requests")
            .child(requestId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Log.d("RequestHistory", "✅ Request $newStatus successfully")
                Toast.makeText(this, "Request $newStatus", Toast.LENGTH_SHORT).show()

                // Add to request history if admin
                if (currentUserRole == RoleUtils.ROLE_ADMIN) {
                    addToRequestHistory(requestId, newStatus, adminNote)
                }
            }
            .addOnFailureListener { e ->
                Log.e("RequestHistory", "💥 Failed to update request: ${e.message}")
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToRequestHistory(requestId: String, action: String, notes: String) {
        val historyData = hashMapOf<String, Any>(
            "timestamp" to System.currentTimeMillis(),
            "adminId" to currentUserId,
            "adminName" to "Thandanani",
            "action" to action,
            "notes" to notes,
            "requestId" to requestId
        )

        database.child("request_history")
            .child(requestId)
            .push()
            .setValue(historyData)
            .addOnSuccessListener {
                Log.d("RequestHistory", "📝 History updated for request $requestId")
            }
            .addOnFailureListener { e ->
                Log.e("RequestHistory", "💥 Failed to update history: ${e.message}")
            }
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
                updateRequestStatus(request.id, "cancelled", "Cancelled by user")
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        rvRequestHistory.visibility = View.GONE
        Log.d("RequestHistory", "📭 Showing empty state")
    }

    private fun showRequestList() {
        emptyState.visibility = View.GONE
        rvRequestHistory.visibility = View.VISIBLE
        Log.d("RequestHistory", "📋 Showing request list")
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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHome()
    }
}