/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */

package com.boub.ethicsearchwidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.boub.ethicsearchwidget.ESWPersistentConfiguration.isWidgetConfigured
import com.boub.ethicsearchwidget.searchproposal.ESWSearchListViewAdapter
import com.boub.ethicsearchwidget.searchproposal.ESWSearchSuggestionObserver
import com.boub.ethicsearchwidget.searchproposal.ESWSearchSuggestionTaskEcosia

class ESWSearchActivity : AppCompatActivity(), ESWSearchText.OnTextChangedListener {
    private lateinit var mAdapter: ESWSearchListViewAdapter

    // the widget which open the search bar
    private var mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val mObserver: ESWSearchSuggestionObserver = ESWSearchSuggestionObserver()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        mWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (!isWidgetConfigured(this, mWidgetId)) {
            /// App is not configured, then start configuration of default app
            val intent = Intent(this, ESWAppWidgetConfigure::class.java)
            startActivity(intent)
            finish()
        } else {
            mAdapter = ESWSearchListViewAdapter(this, R.layout.esw_proposed_item )
            mObserver.configure(mAdapter, this)
            ESWSearchSuggestionTaskEcosia.instance().mObserver = mObserver

            setContentView(R.layout.esw_search_activity)
            val textView : ESWSearchText = findViewById(R.id.search_text)
            textView.listener = this

//            textView.configure(this, mAdapter)

            val mResultList =
                findViewById<ListView>(R.id.result_list)
            mResultList.adapter = mAdapter
            mResultList.setOnTouchListener(OnTouchListener { _, _ ->
                if (mAdapter.count == 0) {
                    finish()
                    return@OnTouchListener true
                }
                false
            })
            mResultList.onItemClickListener = OnItemClickListener { adapterView: AdapterView<*>, _, _, _ ->
                if (adapterView.count == 0) {
                    finish()
                }
            }

            /// Register click in list of results
            mResultList.onItemClickListener = OnItemClickListener { adapterView, _, i, _ ->
                val value = adapterView.getItemAtPosition(i).toString()
                textView.setText(value)
                textView.setSelection(value.length)
            }

            /// Register click in list of results
            val button = findViewById<View>(R.id.button_clear)
            button.setOnClickListener { textView.setText("") }

            parseIntent(intent)
        }
    }

    public override fun onResume() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val textView : ESWSearchText = findViewById(R.id.search_text)
        textView.requestFocus()
        textView.launchCompletion()
        super.onResume()
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    /// Parse intent to launch action
    private fun parseIntent(intent: Intent) {
        mWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val textView : ESWSearchText = findViewById(R.id.search_text)

        if (intent.action != null && intent.action!!
                .startsWith(INTENT_ACTION_VOICE_SEARCH)
        ) {
            if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                mWidgetId = intent.action!!
                    .replace(INTENT_ACTION_VOICE_SEARCH, "").toInt()
            }
            textView.resume(mWidgetId)
            launchSpeechRecognition()
        } else {
            textView.resume(mWidgetId)
        }
    }

    private fun launchSpeechRecognition() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechIntent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            resources.getString(R.string.text_search_vocal)
        )
        try {
            startActivityForResult(speechIntent, VOICE_RECOGNITION_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                R.string.text_error_no_voice_reco,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    public override fun onPause() {
        super.onPause()
    }

    /**
     * Handle the results from the recognition activity.
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            val matches = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            var strResult = ""
            if ( matches != null )
            {
                strResult = matches[0]
            }

            val textView : ESWSearchText = findViewById(R.id.search_text)

            // we use the best match
            textView.setText(strResult)
            textView.setSelection(strResult.length)
        } else {
            Toast.makeText(
                applicationContext,
                R.string.warning_voice_reco_fail, Toast.LENGTH_SHORT
            ).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        // Public API
        const val INTENT_ACTION_VOICE_SEARCH = "com.boub.lilo.VoiceSearch"

        // private members
        private const val VOICE_RECOGNITION_REQUEST_CODE = 5151

    }

    override fun onTextChanged(text: String) {
        val clearButton = findViewById<View>(R.id.button_clear)

        if ( text.isEmpty ()) {
            clearButton.visibility = View.INVISIBLE
        }
        else {
            clearButton.visibility = View.VISIBLE
        }
    }
}