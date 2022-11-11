package ru.aasmc.petfinderapp.common.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.utils.dpToPx
import ru.aasmc.petfinderapp.common.utils.getTextWidth

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    // the attribute in the theme that specifies which style this view uses
    defStyleAttr: Int = R.attr.progressButtonStyle,
    // the style the view uses. this usually ships with the library or SDK.
    defStyleRes: Int = R.style.ProgressButtonStyle
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var buttonText = ""

    private val textPaint = Paint().apply {
        // smooths the edges of the shapes painted on the screen
        isAntiAlias = true
        style = Paint.Style.FILL
        textSize = context.dpToPx(16f)
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = context.dpToPx(2f)
    }

    private val buttonRect = RectF()
    private val progressRect = RectF()

    private var buttonRadius = context.dpToPx(16f)

    private var offset: Float = 0f

    private var widthAnimator: ValueAnimator? = null
    private var loading = false
    private var startAngle = 0f

    private var rotationAnimator: ValueAnimator? = null

    init {
        // passing defStyleAttr and defStyleRes helps check the values and
        // resolve precedence
        val typedArray =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressButton,
                defStyleAttr,
                defStyleRes
            )

        buttonText =
            typedArray.getString(R.styleable.ProgressButton_progressButton_text) ?: ""

        val typedValue = TypedValue()
        // resolve the value of colorPrimary using theme from the context.
        // Using the correct context is vital. If you try using an Activity context,
        // it wil lead to inconsistencies since the Activity and the view can have different
        // themes.
        context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val defaultBackgroundColor = typedValue.data
        val defaultTextColor = Color.WHITE
        val defaultProgressColor = Color.WHITE

        val backgroundColor =
            typedArray.getColor(
                R.styleable.ProgressButton_progressButton_backgroundColor,
                defaultBackgroundColor
            )
        backgroundPaint.color = backgroundColor

        val textColor =
            typedArray.getColor(
                R.styleable.ProgressButton_progressButton_textColor,
                defaultTextColor
            )
        textPaint.color = textColor

        val progressColor =
            typedArray.getColor(
                R.styleable.ProgressButton_progressButton_progressColor,
                defaultProgressColor
            )
        progressPaint.color = progressColor

        typedArray.recycle()
    }

    /**
     * Flag that indicates whether Canvas should draw the check icon.
     */
    private var drawCheck = false

    /**
     * To draw the check we need two lines perpendicular to each other and rotated
     * by 45 degrees.
     *
     * x coordinate of the starting point of the vertical line:
     *    measuredWidth / 2f + buttonRect.width() / 8
     *
     * y coordinate: measuredHeight / 2f + buttonRect.width() / 4
     *
     * coordinates of the final point of the vertical line:
     *   measuredWidth / 2f + buttonRect.width() / 8 and
     *   measuredHeight / 2f - buttonRect.width() / 4
     *
     * x coordinates of the starting point of the horizontal line:
     *      measuredWidth / 2f - buttonRect.width() / 8
     * y coordinates of the starting point of the horizontal line:
     *      measuredHeight / 2f + buttonRect.width() / 4
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // initialize button radius as half of measured height so when the button
        // shrinks, it becomes a circle and not an oval.
        buttonRadius = measuredHeight / 2f

        buttonRect.apply {
            top = 0f
            left = 0f + offset
            right = measuredWidth.toFloat() - offset
            bottom = measuredHeight.toFloat()
        }
        canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius, backgroundPaint)

        // draw the button text while the offset has not reached the final value,
        // i.e. the value when button is fully rounded. It becomes fully rounded when
        // offset == (measuredWidth - measuredHeight) / 2

        if (offset < (measuredWidth - measuredHeight) / 2f) {
            val textX = measuredWidth / 2.0f - textPaint.getTextWidth(buttonText) / 2.0f
            val textY =
                measuredHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(buttonText, textX, textY, textPaint)
        }

        // if we are loading and the button is now a circle
        if (loading && offset == (measuredWidth - measuredHeight) / 2f) {
            progressRect.left = measuredWidth / 2.0f - buttonRect.width() / 4
            progressRect.top = measuredHeight / 2.0f - buttonRect.width() / 4
            progressRect.right = measuredWidth / 2.0f + buttonRect.width() / 4
            progressRect.bottom = measuredHeight / 2.0f + buttonRect.width() / 4
            canvas.drawArc(progressRect, startAngle, 140f, false, progressPaint)
        }

        if (drawCheck) {
            canvas.save()
            canvas.rotate(45f, measuredWidth / 2f, measuredHeight / 2f)
            val x1 = measuredWidth / 2f - buttonRect.width() / 8
            val y1 = measuredHeight / 2f + buttonRect.width() / 4
            val x2 = measuredWidth / 2f + buttonRect.width() / 8
            val y2 = measuredHeight / 2f + buttonRect.width() / 4
            val x3 = measuredWidth / 2f + buttonRect.width() / 8
            val y3 = measuredHeight / 2f - buttonRect.width() / 4
            canvas.drawLine(x1, y1, x2, y2, progressPaint)
            canvas.drawLine(x2, y2, x3, y3, progressPaint)
            canvas.restore()
        }
    }

    /**
     * Animates the button to shrink in width. It uses ValueAnimator to animate between 0
     * and 1 over 200 milliseconds.
     *
     * addUpdateListener adds a listener that gets a callback every time ValueAnimator
     * changes the value. When the value changes, you update the offset to a fraction
     * of the final required value.
     *
     * we call invalidate() which tells Canvas that it needs to redraw the view. Canvas
     * will respond by invoking onDraw().
     *
     * set loading to true to inform onDraw that it needs to redraw the progress bar.
     *
     * set isClickable to false so the user can't click the view while a task is in
     * progress.
     */
    fun startLoading() {
        widthAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                offset = (measuredWidth - measuredHeight) / 2f * it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    startProgressAnimation()
                }
            })
            duration = 200
        }
        loading = true
        isClickable = false
        widthAnimator?.start()
    }

    private fun startProgressAnimation() {
        // animate between 0 and 360
        rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            addUpdateListener {
                startAngle = it.animatedValue as Float
                invalidate()
            }
            duration = 600
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    loading = false
                    invalidate()
                }
            })
        }
        rotationAnimator?.start()
    }

    /**
     * Allows the client of the view to indicate that the task is complete and that
     * the view can hide the progress bar and display the check icon.
     */
    fun done() {
        loading = false
        drawCheck = true
        rotationAnimator?.cancel()
        invalidate()
    }

    /**
     * To prevent memory leaks when the user exits the fragment before
     * the animation completes, we should stop the animations.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        widthAnimator?.cancel()
        rotationAnimator?.cancel()
    }
}
