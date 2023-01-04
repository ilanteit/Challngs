package com.example.myapp


import com.example.myapp.Model.MarkerModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DeleteData {


    fun deleteUser(id: String) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")
        reference.child(id).removeValue()
    }
    fun deleteMarker(id: String){
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Markers")
        reference.child(id).removeValue()

    }
     fun DeleteMarkers(markers:List<MarkerModel>){
        for(mark in markers){
            if(hasDayPassed(mark.start_time_marker!!,mark.end_time_marker!!))
            {
                var deleteData = DeleteData()
                deleteData.deleteMarker(mark.marker_id!!)
            }

        }



    }
    private fun hasDayPassed(date1String: String, date2String: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date1 = dateFormat.parse(date1String)
        val date2 = dateFormat.parse(date2String)
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar2.get(Calendar.YEAR) > calendar1.get(Calendar.YEAR) ||
                calendar2.get(Calendar.MONTH) > calendar1.get(Calendar.MONTH) ||
                calendar2.get(Calendar.DATE) > calendar1.get(Calendar.DATE)
    }
   private fun hasWeekPassed(date1String: String, date2String: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date1 = dateFormat.parse(date1String)
        val date2 = dateFormat.parse(date2String)
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar2.timeInMillis - calendar1.timeInMillis >= 7 * 24 * 60 * 60 * 1000
    }
}