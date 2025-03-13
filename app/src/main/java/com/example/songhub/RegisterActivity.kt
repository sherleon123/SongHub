package com.example.songhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var register_LBL_register: MaterialTextView
    private lateinit var auth: FirebaseAuth
    private lateinit var register_BTN_register: MaterialButton
    private lateinit var register_LBL_password : TextInputEditText
    private lateinit var register_LBL_email : TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        findViews()
        initViews()
    }
    private fun findViews()
    {
        register_LBL_register=findViewById(R.id.register_LBL_register)
        register_BTN_register=findViewById(R.id.register_BTN_register)
        register_LBL_password=findViewById(R.id.register_LBL_password)
        register_LBL_email=findViewById(R.id.register_LBL_email)
    }
    private fun initViews()
    {
        register_BTN_register.setOnClickListener { view: View -> signUp()  }

    }

    

    private fun signUp() {// Choose authentication providers
        var email:String
        var password:String
        email=register_LBL_email.text.toString()
        password=register_LBL_password.text.toString()
        if (email.isEmpty()){
            Toast.makeText(this,"Enter email",Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this,"Enter password",Toast.LENGTH_SHORT).show()

        }else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Register", "createUserWithEmail:success")
                        Toast.makeText(baseContext, "Register Succesful.", Toast.LENGTH_SHORT,).show()
                        val intent= Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("Register", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Register failed.", Toast.LENGTH_SHORT,).show()
                    }
                }
        }
    }

    private fun transactToNextScreen() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            transactToNextScreen()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.

            Toast.makeText(this, "Error: Failed logging in.", Toast.LENGTH_LONG).show()
            //signIn()
        }
    }
}