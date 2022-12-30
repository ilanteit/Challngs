package com.example.myapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateData {
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

     fun updateUserScore(newScore: Int){
         val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")

         if (user != null) {
             reference.child(user).child("personal_score").setValue(newScore)
         }

    }
    fun changePassword(id : String, newPass: Int){
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")
        reference.child(id).child("pass").setValue(newPass)

    }



}

