/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */

package com.boub.ethicsearchwidget.searchproposal

import android.content.Context
import android.text.Spanned
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.core.text.HtmlCompat
import kotlin.collections.ArrayList

class ESWSearchListViewAdapter( context: Context?, resource: Int ) : ArrayAdapter<Spanned?>(context!!, resource) {
    val arrayFilter = ArrayFilter()

    override fun getItem(position: Int): Spanned? {
        return HtmlCompat.fromHtml(arrayFilter.values[position], HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun getCount(): Int {
        return arrayFilter.values.size
    }

    override fun getFilter(): Filter {
        return arrayFilter
    }

    inner class ArrayFilter : Filter() {
        var values = ArrayList<String>()

        override fun performFiltering(prefix: CharSequence): FilterResults {
            val results = FilterResults()
            results.values = values
            results.count = values.size
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults( constraint: CharSequence, results: FilterResults )
        {
            if (results.count > 0) {
                values = results.values as ArrayList<String>
                notifyDataSetChanged()
            } else {
                values = ArrayList()
                notifyDataSetInvalidated()
            }
        }
    }
}