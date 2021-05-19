/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */

package com.boub.ethicsearchwidget.searchproposal

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.Locale.*

class ESWSearchSuggestionTaskEcosia {

    var mObserver: ESWSearchSuggestionObserver? = null
    private var lastCall : Call? = null
    private var okhttpClient = OkHttpClient.Builder().retryOnConnectionFailure(true).build()
    private val mstrCurrentLanguage: String = "&mkt=" + getDefault().language
    private var hasBeenReset = false

    private var callback: Callback = object : Callback {
        override fun onResponse(
            call: Call,
            response: Response
        ) {
            response.use {
                if (response.isSuccessful && response.body != null && !hasBeenReset) {
                    try {
                        val text = response.body!!.string()
                        parseResponse(text)
                    } catch (e : java.lang.Exception){}
                }
            }
            lastCall = null
        }
        override fun onFailure(call: Call, e: IOException) {
            Log.e(javaClass.name, "failure: " + e.message)
            lastCall = null
        }
    }

    private fun getUrl(strSearch: String): String {
        return ("https://ac.ecosia.org/autocomplete?q="
                + strSearch
                + mstrCurrentLanguage)
    }

    fun parseResponse(response: String) {
        val sugestionArray = JSONObject(response).getJSONArray("suggestions")
        val result = ArrayList<String>()
        for (i in 0 until sugestionArray.length() ) {
            result.add( sugestionArray.getString( i ) )
        }
        if (mObserver != null) {
            mObserver!!.notifyResults(result)
        }
    }


    @Synchronized
    fun execute(strQuery: String): Boolean {
        try {
            launchSearch(strQuery)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun launchSearch(proposal: String) {
        hasBeenReset = false
        okhttpClient = OkHttpClient.Builder().retryOnConnectionFailure(true).build()

        val strUrl = getUrl(proposal)
        val myGetRequest = Request.Builder()
            .url(strUrl)
            .build()

        lastCall?.cancel()
        lastCall = okhttpClient.newCall(myGetRequest)
        lastCall!!.enqueue(callback)
    }

    @Synchronized
    fun resetAndNotify() {
        hasBeenReset = true
        lastCall?.cancel()
        lastCall = null
        mObserver?.notifyResults(ArrayList())
    }

    companion object {
        private var mInstance: ESWSearchSuggestionTaskEcosia = ESWSearchSuggestionTaskEcosia()
        fun instance(): ESWSearchSuggestionTaskEcosia {
            return mInstance
        }
    }
}