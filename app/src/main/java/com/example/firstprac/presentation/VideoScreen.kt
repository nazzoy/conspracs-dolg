package com.example.firstprac.presentation

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
fun VerticalVideoScreen() {
    val videoLinks = listOf(
        "https://samplelib.com/mp4/sample-15s-360p.mp4",
        "https://samplelib.com/mp4/sample-5s.mp4"
    )

    val pagerState = rememberPagerState(pageCount = { videoLinks.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        VideoPlayerItem(
            url = videoLinks[page],
            isCurrentPage = pagerState.currentPage == page
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerItem(url: String, isCurrentPage: Boolean) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}