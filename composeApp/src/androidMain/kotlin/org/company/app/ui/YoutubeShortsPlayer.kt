package org.company.app.ui

import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@RequiresApi(31)
@Composable
fun YoutubeShortsPlayer(
    modifier: Modifier = Modifier,
    youtubeURL: String?,
    isPlaying: (Boolean) -> Unit = {},
    isLoading: (Boolean) -> Unit = {},
    onVideoEnded: () -> Unit = {},
) {
    val mContext = LocalContext.current
    val mLifeCycleOwner = LocalLifecycleOwner.current
    var player: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? = null
    val playerFragment = YouTubePlayerView(mContext)
    val playerStateListener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
            super.onReady(youTubePlayer)
            player = youTubePlayer
            youTubePlayer.setLoop(true)
            youTubePlayer.loadVideo("$youtubeURL", 0f)
        }

        override fun onStateChange(
            youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
            state: PlayerConstants.PlayerState,
        ) {
            super.onStateChange(youTubePlayer, state)
            when (state) {
                PlayerConstants.PlayerState.BUFFERING -> {
                    isLoading.invoke(true)
                    isPlaying.invoke(false)
                }

                PlayerConstants.PlayerState.PLAYING -> {
                    isLoading.invoke(false)
                    isPlaying.invoke(true)
                }

                PlayerConstants.PlayerState.ENDED -> {
                    isPlaying.invoke(false)
                    isLoading.invoke(false)
                    onVideoEnded.invoke()
                }

                else -> {}
            }
        }

        override fun onError(
            youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
            error: PlayerConstants.PlayerError,
        ) {
            super.onError(youTubePlayer, error)
            println("iFramePlayer Error Reason = $error")
        }
    }
    val playerBuilder = IFramePlayerOptions.Builder().apply {
        controls(0)
        fullscreen(0)
        autoplay(1)
        modestBranding(1)
        rel(0)
        ccLoadPolicy(0)
        ivLoadPolicy(0)
    }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black),
        factory = {
            playerFragment.apply {
                enableAutomaticInitialization = false
                initialize(playerStateListener, playerBuilder.build())
            }
        }
    )
    DisposableEffect(key1 = Unit, effect = {
        val activity = (mContext as? ComponentActivity)
        activity ?: return@DisposableEffect onDispose {}
        onDispose {
            playerFragment.removeYouTubePlayerListener(playerStateListener)
            playerFragment.release()
            player = null
        }
    })
    DisposableEffect(mLifeCycleOwner) {
        val lifecycle = mLifeCycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    player?.play()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    player?.pause()
                }

                else -> {
                    //
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}