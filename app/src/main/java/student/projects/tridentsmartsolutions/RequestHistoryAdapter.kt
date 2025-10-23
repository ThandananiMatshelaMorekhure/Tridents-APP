package student.projects.tridentsmartsolutions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RequestHistoryAdapter(
    private var requests: List<ServiceRequest>,
    private val onItemClick: (ServiceRequest) -> Unit
) : RecyclerView.Adapter<RequestHistoryAdapter.RequestViewHolder>() {


    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivServiceIcon: ImageView = itemView.findViewById(R.id.iv_service_icon)
        val tvServiceType: TextView = itemView.findViewById(R.id.tv_service_type)
        val tvRequestDate: TextView = itemView.findViewById(R.id.tv_request_date)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        val tvProblemDescription: TextView = itemView.findViewById(R.id.tv_problem_description)
        val tvUrgency: TextView = itemView.findViewById(R.id.tv_urgency)
        val tvPreferredDate: TextView = itemView.findViewById(R.id.tv_preferred_date)
        val tvContactPreference: TextView = itemView.findViewById(R.id.tv_contact_preference)

        fun bind(request: ServiceRequest) {
            // Set service type
            tvServiceType.text = when (request.serviceType) {
                RequestPage.SERVICE_TYPE_PLUMBING -> "Plumbing Service"
                RequestPage.SERVICE_TYPE_SECURITY -> "Security Service"
                RequestPage.SERVICE_TYPE_EMERGENCY -> "Emergency Service"
                else -> "Service Request"
            }

            // Set icon based on service type
            val iconRes = when (request.serviceType) {
                RequestPage.SERVICE_TYPE_PLUMBING -> R.drawable.ic_request
                RequestPage.SERVICE_TYPE_SECURITY -> R.drawable.ic_request
                RequestPage.SERVICE_TYPE_EMERGENCY -> R.drawable.ic_request
                else -> R.drawable.ic_request
            }
            ivServiceIcon.setImageResource(iconRes)

            // Set dates
            tvRequestDate.text = request.getFormattedTimestamp()
            tvPreferredDate.text = request.getFormattedPreferredDate()

            // Set status with color
            tvStatus.text = request.status.capitalize()
            when (request.status.lowercase()) {
                "pending" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_pending)
                }
                "in_progress" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_in_progress)
                }
                "completed" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_completed)
                }
                "cancelled" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_cancelled)
                }
            }

            // Set other details
            tvProblemDescription.text = request.problemDescription
            tvUrgency.text = request.getUrgencyLevel()
            tvContactPreference.text = request.contactPreference

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(request)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_history, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateRequests(newRequests: List<ServiceRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}