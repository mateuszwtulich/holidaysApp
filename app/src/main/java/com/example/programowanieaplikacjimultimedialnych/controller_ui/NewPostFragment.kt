package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.programowanieaplikacjimultimedialnych.view_model.HolidayViewModel
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.Location
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoInput
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_new_post.*
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class NewPostFragment : Fragment() {

    private val holidayViewModel: HolidayViewModel = HolidayViewModel(application = Application())
    private var imagesPaths: ArrayList<String> = ArrayList()
    private lateinit var locationCoordinates: LatLng
    private lateinit var locationAddress: String
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.example.programowanieaplikacjimultimedialnych.R.layout.fragment_new_post, container, false)

        view.savePost.setOnClickListener {
            savePost()
        }

        view.addImage.setOnClickListener {
            addMultimedia()
        }

        view.select_image.setOnClickListener {
            addMultimedia()
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        view.text_input_date.setStartIconOnClickListener {
            addDate(calendar, year, month, day)
        }

        view.text_input_date.setOnClickListener {
            addDate(calendar, year, month, day)
        }

        view.dateText.setOnClickListener{
            addDate(calendar, year, month, day)
        }

        view.text_input_location.setStartIconOnClickListener {
            startLocalizationSearch()
        }

        view.text_input_location.setOnClickListener {
            startLocalizationSearch()
        }

        view.locationText.setOnClickListener {
            startLocalizationSearch()
        }


        return view
    }

    fun startLocalizationSearch(){
        val intent = Intent(activity, LocationSearch::class.java)

        if(!text_input_location.editText!!.text.isEmpty()) {
            intent.putExtra("localization", locationCoordinates)
            intent.putExtra("address", locationAddress)
        }
        startActivityForResult(intent, LOCATION_CODE)
    }

    fun savePost() {
        if (!TextUtils.isEmpty(view!!.text_input_date.editText!!.text.toString()) && !TextUtils.isEmpty(view!!.text_input_title.editText!!.text.toString())
            && !TextUtils.isEmpty(view!!.text_input_description.editText!!.text.toString()) && !TextUtils.isEmpty(view!!.text_input_location.editText!!.text.toString())
            && imagesPaths.size > 0
        ) {
            val title = view!!.text_input_title.editText!!.text.toString()
            val text = view!!.text_input_description.editText!!.text.toString()
            val localDate = LocalDate.parse(view!!.text_input_date.editText!!.text.toString(), formatter)
            val uri = imagesPaths
            val location = Location(locationCoordinates.latitude, locationCoordinates.longitude)

            val post = PostDtoInput(
                id = 0,
                title = title,
                text = text,
                date = localDate,
                uriList = uri,
                location = location
            )

            GlobalScope.launch { holidayViewModel.insert(post) }
            (activity as MainActivity).supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(
                context, com.example.programowanieaplikacjimultimedialnych.R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun addMultimedia() {
        if (checkSelfPermission(this.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED
        ) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(permissions, NewPostFragment.PERMISSION_CODE)
        } else {
            pickImageFromGallery()
        }
    }

    fun addDate(calendar: Calendar, year: Int, month: Int, day: Int) {
        val datePicker = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { viewT, Tyear, Tmonth, Tday ->
            calendar.set(Tyear, Tmonth, Tday)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            view!!.text_input_date.dateText.setText(dateFormat.format(calendar.time))
        }, year, month, day)

        datePicker.show()
    }

    fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_CODE) {
            locationCoordinates = data!!.getParcelableExtra("localization")
            locationAddress = data!!.getStringExtra("address")
            text_input_location.locationText.setText(locationAddress)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imagesPaths.clear()
            val count = data?.clipData?.itemCount
            if (count != null) {
                for (i in 0..(count - 1)) {
                    imagesPaths.add(data.clipData?.getItemAt(i)?.uri.toString())
                }
            }
            if (count == null) {
                imagesPaths.add(data?.data.toString())
            }
            val adapter = ViewPagerAdapter(requireContext(), imagesPaths.map { path -> Uri.parse(path) }, null, 1,images_viewpager.currentItem)

            view!!.images_viewpager.adapter = adapter

            if (imagesPaths.count() > 1) {
                view!!.indicator.visibility = View.VISIBLE
                view!!.indicator.attachToPager(view!!.images_viewpager)
            } else
                view!!.indicator.visibility = View.INVISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
        private const val LOCATION_CODE = 1002
        @JvmStatic
        fun newInstance() = NewPostFragment()
    }
}
