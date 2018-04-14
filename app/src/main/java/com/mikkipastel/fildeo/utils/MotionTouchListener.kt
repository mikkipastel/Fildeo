package com.mikkipastel.fildeo.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View

class MotionTouchListener: View.OnTouchListener {

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {
                view?.animate()?.cancel()
                view?.animate()?.scaleY(0.96f)?.scaleX(0.96f)?.setDuration(200)?.start()

                return false
            }

            MotionEvent.ACTION_UP -> {

                val xBigScale = ObjectAnimator.ofFloat(view, "scaleX", 1.03f)
                xBigScale.duration = 160
                xBigScale.repeatCount = 0

                val yBigScale = ObjectAnimator.ofFloat(view, "scaleY", 1.03f)
                yBigScale.duration = 160
                yBigScale.repeatCount = 0

                val xSmallScale = ObjectAnimator.ofFloat(view, "scaleX", 0.985f)
                xSmallScale.duration = 160
                xSmallScale.repeatCount = 0

                val ySmallScale = ObjectAnimator.ofFloat(view, "scaleY", 0.985f)
                ySmallScale.duration = 160
                ySmallScale.repeatCount = 0

                val xNormalScale = ObjectAnimator.ofFloat(view, "scaleX", 1f)
                xNormalScale.duration = 160
                xNormalScale.repeatCount = 0

                val yNormalScale = ObjectAnimator.ofFloat(view, "scaleY", 1f)
                yNormalScale.duration = 160
                yNormalScale.repeatCount = 0

                val animator = AnimatorSet()
                animator.play(xBigScale).with(yBigScale)
                animator.play(xSmallScale).after(xBigScale)
                animator.play(ySmallScale).after(yBigScale)
                animator.play(xNormalScale).after(xSmallScale)
                animator.play(yNormalScale).after(ySmallScale)
                animator.start()

                return false
            }

            MotionEvent.ACTION_CANCEL -> {
                view?.animate()?.scaleY(1f)?.scaleX(1f)?.setDuration(200)?.start()
                return true
            }

            else -> return true
        }
    }
}