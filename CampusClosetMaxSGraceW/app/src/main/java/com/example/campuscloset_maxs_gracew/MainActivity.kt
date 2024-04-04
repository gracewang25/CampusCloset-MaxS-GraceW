package com.example.campuscloset_maxs_gracew

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var progressStatus = 0
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.hide()

        // Adjusting for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the progress bar
        progressBar = findViewById(R.id.progressBar)

        // Start a thread to update the progress bar
        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus += 1

                // Update the progress bar on the UI thread
                handler.post {
                    progressBar.progress = progressStatus
                }

                // Simulate loading time
                try {
                    Thread.sleep(30)  // 30ms for each 1% progress to simulate 3000ms total
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // After loading is complete, start LoginActivity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Close MainActivity
        }).start()
    }
}
