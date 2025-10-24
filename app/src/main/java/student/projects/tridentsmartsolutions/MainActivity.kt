package student.projects.tridentsmartsolutions

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigate to RequestPage (main dashboard) when "Get Started" is clicked
        val continueButton = findViewById<MaterialButton>(R.id.btn_continue)
        continueButton.setOnClickListener {
            val intent = Intent(this, RequestPage::class.java)
            startActivity(intent)
            finish() // Close MainActivity so back button goes to login
        }
    }
}