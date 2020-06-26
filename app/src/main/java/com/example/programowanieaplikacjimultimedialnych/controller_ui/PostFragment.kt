package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.app.Application
import android.location.Address
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.programowanieaplikacjimultimedialnych.R
import com.example.programowanieaplikacjimultimedialnych.view_model.HolidayViewModel
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoOutput
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_post.*
import kotlinx.android.synthetic.main.fragment_post.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.launch

class PostFragment : Fragment(),BottomSheetDialog.Sheet {

    private var imagePosition: Int = 0
    private var postPosition: Int = 0
    private val holidayViewModel: HolidayViewModel = HolidayViewModel(application = Application())
    lateinit var postDtoOutput: PostDtoOutput
    lateinit var bottomSheetDialog: BottomSheetDialog
    private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        postponeEnterTransition()
        bottomSheetDialog  =  BottomSheetDialog()


        val view = inflater.inflate(R.layout.fragment_post, container, false)
        postponeEnterTransition()

        if (arguments != null) {
            postDtoOutput = arguments?.getParcelable("post")!!
            val array = arguments?.getIntArray("positions")
            val title: TextView = view.findViewById(R.id.Title)
            val description: TextView = view.findViewById(R.id.description)
            val localization: TextView = view.findViewById(R.id.localization)
            val date: TextView = view.findViewById(R.id.dateText)
            val pagerView = view.findViewById<ViewPager>(R.id.PagerView)
            val indicator = view.findViewById<ScrollingPagerIndicator>(R.id.indicator)
            val closeButton = view.findViewById<ImageButton>(R.id.closeButton)
            val expandButton = view.findViewById<ImageButton>(R.id.expandMenu)

            closeButton.setOnClickListener {
                Thread.sleep(150)
               (activity as MainActivity).supportFragmentManager.popBackStack()
            }

            postPosition = array!![0]
            imagePosition = array[1]

            title.text = postDtoOutput.title
            description.text = postDtoOutput.text

            val address = getAddress(LatLng(postDtoOutput.location.latitude, postDtoOutput.location.longitude))
            localization.text = address
            date.text = postDtoOutput.date.format(formatter)

            val adapter = ViewPagerAdapter(context!!, postDtoOutput.uriList, null, array[0],pagerView.currentItem)
            pagerView.adapter = adapter
            pagerView.offscreenPageLimit = 6

            if (postDtoOutput.uriList.count() > 1)
                indicator.visibility = View.VISIBLE
            else
                indicator.visibility = View.INVISIBLE

            indicator.attachToPager(pagerView)
            pagerView.setCurrentItem(imagePosition, false)

            view.locationsMap.setOnClickListener {
                val intent = Intent(activity, LocationsOnMapActivity::class.java)
                intent.putExtra("postsList", arguments?.getParcelableArrayList<PostDtoOutput>("postsList"))
                intent.putExtra("markerOptions",  MarkerOptions().position(
                    LatLng(postDtoOutput.location.latitude, postDtoOutput.location.longitude)).title(address))
                startActivityForResult(intent, LOCATION_CODE)
            }

            expandButton.setOnClickListener {
                bottomSheetDialog.show(activity!!.supportFragmentManager,"example")
            }
            startPostponedEnterTransition()
        }
        return view
    }

    private fun getAddress(location: LatLng): String {
        val geocoder = Geocoder(context)
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


    override fun delete() {
        GlobalScope.launch {
            holidayViewModel.deletePost(postDtoOutput)
        }
        bottomSheetDialog.dismiss()
        val manager = (activity as  MainActivity).supportFragmentManager
        manager.popBackStackImmediate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == LOCATION_CODE && (resultCode == Activity.RESULT_OK ))
            if (data != null) {
                val fragment = PostFragment.newInstance()

                fragment.arguments = Bundle()
                fragment.arguments?.putParcelableArrayList("postsList", data!!.getParcelableArrayListExtra("postsList"))
                fragment.arguments?.putParcelable("post", data!!.getParcelableArrayListExtra<PostDtoOutput>("post")[0])
                fragment.arguments?.putIntArray("positions", intArrayOf(0, 0))

                (activity as MainActivity).supportFragmentManager.beginTransaction()
                    .replace(com.example.programowanieaplikacjimultimedialnych.R.id.fragment_container, fragment, "postFragment")
                    .commit()
            }
    }

    override fun edit(){

        val fragment = EditFragment.newInstance()
        val bundle  = Bundle()
        bundle.putParcelable("post",postDtoOutput)
        bottomSheetDialog.dismiss()
        fragment.arguments = bundle
        (activity as  MainActivity).replaceFragment(fragment)
    }

    override fun share() {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, postDtoOutput.title)
            putExtra(Intent.EXTRA_STREAM, postDtoOutput.uriList[1])
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser( shareIntent , "Udostępnij"))
    }


    companion object {
        @JvmStatic
        fun newInstance() = PostFragment()
        private const val LOCATION_CODE = 1002
    }

}