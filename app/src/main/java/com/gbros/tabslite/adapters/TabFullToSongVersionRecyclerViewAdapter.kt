package com.gbros.tabslite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.R
import com.gbros.tabslite.SongVersionFragment
import com.gbros.tabslite.adapters.viewholders.SongVersionViewHolder
import com.gbros.tabslite.data.IntTabFull

/**
 * [RecyclerView.Adapter] that can display a [IntTabFull] (usually a search result) and makes a call to the
 * specified [SongVersionFragment.OnListFragmentInteractionListener].
 */
class TabFullToSongVersionRecyclerViewAdapter(
    private val mValues: List<IntTabFull>,
    private val mListener: SongVersionFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<SongVersionViewHolder>() {

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { v: View ->
        val item = v.tag as IntTabFull
        // Notify the active callbacks interface (the activity, if the fragment is attached to
        // one) that an item has been selected.
        mListener?.onListFragmentInteraction(item.tabId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVersionViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_song_version, parent, false)

        view.setOnClickListener(mOnClickListener)
        return SongVersionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongVersionViewHolder, position: Int) {
        val item = mValues[position]

        holder.setTag(item)
        holder.mVersionName.text = "Version ${item.version}"  //todo: convert to string resource
        holder.mNumRatings.text = item.votes.toString()

        //set the rating stars; incorporate rounding
        if(item.rating >= .25) {  // first star
            if(item.rating >= .75) {
                holder.mStar1.setImageResource(R.drawable.ic_rating_star_full)
            } else {
                holder.mStar1.setImageResource(R.drawable.ic_rating_star_half)
            }
        } else {
            holder.mStar1.setImageResource(R.drawable.ic_rating_star_empty)
        }
        if(item.rating >= 1.25) {  // second star
            if(item.rating >= 1.75) {
                holder.mStar2.setImageResource(R.drawable.ic_rating_star_full)
            } else {
                holder.mStar2.setImageResource(R.drawable.ic_rating_star_half)
            }
        } else {
            holder.mStar2.setImageResource(R.drawable.ic_rating_star_empty)
        }
        if(item.rating >= 2.25) {  // third star
            if(item.rating >= 1.75) {
                holder.mStar3.setImageResource(R.drawable.ic_rating_star_full)
            } else {
                holder.mStar3.setImageResource(R.drawable.ic_rating_star_half)
            }
        } else {
            holder.mStar3.setImageResource(R.drawable.ic_rating_star_empty)
        }
        if(item.rating >= 3.25) {  // fourth star
            if(item.rating >= 3.75) {
                holder.mStar4.setImageResource(R.drawable.ic_rating_star_full)
            } else {
                holder.mStar4.setImageResource(R.drawable.ic_rating_star_half)
            }
        } else {
            holder.mStar4.setImageResource(R.drawable.ic_rating_star_empty)
        }
        if(item.rating >= 4.25) {  // fifth star
            if(item.rating >= 4.75) {
                holder.mStar5.setImageResource(R.drawable.ic_rating_star_full)
            } else {
                holder.mStar5.setImageResource(R.drawable.ic_rating_star_half)
            }
        } else {
            holder.mStar5.setImageResource(R.drawable.ic_rating_star_empty)
        }
    }

    override fun getItemCount(): Int = mValues.size
}
