package com.hiray

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.IntRange
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import java.util.*

/**
 * Created by hiray on 2018/7/3.
 * @author hiray
 */
class RollingNumberTextView : TextView {

    val TAG = "RollingNumberTextView"
    private val INTERVAL = 80L
    private val MODEL_TEXT = "0123456789"
    private var charWidth: Float = 0f
    // the real number we need to draw
    private lateinit var textArray: List<Int>
    //set a  mark that certain column has finished the rolling animation
    private lateinit var finishedColumn: BitSet
    private var baseLine: Float = 0f
    private lateinit var rolledOffset: Array<Int>
    private var running = false
    private val rollingRunnable = RollingRunnable()
    //default 10 ,and we cycle once   20 twice ...
    @IntRange(from = 10)
    var columns = 10
    /**
     * set speed(unit:pixel) for every rolling column
     */
    lateinit var speed: Array<Int>

    inner class RollingRunnable : Runnable {

        override fun run() {
            for (i in 0 until textArray.size)
                rolledOffset[i] -= speed[i]
            postOnAnimationDelayed(this@RollingRunnable, INTERVAL)
            invalidate()
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        //ascii '0' = 48
        textArray = text.map({ c -> c.toInt() - 48 }).toList()
        finishedColumn = BitSet(textArray.size)
        speed = Array(textArray.size, { i -> 25 - i })
        rolledOffset = Array(textArray.size, { 0 })
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val top = paint.fontMetrics.top
        val bottom = paint.fontMetrics.bottom
        baseLine = measuredHeight / 2 + (-top + bottom) / 2 - bottom
        charWidth = paint.measureText(MODEL_TEXT) / 10
    }


    /**
     * if we setText("1896"),actually we draw four columns of numbers
     * but first we shift the initial number(because it is  'random' number textview)
     * for '1' we shift and get the serial numbers for first column:34578901
     *  '8' : 012345678
     *  '9' : 123456789
     *  '6' : 890123456
     *
     * and we notice that if we combine the last number of each serial numbers ,we get the very '1896'
     * yeah ,what we need to do next is rolling the serial numbers util the certain number show up and
     * stop the certain column's rolling ，and do the same for each column until all the
     * numbers('1896') show up.
     * By the way the example above just shift numbers the same ,you can shift differently for each,
     * but it will be a little more complex
     */
    override fun onDraw(canvas: Canvas) {
        for (i in 0 until textArray.size)
            for (j in 0 until columns) {
                val columnFinished = checkColumnFinished(i, j)
                val text = if (columnFinished) textArray[i].toString() else getNumberText(textArray[i], j)
                val x = i * charWidth
                val base = if (columnFinished) baseLine
                else baseLine + j * measuredHeight + rolledOffset[i]

                if (base >= -measuredHeight + baseLine && base <= measuredHeight + baseLine) {
                    drawText(canvas, text, x, base, paint)
                }
                if (finishedColumn.cardinality() == textArray.size) {
                    removeCallbacks(rollingRunnable)
                    reset()
                }
            }

    }

    /**
     * @param columnIndex  which column of numbers
     * @param rowIndex the number's row index of [columnIndex] column
     */
    private fun checkColumnFinished(columnIndex: Int, rowIndex: Int): Boolean {

        val y = baseLine + rowIndex * measuredHeight + rolledOffset[columnIndex]
        if (rowIndex == columns - 1 && y <= baseLine) {
            Log.i(TAG, ": rowIndex:$rowIndex  rolledOffset:${rolledOffset[columnIndex].toFloat() / measuredHeight}")
            //the last number has rolled up into the textview's area,
            //so this column has been marked  'finished'
            finishedColumn.set(columnIndex)
        }
        return finishedColumn.get(columnIndex)
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, painter: Paint) {
        canvas.drawText(text, x, y, painter)
    }

    /**01234569012345690
     *     456789012345690
     * we calculate from end to get the number for certain rowIndex,the calculation will
     * be quite easy and quick
     */
    private fun getNumberText(origin: Int, rowIndex: Int): String {
        val r = origin - (columns - 1 - rowIndex)
        if (r >= 0) return (r % 10).toString()
        else {
            var e = (r + 10) % 10
            if (e < 0)
                e += 10
            return e.toString()
        }
    }

    private fun reset() {
        rolledOffset.indices.forEach { i -> rolledOffset[i] = 0 }
        running = false
        finishedColumn.clear()
    }

    fun roll() {
        if (running)
            return
        postDelayed(rollingRunnable, INTERVAL)
        running = true
    }

}