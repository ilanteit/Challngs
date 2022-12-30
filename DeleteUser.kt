package com.example.myapp

import com.google.firebase.database.FirebaseDatabase

class DeleteUser {
    fun deleteUser(id: String) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")
        reference.child(id).removeValue()
    }
}