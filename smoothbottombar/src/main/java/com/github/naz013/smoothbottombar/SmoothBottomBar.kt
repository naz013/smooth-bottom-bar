package com.github.naz013.smoothbottombar

import android.content.Context
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

class SmoothBottomBar : View {

    private var mTabs: List<Tab> = listOf()
    @ColorInt
    private var mBgColor: Int = Color.BLUE
    @ColorInt
    private var mTextColor: Int = Color.WHITE
    @ColorInt
    private var mSelectorColor: Int = Color.LTGRAY

    private val mTextPaint = Paint()
    private val mSelectorPaint = Paint()
    private val mIconPaint = Paint()

    private var mSelector: Selector = Selector(Point(0, 0), mSelectorPaint, Rect())
    private val objects: MutableList<InnerTab> = mutableListOf()

    private var mSelectedItem = 0
    private var isSlided = false

    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
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
        mTextPaint.style = Paint.Style.STROKE
        mTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.toFloat(), resources.displayMetrics)

        mSelectorPaint.color = mSelectorColor
        mSelectorPaint.style = Paint.Style.FILL

        mIconPaint.style = Paint.Style.STROKE

        setOnTouchListener { v, event -> processTouch(v, event) }
    }

    private fun processTouch(v: View, event: MotionEvent): Boolean {
        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                isSlided = false
                val item = findIndex(event.x, event.y)
                printLog("processTouch: down $item")
                return true
            }
            event.action == MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.x - mX) > 10 || Math.abs(event.y - mY) > 10) {
                    isSlided = true
                    return false
                }
                printLog("processTouch: slide ")
                return true
            }
            event.action == MotionEvent.ACTION_UP -> {
                if (!isSlided) {
                    val item = findIndex(event.x, event.y)
                    if (item != mSelectedItem) {
                        deSelectTab(mSelectedItem)
                        selectTab(item)
                        mSelectedItem = item
                        invalidate()
    //                    if (getOnTabSelectedListener() != null) {
    //                        getOnTabSelectedListener().onTabSelected(item)
    //                    }
                    }
                    printLog("processTouch: up $item")
                    v.playSoundEffect(SoundEffectConstants.CLICK)
                }
                isSlided = false
                return v.performClick()
            }
            else -> return false
        }
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

    fun setTabs(tabs: List<Tab>) {
        if (tabs.isEmpty()) {
            throw IllegalArgumentException("Tabs cannot be empty")
        }
        this.mTabs = tabs
        invalidate()
    }

    private fun deSelectTab(position: Int) {
        objects[position].label.alpha = 0
        objects[position].icon.alpha = 155
        objects[position].icon.point = objects[position].icon.defaultPoint
    }

    private fun selectTab(position: Int) {
        objects[position].label.alpha = 255
        objects[position].icon.alpha = 255
        objects[position].icon.point = objects[position].icon.onPoint
        mSelector.point = objects[position].defaultPoint
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
        val margin = dp2px(16)
        val tabWidth = (width - (margin * 2)) / mTabs.size
        for (i in 0 until mTabs.size) {
            val tab = mTabs[i]
            val left = i * tabWidth + margin
            val rect = Rect(left, margin, left + tabWidth, height - margin)
            val leftHalf = Rect(rect.left, rect.top, rect.centerX(), rect.bottom)
            val rightHalf = Rect(rect.centerX(), rect.top, rect.right, rect.bottom)
            val label = Label(tab.title ?: "", 0, Point(rightHalf.left, rightHalf.centerY()), mTextPaint, rightHalf)
            val iconHeight = rect.height() / 2
            val iconHeightHalf = iconHeight / 2
            val onPoint = Point(leftHalf.centerX() - iconHeightHalf, rect.centerY() - iconHeightHalf)
            val offPoint = Point(rect.centerX() - iconHeightHalf, rect.centerY() - iconHeightHalf)
            val iconBounds = Rect(0, 0, iconHeight, iconHeight)
            val icon = Icon(onPoint, toDrawable(tab.icon), 255, offPoint, mIconPaint, iconBounds)
            val innerTab = InnerTab(icon, label, rect)
            elements.add(innerTab)
            if (i == 0) {
                mSelector = Selector(Point(margin, margin), mSelectorPaint, rect)
            }
        }
        objects.clear()
        objects.addAll(elements)
        if (mSelectedItem in 0..objects.size) {
            selectTab(mSelectedItem)
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

    private fun toRect(rect: Rect, point: Point): Rect {
        return Rect(point.x, point.y, point.x + rect.width(), point.y + rect.height())
    }

    private fun toRectF(rect: Rect, point: Point): RectF {
        return RectF(
            point.x.toFloat(), point.y.toFloat(),
            point.x.toFloat() + rect.width().toFloat(), point.y.toFloat() + rect.height().toFloat()
        )
    }

    inner class InnerTab(var icon: Icon, var label: Label, bounds: Rect) : PaintObject(Point(bounds.left, bounds.top), Paint(), bounds) {
        override fun draw(canvas: Canvas) {
            icon.draw(canvas)
            label.draw(canvas)
        }
    }

    inner class Icon(val onPoint: Point, private val drawable: Bitmap?, var alpha: Int = 255, defaultPoint: Point, paint: Paint, bounds: Rect) :
        PaintObject(defaultPoint, paint, bounds) {
        override fun draw(canvas: Canvas) {
            if (drawable != null) {
                paint.alpha = alpha
                canvas.drawBitmap(drawable, null, toRect(bounds, point), paint)
            }
        }
    }

    inner class Label(private var text: String, var alpha: Int = 0, defaultPoint: Point, paint: Paint, bounds: Rect) :
        PaintObject(defaultPoint, paint, bounds) {
        override fun draw(canvas: Canvas) {
            val r = Rect()
            paint.alpha = alpha
            paint.getTextBounds(text, 0, text.length, r)

            if (objects.size > 3) {
                val fac = bounds.width().toFloat() / r.width().toFloat()
                mTextPaint.textSize = mTextPaint.textSize * fac
            }

            canvas.drawText(text, defaultPoint.x.toFloat(), bounds.centerY() - r.exactCenterY(), paint)
        }
    }

    inner class Selector(defaultPoint: Point, paint: Paint, bounds: Rect) : PaintObject(defaultPoint, paint, bounds) {
        private val cornersRadius = dp2px(5).toFloat()
        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(toRectF(bounds, point), cornersRadius, cornersRadius, paint)
        }
    }

    abstract inner class PaintObject(val defaultPoint: Point, protected val paint: Paint, val bounds: Rect) {
        var point: Point = Point(defaultPoint.x, defaultPoint.y)
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