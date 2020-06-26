package com.example.programowanieaplikacjimultimedialnych.controller_ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import androidx.viewpager.widget.ViewPager
import com.example.programowanieaplikacjimultimedialnych.R
import com.example.programowanieaplikacjimultimedialnych.view_model.HolidayViewModel
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoOutput
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import net.gotev.speech.GoogleVoiceTypingDisabledException
import net.gotev.speech.Speech
import net.gotev.speech.SpeechDelegate
import net.gotev.speech.SpeechRecognitionNotAvailable
import net.gotev.speech.ui.SpeechProgressView

class MainFragment : Fragment(), MaterialSearchBar.OnSearchActionListener,
    HolidayListAdapter.OnPostListener {

    private val REQUEST_CODE_SPEACH_INPUT = 100
    private val MY_PERMISSIONS_RECORD_AUDIO = 101
    private var searchText: CharSequence = ""
    private lateinit var holidayViewModel: HolidayViewModel
    private lateinit var adapter: HolidayListAdapter
    private var postList = listOf<PostDtoOutput>()
    private lateinit var filter: Filter
    private lateinit var layout: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        postponeEnterTransition()

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        adapter = HolidayListAdapter(requireContext(), this)
        adapter.setHasStableIds(true)

        filter = adapter.filter

        view.recyclerView.adapter = adapter
        view.recyclerView.layoutManager = LinearLayoutManager(view.context)

        view.recyclerView.setHasFixedSize(true)
        view.recyclerView.setItemViewCacheSize(15)

        holidayViewModel = ViewModelProvider(this).get(HolidayViewModel::class.java)
        holidayViewModel.allPosts.observe(requireActivity(), Observer { posts ->
            posts?.let {
                adapter.setPosts(it)
                adapter.notifyDataSetChanged()
                postList = posts
            }
        })

        layout = view.findViewById(R.id.Speek)
        val progress = view.findViewById<SpeechProgressView>(R.id.progress)

        val colors = intArrayOf(
            ContextCompat.getColor(context!!, R.color.primaryColor),
            ContextCompat.getColor(context!!, R.color.searchTwo),
            ContextCompat.getColor(context!!, R.color.searchOne),
            ContextCompat.getColor(context!!, R.color.searchThree),
            ContextCompat.getColor(context!!, R.color.primaryLightColor))

        progress.setColors(colors)

        view.searchBar.setOnSearchActionListener(this)
        view.searchBar.placeHolderView.ellipsize = TextUtils.TruncateAt.END
        view.searchBar.placeHolderView.setTypeface(null, Typeface.NORMAL)
        val cardView = view.findViewById<CardView>(R.id.mt_container)

        view.searchBar.setCardViewElevation(10)
        cardView.useCompatPadding = true

        if (searchText != "")
            filter.filter(searchText)

        view.searchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (view.searchBar.isSearchEnabled)
                    searchText = charSequence
            }

            override fun afterTextChanged(editable: Editable) {
                if (view.searchBar.isSearchEnabled)
                    filter.filter(editable)
                view.searchBar.setPlaceHolder(searchText.toString())
            }
        })

        view.fab.setOnClickListener {
            (activity as MainActivity).replaceFragment(NewPostFragment.newInstance())
        }

        view.homeButton.setOnClickListener {
            view.recyclerView.stopScroll()
            view.recyclerView.layoutManager?.smoothScrollToPosition(view.recyclerView, RecyclerView.State(), 0)
        }

        startPostponedEnterTransition()
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private fun speech() {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_RECORD_AUDIO
            )
        } else {
            try {
                val endSpeechButton = view!!.findViewById<FloatingActionButton>(R.id.cancelSpeak)
                val speech = Speech.getInstance()
                val text = view!!.findViewById<TextView>(R.id.text)

                speech.startListening(progress, object : SpeechDelegate {

                    override fun onStartOfSpeech() {
                        Log.d("DEBUG","Start")
                    }

                    override fun onSpeechRmsChanged(value: Float) {}

                    override fun onSpeechPartialResults(results: List<String>) {
                        val str = StringBuilder()
                        for (result in results) {
                            text.text = str.append(result).append(" ")
                        }
                    }

                    override fun onSpeechResult(result: String) {
                        Log.d("DEBUG","Koniec")
                        searchText = result
                        layout.visibility = View.INVISIBLE
                        onSearchConfirmed(result)
                        text.text = ""
                    }
                })

                if(speech.isListening){
                    layout.visibility = View.VISIBLE
                    endSpeechButton.setOnClickListener{
                        speech.stopListening()
                    }
                }
                else{
                    speech()
                }

            } catch (exc: SpeechRecognitionNotAvailable) {
                Log.e("speech", "Speech recognition is not available on this device!")
            } catch (exc: GoogleVoiceTypingDisabledException) {
                Log.e("speech", "Google voice typing must be enabled!")
            }
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        if (enabled) {
            searchBar.text = searchText.toString()
            searchBar.searchEditText.setSelection(searchText.length)
        } else
            searchBar.setPlaceHolder(searchText.toString())
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        searchBar.disableSearch()
        filter.filter(searchText)
    }

    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_SPEECH -> speech()
            MaterialSearchBar.BUTTON_BACK -> searchBar.disableSearch()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEACH_INPUT && (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED)) {
                if (data != null) {
                val str = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toArray()!![0] as String
                searchText = str
                onSearchConfirmed(str)
            }
        }
    }

    override fun onPostClick(position: Int, image: Int) {
        val post = adapter.getFilterdPost(position)
        val fragment = PostFragment.newInstance()
        searchBar.disableSearch()

        //Dane
        val postsArray = ArrayList<PostDtoOutput>()
        postsArray.addAll(postList)
        fragment.arguments = Bundle()
        fragment.arguments?.putParcelableArrayList("postsList", postsArray)
        fragment.arguments?.putParcelable("post", post)
        fragment.arguments?.putIntArray("positions", intArrayOf(position, image))

        //Animacja
        fragment.sharedElementEnterTransition = DetailsTransition()
        fragment.sharedElementReturnTransition = DetailsTransition()
        fragment.enterTransition = Fade()
        exitTransition = Fade()

        //Wspódzielony element
        val view = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            ?.findViewById<ViewPager>(R.id.PagerView)!!.findViewWithTag<ImageView>("image$image")

        //Start Fragmentu z animacją
        (activity as MainActivity).replaceFragmentWithAnimation(fragment, view,
            "trans_($position,$image)")

    }

    //Klasa animacji
    inner class DetailsTransition : TransitionSet() {
        init {
            ordering = ORDERING_TOGETHER
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
            addTransition(ChangeBounds())
        }
    }

}
