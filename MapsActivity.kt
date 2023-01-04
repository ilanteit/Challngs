package com.example.myapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.myapp.Model.MarkerModel
import com.example.myapp.Model.MyItem
import com.example.myapp.databinding.ActivityMapsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.clustering.ClusterManager
import java.text.SimpleDateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    //firebase variables
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    //map variables
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    //view variables
    private lateinit var myLocationMarker: Marker
    private lateinit var markerPopUp: Dialog
    private lateinit var addMarkerPupUp: Dialog
    private lateinit var addChallenge: View // floating button add challenge
    private lateinit var focusLocation: View // focus the camera floating button
    private lateinit var mapFragment: SupportMapFragment

    //models
    private lateinit var markers: List<MarkerModel>
    private lateinit var challengeSelected: String
    private lateinit var fetchData: FetchData

    //indicators
    private var clicked = false//allows only one click on the map when adding marker
    private var markersOnMap = false

    //const variables
    private val radius = 500.0
    private val defaultZoom = 12f

    //general
    private lateinit var mContext: Context

    //drawer navigation
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    //allows to make a collection of markers with smoother view
    private lateinit var clusterManager: ClusterManager<MyItem>

    //time format
    lateinit var simpleDataFormat: SimpleDateFormat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* init view */
        mContext = this
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //floating action button
        addChallenge = findViewById(R.id.addChallengeBtn)
        focusLocation = findViewById(R.id.focus_camera_location_btn)

        /* drawer layout instance to toggle the menu icon to open
         drawer and back button to close drawer */
        drawerLayout = findViewById(R.id.my_drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // handle the navigation item click
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> {
                    Toast.makeText(mContext, "handle user profile here", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_top_scores -> {
                    setContentView(R.layout.fragment_top_scores)
                    val fragment:Fragment = TopScores()
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_top_scores,fragment)
                   // fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
                R.id.nav_logout -> {
                    firebaseAuth.signOut()
                    signOutGoogle()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
            true
        }

        /* init firebase variables */
        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        //load markers from database
        fetchData = FetchData()
        markers = fetchData.Markers_location()

        //deleting all the markers that are 1 day old
        var deleteData=DeleteData()
        deleteData.DeleteMarkers(markers)

        setMarkersOnMap() // from some reason its not displaying the markers in the onCreate
        //save device last location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getUserCurrentLocation()
        //since map
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
     //   mapFragment.getMapAsync(this)


    }

    /**
     * This method allows the user to click on the menu button (the three lines on the
     * top left side )
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
    //clicked on android phone back btn
    override fun onBackPressed() {

        intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }



    /**
     * This method ask location permissions from the user iff there arent permissions
     * then init the currentLocation and sync the map
     */
    // if there is no location permission then ask from the user permission and get current location
    private fun getUserCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }

        val getLocation =
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location

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
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("My location!")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinicon))

        //set map camera focus on the user location
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        val markerTag = UUID.randomUUID().toString()
        myLocationMarker = mMap.addMarker(markerOptions)!!
        myLocationMarker.tag = markerTag
        //currMarker.title = "My location!"
        myLocationMarker.showInfoWindow()

        //set markers from database
         setMarkersOnMap()

        //get clicked location and add new marker
        addChallenge.setOnClickListener {
            Toast.makeText(this, "Click on desired location ", Toast.LENGTH_LONG).show()
            clicked = false
            mMap.setOnMapClickListener { latlng ->
                val location = LatLng(latlng.latitude, latlng.longitude)
                if (!clicked) {// can add only one marker in each "addChallenge" text clicked
                    clicked = true
                    challengeSelected = "Clicker" //DEFAULT
                    setAddChallengeDialog(markerTag, location)//in the future add top score here

                }
            }
        }
        focusLocation.setOnClickListener{
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        }
    }

    /**
     * This method add the Marker model on the real time  database
     */

    private fun addMarkerToDatabase(
        markerTag: String,
        challengeSelected: String,
        challengeDescription: String,
        latitude: Double,
        longitude: Double,
        topScore: Int,
        personalScore: Int,
        start_time_marker: String,
        end_time_marker: String
    ) {
       // val markerId = dbRef.push().key!!
        val game = MarkerModel(
            markerTag,
            challengeSelected,
            challengeDescription,
            latitude,
            longitude,
            topScore,
            personalScore,
            start_time_marker ,
            end_time_marker,

        )
        dbRef.child(markerTag).setValue(game)
            .addOnCompleteListener {
                Log.e("www", "Marker was add to DB successfully")
            }.addOnFailureListener { err ->
                Log.e("mmm", "Marker was add to DB successfully")
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
        if(p0.tag.toString()== myLocationMarker.tag){
            //user clicked on my location icon
            Log.e("visited1","curr marker is visiter")
            p0.title = "My Location!"
            p0.showInfoWindow()
        }
        val markerModel = getMarkerModel(p0.tag.toString())
        if (markerModel != null) {
            if(markerModel.marker_id != myLocationMarker.tag) {
                setDialog(markerModel)
                Log.e("visited2","curr marker is visiter")
            }
        }
        return true
    }

    /**
     * This method set the marker Dialog that allows the user to start the challenge
     * also the challenge information will display at this dialog
     */

    private fun setDialog(marker: MarkerModel?) {
        // set view and view objects
        markerPopUp = Dialog(this)
       // markerPopUp.setCancelable(false)
        markerPopUp.setContentView(R.layout.marker_popup)
        val btnCloseDialog = markerPopUp.findViewById<TextView>(R.id.closePopup)
        val btnStartChallenge = markerPopUp.findViewById<Button>(R.id.marker_start)
        val description = markerPopUp.findViewById<TextView>(R.id.challenge_description)
        val topScore = markerPopUp.findViewById<TextView>(R.id.challenge_topScore)
        val challengeName = markerPopUp.findViewById<TextView>(R.id.challenge_name)

        //set clicked marker data
        if (marker != null) {
            description.text = marker.chall_description
            topScore.text = marker.top_score.toString()
            challengeName.text = marker.chall_name
        }
        //close dialog here
        btnCloseDialog.setOnClickListener {
            markerPopUp.dismiss()
        }
        btnStartChallenge.setOnClickListener {
            //add code to start game here
            startChallenge(marker)
        }
        markerPopUp.show()
    }

    /**
     * This method start new Challenge activity depend on each Challenge
     */

    private fun startChallenge(marker: MarkerModel?) {
        if (marker != null) {
            val intent: Intent
            when (marker.chall_name) {
                "Guess the flag" -> {
                    intent = Intent(this, FlagChallenge::class.java)
                    startActivity(intent)
                }
                "Calculator" -> {
                    intent = Intent(this, MathChallenge::class.java)
                    startActivity(intent)
                }
                "Clicker" -> {
                    intent = Intent(this, ButtonChallenge::class.java)
                    startActivity(intent)
                    finish()
                }
                "Guess The City" -> {
                    intent = Intent(this, GussTheCityChallenge::class.java)
                    startActivity(intent)
                    finish()
                }
                "Logo Challenge" -> {
                    intent = Intent(this, LogoChallenge::class.java)
                    startActivity(intent)
                }
                "Tap The Number" -> {
                    intent = Intent(this, TapTheNumChallenge::class.java)
                    startActivity(intent)
                }
                else -> {
                    Log.e("challenge", "Filed loading a challenge")
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }


            }
        }
    }

    /**
     * This method return the markerModel by the given marker tag
     */

    private fun getMarkerModel(id: String): MarkerModel? {
        for (marker in markers) {
            if (id == marker.marker_id) {
                return marker
            }
        }
        return null
    }

    /**
     * This method add the markers from the markers list  to the map
     */
    private fun setMarkersOnMap() {
        for (marker in markers) {
            //add check in case that the marker ttl is over
            val latLng = LatLng(marker.lat as Double, marker.long as Double)
            val iconID = getMarkerIcon(marker.chall_name as String)
            val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(iconID))
            //setUpClusterer(marker)
            mMap.addMarker(markerOptions)?.tag = marker.marker_id
        }
    }

    /**
     * This method display the user dialog that allows to choose the specific challenge
     * to add to the map
     */
    private fun setAddChallengeDialog(markerTag: String, markerLocation: LatLng) {
        addMarkerPupUp = Dialog(mContext)
        addMarkerPupUp.setCancelable(false)
        addMarkerPupUp.setContentView(R.layout.add_marker_popup)
        val btnCloseDialog = addMarkerPupUp.findViewById<TextView>(R.id.closePopup)
        val btnSetChallenge = addMarkerPupUp.findViewById<Button>(R.id.set_challenge)

        val challenges = resources.getStringArray(R.array.challenges)
        val spinner = addMarkerPupUp.findViewById<Spinner>(R.id.challenge_spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, challenges
            )
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    challengeSelected = challenges[position]

                    //wait for the user to click "select"
                    btnSetChallenge.setOnClickListener {
                        val currMarkerTag = UUID.randomUUID().toString()
                        val markerIconID = getMarkerIcon(challengeSelected)
                        val challengeDescription = getDescription(challengeSelected)
                        val currMarker = mMap.addMarker(MarkerOptions().position(markerLocation).icon(
                            BitmapDescriptorFactory.fromResource(markerIconID)
                        ))!!
                        currMarker.tag = currMarkerTag
                        Toast.makeText(mContext, "Success ", Toast.LENGTH_SHORT).show()

                        //save the current time and date of the marker
                        var calendar=Calendar.getInstance()
                        simpleDataFormat= SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                        var date1=simpleDataFormat.format(calendar.time)
                        calendar.add(Calendar.DATE,1)
                        var date2=simpleDataFormat.format(calendar.time)



                        //add to database
                        addMarkerToDatabase(
                            currMarkerTag,
                            challengeSelected,
                            challengeDescription,
                            markerLocation.latitude,
                            markerLocation.longitude,
                            0,
                            0,
                            start_time_marker =date1,
                            end_time_marker =date2,

                        )
                        updateMarkersOnTheList()
                        addMarkerPupUp.dismiss()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Toast.makeText(mContext, "Please choose challenge ", Toast.LENGTH_SHORT).show()
                }
            }
            btnCloseDialog.setOnClickListener {
                addMarkerPupUp.dismiss()
            }
            addMarkerPupUp.show()
        }
    }



    private fun getDescription(name: String): String {
        val challenges = resources.getStringArray(R.array.challenges_description)
        for (challenge in challenges) {
            val challengePair = challenge.split(":")
            if (challengePair[0] == name) {
                return challengePair[1]
            }
        }
        return "Exception"
    }


    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

    }

    /**
     * This method return the marker icon id depend the name of the given challenge
     */

    private fun getMarkerIcon(name:String):Int {
        when(name){
            "Guess the flag" -> {
                return R.drawable.flagicon
            }
            "Calculator" -> {
                return R.drawable.calcuicon
            }
            "Clicker" -> {
                return R.drawable.clickericon
            }
            "Guess The City" -> {
                return R.drawable.cityicon
            }
            "Logo Challenge" -> {
                return R.drawable.logoicon
            }
            "Tap The Number" -> {
                return R.drawable.numbericon
            }
            else -> {
                Log.e("icons", "Filed loading icon")
                return R.drawable.pinicon
            }

        }

    }
    private fun updateMarkersOnTheList(){
        markers = fetchData.Markers_location()
    }
    //    private fun setUpClusterer(marker:MarkerModel) {
//        // Initialize the manager with the context and the map.
//        // (Activity extends context, so we can pass 'this' in the constructor.)
//        clusterManager = ClusterManager(mContext, mMap)
//
//        // Point the map's listeners at the listeners implemented by the cluster
//        // manager.
//        mMap.setOnCameraIdleListener(clusterManager)
//        //mMap.setOnMarkerClickListener(clusterManager)
//
//        // Add cluster items (markers) to the cluster manager.
//        addItems(marker)
//    }
//
//    private fun addItems(markerModel: MarkerModel) {
//
//
//        // Create a cluster item for the marker and set the title and snippet using the constructor.
//        val infoWindowItem = MyItem(markerModel.lat as Double, markerModel.long as Double, markerModel.chall_name as String
//            ,markerModel.chall_description as  String)
//
//        // Add the cluster item (marker) to the cluster manager.
//        clusterManager.addItem(infoWindowItem)
//        }
//    }
}





