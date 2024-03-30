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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import com.boub.ethicsearchwidget.searchproposal.ESWSearchSuggestionTaskEcosia
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


@SuppressLint("AppCompatCustomView")
class ESWSearchText : EditText, OnEditorActionListener, OnTouchListener {

    private var mBrowserlaunched = false
    private var mWidgetId = 0

    var listener : OnTextChangedListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super( context, attrs )
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int ) : super(context, attrs, defStyle)

    interface OnTextChangedListener
    {
        fun onTextChanged( text : String )
    }

    init
    {
        setOnEditorActionListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSoftInputOnFocus = true
        }
        isFocusable = true
        setOnTouchListener(this)
    }

    public override fun onTextChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        launchCompletion(s.toString())
    }

    fun resume(_widgetId: Int) {
        mWidgetId = _widgetId
        mBrowserlaunched = false
    }

    @JvmOverloads
    fun launchCompletion(s: String = this.editableText.toString()) {
        listener?.onTextChanged( s )

        if ( s.isEmpty ()) {
            ESWSearchSuggestionTaskEcosia.instance().resetAndNotify()
        }
        else {
            // encode search terms.
            var strSearch = ""
            try {
                strSearch = URLEncoder.encode(s, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            ESWSearchSuggestionTaskEcosia.instance().execute(strSearch)
        }
    }

    override fun onEditorAction( textView: TextView, i: Int,keyEvent: KeyEvent?): Boolean {
        launchSearch( textView.text.toString() )
        return true
    }

    private fun launchSearch(str: String) {
        // this test is to avoid double browser launch
        if (!mBrowserlaunched && str.isNotEmpty() ) {

            /// start browser
            val url = ESWPersistentConfiguration.getSearchProvider(
                this.context.applicationContext, mWidgetId
            )
            val prov = ESWSearchConfiguration.instance().getProvider(url)
            val intent = Intent(Intent.ACTION_VIEW)
            var strEncoded = ""
            try {
                strEncoded = URLEncoder.encode(str, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            mBrowserlaunched = true

            intent.data = Uri.parse(prov.mSearchUrl + strEncoded)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException ) {
                if (!openPlayStore( )) {
                    Toast.makeText( context, R.string.text_error_cannot_open_play_store, Toast.LENGTH_LONG ).show()
                }
                else {
                    Toast.makeText( context, R.string.text_error_redirect_to_play_store, Toast.LENGTH_LONG ).show()
                }
            }
            // allow next browser call in 1 second
            val handler = Handler(Looper.myLooper()!!)
            handler.postDelayed({ mBrowserlaunched = false }, 1000)
        }
    }

    private fun openPlayStore( ): Boolean {
        val appPackageName = "org.mozilla.firefox"

        try {
            val intent = Intent(  Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName") )
            context.startActivity( intent )
            return true
        } catch ( e: ActivityNotFoundException) {
        }
        return false
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, 0)
        return false
    }
}