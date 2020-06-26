package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.programowanieaplikacjimultimedialnych.R
import com.example.programowanieaplikacjimultimedialnych.view_model.HolidayViewModel
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoOutput
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_post.*

class LocationsOnMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var markerList = mutableListOf<Marker>()
    private var holidayViewModel = HolidayViewModel(application = Application())
    private lateinit var markerOptions: MarkerOptions
    private var postList = mutableListOf<PostDtoOutput>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val PLACE_PICKER_REQUEST = 3
        private const val ZOOM = 8f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key))
        }
        setContentView(R.layout.activity_locations_on_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.locations_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun setUpMap() {

        if(intent.hasExtra("markerOptions")){
            val markerOptions = intent.getParcelableExtra("markerOptions") as MarkerOptions
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, ZOOM))
        }

        postList = intent.getParcelableArrayListExtra<PostDtoOutput>("postsList")

        postList.forEach { post ->
            val latLng = LatLng(post.location.latitude, post.location.longitude)
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, post.uriList[0])
            val markerOptions = MarkerOptions().position(latLng).title(post.title).icon(BitmapDescriptorFactory.fromBitmap(bitmap.scale(300)))
            markerList.add(map.addMarker(markerOptions))
        }

        map.isMyLocationEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        postList.forEach { post ->
            if (p0?.position == LatLng(post.location.latitude, post.location.longitude)) {

                val postsArray = ArrayList<PostDtoOutput>()
                postsArray.addAll(postList)

                val postA = ArrayList<PostDtoOutput>()
                postA.add(post)

                val intent = Intent(this@LocationsOnMapActivity,MainActivity::class.java)
                intent.putParcelableArrayListExtra("post", postA)
                intent.putParcelableArrayListExtra("postsList", postsArray)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        return false
    }

    fun Bitmap.scale(maxWidthAndHeight:Int):Bitmap{
        var newWidth = 0
        var newHeight = 0

        if (this.width >= this.height){
            val ratio:Float = this.width.toFloat() / this.height.toFloat()

            newWidth = maxWidthAndHeight
            // Calculate the new height for the scaled bitmap
            newHeight = Math.round(maxWidthAndHeight / ratio)
        }else{
            val ratio:Float = this.height.toFloat() / this.width.toFloat()

            // Calculate the new width for the scaled bitmap
            newWidth = Math.round(maxWidthAndHeight / ratio)
            newHeight = maxWidthAndHeight
        }

        return Bitmap.createScaledBitmap(
            this,
            newWidth,
            newHeight,
            false
        )
    }
}