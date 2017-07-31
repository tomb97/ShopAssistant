package com.estimote.indoorapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.service.BeaconManager
import com.estimote.indoorsdk.algorithm.IndoorLocationManager

import com.estimote.indoorsdk.cloud.Location
import com.estimote.indoorsdk.common.helpers.EstimoteIndoorHelper
import com.estimote.indoorsdk.view.IndoorLocationView

/**
 * Main view for indoor location
 */

class MainActivity : AppCompatActivity() {


    private lateinit var indoorLocationView: IndoorLocationView
    private lateinit var indoorLocationManager: IndoorLocationManager
    private lateinit var location: Location
    private lateinit var beaconManager: BeaconManager
    private val ALL_ESTIMOTE_BEACONS_BEACON_REGION = BeaconRegion("rid", null, null, null)

    companion object {
        val intentKeyLocationId = "location_id"
        fun createIntent(context: Context, locationId: String) : Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(intentKeyLocationId, locationId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get location id from intent and get location object from list of locations
        setupLocation()

        // Init indoor location view here
        indoorLocationView = findViewById(R.id.indoor_view) as IndoorLocationView

        // Give location object to your view to draw it on your screen
        indoorLocationView.setLocation(location)

        // Setup Beacon manager. It is necessary to do scanning for Location Packets advertised by your beacons.
        beaconManager = BeaconManager(this)

        // Create IndoorManager object.
        // Long story short - it takes list of scanned beacons, does the magic and returns estimated position (x,y)
        // You need to setup it with your app context and location object
        indoorLocationManager = IndoorLocationManager.create(applicationContext, location)

        // Use helper below to quickly setup listeners between BeaconManager -> LocationManager -> LocationView
        // It also configures BeaconManager scanning with best scan times for indoor positioning.
        // CUSTOMIZATION - if you want to customize this setup, feel free to do it manually.
        EstimoteIndoorHelper.setupIndoorPositioning(beaconManager, indoorLocationManager, indoorLocationView)
    }

    private fun setupLocation() {
        // get id of location to show from intent
        val locationId = intent.extras.getString(intentKeyLocationId)
        // get object of location. If something went wrong, we build empty location with no data.
        location = (application as IndoorApplication).locationsById[locationId] ?: buildEmptyLocation()
        // Set the Activity title to you location name
        title = location.name
    }

    private fun buildEmptyLocation(): Location {
        return Location("", "", true, "", 0.0, emptyList(), emptyList(), emptyList())
    }

    override fun onStart() {
        super.onStart()
        // BeaconManager needs to connect to underlying Service, this is why we use connect() method first.
        beaconManager.connect {
            // When Beacon Manager has established connection with Service, then we start Location Packet Discovery
            beaconManager.startLocationDiscovery()

            // FOR OLD PROXIMITY BEACONS:
            // if you want to scan for iBeacon packets - i.e when you have old Proximity beacons (D3.4)
            // then uncomment the line below:
            // beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_BEACON_REGION)

            // ... and inform the LocationManager to start doing it's magic :)
            indoorLocationManager.startPositioning()
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop discovery for Location packets
        beaconManager.stopLocationDiscovery()

        // FOR OLD PROXIMITY BEACONS:
        // don't forget to stop ranging here:
        // beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_BEACON_REGION)

        // Disconnect BeaconManager from underlying bluetooth Service
        beaconManager.disconnect()

        // ... and let LocationManager to stop also!
        indoorLocationManager.stopPositioning()
    }
}
