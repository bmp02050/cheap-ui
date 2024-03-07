package com.example.cheap.ui.home

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cheap.R
import com.example.cheap.databinding.FragmentHomeBinding
import com.example.cheap.services.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentHomeBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var listView: ListView
    private lateinit var adapter: CustomListAdapter

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val REQUEST_CODE_CAMERA_PERMISSION = 100

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", null)
        val refreshToken = sharedPreferences.getString("refreshToken", null)
        val userId = sharedPreferences.getString("userId", null)
        val clientService = RetrofitClient.getClientService(accessToken)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        listView = binding.listView
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = clientService.listMyRecords(userId ?: "")
                if (!response.isSuccessful) {
                    throw Exception(response.message())
                }
                val responseBody = response.body()
                responseBody?.data?.let { data ->
                    adapter = CustomListAdapter(requireContext(), data)
                    listView.adapter = adapter
                    // Notify ListView to refresh its display
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Handle exceptions here
                Log.e("HomeFragment", "Error: ${e.message}", e)
            }
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            val record = adapter.getItem(position)
            record?.let {
                val location = it.location
                location?.let {
                    val locationName = location.locationName
                    Log.d("LocationName", "Location Name: $locationName")
                    val latLng = LatLng(location.latitude ?: 0.0, location.longitude ?: 0.0)
                    val markerOptions = MarkerOptions().position(latLng).title(locationName)
                    map.addMarker(markerOptions)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                }
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!checkCameraPermissions()) {
            requestCameraPermission()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return root
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                }
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15f
    }

    private fun checkCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION
        )
    }
}
