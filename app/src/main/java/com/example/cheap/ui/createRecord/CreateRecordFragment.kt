package com.example.cheap.ui.createRecord

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cheap.databinding.FragmentCreateRecordBinding
import com.example.cheap.models.Item
import com.example.cheap.models.Location
import com.example.cheap.services.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class CreateRecordFragment : Fragment() {

    lateinit var imageView: ImageView;
    private lateinit var sharedPreferences: SharedPreferences
    private val REQUEST_LOCATION_PERMISSION = 100
    private var location = Location(
        id = null,
        recordId = null,
        latitude = null,
        longitude = null,
        locationName = ""
    );

    companion object {
        fun newInstance() = CreateRecordFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private lateinit var viewModel: CreateRecordViewModel
    private var _binding: FragmentCreateRecordBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        _binding = FragmentCreateRecordBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this)[CreateRecordViewModel::class.java]
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", null)
        val refreshToken = sharedPreferences.getString("refreshToken", null)
        val userId = sharedPreferences.getString("userId", null)

        // Access views using binding
        val btnTakePhoto = binding.btnTakePhoto
        val editTextDescription = binding.editTextDescription
        val editTextPrice = binding.editTextPrice
        val editTextUnitPrice = binding.editTextUnitPrice
        val btnSave = binding.btnSave
        imageView = binding.idIVImage;
        val clientService = RetrofitClient.getClientService(accessToken)
        // Add your fragment logic here
        binding.btnTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }
        btnSave.setOnClickListener {
            // Retrieve user input from EditText fields
            val description = editTextDescription.text.toString()
            val price = editTextPrice.text.toString().toDoubleOrNull() ?: 0.0
            val unitPrice = editTextUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
            // Perform necessary actions with the user input
            // For example, save the data using ViewModel
            val imageString = imageView.drawable?.toBitmap()?.bitmapToBase64();
            val item = Item(
                null,
                null,
                "Name",
                description,
                unitPrice,
                20.00,
                1.00,
                "someBarcode",
                imageString
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    requestLocationUpdates { l
                        ->
                        location.copy(
                            id = null,
                            recordId = null,
                            latitude = l.latitude,
                            longitude = l.longitude,
                            locationName = l.locationName
                        )
                    };
                    val record = com.example.cheap.models.Record(null, userId, location, item)
                    val response = clientService.createRecord(record);
                    if (!response.isSuccessful) {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                response.errorBody().toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    // Handle network or unexpected errors
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CreateRecordViewModel::class.java]
        // TODO: Use the ViewModel
    }

    fun Bitmap.toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (requestCode == 123) {
                val image = data!!.extras!!["data"] as Bitmap?
                imageView.setImageBitmap(image)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                100
            )
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, 123)
        }
    }

    private fun Bitmap.bitmapToBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun requestLocationUpdates(callback: (Location) -> Unit) {
        var long = 0.0
        var lat = 0.0

        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 20 * 1000 // 20 seconds

        // Use a local variable to hold a reference to locationCallback
        val localLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    // Handle location updates here
                    lat = location.latitude
                    long = location.longitude
                    callback(Location(null, null, lat, long, ""))
                }
            }
        }


        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            return  // Exit early if permissions are not granted
        }

        // Start location updates using localLocationCallback
        fusedLocationClient.requestLocationUpdates(
            locationRequest!!,
            localLocationCallback,
            null
        )
    }


    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
    }
}