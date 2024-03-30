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
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.boub.ethicsearchwidget.ESWImageManager.DownloadImage
import com.boub.ethicsearchwidget.ESWSearchConfiguration.ESWSearchProvider
import java.util.*

class ESWAppWidgetConfigure : Activity() {
    // new widget Id
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // Saved valued for current widget
    private val mDefaultSelectedItem = -1
    private var mSelectedRadioButtonId = mDefaultSelectedItem
    private var mSearchProviderName = ""

    private lateinit var mCheckBoxEnableVoiceSearch : CheckBox

    // The View Holder is handling a line of the item list
    internal class ConfigureViewHolder( convertView : View, searchProvider : ESWSearchProvider ) {

        // Name of the search engine
        val text: TextView = convertView.findViewById(R.id.configure_list_name)

        // image associated to the provider
        val image: ImageView = convertView.findViewById(R.id.configure_list_image)

        // RadioButton to select a provider
        var checkBox: RadioButton = convertView.findViewById(R.id.configure_list_checkbox)

        // To inform user the image is loading loading
        var progressBar: ProgressBar = convertView.findViewById(R.id.configure_download_progress)

        /// Global layout of the item
        var itemLayout: RelativeLayout = convertView.findViewById(R.id.configure_list_layout)

        // The search engine associated to the item
        private var item: ESWSearchProvider = searchProvider

        // start a refresh of an item: display loading and start download of the image
        fun refresh() {
            progressBar.visibility = View.VISIBLE
            image.visibility = View.INVISIBLE
            text.text = item.name
            item.mHolder = this
            DownloadImage().execute(item)
        }
    }

    // Adapter for the list of configuration provider
    private inner class ConfigureListAdapter(
        context: Context, textViewResourceId: Int,
        searchProviderList: ArrayList<ESWSearchProvider?>
    ) : ArrayAdapter<ESWSearchProvider?>(context, textViewResourceId, searchProviderList) {

        // The list of choice
        private val mSearchProviderList: ArrayList<ESWSearchProvider> = ArrayList()
        private val layoutInflater : LayoutInflater = getSystemService( Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater
        private var mSelectedRadioButton: RadioButton? = null

        @SuppressLint("InflateParams")
        override fun getView(
            position: Int,
            view: View?,
            parent: ViewGroup
        ): View {
            var convertView = view

            if (convertView == null) {

                convertView = layoutInflater.inflate(R.layout.esw_configure_provider_item, null)

                // Init the  view holder
                val holder = ConfigureViewHolder(convertView, mSearchProviderList[position])
                holder.refresh()


                if (position == mSelectedRadioButtonId ) {
                    holder.checkBox.isChecked = true
                    mSearchProviderName = holder.text.text.toString()
                    mSelectedRadioButton = holder.checkBox
                }
                holder.image.setOnClickListener { clickedView ->
                    val parentView = clickedView.parent as ViewGroup
                    val listView = parentView.parent as ViewGroup
                    val textView = listView.findViewById<TextView>(R.id.configure_list_name)
                    val provider = ESWSearchConfiguration.instance().getProvider(textView.text.toString())

                    val activityIntent = Intent(Intent.ACTION_VIEW)
                    activityIntent.data = Uri.parse(provider.webSite)
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        applicationContext.startActivity(activityIntent)
                    } catch (e: Exception) {
                        /// Surely because no browser installed
                        Toast.makeText(
                            applicationContext,
                            R.string.text_no_browser,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                holder.itemLayout.setOnClickListener { v ->
                    onItemClicked( v as ViewGroup )
                }
                holder.checkBox.setOnClickListener { v ->
                    onItemClicked( v.parent as ViewGroup )
                }

                convertView.tag = holder

            }
            return convertView!!
        }

        fun onItemClicked( parentView : ViewGroup )
        {
            val listParentParent = parentView.parent as ViewGroup
            val textView = parentView.findViewById<TextView>(R.id.configure_list_name)
            val cb =  parentView.findViewById<RadioButton>(R.id.configure_list_checkbox)
            if (mSelectedRadioButton != cb) {
                // uncheck previous
                mSelectedRadioButton?.isChecked = false
                mSelectedRadioButton = cb
                cb.isChecked = true
                mSelectedRadioButtonId = listParentParent.indexOfChild(parentView)
                mSearchProviderName = textView.text.toString()
            }
        }

        init {
            for (i in searchProviderList.indices) {
                mSearchProviderList.add(searchProviderList[i]!!)
            }
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("CHECKBOX_ID", mSelectedRadioButtonId)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.esw_configure)
        /* configure search provider list*/
        mSelectedRadioButtonId = bundle?.getInt("CHECKBOX_ID") ?: mDefaultSelectedItem
        mCheckBoxEnableVoiceSearch = findViewById(R.id.configure_enable_voice_search)
        ESWImageManager( this )

        val listView = findViewById<ListView>(R.id.configure_list)

        val listItems =
            ArrayList(
                ESWSearchConfiguration.instance().providersOrdered
            )
        val adapter = ConfigureListAdapter(
            this,
            R.layout.esw_configure_provider_item,
            listItems
        )
        listView.adapter = adapter
        adapter.notifyDataSetChanged()

        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        val configureButton = findViewById<Button>(R.id.configure)
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            configureButton.setText(R.string.text_app_configure)
        }
        configureButton.setOnClickListener(onValidate)
    }

    private var onValidate = View.OnClickListener {
        val layoutId = R.layout.esw_layout_standard_search_bar
        if (mSelectedRadioButtonId == -1) {
            Toast.makeText(
                applicationContext,
                R.string.warning_no_engine_selected,
                Toast.LENGTH_LONG
            ).show()
            return@OnClickListener
        }

        ESWPersistentConfiguration.setSearchProvider(applicationContext, mAppWidgetId, mSearchProviderName )
        if (mCheckBoxEnableVoiceSearch.isChecked) {
            ESWPersistentConfiguration.enableVoiceSearch(applicationContext, mAppWidgetId)
        } else {
            ESWPersistentConfiguration.disableVoiceSearch(applicationContext, mAppWidgetId)
        }
        ESWPersistentConfiguration.setWidgetConfigured(applicationContext, mAppWidgetId)

        val appWidgetManager =
            AppWidgetManager.getInstance(applicationContext)
        val remoteViews = RemoteViews(packageName, layoutId)
        appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews)
        appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, layoutId)
        ESWAppWidgetProvider.forceUpdate(applicationContext, mAppWidgetId)

        val intent = Intent(applicationContext, ESWSearchActivity::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        if (mCheckBoxEnableVoiceSearch.isChecked) {
            intent.action = ESWSearchActivity.INTENT_ACTION_VOICE_SEARCH + mAppWidgetId
        }
        startActivity(intent)
        finish()
    }
}