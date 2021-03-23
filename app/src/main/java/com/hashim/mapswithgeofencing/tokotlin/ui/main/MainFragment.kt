/*
 * Copyright (c) 2021/  3/ 20.  Created by Hashim Tahir
 */

package com.hashim.mapswithgeofencing.tokotlin.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.hashim.mapswithgeofencing.tokotlin.location.LocationUtis
import com.hashim.mapswithgeofencing.tokotlin.ui.events.MainStateEvent
import com.hashim.mapswithgeofencing.tokotlin.ui.events.MainStateEvent.OnCurrentLocationFound
import com.hashim.mapswithgeofencing.tokotlin.ui.events.MainStateEvent.OnMapReady
import com.hashim.mapswithgeofencing.tokotlin.ui.events.MainViewState
import com.hashim.mapswithgeofencing.tokotlin.utils.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MainFragment : Fragment() {
    lateinit var hFragmentMainBinding: FragmentMainBinding
    private val hMainViewModel: MainViewModel by viewModels()
    private var hGoogleMap: GoogleMap? = null
    private lateinit var hCategoriesAdapter: CategoriesAdapter

    @SuppressLint("MissingPermission")
    private val hMapCallBack = OnMapReadyCallback { googleMap ->
        hGoogleMap = googleMap
        hGoogleMap?.isMyLocationEnabled = true
        hMainViewModel.hSetStateEvent(OnMapReady())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    MainStateEvent.OnCategorySelected(category)
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


        if (hHasPermission()) {
            hInitMap()
            var hLocationUtis = LocationUtis(
                    context = requireContext(),
                    onLocationRetrieved = { location ->
                        hMainViewModel.hSetStateEvent(
                                OnCurrentLocationFound(location)
                        )
//                        hOnFragmentInteraction.hSetLocation(location)
                    },
                    onLocationUpdated = {}
            )
        } else {
            hRequestPermissions()
        }


    }

    private fun hSubscribeObservers() {
        hMainViewModel.hDataState.observe(viewLifecycleOwner) {
            Timber.d("Data State is ${it}")
            it.hData?.let {
                it.hMainFields?.let {
                    hMainViewModel.hSetMainData(it)
                }
            }
        }

        hMainViewModel.hMainViewState.observe(viewLifecycleOwner) {
            it.hMainFields?.let {
                hSetCurrentMarker(it)
            }

        }
    }

    private fun hSetCurrentMarker(mainfields: MainViewState.MainFields) {
        hGoogleMap?.addMarker(mainfields.currentMarkerOptions)?.showInfoWindow()
        hGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(
                        mainfields.currentLocation?.latitude!!,
                        mainfields.currentLocation?.longitude!!

                ), 12.0f))

    }

    private fun hHasPermission(): Boolean {
        return (
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun hRequestPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            hRequestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            UiHelper.hShowSnackBar(
                    view = hFragmentMainBinding.hSnackBarCL,
                    message = getString(R.string.location_permision),
                    onTakeAction = {
                        hRequestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
            )
        }
    }

    private fun hInitMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.hGoogleMap) as SupportMapFragment?
        mapFragment?.getMapAsync(hMapCallBack)
    }


    private val hRequestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    hInitMap()
                } else {
                    UiHelper.hShowSnackBar(
                            view = hFragmentMainBinding.hSnackBarCL,
                            message = getString(R.string.location_permision),
                            onTakeAction = {}
                    )
                }
            }


}