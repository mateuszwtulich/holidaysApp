
package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import net.gotev.speech.Speech


class MainActivity : AppCompatActivity() {

    private  var picasso : Picasso? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.programowanieaplikacjimultimedialnych.R.layout.activity_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        replaceFragment(MainFragment.newInstance())
        Speech.init(this)

        picasso = Picasso.Builder(this)
            .memoryCache(LruCache(550000000))
            .build()
        Picasso.setSingletonInstance(picasso!!)

        Picasso.get().setIndicatorsEnabled(true)
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(com.example.programowanieaplikacjimultimedialnych.R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun replaceFragmentWithAnimation(fragment: Fragment, view: View, sharedElementName: String) {
        supportFragmentManager
            .beginTransaction()
            .addSharedElement(view, sharedElementName)
            .replace(com.example.programowanieaplikacjimultimedialnych.R.id.fragment_container, fragment,"postFragment")
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) this.finish()
        else supportFragmentManager.popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        Speech.getInstance().shutdown()
    }

    fun popStacks(int: Int){
        for(i in 0 until int)
            supportFragmentManager.popBackStackImmediate()
    }
}

