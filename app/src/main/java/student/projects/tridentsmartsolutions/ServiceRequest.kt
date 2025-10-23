package student.projects.tridentsmartsolutions

import java.util.Date

data class ServiceRequest(
    var id: String = "",
    val serviceType: String = "",
    val problemDescription: String = "",
    val urgency: String = "",
    val preferredDate: Long = 0L, // Store as timestamp for Firebase
    val contactPreference: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, in_progress, completed, cancelled
    val userId: String = ""
)
{
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", 0L, "", 0L, "pending", "")

    // Helper function to get formatted date
    fun getFormattedPreferredDate(): String {
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