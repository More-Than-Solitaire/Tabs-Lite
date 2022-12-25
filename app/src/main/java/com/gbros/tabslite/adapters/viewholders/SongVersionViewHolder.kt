package com.gbros.tabslite.adapters.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.R
import com.gbros.tabslite.data.IntTabFull

class SongVersionViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

    val mVersionName: TextView = mView.findViewById(R.id.versionName)
    val mNumRatings: TextView = mView.findViewById(R.id.numRatings)
    val mStar5: ImageView = mView.findViewById(R.id.star5)
    val mStar4: ImageView = mView.findViewById(R.id.star4)
    val mStar3: ImageView = mView.findViewById(R.id.star3)
    val mStar2: ImageView = mView.findViewById(R.id.star2)
    val mStar1: ImageView = mView.findViewById(R.id.star1)

    override fun toString(): String {
        return super.toString() + " " + mVersionName.text
    }

    fun setTag(tag: IntTabFull) {
        mView.tag = tag
    }
}
