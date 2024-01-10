package com.gbros.tabslite.data

import com.gbros.tabslite.data.tab.ITab

interface ISearch {
    suspend fun getSearchResults(page: Int, query: String): List<ITab>
}