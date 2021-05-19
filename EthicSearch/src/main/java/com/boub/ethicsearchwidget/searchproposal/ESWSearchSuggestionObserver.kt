/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */

package com.boub.ethicsearchwidget.searchproposal

import android.app.Activity
import java.util.*

class ESWSearchSuggestionObserver {
    private lateinit var mAdapter: ESWSearchListViewAdapter
    private lateinit var mActivity: Activity

    fun configure(_adapter: ESWSearchListViewAdapter, activity: Activity) {
        mAdapter = _adapter
        mActivity = activity
    }

    // onPostExecute displays the results of the AsyncTask.
    fun notifyResults(result: ArrayList<String>) {
        mActivity.runOnUiThread {
            mAdapter.arrayFilter.values = result
            mAdapter.notifyDataSetChanged()
        }
    }
}