package com.example.exemploapi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
    lateinit var text: TextView
    lateinit var button: Button
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        text = findViewById(R.id.text)
        button = findViewById(R.id.button)
        button.setOnClickListener {
            text.text = "Loading..."
            fetchMessage { message ->
                text.text = message
            }
        }
    }

    private fun fetchMessage(callback: (String) -> Unit) {
        try {
            val request = okhttp3.Request.Builder()
                .url("http://172.17.9.92:3000/api/hello")
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    runOnUiThread { callback("Error: ${e.message ?: "Unknown error"}") }
                }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            runOnUiThread { callback("Error: HTTP ${response.code}") }
                            return
                        }

                        val body = response.body?.string() ?: run {
                            runOnUiThread { callback("Error: Empty response body") }
                            return
                        }
                        try {
                            val json = org.json.JSONObject(body)
                            val message = json.getString("message")
                            runOnUiThread { callback(message) }
                        } catch (e: Exception) {
                            runOnUiThread { callback("Error: ${e.message ?: "Invalid JSON format"}") }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            runOnUiThread { callback("Error: ${e.message ?: "Failed to create request"}") }
        }
    }
    }