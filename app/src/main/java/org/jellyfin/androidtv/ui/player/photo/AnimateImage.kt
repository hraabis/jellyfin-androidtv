package org.jellyfin.androidtv.ui.player.photo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto
import kotlin.math.floor


private const val EIGHT_SECONDS = 8000
@Composable
fun Modifier.getModifierAnimateWithZoomAndPan(
	presentationActive: Boolean,
	animatePhoto: Boolean,
	duration: Long?,
	item: BaseItemDto?,
	screenWidth: Int?,
	screenHeight: Int?
): Modifier {
	if(!presentationActive || !animatePhoto || duration == null || item == null || screenWidth == null || screenHeight == null || item.width == null || item.height == null) {
		return this.fillMaxSize()
	}
	val imageAspectRatio = item.width!!.toDouble() / item.height!!.toDouble()
	val use1PhaseActions = duration < EIGHT_SECONDS
	val key = item.id.toString()
	if(use1PhaseActions || (imageAspectRatio > 0.56 && imageAspectRatio < 2.0)) {
		fun getRandomInt(min: Int, max: Int): Int {
			return floor(Math.random() * (max + 1 - min).toDouble()).toInt() + min
		}
		fun getRandomScaleValue(min: Int, max: Int): Float {
			return (getRandomInt(min, max).toFloat() / 100.0f) + 1.0f
		}
		fun getRandomPanValue(limit: Int): Float {
			return getRandomInt(-1 * limit, limit).toFloat()
		}
		fun getRandomSubDuration(duration: Long, min: Int, max: Int) : Long{
			return (duration.toFloat() * (getRandomInt(min, max).toFloat() / 100.0f)).toLong()
		}
		fun getScaledPanLimit(value: Int, scale: Float): Int{
			return (value * (scale * 0.05f)).toInt()
		}

		val randomAction = getRandomInt(0, 10)
		if (randomAction == 0) {
			//Static Image
			return this.fillMaxSize()
		}
		val randomScale1 = getRandomScaleValue(25,100)
		val xLimit = getScaledPanLimit((item.width ?: screenWidth),randomScale1)
		val yLimit = getScaledPanLimit((item.height ?: screenHeight), randomScale1)
		val rndX = getRandomPanValue(xLimit)
		val rndY = getRandomPanValue(yLimit)
		val actionDuration = (duration * 0.93f).toLong()
		val randomDuration = getRandomSubDuration(actionDuration,50,100)
		if (randomAction <= 2 || (use1PhaseActions && randomAction <= 3)) {
			//Zoom In
			return this.zoomAndPanRunnable(key,20,100, 1.0f, 0.0f, 0.0f,randomDuration, randomScale1, rndX, rndY, false, 0)
		}
		else if (randomAction <= 4 || (use1PhaseActions && randomAction <= 6)) {
			//Zoom Out
			return this.zoomAndPanRunnable(key,20,50, randomScale1, rndX, rndY, randomDuration, 1.0f, 0.0f, 0.0f, false,0)
		}
		else {
			val randomDuration1 = getRandomSubDuration(actionDuration,40,60)
			val randomDuration2 = actionDuration - randomDuration1
			val randomScale2 = getRandomScaleValue(25,100)
			val xLimit2 = getScaledPanLimit((item.width ?: screenWidth),randomScale2)
			val yLimit2 = getScaledPanLimit((item.height ?: screenHeight), randomScale2)
			val rndX2 = getRandomPanValue(xLimit2)
			val rndY2 = getRandomPanValue(yLimit2)
			if (randomAction <= 6 || use1PhaseActions) {
				//Zoom 1 to Zoom 2
				return this.zoomAndPanRunnable(key, 20, 50, randomScale1, rndX, rndY, randomDuration, randomScale2, rndX2, rndY2, false, 0)
			}
			else if (randomAction <= 8) {
				//Zoom In and Zoom 2
				return this.zoomAndPanRunnable(key, 20, randomDuration1, randomScale1, rndX, rndY, randomDuration2, randomScale2, rndX2, rndY2, false, 0)
			}
			else if (randomAction <= 10) {
				//Zoom 1 to Zoom 2 and Out
				return this.zoomAndPanRunnable(key, 20, 50, randomScale1, rndX, rndY, randomDuration1, randomScale2, rndX2, rndY2, true, randomDuration2)
			}
		}
	}
	else if (imageAspectRatio <= 0.56 || imageAspectRatio >= 2.0){
		val scaleUp = getScaleUpToViewBounds(imageAspectRatio,screenWidth,screenHeight)
		if (scaleUp > 0.9f && scaleUp < 1.1f) {
			return this.fillMaxSize()
		}
		val xZoomPan = (screenWidth.toFloat() * scaleUp * 0.5f) - (screenWidth.toFloat() * 0.5f)
		val yZoomPan = (screenHeight.toFloat() * scaleUp * 0.5f) - (screenHeight.toFloat() * 0.5f)
		val holdOrZoomDuration = ((duration).toFloat() * 0.03f).toLong()
		val scrollDuration = ((duration).toFloat() * 0.65f).toLong()
		val randomizeDirection = floatArrayOf(-1f,1f).random()
		if (imageAspectRatio > 2.0f) {
			return this.zoomAndPanRunnable(key,holdOrZoomDuration, holdOrZoomDuration, scaleUp, randomizeDirection*xZoomPan, 0.0f, scrollDuration, scaleUp,-1*randomizeDirection*xZoomPan,0.0f, true, holdOrZoomDuration)
		}
		else if (imageAspectRatio <= 0.56f) {
			return this.zoomAndPanRunnable(key,holdOrZoomDuration,holdOrZoomDuration, scaleUp, 0.0f, -1*randomizeDirection*yZoomPan, scrollDuration, scaleUp,0.0f, randomizeDirection*yZoomPan, true, holdOrZoomDuration)
		}
	}
	return this.fillMaxSize()
}

