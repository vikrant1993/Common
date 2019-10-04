package vk.help.network

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import vk.help.R
import kotlin.math.max
import kotlin.math.min

open class NumberProgressBar(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mMaxProgress = 100

    private var mCurrentProgress = 0

    /**
     * The progress area bar color.
     */
    private var mReachedBarColor: Int = 0

    /**
     * The bar unreached area color.
     */
    private var mUnreachedBarColor: Int = 0

    /**
     * The progress text color.
     */
    private var mTextColor: Int = 0

    /**
     * The progress text size.
     */
    private var mTextSize: Float = 0.toFloat()

    /**
     * The height of the reached area.
     */
    private var mReachedBarHeight: Float = 0.toFloat()

    /**
     * The height of the unreached area.
     */
    private var mUnreachedBarHeight: Float = 0.toFloat()

    /**
     * The suffix of the number.
     */
    private var mSuffix = "%"

    /**
     * The prefix.
     */
    private var mPrefix = ""

    private val default_text_color = Color.rgb(66, 145, 241)
    private val default_reached_color = Color.rgb(66, 145, 241)
    private val default_unreached_color = Color.rgb(204, 204, 204)
    private var default_progress_text_offset: Float
    private var default_text_size: Float
    private var default_reached_bar_height: Float
    private var default_unreached_bar_height: Float

    /**
     * For save and restore instance of progressbar.
     */
    private val INSTANCE_STATE = "saved_instance"
    private val INSTANCE_TEXT_COLOR = "text_color"
    private val INSTANCE_TEXT_SIZE = "text_size"
    private val INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height"
    private val INSTANCE_REACHED_BAR_COLOR = "reached_bar_color"
    private val INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height"
    private val INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color"
    private val INSTANCE_MAX = "max"
    private val INSTANCE_PROGRESS = "progress"
    private val INSTANCE_SUFFIX = "suffix"
    private val INSTANCE_PREFIX = "prefix"
    private val INSTANCE_TEXT_VISIBILITY = "text_visibility"

    private val PROGRESS_TEXT_VISIBLE = 0


    /**
     * The width of the text that to be drawn.
     */
    private var mDrawTextWidth: Float = 0.toFloat()

    /**
     * The drawn text start.
     */
    private var mDrawTextStart: Float = 0.toFloat()

    /**
     * The drawn text end.
     */
    private var mDrawTextEnd: Float = 0.toFloat()

    /**
     * The text that to be drawn in onDraw().
     */
    private var mCurrentDrawText: String? = null

    /**
     * The Paint of the reached area.
     */
    private var mReachedBarPaint: Paint? = null
    /**
     * The Paint of the unreached area.
     */
    private var mUnreachedBarPaint: Paint? = null
    /**
     * The Paint of the progress text.
     */
    private var mTextPaint: Paint? = null

    /**
     * Unreached bar area to draw rect.
     */
    private val mUnreachedRectF = RectF(0f, 0f, 0f, 0f)
    /**
     * Reached bar area rect.
     */
    private val mReachedRectF = RectF(0f, 0f, 0f, 0f)

    /**
     * The progress text offset.
     */
    private var mOffset: Float

    /**
     * Determine if need to draw unreached area.
     */
    private var mDrawUnreachedBar = true

    private var mDrawReachedBar = true

    private var mIfDrawText = true

    /**
     * Listener
     */
    private lateinit var mListener: OnProgressBarListener

    enum class ProgressTextVisibility {
        Visible, Invisible
    }

    init {
        default_reached_bar_height = dp2px(1.5f)
        default_unreached_bar_height = dp2px(1.0f)
        default_text_size = sp2px(10f)
        default_progress_text_offset = dp2px(3.0f)

        //load styled attributes.
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NumberProgressBar,
            defStyleAttr,
            0
        )

        mReachedBarColor = attributes.getColor(
            R.styleable.NumberProgressBar_progress_reached_color,
            default_reached_color
        )
        mUnreachedBarColor = attributes.getColor(
            R.styleable.NumberProgressBar_progress_unreached_color,
            default_unreached_color
        )
        mTextColor = attributes.getColor(
            R.styleable.NumberProgressBar_progress_text_color,
            default_text_color
        )
        mTextSize = attributes.getDimension(
            R.styleable.NumberProgressBar_progress_text_size,
            default_text_size
        )

        mReachedBarHeight = attributes.getDimension(
            R.styleable.NumberProgressBar_progress_reached_bar_height,
            default_reached_bar_height
        )
        mUnreachedBarHeight = attributes.getDimension(
            R.styleable.NumberProgressBar_progress_unreached_bar_height,
            default_unreached_bar_height
        )
        mOffset = attributes.getDimension(
            R.styleable.NumberProgressBar_progress_text_offset,
            default_progress_text_offset
        )

        val textVisible = attributes.getInt(
            R.styleable.NumberProgressBar_progress_text_visibility,
            PROGRESS_TEXT_VISIBLE
        )
        if (textVisible != PROGRESS_TEXT_VISIBLE) {
            mIfDrawText = false
        }

        setProgress(attributes.getInt(R.styleable.NumberProgressBar_progress_current, 0))
        setMax(attributes.getInt(R.styleable.NumberProgressBar_progress_max, 100))

        attributes.recycle()
        initializePainters()
    }

    override fun getSuggestedMinimumWidth(): Int {
        return mTextSize.toInt()
    }

    override fun getSuggestedMinimumHeight(): Int {
        return max(
            mTextSize.toInt(),
            max(mReachedBarHeight.toInt(), mUnreachedBarHeight.toInt())
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false))
    }

    private fun measure(measureSpec: Int, isWidth: Boolean): Int {
        var result: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        val padding = if (isWidth) paddingLeft + paddingRight else paddingTop + paddingBottom
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = if (isWidth) suggestedMinimumWidth else suggestedMinimumHeight
            result += padding
            if (mode == MeasureSpec.AT_MOST) {
                result = if (isWidth) {
                    max(result, size)
                } else {
                    min(result, size)
                }
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        if (mIfDrawText) {
            calculateDrawRectF()
        } else {
            calculateDrawRectFWithoutProgressText()
        }

        if (mDrawReachedBar) {
            canvas.drawRect(mReachedRectF, mReachedBarPaint)
        }

        if (mDrawUnreachedBar) {
            canvas.drawRect(mUnreachedRectF, mUnreachedBarPaint)
        }

        if (mIfDrawText)
            canvas.drawText(mCurrentDrawText, mDrawTextStart, mDrawTextEnd, mTextPaint)
    }

    private fun initializePainters() {
        mReachedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mReachedBarPaint.setColor(mReachedBarColor)

        mUnreachedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mUnreachedBarPaint.setColor(mUnreachedBarColor)

        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint.setColor(mTextColor)
        mTextPaint.setTextSize(mTextSize)
    }


    private fun calculateDrawRectFWithoutProgressText() {
        mReachedRectF.left = paddingLeft.toFloat()
        mReachedRectF.top = height / 2.0f - mReachedBarHeight / 2.0f
        mReachedRectF.right =
            (width - paddingLeft - paddingRight) / (getMax() * 1.0f) * getProgress() + paddingLeft
        mReachedRectF.bottom = height / 2.0f + mReachedBarHeight / 2.0f

        mUnreachedRectF.left = mReachedRectF.right
        mUnreachedRectF.right = (width - paddingRight).toFloat()
        mUnreachedRectF.top = height / 2.0f + -mUnreachedBarHeight / 2.0f
        mUnreachedRectF.bottom = height / 2.0f + mUnreachedBarHeight / 2.0f
    }

    private fun calculateDrawRectF() {

        mCurrentDrawText = String.format("%s", getProgress() * 100 / getMax())
        mCurrentDrawText = mPrefix + mCurrentDrawText + mSuffix
        mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText)

        if (getProgress() == 0) {
            mDrawReachedBar = false
            mDrawTextStart = paddingLeft.toFloat()
        } else {
            mDrawReachedBar = true
            mReachedRectF.left = paddingLeft.toFloat()
            mReachedRectF.top = height / 2.0f - mReachedBarHeight / 2.0f
            mReachedRectF.right =
                (width - paddingLeft - paddingRight) / (getMax() * 1.0f) * getProgress() - mOffset + paddingLeft
            mReachedRectF.bottom = height / 2.0f + mReachedBarHeight / 2.0f
            mDrawTextStart = mReachedRectF.right + mOffset
        }

        mDrawTextEnd =
            (height / 2.0f - (mTextPaint.descent() + mTextPaint.ascent()) / 2.0f).toInt().toFloat()

        if (mDrawTextStart + mDrawTextWidth >= width - paddingRight) {
            mDrawTextStart = width.toFloat() - paddingRight.toFloat() - mDrawTextWidth
            mReachedRectF.right = mDrawTextStart - mOffset
        }

        val unreachedBarStart = mDrawTextStart + mDrawTextWidth + mOffset
        if (unreachedBarStart >= width - paddingRight) {
            mDrawUnreachedBar = false
        } else {
            mDrawUnreachedBar = true
            mUnreachedRectF.left = unreachedBarStart
            mUnreachedRectF.right = (width - paddingRight).toFloat()
            mUnreachedRectF.top = height / 2.0f + -mUnreachedBarHeight / 2.0f
            mUnreachedRectF.bottom = height / 2.0f + mUnreachedBarHeight / 2.0f
        }
    }

    /**
     * Get progress text color.
     *
     * @return progress text color.
     */
    fun getTextColor(): Int {
        return mTextColor
    }

    /**
     * Get progress text size.
     *
     * @return progress text size.
     */
    fun getProgressTextSize(): Float {
        return mTextSize
    }

    fun getUnreachedBarColor(): Int {
        return mUnreachedBarColor
    }

    fun getReachedBarColor(): Int {
        return mReachedBarColor
    }

    fun getProgress(): Int {
        return mCurrentProgress
    }

    fun getMax(): Int {
        return mMaxProgress
    }

    fun getReachedBarHeight(): Float {
        return mReachedBarHeight
    }

    fun getUnreachedBarHeight(): Float {
        return mUnreachedBarHeight
    }

    fun setProgressTextSize(textSize: Float) {
        this.mTextSize = textSize
        mTextPaint.setTextSize(mTextSize)
        invalidate()
    }

    fun setProgressTextColor(textColor: Int) {
        this.mTextColor = textColor
        mTextPaint!!.setColor(mTextColor)
        invalidate()
    }

    fun setUnreachedBarColor(barColor: Int) {
        this.mUnreachedBarColor = barColor
        mUnreachedBarPaint!!.setColor(mUnreachedBarColor)
        invalidate()
    }

    fun setReachedBarColor(progressColor: Int) {
        this.mReachedBarColor = progressColor
        mReachedBarPaint.setColor(mReachedBarColor)
        invalidate()
    }

    fun setReachedBarHeight(height: Float) {
        mReachedBarHeight = height
    }

    fun setUnreachedBarHeight(height: Float) {
        mUnreachedBarHeight = height
    }

    fun setMax(maxProgress: Int) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress
            invalidate()
        }
    }

    fun setSuffix(suffix: String?) {
        if (suffix == null) {
            mSuffix = ""
        } else {
            mSuffix = suffix
        }
    }

    fun getSuffix(): String {
        return mSuffix
    }

    fun setPrefix(prefix: String?) {
        if (prefix == null)
            mPrefix = ""
        else {
            mPrefix = prefix
        }
    }

    fun getPrefix(): String {
        return mPrefix
    }

    fun incrementProgressBy(by: Int) {
        if (by > 0) {
            setProgress(getProgress() + by)
        }

        if (mListener != null) {
            mListener.onProgressChange(getProgress(), getMax())
        }
    }

    fun setProgress(progress: Int) {
        if (progress <= getMax() && progress >= 0) {
            this.mCurrentProgress = progress
            invalidate()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor())
        bundle.putFloat(INSTANCE_TEXT_SIZE, getProgressTextSize())
        bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getReachedBarHeight())
        bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getUnreachedBarHeight())
        bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getReachedBarColor())
        bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getUnreachedBarColor())
        bundle.putInt(INSTANCE_MAX, getMax())
        bundle.putInt(INSTANCE_PROGRESS, getProgress())
        bundle.putString(INSTANCE_SUFFIX, getSuffix())
        bundle.putString(INSTANCE_PREFIX, getPrefix())
        bundle.putBoolean(INSTANCE_TEXT_VISIBILITY, getProgressTextVisibility())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            mTextColor = state.getInt(INSTANCE_TEXT_COLOR)
            mTextSize = state.getFloat(INSTANCE_TEXT_SIZE)
            mReachedBarHeight = state.getFloat(INSTANCE_REACHED_BAR_HEIGHT)
            mUnreachedBarHeight = state.getFloat(INSTANCE_UNREACHED_BAR_HEIGHT)
            mReachedBarColor = state.getInt(INSTANCE_REACHED_BAR_COLOR)
            mUnreachedBarColor = state.getInt(INSTANCE_UNREACHED_BAR_COLOR)
            initializePainters()
            setMax(state.getInt(INSTANCE_MAX))
            setProgress(state.getInt(INSTANCE_PROGRESS))
            setPrefix(state.getString(INSTANCE_PREFIX))
            setSuffix(state.getString(INSTANCE_SUFFIX))
            setProgressTextVisibility(if (state.getBoolean(INSTANCE_TEXT_VISIBILITY)) ProgressTextVisibility.Visible else ProgressTextVisibility.Invisible)
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    fun dp2px(dp: Float): Float {
        val scale = resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun sp2px(sp: Float): Float {
        val scale = resources.displayMetrics.scaledDensity
        return sp * scale
    }

    fun setProgressTextVisibility(visibility: ProgressTextVisibility) {
        mIfDrawText = visibility == ProgressTextVisibility.Visible
        invalidate()
    }

    fun getProgressTextVisibility(): Boolean {
        return mIfDrawText
    }

    fun setOnProgressBarListener(listener: OnProgressBarListener) {
        mListener = listener
    }

    interface OnProgressBarListener {
        fun onProgressChange(current: Int, max: Int)
    }

}