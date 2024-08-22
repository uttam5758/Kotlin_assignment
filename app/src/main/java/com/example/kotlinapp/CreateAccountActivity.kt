package com.example.kotlinapp

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createAccountBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loginBtnTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        createAccountBtn = findViewById(R.id.create_account_btn)
        progressBar = findViewById(R.id.progress_bar)
        loginBtnTextView = findViewById(R.id.login_text_view_btn)
        createAccountBtn.setOnClickListener(View.OnClickListener { v: View? -> createAccount() })
        loginBtnTextView.setOnClickListener(View.OnClickListener { v: View? -> finish() })
    }

    fun createAccount() {
        val email = emailEditText!!.text.toString()
        val password = passwordEditText!!.text.toString()
        val confirmPassword = confirmPasswordEditText!!.text.toString()
        val isValidated = validateData(email, password, confirmPassword)
        if (!isValidated) {
            return
        }
        createAccountInFirebase(email, password)
    }

    fun createAccountInFirebase(email: String?, password: String?) {
        changeInProgress(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email!!, password!!).addOnCompleteListener(
            this@CreateAccountActivity
        ) { task ->
            changeInProgress(false)
            if (task.isSuccessful) {
                //creating acc is done
                Utility.showToast(
                    this@CreateAccountActivity,
                    "Successfully create account,Check email to verify"
                )
                firebaseAuth.currentUser!!.sendEmailVerification()
                firebaseAuth.signOut()
                finish()
            } else {
                //failure
                Utility.showToast(
                    this@CreateAccountActivity, task.exception!!
                        .localizedMessage
                )
            }
        }
    }

    fun changeInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar!!.visibility = View.VISIBLE
            createAccountBtn!!.visibility = View.GONE
        } else {
            progressBar!!.visibility = View.GONE
            createAccountBtn!!.visibility = View.VISIBLE
        }
    }

    fun validateData(email: String?, password: String, confirmPassword: String): Boolean {
        //validate the data that are input by user.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText!!.error = "Email is invalid"
            return false
        }
        if (password.length < 6) {
            passwordEditText!!.error = "Password length is invalid"
            return false
        }
        if (password != confirmPassword) {
            confirmPasswordEditText!!.error = "Password not matched"
            return false
        }
        return true
    }
}