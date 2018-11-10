package com.cyberschnitzel.phonear

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ScaleController
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import android.view.ViewGroup



class PhoneDialog(context: Context, transformationSystem: TransformationSystem) :
        TransformableNode(transformationSystem), InputChangedTrigger {

    private var transitionsContainer: ViewGroup? = null

    lateinit var inputRenderable: ViewRenderable
    lateinit var parentPhoneNameInput: EditText
    private lateinit var phoneNameInput: EditText
    private var phoneAdapter: AutoCompletePhoneAdapter? = null
    lateinit var phoneSelectedTrigger: PhoneSelectedTrigger
    var suggestionListRecyclerView: RecyclerView? = null


    init {
        fillSugestionsList()
        ViewRenderable.builder()
                .setView(context, R.layout.dialog_phone_searcher)
                .build()
                .thenAccept { renderable ->
                    inputRenderable = renderable

                    val view = renderable.view  // Get the view

                    transitionsContainer = view.findViewById(R.id.dialog_layout)

                    // Set the edit text
                    phoneNameInput = transitionsContainer!!.findViewById(R.id.phone_name_input) as EditText
                    phoneNameInput.setOnClickListener(phoneNameInputClickListener)

                    // Set the button
                    val actionButton = transitionsContainer!!.findViewById(R.id.action_button) as Button
                    actionButton.setOnClickListener(onShowPhoneClickListener)

                    // TODO delete this dummy
                    suggestionListRecyclerView = transitionsContainer!!.findViewById(R.id.suggestion_list) as RecyclerView
                    suggestionListRecyclerView!!.layoutManager = LinearLayoutManager(context)
                    phoneAdapter = AutoCompletePhoneAdapter(fillSugestionsList(), context)
                    suggestionListRecyclerView!!.adapter = phoneAdapter
                    suggestionListRecyclerView!!.visibility = View.GONE

                    // Set the Node rendarable
                    this.renderable = inputRenderable
                }
                .exceptionally { throwable ->
                    throw AssertionError("Could not load plane card view.", throwable)
                }

        scaleController.minScale = 0.2f
        scaleController.maxScale = 0.3f
        translationController.isEnabled = false


    }

    private val phoneNameInputClickListener = View.OnClickListener {
        if (::parentPhoneNameInput.isInitialized) {
            parentPhoneNameInput.isFocusableInTouchMode = true
            parentPhoneNameInput.requestFocusFromTouch()
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(parentPhoneNameInput, 0)

        }
    }

    private val onShowPhoneClickListener = View.OnClickListener {
        if (phoneNameInput.text.toString() != "") {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(parentPhoneNameInput.windowToken, 0)

            // Find the phone and build it
            ModelRenderable.builder()
                    .setSource(context, Uri.parse("Phone_01.sfb"))
                    .build()
                    .thenAccept { model ->
                        val phone = Phone(context, transformationSystem,
                                PhoneData("samsung_note_9", Size(70.0f, 143.0f, 7.7f),
                                        "android", "snapdragon", "12mp"),
                                model)
                        phoneSelectedTrigger.onPhoneSelected(phone)
                        phoneNameInput.setText("")
                    }
                    .exceptionally { throwable ->
                        Log.d(TAG, throwable.localizedMessage)
                        val toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        return@exceptionally null
                    }
        }

    }

    override fun updateText(text: String) {
        phoneNameInput.setText(text)
    }

    override fun onUpdate(p0: FrameTime?) {
        if (!::inputRenderable.isInitialized) {
            return
        }

        if (scene == null) {
            return
        }

        val cameraPosition = scene.camera.worldPosition
        val cardPosition = worldPosition
        val direction = Vector3.subtract(cameraPosition, cardPosition)
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
        worldRotation = lookRotation
    }


    fun fillSugestionsList(): List<PhoneData> {
        return listOf(
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp"),
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp"),
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp"),
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp"),
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp"),
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp"),
                PhoneData("Iphone X", Size(1f, 2f, 3f), "ios", "Bionic 12", "12mp")
        )
    }

    fun updateSuggestions(items: List<PhoneData>) {
        if (suggestionListRecyclerView!!.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(transitionsContainer)
            suggestionListRecyclerView!!.visibility = View.VISIBLE
        }

        phoneAdapter!!.updateItems(items)
    }
}