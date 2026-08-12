package org.jellyfin.androidtv.ui

import org.jellyfin.sdk.model.api.BaseItemDto
import kotlin.math.floor


private const val SIX_SECONDS = 6000
fun AsyncImageView.photoAnimateWithZoomAndPan(duration: Long?, panEffectPercent: Float, zoomEffectPercent: Float, item: BaseItemDto?, screenWidth: Int?, screenHeight: Int?){
	if(duration == null || item == null || screenWidth == null || screenHeight == null || item.width == null || item.height == null)
		return
	val imageAspectRatio = item.width!!.toDouble() / item.height!!.toDouble()
	if(imageAspectRatio > 0.56 && imageAspectRatio < 2.0) {
		val xLimit = ((item.width ?: screenWidth) * 0.25).toInt()
		val yLimit = ((item.height ?: screenHeight) * 0.25).toInt()
		fun getRandomInt(min: Int, max: Int): Int {
			return floor(Math.random() * (max + 1 - min).toDouble()).toInt() + min
		}
		fun getRandomScaleValue(): Float {
			return (getRandomInt(50,200).toFloat() / 100.0f * zoomEffectPercent) + 1.0f
		}
		fun getRandomPanValue(limit: Int): Float {
			return getRandomInt(-1 * limit, limit).toFloat() * panEffectPercent
		}
		fun getRandomSubDuration(duration: Long) : Long{
			return (duration * (getRandomInt(30,70).toFloat() / 100.0f)).toLong()
		}
		val rndX = getRandomPanValue(xLimit)
		val rndY = getRandomPanValue(yLimit)
		val rndScale = getRandomScaleValue()
		val rndScaleModifier = getRandomInt(85,115).toFloat() / 100f
		var rndX2 = rndX
		var rndY2 = rndY
		var rndScale2 = rndScale
		val actionDuration = (duration * 0.98f - crossFadeDuration.inWholeMilliseconds).toLong()
		var phase1Duration = actionDuration
		var phase2Duration = actionDuration
		var randomAction = 0
		val onlyUse1PhaseActions = duration <= SIX_SECONDS
		val canUseZoomOutActions = duration >= SIX_SECONDS
		val randomActionLowerLimit = 0
		val randomActionUpperLimit1Phase = 2
		val randomActionUpperLimit1PhaseExcludingZoomOut = 1
		val randomActionUpperLimit2PhaseExcludingZoomOut = 8
		var randomActionUpperLimit = 13

		if(onlyUse1PhaseActions) {
			randomActionUpperLimit = if (canUseZoomOutActions) randomActionUpperLimit1Phase else randomActionUpperLimit1PhaseExcludingZoomOut
			randomAction = getRandomInt(randomActionLowerLimit, randomActionUpperLimit)
		}
		else {
			randomActionUpperLimit = if (canUseZoomOutActions) randomActionUpperLimit else randomActionUpperLimit2PhaseExcludingZoomOut
			randomAction = getRandomInt(randomActionLowerLimit, randomActionUpperLimit)
			phase1Duration = getRandomSubDuration(actionDuration)
			phase2Duration =  actionDuration - phase1Duration
			if(randomAction in 8..9) {
				rndX2 = getRandomPanValue(xLimit)
				rndY2 = getRandomPanValue(yLimit)
				rndScale2 = getRandomScaleValue()
			}
		}

		if(!canUseZoomOutActions && randomAction == 2)
			randomAction = 1

		when (randomAction) {
			1 -> {
				//Zoom In
				zoomAndPanRunnable(this,0,0, 1.0f, 0.0f, 0.0f,actionDuration, rndScale, rndX, rndY, false, 0)
			}
			2 -> {
				//Zoom Out
				zoomAndPanRunnable(this,0,0, rndScale, rndX, rndY, actionDuration, 1.0f, 0.0f, 0.0f, false,0)
			}
			3 -> {
				//Zoom In and Pan to Center
				zoomAndPanRunnable(this,0,phase1Duration, rndScale, rndX, rndY, phase2Duration, (rndScale * rndScaleModifier), 0.0f, 0.0f, false,0)
			}
			4 -> {
				//Zoom In and Pan opposite XY
				zoomAndPanRunnable(this,0,phase1Duration, rndScale, rndX, rndY, phase2Duration, (rndScale * rndScaleModifier), -1 * rndX, -1 * rndY, false,0)
			}
			5 -> {
				//Zoom In and Pan opposite X
				zoomAndPanRunnable(this,0,phase1Duration, rndScale, rndX, rndY, phase2Duration, (rndScale * rndScaleModifier), -1 * rndX, rndY, false,0)
			}
			6 -> {
				//Zoom In and Pan opposite Y
				zoomAndPanRunnable(this,0,phase1Duration, rndScale, rndX, rndY, phase2Duration, (rndScale * rndScaleModifier), rndX, -1 * rndY, false,0)
			}
			7 -> {
				//Zoom In and hold
				zoomAndPanRunnable(this,0,phase1Duration, rndScale, rndX, rndY, phase2Duration, rndScale, rndX, rndY, false,0)
			}
			8 -> {
				//Zoom In and to new Random
				zoomAndPanRunnable(this,0,phase1Duration, rndScale, rndX, rndY, phase2Duration, rndScale2, rndX2, rndY2, false,0)
			}
			9 -> {
				//Zoomed In To New Random, if slide animation is 100 or less, and thumbnails are being used below 10 second interval
				zoomAndPanRunnable(this,0,0, rndScale, rndX, rndY, actionDuration, rndScale2, rndX2, rndY2, false,0)
			}
			10 -> {
				//Zoomed In Pan to Center
				zoomAndPanRunnable(this,0,0, rndScale, rndX, rndY, actionDuration, (rndScale * rndScaleModifier), 0.0f, 0.0f, false,0)
			}
			11 -> {
				//Zoomed In Pan opposite XY
				zoomAndPanRunnable(this,0,0, rndScale, rndX, rndY, actionDuration, (rndScale * rndScaleModifier), -1 * rndX, -1 * rndY, false,0)
			}
			12 -> {
				//Zoomed In Pan opposite X
				zoomAndPanRunnable(this,0,0, rndScale, rndX, rndY, actionDuration, (rndScale * rndScaleModifier), -1 * rndX, rndY, false,0)
			}
			13 -> {
				//Zoomed In Pan opposite Y
				zoomAndPanRunnable(this,0,0, rndScale, rndX, rndY, actionDuration, (rndScale * rndScaleModifier), rndX, -1 * rndY, false,0)
			}
		}
	}
	else if (duration >= SIX_SECONDS && (imageAspectRatio <= 0.56 || imageAspectRatio >= 2.0)){
		val scaleUp = getScaleUpToViewBounds(imageAspectRatio,screenWidth,screenHeight)
		if (scaleUp > 0.9f && scaleUp < 1.1f)
			return

		val xZoomPan = (screenWidth.toFloat() * scaleUp * 0.5f) - (screenWidth.toFloat() * 0.5f)
		val yZoomPan = (screenHeight.toFloat() * scaleUp * 0.5f) - (screenHeight.toFloat() * 0.5f)
		val holdOrZoomDuration = ((duration - crossFadeDuration.inWholeMilliseconds).toFloat() * 0.03f).toLong()
		val scrollDuration = ((duration - crossFadeDuration.inWholeMilliseconds).toFloat() * 0.8f).toLong()
		val randomizeDirection = floatArrayOf(-1f,1f).random()
		if (imageAspectRatio > 2.0f) {
			zoomAndPanRunnable(this,holdOrZoomDuration, holdOrZoomDuration, scaleUp, randomizeDirection*xZoomPan, 0.0f, scrollDuration, scaleUp,-1*randomizeDirection*xZoomPan,0.0f, true, holdOrZoomDuration)
		}
		else if (imageAspectRatio <= 0.56f) {
			zoomAndPanRunnable(this,holdOrZoomDuration,holdOrZoomDuration, scaleUp, 0.0f, -1*randomizeDirection*yZoomPan, scrollDuration, scaleUp,0.0f, randomizeDirection*yZoomPan, true, holdOrZoomDuration)
		}
	}
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

private fun zoomAndPanRunnable(view:AsyncImageView, startDelay: Long, duration1: Long, scale1: Float, x1: Float, y1: Float, duration2: Long, scale2: Float, x2: Float, y2: Float, resetZoomAndPan: Boolean, resetDuration: Long) {
	val resetZoomAndPanAction = Runnable{
		view.animate()
			.setDuration(resetDuration)
			.scaleY(1.0f).scaleX(1.0f)
			.x(0.0f).y(0.0f)
	}
	val panAction = Runnable {
		view.animate()
			.setDuration(duration2)
			.scaleY(scale2).scaleX(scale2)
			.x(x2).y(y2)
			.withEndAction(if(resetZoomAndPan) resetZoomAndPanAction else null)
	}
	view.animate()
		.setStartDelay(startDelay)
		.setDuration(duration1)
		.scaleY(scale1).scaleX(scale1)
		.x(x1).y(y1)
		.withEndAction(panAction)
}
