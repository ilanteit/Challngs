package com.example.myapp


import android.util.Log
import com.example.myapp.Model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UpdateData {
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user_id: String
    private lateinit var user: Users

    fun updateUserScore1(newScore: Int){
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")

        if (user != null) {
            reference.child(user).child("personal_score").setValue(newScore)
        }

    }
    fun updateUserScore(newScore: Int) {

        val user_id = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")


       // user_id = firebaseAuth.currentUser?.uid.toString()
        dbRef.child(user_id!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user= snapshot.getValue(Users::class.java)!!
                var sum=user.personal_score
                sum = sum?.plus(newScore)
                if (user != null) {
                    reference.child(user.toString()).child("personal_score").setValue(newScore)
                }
                // Log.e("zzzzzzzz","${user.personal_score}")
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    fun changePassword(id : String, newPass: Int){
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")
        reference.child(id).child("pass").setValue(newPass)
    }
    
    
    fun updatePersonalScore(newScore: Int) {
    val database = FirebaseDatabase.getInstance()
    val reference = database.getReference("Users")

    reference.child("personal_score").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val currentScore = dataSnapshot.getValue(Int::class.java) ?: 0
            val updatedScore = currentScore + newScore
            reference.child("personal_score").setValue(updatedScore)
        }

        override fun onCancelled(error: DatabaseError) {
            // An error occurred
            Log.e("Firebase", error.toException().toString())
        }
    })
}



}
