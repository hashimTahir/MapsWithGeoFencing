/*
 * Copyright (c) 2021/  4/ 4.  Created by Hashim Tahir
 */

package com.hashim.mapswithgeofencing.ui.main.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.hashim.mapswithgeofencing.R
import com.hashim.mapswithgeofencing.databinding.FragmentMainBinding
import com.hashim.mapswithgeofencing.others.prefrences.HlatLng
import com.hashim.mapswithgeofencing.others.prefrences.PrefTypes
import com.hashim.mapswithgeofencing.others.prefrences.SettingsPrefrences
import com.hashim.mapswithgeofencing.ui.events.MainStateEvent.*
import com.hashim.mapswithgeofencing.ui.events.MainViewState.*
import com.hashim.mapswithgeofencing.ui.main.fragments.adapter.CategoriesAdapter
import com.hashim.mapswithgeofencing.utils.PermissionsUtils.Companion.hRequestLocationPermission
import com.hashim.mapswithgeofencing.utils.UiHelper
import com.hashim.mapswithgeofencing.utils.UiHelper.Companion.hHideView
import com.hashim.mapswithgeofencing.utils.UiHelper.Companion.hShowView
import com.hashim.mapswithgeofencing.utils.geofencing.GeoFenceUtils
import com.hashim.mapswithgeofencing.utils.location.LocationUtis
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment() {
    lateinit var hFragmentMainBinding: FragmentMainBinding
    private val hMainViewModel: MainViewModel by viewModels()
    private var hGoogleMap: GoogleMap? = null
    private lateinit var hCategoriesAdapter: CategoriesAdapter

    @Inject
    lateinit var hSettingsPrefrences: SettingsPrefrences

    @SuppressLint("MissingPermission")
    private val hMapCallBack = OnMapReadyCallback { googleMap ->
        hGoogleMap = googleMap
        hGoogleMap?.isMyLocationEnabled = true
        hMainViewModel.hSetStateEvent(OnMapReady())

        hSetMapListerns()


        /*Todo : Remove this. test purpose only*/
        val hGeofencUtis = GeoFenceUtils(requireContext())

        val hCurrentHlatLng: HlatLng = hSettingsPrefrences
                .hGetSettings(PrefTypes.CURRENT_LAT_LNG_PT) as HlatLng
        val hTest = mutableMapOf<String, LatLng>()
        hTest.put("test", LatLng(hCurrentHlatLng.hLat!!, hCurrentHlatLng.hLng!!))
        hGeofencUtis.hCreateAllGeoFences(hTest as HashMap<String, LatLng>)
        hGeofencUtis.hStartService()
        hGoogleMap?.addCircle(hGeofencUtis.hShowVisibleGeoFence())
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun hSetMapListerns() {
        hGoogleMap?.setOnMarkerClickListener { marker ->
            /* Todo: Show details */
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            } else {
                marker.showInfoWindow()
            }
            hMainViewModel.hSetStateEvent(OnMarkerClicked(marker))
            return@setOnMarkerClickListener true
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        hFragmentMainBinding = FragmentMainBinding.inflate(
                inflater,
                container,
                false
        )
        return hFragmentMainBinding.root
    }

    private fun hInitCategoryRv() {


        hCategoriesAdapter = CategoriesAdapter(requireContext())
        hCategoriesAdapter.hSetCategoriesCallback { category ->
            hMainViewModel.hSetStateEvent(
                    OnCategorySelected(category)
            )
        }


        hFragmentMainBinding.hCategoriesRv.apply {
            adapter = hCategoriesAdapter
            layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hSubscribeObservers()

        hInitCategoryRv()

        hSetupListeners()

        hRequestPermissions()

    }

    private fun hSetupListeners() {
        hFragmentMainBinding.hSearchBar.setHint("Search here")
        hFragmentMainBinding.hSearchBar.setSpeechMode(true)
//        hFragmentMainBinding.hSearchBar.setOnSearchActionListener(this)

        hFragmentMainBinding.hDetailCardView.setOnClickListener {
            /*Todo Execute route*/

        }

    }

    private fun hSubscribeObservers() {
        hMainViewModel.hDataState.observe(viewLifecycleOwner) { dataState ->
            dataState.hLoading.let {
                when (it) {
                    true -> {
                        hShowView(hFragmentMainBinding.hProgressbar)
                    }
                    false -> {
                        hHideView(hFragmentMainBinding.hProgressbar)
                    }
                }
            }

            dataState.hData?.let { mainViewState ->

                mainViewState.hMainFields.hCurrentLocationVS?.let { currentLocationVS ->
                    hMainViewModel.hSetCurrentLocationData(currentLocationVS)
                }
                mainViewState.hMainFields.hNearByPlacesVS?.let { nearByPlacesVS ->
                    hMainViewModel.hSetNearByPlacesData(nearByPlacesVS)
                }
                mainViewState.hMainFields.hOnMarkerClickVS?.let { onMarkerClickVS ->
                    hMainViewModel.hSetMarkerClickData(onMarkerClickVS)
                }
            }
        }

        hMainViewModel.hMainViewState.observe(viewLifecycleOwner) { mainviewstate ->
            mainviewstate.hMainFields.hNearByPlacesVS?.let { nearByPlacesVS ->
                hCreateNearByMarker(nearByPlacesVS)
            }
            mainviewstate.hMainFields.hCurrentLocationVS?.let { currentLocationVS ->
                hSetCurrentMarker(currentLocationVS)
            }
            mainviewstate.hMainFields.hOnMarkerClickVS?.let { onMarkerClickVS ->
                hSetBottomCard(onMarkerClickVS)

            }

        }
    }

    private fun hSetBottomCard(onMarkerClickVS: OnMarkerClickVS) {
        hShowView(hFragmentMainBinding.hDetailCardView)
        hFragmentMainBinding.hAddressTv.text = onMarkerClickVS.hAddress
        hFragmentMainBinding.hNameTv.text = onMarkerClickVS.hPlaceName
    }

    private fun hCreateNearByMarker(nearByPlacesVS: NearByPlacesVS) {
        hGoogleMap?.clear()
        nearByPlacesVS.hMarkerList?.forEach {
            hGoogleMap?.addMarker(it)
        }
    }

    private fun hSetCurrentMarker(mainfields: CurrentLocationVS) {
        hGoogleMap?.addMarker(mainfields.currentMarkerOptions)?.showInfoWindow()
        hGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(
                        mainfields.currentLocation?.latitude!!,
                        mainfields.currentLocation.longitude

                ), mainfields.cameraZoom!!))

    }


    private fun hRequestPermissions() {
        hShowView(hFragmentMainBinding.hProgressbar)

        hRequestLocationPermission(
                requireContext(),
                hRequestLocationPermissionLauncher) {
            hInitMap()
            var hLocationUtis = LocationUtis(
                    context = requireContext(),
                    onLocationRetrieved = { location ->
                        hMainViewModel.hSetStateEvent(
                                OnCurrentLocationFound(location)
                        )
                    },
                    onLocationUpdated = {}
            )
        }
    }

    private fun hInitMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.hGoogleMap) as SupportMapFragment?
        mapFragment?.getMapAsync(hMapCallBack)
    }


    private val hRequestLocationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    hInitMap()
                } else {
                    UiHelper.hShowSnackBar(
                            view = hFragmentMainBinding.hSnackBarCL,
                            message = getString(R.string.location_permision),
                            onTakeAction = {
                                hRequestPermissions()
                            }
                    )
                }
            }


}