private fun getScaleUpToViewBounds(imageAspectRatio: Double, screenWidth: Int, screenHeight: Int): Float {
	var scaleUp = 1.0f
	val screenAspectRatio = screenWidth.toDouble() / screenHeight.toDouble()
	if(imageAspectRatio >= 2.0){
		scaleUp = (imageAspectRatio / screenAspectRatio).toFloat()
	}
	else if (imageAspectRatio <= 0.56){
		scaleUp = (screenAspectRatio / imageAspectRatio).toFloat()
	}
	return scaleUp
}

@Composable
private fun Modifier.zoomAndPanRunnable(key: String, startDelay: Long, duration1: Long, scale1: Float, x1: Float, y1: Float, duration2: Long, scale2: Float, x2: Float, y2: Float, resetZoomAndPan: Boolean, resetDuration: Long): Modifier {
// Define starting values
	val initialX = 0f
	val initialY = 0f
	val initialScale = 1.0f
// Define animatables with starting values
	val scalePhoto = remember { Animatable(initialScale) }
	val offsetX = remember { Animatable(initialX) }
	val offsetY = remember { Animatable(initialY) }
// Trigger the value-based animation
	LaunchedEffect(key) {
		//Phase 1
		coroutineScope {
			val animationSpec1 = tween<Float>(
				durationMillis = duration1.toInt(),
				delayMillis = startDelay.toInt(),
				easing =  EaseInOutSine)
			launch { scalePhoto.animateTo(scale1, animationSpec = animationSpec1) }
			launch { offsetX.animateTo(x1, animationSpec = animationSpec1) }
			launch { offsetY.animateTo(y1, animationSpec = animationSpec1) }
		}
		//Phase 2
		coroutineScope {
			val animationSpec2 = tween<Float>(
				durationMillis = duration2.toInt(),
				easing = EaseInOutSine)
			launch { scalePhoto.animateTo(scale2, animationSpec = animationSpec2) }
			launch { offsetX.animateTo(x2, animationSpec = animationSpec2) }
			launch { offsetY.animateTo(y2, animationSpec = animationSpec2) }
		}
		//Phase 3 return to initial
		if(resetZoomAndPan) {
			coroutineScope {
				val animationSpec3 = tween<Float>(
					durationMillis = resetDuration.toInt(),
					easing = EaseInOutSine)
				launch { scalePhoto.animateTo(initialScale, animationSpec = animationSpec3) }
				launch { offsetX.animateTo(initialX, animationSpec = animationSpec3) }
				launch { offsetY.animateTo(initialY, animationSpec = animationSpec3) }
			}
		}
	}

return this
	.fillMaxSize()
	.graphicsLayer {
		scaleX = scalePhoto.value
		scaleY = scalePhoto.value
		translationX = offsetX.value
		translationY = offsetY.value
	}
}
