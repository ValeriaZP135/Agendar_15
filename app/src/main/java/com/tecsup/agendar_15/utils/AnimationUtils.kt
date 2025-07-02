package com.tecsup.agendar_15.utils

import android.app.Activity
import android.animation.*
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.animation.*
import androidx.core.view.ViewCompat

object AnimationUtils {

    fun overrideActivityTransition(activity: Activity, enterAnim: Int, exitAnim: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+ - Usar el nuevo método
            activity.overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                enterAnim,
                exitAnim
            )
        } else {
            // API < 34 - Usar el método legacy
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null)
    }

    fun fadeOut(view: View, duration: Long = 300, onComplete: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onComplete?.invoke()
                }
            })
    }

    fun slideInFromBottom(view: View, duration: Long = 400) {
        view.translationY = view.height.toFloat()
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(null)
    }

    fun slideOutToBottom(view: View, duration: Long = 300, onComplete: (() -> Unit)? = null) {
        view.animate()
            .translationY(view.height.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.translationY = 0f
                    onComplete?.invoke()
                }
            })
    }

    fun scaleIn(view: View, duration: Long = 300) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator())
            .setListener(null)
    }

    fun pulse(view: View?, scale: Float = 1.1f, duration: Long = 200) {
        view?.let {
            val scaleX = ObjectAnimator.ofFloat(it, "scaleX", 1f, scale, 1f)
            val scaleY = ObjectAnimator.ofFloat(it, "scaleY", 1f, scale, 1f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY)
            animatorSet.duration = duration
            animatorSet.interpolator = DecelerateInterpolator()
            animatorSet.start()
        }
    }

    fun checkmarkAnimation(view: View) {
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.2f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(rotation, scaleX, scaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = BounceInterpolator()
        animatorSet.start()
    }

    fun fabMorphToDialog(fab: View, dialog: View, duration: Long = 400) {
        // Ocultar FAB con animación
        fab.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(duration / 2)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fab.visibility = View.GONE
                    // Mostrar dialog
                    slideInFromBottom(dialog, duration / 2)
                }
            })
    }

    // FIX: Corrección del método addRippleEffect
    fun addRippleEffect(view: View) {
        val context = view.context
        val typedValue = TypedValue()

        // Resolver el atributo para obtener el resource ID correcto
        if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)) {
            view.setBackgroundResource(typedValue.resourceId)
        } else {
            // Fallback: crear un drawable ripple básico
            view.isClickable = true
            view.isFocusable = true
        }
    }
}