package com.example.kotlinapp

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricManager.Authenticators
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var loginBtn: Button
    private lateinit var fingerbtn: ImageView
    private lateinit var createAccountBtnTextView: TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences = getSharedPreferences("date", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginBtn = findViewById(R.id.login_btn)
        progressBar = findViewById(R.id.progress_bar)
        createAccountBtnTextView = findViewById(R.id.create_account_text_view_btn)
        fingerbtn = findViewById(R.id.fingerbtn)
        loginBtn.setOnClickListener(View.OnClickListener { v: View? -> loginUser() })
        createAccountBtnTextView.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(this@LoginActivity, CreateAccountActivity::class.java)
            )
        })
        //fingerbtn.setOnClickListener((v)->biometric());
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> Toast.makeText(
                this,
                "App can authenticate using biometrics.",
                Toast.LENGTH_LONG
            ).show()

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Toast.makeText(
                this,
                "No biometric features available on this device",
                Toast.LENGTH_LONG
            ).show()

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Toast.makeText(
                this,
                "Sensor no available or busy",
                Toast.LENGTH_LONG
            ).show()

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                enrollIntent.putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL
                )
                startActivityForResult(enrollIntent, REQUEST_CODE)
            }
        }
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this,
            executor!!, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val islogin = sharedPreferences.getBoolean("KEY_ISLOGIN", false)
                    if (islogin) {
                        val saved_user = sharedPreferences.getString("KEY_EMAIL", "")
                        val saved_password = sharedPreferences.getString("KEY_PASSWORD", "")
                        loginAccountInFirebase(saved_user, saved_password)
                        Toast.makeText(
                            applicationContext,
                            "Authentication succeeded!", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please login first to enable Biometric Auth", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        promptInfo = PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
        fingerbtn.setOnClickListener(View.OnClickListener { view: View? ->
            biometricPrompt!!.authenticate(
                promptInfo!!
            )
        })
    }

    fun loginUser() {
        val email = emailEditText!!.text.toString()
        val password = passwordEditText!!.text.toString()
        val isValidated = validateData(email, password)
        if (!isValidated) {
            return
        }
        loginAccountInFirebase(email, password)
    }

    fun loginAccountInFirebase(email: String?, password: String?) {
        val firebaseAuth = FirebaseAuth.getInstance()
        changeInProgress(true)
        firebaseAuth.signInWithEmailAndPassword(email!!, password!!).addOnCompleteListener { task ->
            changeInProgress(false)
            if (task.isSuccessful) {
                //login is success
                if (firebaseAuth.currentUser!!.isEmailVerified) {
                    editor!!.putString("KEY_EMAIL", email)
                    editor!!.putString("KEY_PASSWORD", password)
                    editor!!.putBoolean("KEY_ISLOGIN", true)
                    editor!!.apply()

                    //go to mainactivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Utility.showToast(
                        this@LoginActivity,
                        "Email not verified, Please verify your email."
                    )
                }
            } else {
                //login failed
                Utility.showToast(this@LoginActivity, task.exception!!.localizedMessage)
            }
        }
    }

    fun changeInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar!!.visibility = View.VISIBLE
            loginBtn!!.visibility = View.GONE
        } else {
            progressBar!!.visibility = View.GONE
            loginBtn!!.visibility = View.VISIBLE
        }
    }

    fun validateData(email: String?, password: String): Boolean {
        //validate the data that are input by user.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText!!.error = "Email is invalid"
            return false
        }
        if (password.length < 6) {
            passwordEditText!!.error = "Password length is invalid"
            return false
        }
        return true
    }

    companion object {
        private const val REQUEST_CODE = 1042
    }
}