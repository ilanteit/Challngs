package com.example.myapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapp.MathChallenge
import com.example.myapp.databinding.ActivityMapsBinding
import com.example.myapp.models.MarkerModel
import com.example.myapp.models.Users
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var currentLocation :Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private lateinit var marker: Marker
    private lateinit var markerPopUp: Dialog
    private val radius = 500.0
    private val defaultZoom = 12f
    // for map custom design

    lateinit var menu_bar: LinearLayout
    lateinit var addChallenge: TextView
    // indicator - allows only one click on the map when adding marker
    private var clicked = false
    private var locationPermissionGranted = false

    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"


    //
    private lateinit var cameraPosition: CameraPosition

    // Realtime database variable
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* set layer  */
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        menu_bar = findViewById(R.id.map_bar)
        addChallenge = findViewById(R.id.mapAddChallenge)

        //save device last location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // go to main page
        binding.mapMainBtn.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)

            startActivity(intent)
        }

        getUserCurrentLocation()
        /* manage view */
        // go to main page
        binding.mapMainBtn.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        //load marker popup
        markerPopUp = Dialog(this)
        markerPopUp.setCancelable(false)
        markerPopUp.setContentView(R.layout.marker_popup)






    }
    //if there is no location permission then ask from the user permission and get current location
    private fun getUserCurrentLocation(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            !=PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)
            return
        }

        val getLocation = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            if(location != null){
                currentLocation  = location
                Toast.makeText(this,currentLocation .latitude.toString() + " " +
                        currentLocation .longitude.toString(), Toast.LENGTH_LONG).show()

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map_frag) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }
    /* when the user gave permission get the user location */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getUserCurrentLocation()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        // Get the current location of the device and set the position of the map.
        val latLng = LatLng(currentLocation.latitude,currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("My location!")

        //set map camera focus on the user location
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f))
        mMap.addMarker(markerOptions)
        //get clicked location and add new marker
        addChallenge.setOnClickListener{
            Toast.makeText(this,"Click on desired location ",Toast.LENGTH_LONG).show()
            clicked = false
            mMap.setOnMapClickListener { latlng ->
                val location = LatLng(latlng.latitude, latlng.longitude)
                if(!clicked) {// can add only one marker in each "addChallenge" text clicked
                    clicked = true
                     mMap.addMarker(MarkerOptions().position(location))!!
                    Toast.makeText(this, "success ", Toast.LENGTH_SHORT).show()



                    //add marker to DB
                    val markerId = dbRef.push().key!!
                    dbRef = FirebaseDatabase.getInstance().getReference("Markers")
                    val game= MarkerModel("Calculator",latlng.latitude,latlng.longitude,0,0)
                    dbRef.child(markerId).setValue(game)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Marker was add to DB successfully", Toast.LENGTH_LONG).show()
                }.addOnFailureListener { err ->
                            Toast.makeText(this, "Error ->Marker Failed ${err.message}", Toast.LENGTH_LONG).show()
                        }
                }
                else{

                }

            }

        }

    }

    /**
     * This method allows to draw a circle where the center of the circle is Marker
     * this circle represents the max radius that the payer can start a challenge
     */
    private fun drawCircle(location: LatLng) {
        val circleOptions = CircleOptions()
        //specify the center of the circle
        circleOptions.center(location)
        circleOptions.radius(radius)
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);

        // Border width of the circle
        circleOptions.strokeWidth(2F);

        mMap.addCircle(circleOptions)
    }






    /**
     * When the marker has clicked open the popup dialog
     * Need to load the popup dialog according to the challenge type
     */
    override fun onMarkerClick(p0: Marker): Boolean {
        //load marker dialog
        markerPopUp = Dialog(this)
        markerPopUp.setCancelable(false)
        markerPopUp.setContentView(R.layout.marker_popup)
        val btnCloseDialog = markerPopUp.findViewById<TextView>(R.id.closePopup)
        val btnStartChallenge = markerPopUp.findViewById<Button>(R.id.marker_start)
        btnCloseDialog.setOnClickListener{
            markerPopUp.dismiss()
        }
        btnStartChallenge.setOnClickListener{
            //add code to start game here
            val intent = Intent(this, ButtonChallenge::class.java)
            startActivity(intent)
        }
        markerPopUp.show()
        return true
    }

}