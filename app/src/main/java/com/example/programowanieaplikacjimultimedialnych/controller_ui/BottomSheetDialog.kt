package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModelProvider
import com.example.programowanieaplikacjimultimedialnych.R
import com.example.programowanieaplikacjimultimedialnych.room_database.model.Post
import com.example.programowanieaplikacjimultimedialnych.view_model.HolidayViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet.*

class BottomSheetDialog: BottomSheetDialogFragment() {

   interface Sheet{
       fun delete()
       fun edit()
       fun share()
   }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.bottom_sheet,container,false)

        val deleteButton :Button = v.findViewById(R.id.buttonDelete)
        val editButton : Button = v.findViewById(R.id.buttonEdit)
        val shareButton : Button = v.findViewById(R.id.buttonShare)

        val manager =  (activity as  MainActivity).supportFragmentManager
        val fragment =  manager.findFragmentByTag("postFragment") as PostFragment

        deleteButton.setOnClickListener{
            fragment.delete()
        }

        editButton.setOnClickListener {
            fragment.edit()
        }

        shareButton.setOnClickListener{
            fragment.share()
        }

        return v
    }

}