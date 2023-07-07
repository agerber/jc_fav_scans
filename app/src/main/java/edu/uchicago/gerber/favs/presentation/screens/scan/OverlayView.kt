
/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uchicago.gerber.favs.presentation.screens.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import edu.uchicago.gerber.favs.R


class OverlayView(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {

    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var buttonPaint = Paint()
    private var buttonTextPaint = Paint()

    // private var confidenceThreshold = .toFloat() / 100
    private var currentLabel = ""

    private var results: List<ImageClassificationHelper.Recognition>? = emptyList()

    private var bounds = Rect()

    var onLabelClickListener: OnLabelClickListener? = null

    private var confidenceThr = 75

    init {
        initPaints()
    }


    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.style = Paint.Style.FILL
        buttonTextPaint.textSize = 100f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE

        buttonPaint.color = ContextCompat.getColor(context!!, R.color.button_color)
        buttonPaint.strokeWidth = 8F
        buttonPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val measuredY = this.measuredHeight.toFloat()
        val buttonHeight = measuredY * 0.2
        val inBounds = event.y > (measuredY - buttonHeight)
        if (inBounds && currentLabel.isNotEmpty()) {
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    buttonPaint.color = ContextCompat.getColor(context!!, R.color.button_color_dark)
                    buttonTextPaint.color =
                        ContextCompat.getColor(context!!, R.color.button_text_pressed)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    buttonPaint.color = ContextCompat.getColor(context!!, R.color.button_color)
                    buttonTextPaint.color = ContextCompat.getColor(context!!, R.color.white)

                    Log.d("TOUCH", "BUTTON RELEASED")

                    onLabelClickListener?.onLabelClicked(currentLabel)
                    return true
                }
            }
        }
        return false
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (results == null || results!!.isEmpty()) {
            return
        }

        val measuredX = this.measuredWidth.toFloat()
        val measuredY = this.measuredHeight.toFloat()
        val buttonHeight = measuredY * 0.2f
        val buttonRect = RectF(0f, (measuredY - buttonHeight), measuredX, measuredY)

        val label = results!!.first().title
        val confidence = results!!.first().confidence

        val labelWithConfidence =  "$label : ${String.format("%.2f", confidence)}"
        val textWidth = buttonTextPaint.measureText(labelWithConfidence)
        textBackgroundPaint.getTextBounds(label, 0, label.length, bounds)
        val textHeight = bounds.height().toFloat()
        val textX = (measuredX - textWidth) / 2

        if (confidence > (confidenceThr.toFloat() / 100)){
            currentLabel = label
            canvas.drawRect(buttonRect, buttonPaint)
            canvas.drawText(
                labelWithConfidence,
                textX,
                measuredY - (buttonHeight / 2f) + (textHeight / 2f),
                buttonTextPaint
            )

        } else {
            currentLabel = ""
        }


    }

    fun setResults(
        detectionResults: List<ImageClassificationHelper.Recognition>?,
    ) {
        results = detectionResults
        invalidate()
    }

    fun setConfidenceThreshold(confidence: Int) {
        confidenceThr = confidence
    }

    fun setOnButtonClickListener(listener: OnLabelClickListener) {
        onLabelClickListener = listener
    }

    interface OnLabelClickListener {
        fun onLabelClicked(name: String)
    }

}