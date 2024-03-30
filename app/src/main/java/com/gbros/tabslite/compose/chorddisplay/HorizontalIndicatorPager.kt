package com.gbros.tabslite.compose.chorddisplay

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.ui.theme.AppTheme

/**
 * HorizontalPager with automatic indicators at the bottom. Automatically tracks pager state.
 *
 * Thanks https://bootcamp.uxdesign.cc/improving-compose-horizontal-pager-indicator-bcf3b67835a
 *
 * @param pageCount: The number of pages to display
 * @param content: A content generator given the page to display
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalIndicatorPager(modifier: Modifier = Modifier, pageCount: Int, content: @Composable PagerScope.(page: Int) -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val pagerState = rememberPagerState(pageCount = { pageCount })
        val indicatorScrollState = rememberLazyListState()

        LaunchedEffect(key1 = pagerState.currentPage, block = {
            // Make sure the page indicator representing this page is visible
            val size = indicatorScrollState.layoutInfo.visibleItemsInfo.size
            if (size > 1) {
                val currentPage = pagerState.currentPage
                val lastVisibleIndex =
                    indicatorScrollState.layoutInfo.visibleItemsInfo.last().index // don't run with empty lists to prevent crashes
                val firstVisibleItemIndex = indicatorScrollState.firstVisibleItemIndex

                if (currentPage > lastVisibleIndex - 1) {
                    indicatorScrollState.animateScrollToItem(currentPage - size + 2)
                } else if (currentPage <= firstVisibleItemIndex + 1) {
                    indicatorScrollState.animateScrollToItem((currentPage - 1).coerceAtLeast(0))
                }
            }
        })
        HorizontalPager(
            state = pagerState,
            pageContent = content
        )

        val activeColor = MaterialTheme.colorScheme.outline
        val inactiveColor = MaterialTheme.colorScheme.outlineVariant

        // scroll state
        LazyRow(
            state = indicatorScrollState,
            userScrollEnabled = false,
            modifier = Modifier
                .width(((6 + 16) * 2 + 3 * (10 + 16)).dp), // I'm hard computing it to simplify
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) activeColor else inactiveColor
                item(key = "item$iteration") {
                    val currentPage = pagerState.currentPage
                    val firstVisibleIndex by remember { derivedStateOf { indicatorScrollState.firstVisibleItemIndex } }
                    val lastVisibleIndex = indicatorScrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val size by animateDpAsState(
                        targetValue = when (iteration) {
                            currentPage -> 10.dp
                            in (firstVisibleIndex + 1) until lastVisibleIndex -> 10.dp
                            else -> 6.dp
                        },
                        label = "horizontal indicator size"
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(color, CircleShape)
                            .size(size)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable @Preview
fun HorizontalIndicatorPagerPreview() {
    AppTheme {
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
            HorizontalIndicatorPager(pageCount = 100) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Page $it", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
