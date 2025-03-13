package com.example.songhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity: AppCompatActivity() {
    private lateinit var login_LBL_login: MaterialTextView
    private lateinit var auth: FirebaseAuth
    private lateinit var login_BTN_login: MaterialButton
    private lateinit var login_LBL_password : TextInputEditText
    private lateinit var login_LBL_email : TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        findViews()
        initViews()

    }
    private fun findViews()
    {
        login_LBL_login=findViewById(R.id.login_LBL_login)
        login_BTN_login=findViewById(R.id.login_BTN_login)
        login_LBL_password=findViewById(R.id.login_LBL_password)
        login_LBL_email=findViewById(R.id.login_LBL_email)
    }
    private fun initViews()
    {
        login_BTN_login.setOnClickListener { view: View -> login()  }

    }
    private fun login()
    {
        var email:String
        var password:String
        email=login_LBL_email.text.toString()
        password=login_LBL_password.text.toString()
        if (email.isEmpty()){
            Toast.makeText(this,"Enter email",Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this,"Enter password",Toast.LENGTH_SHORT).show()

        }else
        {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Authentication succesful.", Toast.LENGTH_SHORT,).show()
                        val intent= Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                    }
                }
        }

    }

}