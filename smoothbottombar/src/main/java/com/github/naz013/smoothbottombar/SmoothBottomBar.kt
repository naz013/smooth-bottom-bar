package com.github.naz013.smoothbottombar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class SmoothBottomBar : View {

    private var mTabs: List<Tab> = listOf()

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, deffStyles: Int): super(context, attributeSet, deffStyles) {

    }

    override fun onDraw(canvas: Canvas?) {

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

    }
}