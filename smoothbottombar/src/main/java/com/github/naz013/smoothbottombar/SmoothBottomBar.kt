package com.github.naz013.smoothbottombar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.Px

class SmoothBottomBar : View {

    private var mTabs: List<Tab> = listOf()
    @ColorInt
    private var mBgColor: Int = Color.BLUE
    @ColorInt
    private var mTextColor: Int = Color.WHITE
    @ColorInt
    private var mIconColor: Int = Color.WHITE
    @ColorInt
    private var mSelectorColor: Int = Color.LTGRAY

    private val mTextPaint = Paint()
    private val mSelectorPaint = Paint()
    private val mIconPaint = Paint()

    private var mSelector: Selector = Selector(Point(0, 0), mSelectorPaint, Rect())

    private val objects: MutableList<PaintObject> = mutableListOf()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SmoothBottomBar, 0, 0)
            try {
                mBgColor = a.getColor(R.styleable.SmoothBottomBar_bar_background, mBgColor)
                mTextColor = a.getColor(R.styleable.SmoothBottomBar_bar_textColor, mTextColor)
                mIconColor = a.getColor(R.styleable.SmoothBottomBar_bar_iconColor, mIconColor)
                mSelectorColor = a.getColor(R.styleable.SmoothBottomBar_bar_selectorColor, mSelectorColor)
            } catch (e: Exception) {
                printLog("init: " + e.localizedMessage)
            } finally {
                a.recycle()
            }
        }
        mTextPaint.color = mTextColor
        mTextPaint.style = Paint.Style.STROKE

        mSelectorPaint.color = mSelectorColor
        mSelectorPaint.style = Paint.Style.FILL

        mIconPaint.color = mIconColor
        mIconPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            canvas.drawColor(mBgColor)
            objects.forEach { it.draw(canvas) }
//            for (r in mTabRects) {
//                canvas?.drawRect(r, mTextPaint)
//            }
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, height)
        calculateRectangles(width, height)
    }

    private fun calculateRectangles(width: Int, height: Int) {
        val elements = mutableListOf<PaintObject>()
        val margin = dp2px(16)
        val tabWidth = (width - (margin * 2)) / mTabs.size
        for (i in 0 until mTabs.size) {
            val tab = mTabs[i]

            val left = i * tabWidth + margin
            val rect = Rect(left, margin, left + tabWidth, height - margin)

            val leftHalf = Rect(rect.left, rect.top, rect.centerX(), rect.bottom)
            val rightHalf = Rect(rect.centerX(), rect.top, rect.right, rect.bottom)

            val label = Label(tab.title ?: "", 0, Point(rightHalf.left, rightHalf.centerY()), mTextPaint, rightHalf)

            if (i == 0) {
                mSelector = Selector(Point(margin, margin), mSelectorPaint, rect)
                elements.add(mSelector)
            }
        }
        objects.clear()
        objects.addAll(elements)
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

    private fun toRectF(rect: Rect, point: Point): RectF {
        return RectF(
            point.x.toFloat(), point.y.toFloat(),
            point.x.toFloat() + rect.width().toFloat(), point.y.toFloat() + rect.height().toFloat()
        )
    }

    inner class Icon(var onPoint: Point, var drawable: Drawable, var alpha: Int = 255, defaultPoint: Point, paint: Paint, bounds: Rect) :
        PaintObject(defaultPoint, paint, bounds) {
        override fun draw(canvas: Canvas) {
//            val r = Rect()
//            mTextPaint.alpha = alpha
//            mTextPaint.getTextBounds(text, 0, text.length, r)
//            canvas.drawText(text, bounds.centerX().toFloat(), bounds.centerY() + (r.height() / 2f), mTextPaint)
        }
    }

    inner class Label(var text: String, var alpha: Int = 0, defaultPoint: Point, paint: Paint, bounds: Rect) :
        PaintObject(defaultPoint, paint, bounds) {
        override fun draw(canvas: Canvas) {
            val r = Rect()
            paint.alpha = alpha
            paint.getTextBounds(text, 0, text.length, r)
            canvas.drawText(text, defaultPoint.x.toFloat(), defaultPoint.y.toFloat(), paint)
        }
    }

    inner class Selector(defaultPoint: Point, paint: Paint, bounds: Rect) : PaintObject(defaultPoint, paint, bounds) {
        private val cornersRadius = dp2px(5).toFloat()
        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(toRectF(bounds, point), cornersRadius, cornersRadius, paint)
        }
    }

    abstract inner class PaintObject(val defaultPoint: Point, val paint: Paint, val bounds: Rect) {
        var point: Point = Point(defaultPoint.x, defaultPoint.y)
        abstract fun draw(canvas: Canvas)
    }
}