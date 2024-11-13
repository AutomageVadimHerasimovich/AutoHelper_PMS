package com.example.autohelper_pms

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.widget.Button
import java.io.File

import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isTablet = resources.getBoolean(R.bool.isTablet)
        requestedOrientation = if (isTablet) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val label = findViewById<TextView>(R.id.main_label)
        val password: EditText = findViewById(R.id.password)
        val login: EditText = findViewById(R.id.login)
        val loginButton = findViewById<TextView>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)

    loginButton.setOnClickListener {
    val loginText = login.text.toString().trim()
    val passwordText = password.text.toString().trim()
    if (checkCredentials(loginText, passwordText)) {
        Toast.makeText(this, "Добро пожаловать", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USERNAME", loginText)
        }
        startActivity(intent)
    } else {
        Toast.makeText(this, "Проверьте корректность ведённых данных", Toast.LENGTH_SHORT).show()
    }
}

        registerButton.setOnClickListener {
            val loginText = login.text.toString().trim()
            val passwordText = password.text.toString().trim()
            if (loginText.isNotEmpty() && passwordText.isNotEmpty()) {
                saveCredentialsToFile(loginText, passwordText)
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Пожалуйста, введите логин и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

private fun saveCredentialsToFile(login: String, password: String) {
    val fileName = "credentials.txt"
    val fileContents = "Login: $login\nPassword: $password\n"
    openFileOutput(fileName, Context.MODE_APPEND).use {
        it.write(fileContents.toByteArray())
    }
}
private fun checkCredentials(login: String, password: String): Boolean {
    val fileName = "credentials.txt"
    return try {
        val fileInputStream = openFileInput(fileName)
        val reader = BufferedReader(InputStreamReader(fileInputStream))
        var line: String?
        var storedLogin: String? = null
        var storedPassword: String? = null

        while (reader.readLine().also { line = it } != null) {
            if (line!!.startsWith("Login: ")) {
                storedLogin = line!!.split(": ")[1]
            } else if (line!!.startsWith("Password: ")) {
                storedPassword = line!!.split(": ")[1]
                if (login == storedLogin && password == storedPassword) {
                    reader.close()
                    return true
                }
            }
        }
        reader.close()
        false
    } catch (e: Exception) {
        false
    }
}}