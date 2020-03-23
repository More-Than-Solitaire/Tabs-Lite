package com.gbros.tabslite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.HomeViewPagerFragmentDirections
import com.gbros.tabslite.R
import com.gbros.tabslite.data.IntTabBasic
import com.gbros.tabslite.data.TabFull
import com.gbros.tabslite.databinding.ListItemBrowseTabsBinding

class BrowseTabsAdapter :
        ListAdapter<IntTabBasic, BrowseTabsAdapter.TabViewHolder>(
                TabDiffCallback()
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        return TabViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_browse_tabs, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TabViewHolder(private val binding: ListItemBrowseTabsBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.tab?.let { tab ->
                    navigateToTab(tab, it)
                }
            }
        }

        private fun navigateToTab(tab: IntTabBasic, view: View) {
            val direction = HomeViewPagerFragmentDirections
                    .actionViewPagerFragmentToTabDetailFragment(tab.tabId)
            view.findNavController().navigate(direction)
        }

        fun bind(item: IntTabBasic) {
            binding.apply {
                tab = item

                whichVersion.text = "ver. ${item.version}"
                executePendingBindings()
            }
        }
    }
}

private class TabDiffCallback : DiffUtil.ItemCallback<IntTabBasic>() {

    override fun areItemsTheSame(
            oldItem: IntTabBasic,
            newItem: IntTabBasic
    ): Boolean {
        return oldItem.tabId == newItem.tabId
    }

    override fun areContentsTheSame(
            oldItem: IntTabBasic,
            newItem: IntTabBasic
    ): Boolean {
        return oldItem.tabId == newItem.tabId
    }
}