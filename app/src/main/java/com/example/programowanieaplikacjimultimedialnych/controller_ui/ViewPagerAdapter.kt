
package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.renderscript.RenderScript
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.Constraints.TAG
import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ViewPagerAdapter internal constructor(private val context: Context,
                                            private val imageUrls: List<Uri>,
                                            private val onPostListener: HolidayListAdapter.OnPostListener?,
                                            private val position_post: Int,
                                            private val positionImage: Int) :
    PagerAdapter(){

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val imageView = if(positionImage == position)
            picassoLoadWithPriority(position,Picasso.Priority.HIGH)
        else
            picassoLoadWithPriority(position,Picasso.Priority.NORMAL)

        imageView.transitionName = "trans_($position_post,$position)"
        imageView.tag = "image$position"

        container.addView(imageView)

        imageView.setOnClickListener {
            Log.d("Image tag:" , imageView.tag.toString())
            Log.d("List postion:" , position_post.toString())
            onPostListener?.onPostClick(position_post,position)
        }

        return imageView
    }

    private fun picassoLoadWithPriority(position :Int, priority: Picasso.Priority) :ImageView{
        val imageView = ImageView(context)

        Picasso.get()
            .load(imageUrls[position])
            .centerCrop()
            .priority(priority)
            .fit()
            .into(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}