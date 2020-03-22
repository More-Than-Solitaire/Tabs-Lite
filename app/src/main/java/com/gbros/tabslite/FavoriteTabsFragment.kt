package com.gbros.tabslite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.gbros.tabslite.adapters.FavoriteTabsAdapter
import com.gbros.tabslite.databinding.FragmentFavoriteTabsBinding
import com.gbros.tabslite.utilities.InjectorUtils
import com.gbros.tabslite.viewmodels.FavoriteTabsListViewModel

class FavoriteTabsFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteTabsBinding

    private val viewModel: FavoriteTabsListViewModel by viewModels {
        InjectorUtils.provideFavoriteTabViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteTabsBinding.inflate(inflater, container, false)
        val adapter = FavoriteTabsAdapter()
        binding.favoriteTabsList.adapter = adapter

        binding.findNewSongs.setOnClickListener {
            (activity as HomeActivity).focusSearch()
        }

        subscribeUi(adapter, binding)
        return binding.root
    }

    private fun subscribeUi(adapter: FavoriteTabsAdapter, binding: FragmentFavoriteTabsBinding) {
        viewModel.favoriteTabs.observe(viewLifecycleOwner) { result ->
            binding.hasHistory = !result.isNullOrEmpty()
            adapter.submitList(result)
        }
    }
}
