package org.company.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.youtube.clone.db.YoutubeDatabase
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRect
import platform.Foundation.*
import platform.Foundation.NSError
import platform.Foundation.NSErrorDomain
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLConnection
import platform.Foundation.NSURLRequest
import platform.Foundation.sendSynchronousRequest
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIView
import platform.darwin.*

internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun VideoPlayer(modifier: Modifier, url: String?, thumbnail: String?) {

    val player = remember { NSURL.URLWithString(url.toString())?.let { AVPlayer(uRL = it) } }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true

    playerLayer.player = player
    UIKitView(
        factory = {
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            playerContainer
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            playerLayer.setFrame(rect)
            avPlayerViewController.view.layer.frame = rect
            CATransaction.commit()
        },
        update = { view ->
            player!!.play()
            avPlayerViewController.player!!.play()
        },
        modifier = modifier
    )
}
@Composable
internal actual fun provideShortCuts(){
    return
}

@Composable
internal actual fun Notify(message: String) {
    val viewController = UIApplication.sharedApplication.keyWindow?.rootViewController?.modalViewController
    val alertController = UIAlertController.alertControllerWithTitle(
        title = UIDevice.currentDevice.systemName,
        message = message,
        preferredStyle = UIAlertControllerStyle.MAX_VALUE
    )
    alertController.addAction(
        UIAlertAction.actionWithTitle(
            "OK",
            style = UIAlertControllerStyle.MAX_VALUE,
            handler = null
        )
    )
    viewController?.presentViewController(alertController, animated = true, completion = null)
}

@Composable
internal actual fun ShareManager(title: String, videoUrl: String) {
    val viewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    val activityItems = listOf("$title: $videoUrl")
    val activityViewController = UIActivityViewController(activityItems, null)
    viewController?.presentViewController(activityViewController, true, null)
}
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun ShortsVideoPlayer(url: String?, modifier: Modifier) {
    val player = remember { AVPlayer(uRL = url?.let { NSURL.URLWithString(it) }!!) }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true

    playerLayer.player = player
    UIKitView(
        factory = {
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            playerContainer
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            playerLayer.setFrame(rect)
            avPlayerViewController.view.layer.frame = rect
            CATransaction.commit()
        },
        update = { view ->
            player.play()
            avPlayerViewController.player!!.play()
        },
        modifier = modifier
    )
}
internal actual fun UserRegion(): String {
    return NSLocale.currentLocale.countryCode ?: "us"
}

@Composable
internal actual fun isConnected(retry: () -> Unit): Boolean{
    val urlString = "https://www.google.com"
    val url = NSURL(string = urlString)
    val session = NSURLSession.sessionWithConfiguration(
        NSURLSessionConfiguration.defaultSessionConfiguration(),
        delegate = null,
        delegateQueue = NSOperationQueue.mainQueue()
    )
    val task = session.dataTaskWithURL(url) { data, response, error ->
        val httpResponse = response as? NSHTTPURLResponse
        if (httpResponse != null && httpResponse.statusCode.toInt() == 200 && error == null) {
            retry.invoke()
        } else {
            val errorMessage = if (error != null) {
                "Error: ${error.localizedDescription}"
            } else {
                "HTTP Status Code: ${httpResponse?.statusCode?.toInt()}"
            }
            println("Connection failed: $errorMessage")
        }
    }

    task.resume()

    return true
}

actual class DriverFactory actual constructor(){
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(YoutubeDatabase.Schema,"YouTubeDatabase.db")
    }
}