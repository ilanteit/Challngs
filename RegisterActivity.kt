package com.example.myapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapp.Model.Billboard
import com.example.myapp.Model.Users
import com.example.myapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth:FirebaseAuth
    // realtime data base variables
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef_2: DatabaseReference

    //profile image var
    private lateinit var selectedImg: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImgString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef_2 = FirebaseDatabase.getInstance().getReference("Billboard")

        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //allows to go to login page
        binding.registerPage.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

                //image view for profile picture
        binding.loginImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)

        }



        // set user information  - not loading to db yet
        binding.btnSignUp.setOnClickListener{
            val email = binding.registerEmail.text.toString()
            val fName = binding.registerFName.text.toString()
            val lName = binding.registerLName.text.toString()
            val pass = binding.registerPass.text.toString()

            val userId = dbRef.push().key!! //the unique key

            //variable thats holds all the register values

            val user_billboard= Billboard(fName,0)

            val reference = storage.reference.child("Profile").child(Date().time.toString())



            if(email.isNotEmpty() && fName.isNotEmpty() && lName.isNotEmpty() && pass.isNotEmpty()){
                //create user and go to login screen
                firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if(it.isSuccessful){

                        reference.putFile(selectedImg).addOnCompleteListener {
                            if (it.isSuccessful) {
                                reference.downloadUrl.addOnSuccessListener { task ->
                                    selectedImgString = task.toString()
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    val full_user= Users(fName,lName,email,pass,0,task.toString())
                                    //insert User data to our real time data base
                                    if (userId != null) {
                                        dbRef.child(userId).setValue(full_user).addOnCompleteListener{Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_LONG).show()}
                                    }
                                    //insert user info into billboard table
                                    if (userId != null) {
                                        dbRef_2.child(userId).setValue(user_billboard).addOnCompleteListener{}
                                    }

                                }
                            }
                        }





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
            //updates the image profile after selecting an image
       override fun onActivityResult(requestCode: Int, resultCode:Int, data :Intent?){
            super.onActivityResult(requestCode,resultCode,data)
            if(data!=null)
            {
                if(data.data!=null) {
                    selectedImg = data.data!!
                    binding.loginImage.setImageURI(selectedImg)
                }
            }
        }
}