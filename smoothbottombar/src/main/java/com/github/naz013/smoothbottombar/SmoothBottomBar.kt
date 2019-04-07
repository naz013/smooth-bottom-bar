package com.github.naz013.smoothbottombar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class SmoothBottomBar : View {

    private var mTabs: List<Tab> = listOf()
    @ColorInt
    private var mBgColor: Int = Color.parseColor("#110A88")
    @ColorInt
    private var mTextColor: Int = Color.WHITE
    @ColorInt
    private var mSelectorColor: Int = Color.parseColor("#40FFFFFF")

    private val mTextPaint = Paint()
    private val mSelectorPaint = Paint()
    private val mIconPaint = Paint()

    private var mSelector: Selector = Selector(PointF(0.0f, 0.0f), mSelectorPaint, Rect())
    private val objects: MutableList<InnerTab> = mutableListOf()

    private var mSelectedItem = 0
    private var isSlided = false

    private var mX: Float = 0f
    private var mY: Float = 0f

    private var mOnTabSelectedListener: OnTabSelectedListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mTabs = defaultTabs()

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SmoothBottomBar, 0, 0)
            try {
                mBgColor = a.getColor(R.styleable.SmoothBottomBar_bar_background, mBgColor)
                mTextColor = a.getColor(R.styleable.SmoothBottomBar_bar_textColor, mTextColor)
                mSelectorColor = a.getColor(R.styleable.SmoothBottomBar_bar_selectorColor, mSelectorColor)
            } catch (e: Exception) {
                printLog("init: " + e.localizedMessage)
            } finally {
                a.recycle()
            }
        }

        mTextPaint.color = mTextColor
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.toFloat(), resources.displayMetrics)

        mSelectorPaint.color = mSelectorColor
        mSelectorPaint.style = Paint.Style.FILL

        mIconPaint.style = Paint.Style.STROKE
        setOnTouchListener { _, _ -> false }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
    }

    override fun setOnClickListener(l: OnClickListener?) {
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener { v, event -> processTouch(v, event) }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            canvas.drawColor(mBgColor)
            mSelector.draw(canvas)
            objects.forEach { it.draw(canvas) }
        } else {
            super.onDraw(canvas)
        }
    }

    @Suppress("unused")
    fun setOnTabSelectedListener(listener: ((position: Int) -> Unit)?) {
        this.mOnTabSelectedListener = object : OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                listener?.invoke(position)
            }
        }
    }

    @Suppress("unused")
    fun setOnTabSelectedListener(listener: OnTabSelectedListener?) {
        this.mOnTabSelectedListener = listener
    }

    @Suppress("unused")
    override fun setBackgroundColor(@ColorInt color: Int) {
        this.mBgColor = color
        this.invalidate()
    }

    @Suppress("unused")
    fun setSelectorColor(@ColorInt color: Int) {
        this.mSelectorColor = color
        this.mSelectorPaint.color = color
        this.invalidate()
    }

    @Suppress("unused")
    fun setTextColor(@ColorInt color: Int) {
        this.mTextColor = color
        this.mTextPaint.color = color
        this.invalidate()
    }

    /**
     * Disabled
     */
    override fun setBackground(background: Drawable?) {
    }

    /**
     * Disabled
     */
    override fun setBackgroundResource(resid: Int) {
    }

    /**
     * Disabled
     */
    override fun setBackgroundTintList(tint: ColorStateList?) {
    }

    /**
     * Disabled
     */
    override fun setBackgroundTintMode(tintMode: PorterDuff.Mode?) {
    }

    fun setTabs(tabs: List<Tab>) {
        if (tabs.size < 2 || tabs.size > 5) {
            throw IllegalArgumentException("View can only handle from 2 to 5 tabs")
        }
        this.mTabs = tabs
        this.invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, height)
        calculateRectangles(width, height)
    }

    private fun calculateRectangles(width: Int, height: Int) {
        val elements = mutableListOf<InnerTab>()
        if (mTabs.isNotEmpty()) {
            val margin = dp2px(16)
            val tabWidth = (width - (margin * 2)) / mTabs.size
            for (i in 0 until mTabs.size) {
                val tab = mTabs[i]
                val left = i * tabWidth + margin
                val rect = Rect(left, margin, left + tabWidth, height - margin)
                val leftHalf = Rect(rect.left, rect.top, rect.centerX(), rect.bottom)
                val rightHalf = Rect(rect.centerX(), rect.top, rect.right, rect.bottom)
                val label = Label(tab.title ?: "", 0, PointF(rightHalf.left.toFloat(), rightHalf.centerY().toFloat()), mTextPaint, rightHalf)
                val iconHeight = rect.height() / 2
                val iconHeightHalf = iconHeight / 2
                val onPoint = PointF(leftHalf.centerX().toFloat() - iconHeightHalf, rect.centerY().toFloat() - iconHeightHalf)
                val offPoint = PointF(rect.centerX().toFloat() - iconHeightHalf, rect.centerY().toFloat() - iconHeightHalf)
                val iconBounds = Rect(0, 0, iconHeight, iconHeight)
                val icon = Icon(onPoint, toDrawable(tab.icon), offPoint, mIconPaint, iconBounds)
                val innerTab = InnerTab(icon, label, rect)
                elements.add(innerTab)
                if (i == 0) {
                    mSelector = Selector(PointF(margin.toFloat(), margin.toFloat()), mSelectorPaint, rect)
                }
            }
            objects.clear()
            objects.addAll(elements)
            if (mSelectedItem in 0..objects.size) {
                selectTab(mSelectedItem)
            }
        }
    }

    @Nullable
    override fun onSaveInstanceState(): Parcelable? {
        val savedInstance = Bundle()
        savedInstance.putParcelable("super", super.onSaveInstanceState())
        savedInstance.putInt("position", mSelectedItem)
        return savedInstance
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            mSelectedItem = state.getInt("position", mSelectedItem)
            super.onRestoreInstanceState(state.getParcelable("super"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun processTouch(v: View, event: MotionEvent): Boolean {
        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                isSlided = false
                return true
            }
            event.action == MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.x - mX) > 30 || Math.abs(event.y - mY) > 30) {
                    isSlided = true
                    return false
                }
                return true
            }
            event.action == MotionEvent.ACTION_UP -> {
                if (!isSlided) {
                    val item = findIndex(event.x, event.y)
                    if (item != mSelectedItem) {
                        deSelectTab(mSelectedItem)
                        selectTab(item)
                        mSelectedItem = item
                        mOnTabSelectedListener?.onTabSelected(item)
                    }
                    v.playSoundEffect(SoundEffectConstants.CLICK)
                }
                isSlided = false
                return v.performClick()
            }
            else -> return false
        }
    }

    private fun deSelectTab(position: Int) {
        objects[position].animateOut()
    }

    private fun selectTab(position: Int) {
        mSelector.animateTo(objects[position].point)
        objects[position].animateIn()
    }

    private fun findIndex(x: Float, y: Float): Int {
        var selected = 0
        var minDist = calcDist(x, y, objects[0])
        for (i in 0 until objects.size) {
            val innerTab = objects[i]
            val dist = calcDist(x, y, innerTab)
            if (dist < minDist) {
                minDist = dist
                selected = i
            }
        }
        return selected
    }

    private fun calcDist(x: Float, y: Float, tab: InnerTab): Float {
        val xDiff = tab.bounds.centerX() - x
        val yDiff = tab.bounds.centerY() - y
        return Math.sqrt((xDiff * xDiff + yDiff * yDiff).toDouble()).toFloat()
    }

    private fun toDrawable(@DrawableRes res: Int): Bitmap? {
        if (res == 0) return null
        val drawable = ContextCompat.getDrawable(context, res)
        return drawable?.toBitmap()
    }

    @Px
    private fun dp2px(dp: Int): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        var display: Display? = null
        if (wm != null) display = wm.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display?.getMetrics(displayMetrics)
        return (dp * displayMetrics.density + 0.5f).toInt()
    }

    private fun printLog(message: String) {
        Log.d("SmoothBottomBar", message)
    }

    private fun toRect(rect: Rect, point: PointF): Rect {
        return Rect(point.x.toInt(), point.y.toInt(), point.x.toInt() + rect.width(), point.y.toInt() + rect.height())
    }

    private fun toRectF(rect: Rect, point: PointF): RectF {
        return RectF(
            point.x, point.y,
            point.x + rect.width().toFloat(), point.y + rect.height().toFloat()
        )
    }

    private fun defaultTabs(): List<Tab> {
        return listOf(
            Tab(icon = R.drawable.ic_action_home, title = "Home"),
            Tab(icon = R.drawable.ic_action_inbox, title = "Inbox"),
            Tab(icon = R.drawable.ic_action_profile, title = "Profile")
        )
    }

    inner class InnerTab(private var icon: Icon, private var label: Label, bounds: Rect)
        : PaintObject(PointF(bounds.left.toFloat(), bounds.top.toFloat()), Paint(), bounds) {
        override fun draw(canvas: Canvas) {
            icon.draw(canvas)
            label.draw(canvas)
        }

        fun animateIn() {
            icon.animateIn()
            label.animateIn()
        }

        fun animateOut() {
            icon.animateOut()
            label.animateOut()
        }
    }

    inner class Icon(private val onPoint: PointF, private val drawable: Bitmap?,
                     defaultPoint: PointF, paint: Paint, bounds: Rect) : PaintObject(defaultPoint, paint, bounds) {
        var alpha: Int = 123

        private val alphaPropertyAnim = object : FloatPropertyCompat<Icon>("icon_alpha") {
            override fun getValue(icon: Icon?): Float {
                return icon?.alpha?.toFloat() ?: alpha.toFloat()
            }

            override fun setValue(icon: Icon?, value: Float) {
                icon?.alpha = value.toInt()
                invalidate()
            }
        }

        private val translatePropertyAnimX = object : FloatPropertyCompat<Icon>("icon_x") {
            override fun setValue(icon: Icon?, value: Float) {
                icon?.point = PointF(value, point.y)
                invalidate()
            }
            override fun getValue(icon: Icon?): Float {
                return icon?.point?.x ?: defaultPoint.x
            }
        }

        override fun draw(canvas: Canvas) {
            if (drawable != null) {
                paint.alpha = alpha
                canvas.drawBitmap(drawable, null, toRect(bounds, point), paint)
            }
        }

        fun animateIn() {
            SpringAnimation(this, translatePropertyAnimX, onPoint.x).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
            SpringAnimation(this, alphaPropertyAnim, 255f).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }

        fun animateOut() {
            SpringAnimation(this, translatePropertyAnimX, defaultPoint.x).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
            SpringAnimation(this, alphaPropertyAnim, 123f).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }
    }

    interface OnTabSelectedListener {
        fun onTabSelected(position: Int)
    }

    inner class Label(private var text: String, var alpha: Int = 0, defaultPoint: PointF, paint: Paint, bounds: Rect) :
        PaintObject(defaultPoint, paint, bounds) {

        private val alphaPropertyAnim = object : FloatPropertyCompat<Label>("label_alpha") {
            override fun getValue(label: Label?): Float {
                return label?.alpha?.toFloat() ?: alpha.toFloat()
            }

            override fun setValue(label: Label?, value: Float) {
                label?.alpha = value.toInt()
                invalidate()
            }
        }

        override fun draw(canvas: Canvas) {
            val r = Rect()
            paint.alpha = alpha
            paint.getTextBounds(text, 0, text.length, r)

            val fac = bounds.width().toFloat() * 0.85f / r.width().toFloat()
            if (fac < 1.0f) {
                mTextPaint.textSize = mTextPaint.textSize * fac
            }

            canvas.drawText(text, defaultPoint.x, bounds.centerY() - r.exactCenterY(), paint)
        }

        fun animateIn() {
            SpringAnimation(this, alphaPropertyAnim, 255f).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }

        fun animateOut() {
            SpringAnimation(this, alphaPropertyAnim, 0f).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }
    }

    inner class Selector(defaultPoint: PointF, paint: Paint, bounds: Rect) : PaintObject(defaultPoint, paint, bounds) {
        private val cornersRadius = dp2px(5).toFloat()
        private val floatPropertyAnimX = object : FloatPropertyCompat<Selector>("selector_x") {
            override fun setValue(selector: Selector?, value: Float) {
                selector?.point = PointF(value, point.y)
                invalidate()
            }
            override fun getValue(selector: Selector?): Float {
                return selector?.point?.x ?: defaultPoint.x
            }
        }

        fun animateTo(point: PointF) {
            SpringAnimation(this, floatPropertyAnimX, point.x).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(toRectF(bounds, point), cornersRadius, cornersRadius, paint)
        }
    }

    abstract inner class PaintObject(val defaultPoint: PointF, protected val paint: Paint, val bounds: Rect) {
        var point: PointF = PointF(defaultPoint.x, defaultPoint.y)
        abstract fun draw(canvas: Canvas)
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return bitmap
        }

        val width = if (bounds.isEmpty) intrinsicWidth else bounds.width()
        val height = if (bounds.isEmpty) intrinsicHeight else bounds.height()

        return Bitmap.createBitmap(width.nonZero(), height.nonZero(), Bitmap.Config.ARGB_8888).also {
            val canvas = Canvas(it)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }

    private fun Int.nonZero() = if (this <= 0) 1 else this
}