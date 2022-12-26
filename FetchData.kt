package com.example.myapp

import android.util.Log
import com.example.myapp.models.Challenges
import com.example.myapp.models.MarkerModel
import com.example.myapp.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*



// Fetching the data from Realtime data base
class FetchData {

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth




    // Get last name by "Email"
    private fun getLastname(name : String?): String  {
        firebaseAuth = FirebaseAuth.getInstance()
        var temp1=""
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val userData = empSnap.getValue(Users::class.java)
                        if(userData!=null && userData.userEmail=="$name")
                        {
                            temp1= userData.lastName.toString()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return temp1
    }


    // Get the user score by "Email"
    private fun getUserScore(name : String?): Int  {
        var temp1=0
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val userData = empSnap.getValue(Users::class.java)
                        if(userData!=null && userData.userEmail=="$name")
                        {
                            temp1= userData.personal_score!!
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return temp1
    }
    private fun getDescription(name : String?): String  {



        var temp1=""
        dbRef = FirebaseDatabase.getInstance().getReference("Challenges")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val userData = empSnap.getValue(Challenges::class.java)
                        if(userData!=null && userData.challenge_Name=="$name")
                        {
                            temp1= userData.challenge_description.toString()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return temp1
    }



    // Returns all the info of all the markers placed
    private fun Markers_location(): List<Any>  {
        val markers_list = arrayListOf<MarkerModel>()
        dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val userData = empSnap.getValue(MarkerModel::class.java)
                        if(userData!=null)
                        {
                            markers_list.add(userData!!)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return markers_list
    }





}