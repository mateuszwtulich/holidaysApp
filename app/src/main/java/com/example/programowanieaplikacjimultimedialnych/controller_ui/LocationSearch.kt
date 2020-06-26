package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.programowanieaplikacjimultimedialnych.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.activity_location_search.*
import java.lang.Math.abs

class LocationSearch : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var markerList = mutableListOf<Marker>()
    private lateinit var placesClient: PlacesClient
    private lateinit var predictionList: MutableList<AutocompletePrediction>
    private lateinit var markerOptions: MarkerOptions

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
        setContentView(R.layout.activity_location_search)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val mapView = mapFragment.view
        fab_adding_location.hide()

        val locationButton =
            (mapView!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 300)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        searchPlace.placeHolderView.ellipsize = TextUtils.TruncateAt.END
        searchPlace.placeHolderView.setTypeface(null, Typeface.NORMAL)
        searchPlace.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {

            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString(), true, null, true)
            }

            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                    MaterialSearchBar.BUTTON_BACK -> searchPlace.disableSearch()
                }
            }

        })

        val bounds = RectangularBounds.newInstance(LatLng(-33.880490, 151.184363), LatLng(-33.880490, 151.184363))

        searchPlace.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchPlace.clearSuggestions()
                placesClient = Places.createClient(baseContext)
                val token = AutocompleteSessionToken.newInstance()
                val predictionsRequest = FindAutocompletePredictionsRequest.builder().setLocationBias(bounds)
                    .setTypeFilter(TypeFilter.CITIES)
                    .setSessionToken(token)
                    .setQuery(charSequence.toString()).build()

                placesClient.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        predictionList = task.result!!.autocompletePredictions

                        searchPlace.updateLastSuggestions(predictionList.map { prediction ->
                            prediction.getFullText(null).toString()
                        })
                        searchPlace.showSuggestionsList()
                    }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        searchPlace.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View?) {
                val suggestion: String = searchPlace.lastSuggestions[position].toString()
                placeMarkerOnMap(getLatLng(suggestion))
                searchPlace.text = suggestion
                searchPlace.clearSuggestions()
            }

            override fun OnItemDeleteListener(position: Int, v: View?) {
                val newPredictionList = ArrayList<AutocompletePrediction>()
                newPredictionList.addAll(predictionList)
                newPredictionList.removeAt(position)
                searchPlace.updateLastSuggestions(newPredictionList)
            }
        })
    }

    private fun setUpMap() {

        if (intent.hasExtra("localization") && intent.hasExtra("address")) {
            val location = intent.getParcelableExtra<LatLng>("localization")
            val address = intent.getStringExtra("address")
            val markerOptions = MarkerOptions().title(address).position(location)
            placeMarkerOnMap(markerOptions.position)
        }

    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        fab_adding_location.show()
        fab_adding_location.setOnClickListener {
            intent.putExtra("localization", p0!!.position)
            intent.putExtra("address", p0!!.title)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        return false
    }

    private fun placeMarkerOnMap(location: LatLng) {
        if(!markerList.isEmpty()){
            markerList[0].remove()
            markerList.removeAt(0)
        }
        markerOptions = MarkerOptions().position(location).title(getAddress(location))
        markerList.add(map.addMarker(markerOptions))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM))
    }

    private fun getLatLng(location: String): LatLng {
        val geocoder = Geocoder(this)
        val adresses: List<Address> = geocoder.getFromLocationName(location, 1)
        if (adresses.isNotEmpty()) {
            return LatLng(adresses[0].latitude, adresses[0].longitude)
        }
        return LatLng(0.0, 0.0)
    }

    private fun getAddress(location: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            if (addresses[0].locality != null) {
                return addresses[0].locality
            }
            if (addresses[0].countryName != null) {
                return addresses[0].countryName
            }
        }
        return "No valid address"
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                if (!intent.hasExtra("localization") && !intent.hasExtra("address")) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM))
                }
            }
        }

        map.setOnMarkerClickListener(this)

        map.setOnMapClickListener {
            fab_adding_location.hide()
            placeMarkerOnMap(it)
        }

        map.setOnMapLongClickListener { latLng ->
            markerList.forEach {
                if (abs(it.position.latitude - latLng.latitude) < 0.02 &&
                    abs(it.position.longitude - latLng.longitude) < 0.02
                )
                    it.remove()
            }

            markerList.removeIf {
                (abs(it.position.latitude - latLng.latitude) < 0.02 &&
                        abs(it.position.longitude - latLng.longitude) < 0.02)
            }
        }
        setUpMap()
        onTitleClickListener()
    }

    fun onTitleClickListener() {
        map.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener {

            override fun onInfoWindowClick(p0: Marker?) {
                intent.putExtra("localization", p0!!.position)
                intent.putExtra("address", p0!!.title)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
    }
}
