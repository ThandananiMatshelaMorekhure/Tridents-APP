package student.projects.tridentsmartsolutions

import java.util.Date

data class ServiceRequest(
    var id: String = "",
    val serviceType: String = "",
    val problemDescription: String = "",
    val urgency: String = "",
    val preferredDate: Long = 0L, // Store as timestamp for Firebase
    val preferredTime: String = "", // ADDED: Missing field
    val contactPreference: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending", // CHANGED: pending, approved, declined, completed, cancelled
    val userId: String = "",

    // ADDED: Missing fields from Firebase with different names to avoid conflicts
    val displayPreferredDate: String = "",  // was formattedPreferredDate
    val displayUrgency: String = "",        // was urgencyLevel
    val displayTimestamp: String = "",      // was formattedTimestamp
    val adminNotes: String = "",
    val updatedBy: String = "",
    val lastUpdated: Long = 0L
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", 0L, "", "", 0L, "pending", "", "", "", "", "", "", 0L)

    // Helper function to get formatted date
    fun getFormattedPreferredDate(): String {
        if (preferredDate == 0L) return "Not specified"
        return java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(Date(preferredDate))
    }

    // Helper function to get formatted timestamp
    fun getFormattedTimestamp(): String {
        return java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(Date(timestamp))
    }

    // Helper function to get urgency level (extract first word)
    fun getUrgencyLevel(): String {
        return urgency.split(" ").firstOrNull() ?: urgency
    }
}