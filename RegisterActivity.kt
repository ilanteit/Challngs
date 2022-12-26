package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapp.databinding.ActivityRegisterBinding
import com.example.myapp.models.Billboard
import com.example.myapp.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth:FirebaseAuth

   // realtime data base variables
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef_2: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef_2 = FirebaseDatabase.getInstance().getReference("Billboard")

        //allows to go to login page
        binding.registerLoginPage.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }


        // set user information  - not loading to db yet
        binding.btnSigUp.setOnClickListener{
            val email = binding.registerEmail.text.toString()
            val fName = binding.registerFName.text.toString()
            val lName = binding.registerLName.text.toString()
            val pass = binding.registerPass.text.toString()

            val userId = dbRef.push().key!! //the unique key

            //variable thats holds all the register values
            val full_user= Users(fName,lName,email,pass,0)
            val user_billboard=Billboard(fName,0)



            if(email.isNotEmpty() && fName.isNotEmpty() && lName.isNotEmpty() && pass.isNotEmpty()){
                //create user and go to login screen
                firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if(it.isSuccessful){



                        //insert User data to our real time data base
                        dbRef.child(userId).setValue(full_user).addOnCompleteListener{Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_LONG).show()}
                        //insert user info into billboard table
                        dbRef_2.child(userId).setValue(user_billboard).addOnCompleteListener{}



                        val intent = Intent(this,LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this,it.exception.toString(),Toast.LENGTH_LONG).show()
                    }
                }


            }
            else {
                Toast.makeText(this,"empty fields",Toast.LENGTH_LONG).show()
            }

        }
    }
}