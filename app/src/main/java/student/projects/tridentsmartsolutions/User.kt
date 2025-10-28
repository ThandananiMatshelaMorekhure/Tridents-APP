package student.projects.tridentsmartsolutions

data class User(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val registrationDate: Long = 0L,
    val role: String = "client", // Add this line - default role
    val uid: String = "" // Add user ID for easier reference
)