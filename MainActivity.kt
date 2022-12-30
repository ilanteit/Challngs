package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.models.Users
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var dbRef: DatabaseReference
    companion object{
        var globalList = arrayListOf<Any>()
    }

 //   var sharedPreferences: SharedPreferences? = null
      var list_temp= arrayListOf<Any>()
    private lateinit var fetchData: FetchData
    private lateinit var updateData: UpdateData
    private lateinit var deleteUser: DeleteUser
    private lateinit var user_id: String
    private lateinit var user: Users






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //dbRef = FirebaseDatabase.getInstance().getReference("Challenges")
       // val list = MutableLiveData<Any>()

        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        user_id = firebaseAuth.currentUser?.uid.toString()
//        if (user_id != null) {
//            if(user_id.isEmpty()){
//                getUsersinfo()
//            }
//        }

        fetchData= FetchData()
//         var scores = fetchData.getDescription()
//        var user_info=fetchData.getUsersinfo()
        updateData= UpdateData()
        deleteUser= DeleteUser()

        getUsersinfo()

        //go to map page
        binding.mainMapBtn.setOnClickListener {
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)

        }

        binding.fetch.setOnClickListener {
            //val user = FirebaseAuth.getInstance().currentUser?.uid
            updateData.updateUserScore(123456)
            Log.e("wow","Updated successfully")



                    }
        binding.showProfile.setOnClickListener{
            getUsersinfo()

        }







        binding.mainLogoutBtn.setOnClickListener {
            //log out
            firebaseAuth.signOut()
            signOutGoogle()
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }




    fun getUsersinfo() {


        dbRef.child(user_id).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user= snapshot.getValue(Users::class.java)!!
                binding.userName.setText(user.firstName+" " +user.lastName+" " +user.personal_score.toString() )
               // Log.e("zzzzzzzz","${user.personal_score}")
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }






//    private fun onPostExecute(name : String?) {
//        list_temp.add(name.toString())
//
//
//    }

//    private fun getDescription(name : String?)  {
//        var temp1=""
//        val temp_list = arrayListOf<Any>()
//
//        dbRef = FirebaseDatabase.getInstance().getReference("Challenges")
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (empSnap in snapshot.children) {
//                        val userData = empSnap.getValue(Challenges::class.java)
//                        if(userData!=null && userData.challenge_Name=="$name")
//                        {
//                            temp1= userData.challenge_description.toString()
//                            onPostExecute(temp1)
//
//
//
//
//                        }
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//       // Log.e("oooooooo","$globalList")
//        //return temp_list
//    }


    private fun signOutGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

    }


}


