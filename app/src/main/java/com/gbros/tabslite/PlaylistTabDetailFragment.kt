package com.gbros.tabslite

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gbros.tabslite.viewmodels.PlaylistTabDetailViewModel

class PlaylistTabDetailFragment : Fragment() {

    companion object {
        fun newInstance() = PlaylistTabDetailFragment()
    }

    private lateinit var viewModel: PlaylistTabDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist_tab_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaylistTabDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

}