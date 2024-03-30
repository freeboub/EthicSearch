/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <freeboub@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Olivier Bouillet
 * ----------------------------------------------------------------------------
 */

package com.boub.ethicsearchwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.boub.ethicsearchwidget.ESWAppWidgetConfigure.ConfigureViewHolder
import java.util.*

internal class ESWSearchConfiguration private constructor() {
    class ESWSearchProvider(
        var name: String,
        logo: String,
        searchUrl: String,
        var webSite: String,
        localImage: String,
        resourceId: Int
    ) {
        var mLogo: String = logo
        var mSearchUrl: String = searchUrl
        private var mLocalImage: String = localImage
        var mResourceId: Int = resourceId

        // The view holder associated to the item
        var mHolder: ConfigureViewHolder? = null

        fun getLogoPath(context: Context): String {
            return context.cacheDir.absolutePath + mLocalImage
        }

        fun getLogoVoicePath(context: Context): String {
            return context.cacheDir.absolutePath + "voice_" + mLocalImage
        }

        fun getLogoBitmap(context: Context): Bitmap {
            return if (mResourceId != -1) {
                BitmapFactory.decodeResource(
                    context.resources,
                    mResourceId
                )
            } else {
                BitmapFactory.decodeFile(getLogoPath(context))
            }
        }

        fun getLogoVoiceBitmap(context: Context): Bitmap {
            return BitmapFactory.decodeFile(getLogoVoicePath(context))
        }
    }

    private val mListSearchProvider: HashMap<String, ESWSearchProvider> = HashMap()
    private val mCollectionSearchProvider = ArrayList<ESWSearchProvider>()

    private fun addEntry(
        strFriendlyName: String,
        strLogoUrl: String,
        strSearchQuery: String,
        strWebSiteUrl: String,
        strLogoPath: String,
        resourceId: Int
    ) {
        val provider = ESWSearchProvider(
            strFriendlyName,
            strLogoUrl,
            strSearchQuery,
            strWebSiteUrl,
            strLogoPath,
            resourceId
        )
        mListSearchProvider[strFriendlyName] = provider
        mCollectionSearchProvider.add(provider)
    }

    val providersOrdered: Collection<ESWSearchProvider>
        get() = mCollectionSearchProvider

    fun getProvider(name: String): ESWSearchProvider {
        return mListSearchProvider[name] ?: ESWSearchProvider(
            "Lilo",
            "https://www.lilosearch.org/img/searchhome/logolilonew.png",
            "https://search.lilo.org/searchweb.php?q=",
            "https://search.lilo.org",
            "logo_lilo.png",
            R.drawable.logo_lilo
        )
    }

    companion object {
        private var mESWSearchConfiguration: ESWSearchConfiguration = ESWSearchConfiguration()
        fun instance(): ESWSearchConfiguration {
            return mESWSearchConfiguration
        }
    }

    init {
        addEntry(
            "Lilo",
            "https://www.lilosearch.org/img/searchhome/logolilonew.png",
            "https://search.lilo.org/searchweb.php?q=",
            "https://search.lilo.org",
            "logo_lilo.png",
            R.drawable.logo_lilo
        )
        addEntry(
            "Ecosia",
            "https://d3mobp9w9ljhax.cloudfront.net/assets/images/png/logo.png",
            "https://www.ecosia.org/search?q=",
            "https://www.ecosia.org/",
            "logo_ecosia.png",
            R.drawable.logo_ecosia
        )
        addEntry(
            "Youcare",
            "https://youcare.world/img/youcare-bold.77669114.svg",
            "https://youcare.world/all?q=",
            "https://www.youcare.world",
            "logo_youcare.png",
            R.drawable.logo_youcare
        )

        addEntry(
            "Preeska",
            "https://www.preeska.com/assets/img/logo.png",
            "https://www.preeska.com/search?q=",
            "https://www.preeska.com",
            "preeska.png",
            R.drawable.logo_preeska
        )

        addEntry(
            "Ecogine",
            "https://www.projetprimates.com/wp-content/uploads/Partenaires/logoecoginegine.png",
            "http://www.logoecogine.org/index.php?cx=partner-pub-5931752900282266:2598261700&cof=FORID:10&ie=UTF-8&lr=lang_fr&sa=Recherche&page=results&largeur=1920&hauteur=1080&q=",
            "http://ecogine.org/",
            "logo_ecoginegine.png",
            R.drawable.logo_ecogine
        )
    }
}
