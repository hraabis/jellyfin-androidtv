package org.jellyfin.androidtv.ui.player.photo

import android.app.ActivityManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.IntSize
import androidx.core.content.getSystemService
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.composable.blurHashPainter
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import org.koin.compose.koinInject
import kotlin.math.round

@Composable
fun PhotoPlayerContent(
	item: BaseItemDto?,
	presentationActive: Boolean,
	animationDuration: Long?,
	animatePhoto: Boolean,
) {
	val api = koinInject<ApiClient>()
	val resources = LocalResources.current
	val isLowRamDevice = LocalContext.current.getSystemService<ActivityManager>()?.isLowRamDevice == true
	val modifier = Modifier.getModifierAnimateWithZoomAndPan(
		presentationActive = presentationActive,
		animatePhoto = animatePhoto,
		duration = animationDuration,
		item = item,
		screenWidth = resources.displayMetrics.widthPixels,
		screenHeight = resources.displayMetrics.heightPixels)

	AnimatedContent(
		targetState = item,
		transitionSpec = {
			fadeIn() togetherWith fadeOut()
		},
	) { item ->
		val image = item?.itemImages[ImageType.PRIMARY]
		val aspectRatio = image?.aspectRatio ?: 1f
		val blurHashResolution = 32
		if(!isLowRamDevice && image?.blurHash != null) {
			Image(
				painter = blurHashPainter(image.blurHash, IntSize(
					if (aspectRatio > 1) round(blurHashResolution * aspectRatio).toInt() else blurHashResolution,
					if (aspectRatio >= 1) blurHashResolution else round(blurHashResolution / aspectRatio).toInt())),
				contentDescription = null,
				alignment = Alignment.Center,
				contentScale = ContentScale.FillBounds,
				modifier = Modifier.fillMaxSize(),
			)
		}
		AsyncImage(
			url = image?.getUrl(
				api = api,
				maxWidth = resources.displayMetrics.widthPixels,
				maxHeight = resources.displayMetrics.heightPixels,
			),
			blurHash = image?.blurHash,
			aspectRatio = aspectRatio,
			modifier = modifier
		)
	}
}
