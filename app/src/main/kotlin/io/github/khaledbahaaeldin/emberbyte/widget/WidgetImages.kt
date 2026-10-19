package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.graphics.shapes.star
import io.github.khaledbahaaeldin.emberbyte.ui.design.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.toArgb
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bitmap image renderer for RemoteViews widgets (Contract 13.7).
 *
 * Renders hero numbers, budget progress rings, progress bars, app icons, and mini charts
 * directly into ARGB_8888 Bitmaps without requiring Compose runtime inside Glance RemoteViews.
 */
object WidgetImages {

    @VisibleForTesting
    internal var typefaceFactory: ((context: Context, resId: Int, variationSettings: String) -> Typeface?)? = null

    @VisibleForTesting
    internal fun resetTypefaceFactory() {
        typefaceFactory = null
    }

    private fun buildTypeface(context: Context, resId: Int, variationSettings: String): Typeface {
        val customFactory = typefaceFactory
        if (customFactory != null) {
            val custom = customFactory(context, resId, variationSettings)
            if (custom != null) return custom
            return Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val typeface = Typeface.Builder(context.resources.assets, "font/roboto_flex.ttf")
                    .setFontVariationSettings(variationSettings)
                    .build()
                if (typeface != null) return typeface
            } catch (_: Throwable) {}

            try {
                val pfd = context.resources.openRawResourceFd(resId)
                if (pfd != null) {
                    val typeface = pfd.use {
                        Typeface.Builder(it.fileDescriptor)
                            .setFontVariationSettings(variationSettings)
                            .build()
                    }
                    if (typeface != null) return typeface
                }
            } catch (_: Throwable) {}
        }

        return Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private fun shapePath(shape: AppShape, sizePx: Float): Path {
        return when (shape) {
            AppShape.Cookie -> {
                // 9-sided rounded cookie
                val poly = RoundedPolygon.star(
                    numVerticesPerRadius = 9,
                    radius = sizePx / 2f,
                    innerRadius = sizePx / 2f * 0.8f,
                    rounding = CornerRounding(sizePx * 0.15f),
                    innerRounding = CornerRounding(sizePx * 0.15f),
                    centerX = sizePx / 2f,
                    centerY = sizePx / 2f,
                )
                poly.toPath()
            }
            AppShape.Squircle -> {
                val path = Path()
                val radius = sizePx * 0.32f
                path.addRoundRect(RectF(0f, 0f, sizePx, sizePx), radius, radius, Path.Direction.CW)
                path
            }
            AppShape.Sunny -> {
                val poly = RoundedPolygon.star(
                    numVerticesPerRadius = 8,
                    radius = sizePx / 2f,
                    innerRadius = sizePx / 2f * 0.85f,
                    rounding = CornerRounding(sizePx * 0.15f),
                    innerRounding = CornerRounding(sizePx * 0.15f),
                    centerX = sizePx / 2f,
                    centerY = sizePx / 2f,
                )
                poly.toPath()
            }
            AppShape.Burst -> {
                val poly = RoundedPolygon.star(
                    numVerticesPerRadius = 12,
                    radius = sizePx / 2f,
                    innerRadius = sizePx / 2f * 0.70f,
                    rounding = CornerRounding(sizePx * 0.10f),
                    innerRounding = CornerRounding(sizePx * 0.10f),
                    centerX = sizePx / 2f,
                    centerY = sizePx / 2f,
                )
                poly.toPath()
            }
        }
    }

    fun heroNumber(
        text: String,
        unit: String,
        palette: EmberbytePalette,
        widthPx: Int,
        heightPx: Int,
        context: Context? = null,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val ctx = context ?: try {
            val app = Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as? Context
            app ?: try {
                val testApp = Class.forName("androidx.test.core.app.ApplicationProvider")
                    .getMethod("getApplicationContext")
                    .invoke(null) as? Context
                testApp ?: throw IllegalStateException("No context available")
            } catch (_: Throwable) {
                throw IllegalStateException("No context available")
            }
        } catch (_: Throwable) {
            throw IllegalStateException("No context available")
        }

        val typeface = buildTypeface(ctx, R.font.roboto_flex, "'wght' 800, 'wdth' 36")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = palette.primary.toArgb()
            this.typeface = typeface
            this.textAlign = Paint.Align.LEFT
        }

        var textSize = heightPx * 0.85f
        paint.textSize = textSize
        var textWidth = paint.measureText(text)
        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = palette.onPrimary.toArgb()
            this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            this.textAlign = Paint.Align.CENTER
        }

        val badgeSize = (heightPx * 0.42f).coerceAtLeast(16f)
        val spacing = 8f
        val maxAvailableWidth = (widthPx - badgeSize - spacing - 8f).coerceAtLeast(20f)

        while (textWidth > maxAvailableWidth && textSize > 12f) {
            textSize -= 2f
            paint.textSize = textSize
            textWidth = paint.measureText(text)
        }

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val baseline = heightPx / 2f + textBounds.height() / 2f

        val totalWidth = textWidth + spacing + badgeSize
        val startX = (widthPx - totalWidth) / 2f

        // Draw main number
        canvas.drawText(text, startX, baseline, paint)

        // Draw cookie unit badge
        val badgeX = startX + textWidth + spacing
        val badgeY = baseline - textBounds.height() + (textBounds.height() - badgeSize) / 2f

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = palette.primary.toArgb()
            this.style = Paint.Style.FILL
        }

        val cookiePath = shapePath(AppShape.Cookie, badgeSize)
        canvas.save()
        canvas.translate(badgeX, badgeY)
        canvas.drawPath(cookiePath, badgeBgPaint)

        unitPaint.textSize = badgeSize * 0.45f
        val unitBounds = Rect()
        unitPaint.getTextBounds(unit, 0, unit.length, unitBounds)
        val unitY = badgeSize / 2f + unitBounds.height() / 2f
        canvas.drawText(unit, badgeSize / 2f, unitY, unitPaint)
        canvas.restore()

        return bitmap
    }

    fun ring(
        fraction: Float,
        state: BudgetStateUi,
        palette: EmberbytePalette,
        sizePx: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokeWidth = sizePx * 0.12f
        val amplitude = strokeWidth * 0.25f
        val baseRadius = (sizePx - strokeWidth) / 2f - amplitude
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val waves = 12
        val startAngle = -Math.PI.toFloat() / 2f

        val trackColor = palette.track.toArgb()
        val arcColor = when (state) {
            BudgetStateUi.Ok -> palette.primary.toArgb()
            BudgetStateUi.Warning -> palette.warn.toArgb()
            BudgetStateUi.Exceeded -> palette.danger.toArgb()
        }

        // 1. Wavy track (360 degrees)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = trackColor
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            this.strokeCap = Paint.Cap.ROUND
        }
        val trackPath = buildWavyCirclePath(cx, cy, baseRadius, amplitude, waves, startAngle, 2f * Math.PI.toFloat())
        canvas.drawPath(trackPath, trackPaint)

        // 2. Outline ring at 0 percent
        val clampedFraction = if (fraction.isNaN() || fraction < 0f) 0f else minOf(fraction, 1f)
        if (clampedFraction == 0f) {
            val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = palette.muted.toArgb()
                this.style = Paint.Style.STROKE
                this.strokeWidth = 2f
            }
            canvas.drawCircle(cx, cy, baseRadius, outlinePaint)
        } else {
            // 3. Progress arc in state color
            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = arcColor
                this.style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                this.strokeCap = Paint.Cap.ROUND
            }
            val arcPath = buildWavyCirclePath(cx, cy, baseRadius, amplitude, waves, startAngle, clampedFraction * 2f * Math.PI.toFloat())
            canvas.drawPath(arcPath, arcPaint)
        }

        return bitmap
    }

    fun bar(
        fraction: Float,
        color: Int,
        widthPx: Int,
        heightPx: Int,
        wavy: Boolean,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val clamped = if (fraction.isNaN() || fraction < 0f) 0f else minOf(fraction, 1f)
        val strokeWidth = (heightPx * 0.35f).coerceAtLeast(3f)
        val cy = heightPx / 2f
        val startX = strokeWidth / 2f
        val endX = widthPx - strokeWidth / 2f
        val availableWidth = (endX - startX).coerceAtLeast(0f)

        // Track line
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.DKGRAY
            this.strokeWidth = strokeWidth
            this.strokeCap = Paint.Cap.ROUND
            this.style = Paint.Style.STROKE
        }
        canvas.drawLine(startX, cy, endX, cy, trackPaint)

        // Filled part
        if (clamped > 0f && availableWidth > 0f) {
            val fillEndX = startX + availableWidth * clamped
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                this.strokeWidth = strokeWidth
                this.strokeCap = Paint.Cap.ROUND
                this.style = Paint.Style.STROKE
            }

            if (wavy) {
                val fillPath = Path()
                val wavelengthPx = 24f
                val amplitude = (heightPx / 2f - strokeWidth / 2f).coerceAtLeast(1f)
                val fillWidth = fillEndX - startX
                val steps = maxOf(4, (fillWidth / 2f).toInt())
                val dx = fillWidth / steps

                for (i in 0..steps) {
                    val x = startX + i * dx
                    val wavePhase = (x - startX) / wavelengthPx * 2f * Math.PI.toFloat()
                    val y = cy + amplitude * sin(wavePhase)
                    if (i == 0) {
                        fillPath.moveTo(x, y)
                    } else {
                        fillPath.lineTo(x, y)
                    }
                }
                canvas.drawPath(fillPath, fillPaint)
            } else {
                canvas.drawLine(startX, cy, fillEndX, cy, fillPaint)
            }
        }

        return bitmap
    }

    fun appIcon(
        icon: Bitmap?,
        shape: AppShape,
        letter: String,
        sizePx: Int,
        palette: EmberbytePalette,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val clipPath = shapePath(shape, sizePx.toFloat())

        if (icon != null) {
            canvas.save()
            canvas.clipPath(clipPath)
            val src = Rect(0, 0, icon.width, icon.height)
            val dst = Rect(0, 0, sizePx, sizePx)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(icon, src, dst, paint)
            canvas.restore()
        } else {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = palette.surface2.toArgb()
                this.style = Paint.Style.FILL
            }
            canvas.drawPath(clipPath, bgPaint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = palette.primary.toArgb()
                this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                this.textSize = sizePx * 0.5f
                this.textAlign = Paint.Align.CENTER
            }
            val charText = if (letter.isNotEmpty()) letter.take(1).uppercase() else "?"
            val textBounds = Rect()
            textPaint.getTextBounds(charText, 0, charText.length, textBounds)
            val y = sizePx / 2f + textBounds.height() / 2f
            canvas.drawText(charText, sizePx / 2f, y, textPaint)
        }

        return bitmap
    }

    fun chart(
        values: List<Long>,
        palette: EmberbytePalette,
        widthPx: Int,
        heightPx: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (values.isEmpty()) return bitmap

        val maxValue = (values.maxOrNull() ?: 1L).coerceAtLeast(1L).toFloat()
        val barCount = values.size
        val barGap = (widthPx * 0.05f / barCount).coerceAtLeast(2f)
        val totalGaps = barGap * (barCount - 1)
        val barWidth = ((widthPx - totalGaps) / barCount).coerceAtLeast(2f)

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = palette.primary.toArgb()
            this.style = Paint.Style.FILL
        }

        for (i in 0 until barCount) {
            val value = values[i]
            val barHeight = ((value.toFloat() / maxValue) * (heightPx * 0.85f)).coerceAtLeast(2f)
            val left = i * (barWidth + barGap)
            val right = left + barWidth
            val bottom = heightPx.toFloat()
            val top = bottom - barHeight
            val rect = RectF(left, top, right, bottom)
            val radius = barWidth / 2f
            canvas.drawRoundRect(rect, radius, radius, barPaint)
        }

        return bitmap
    }

    private fun buildWavyCirclePath(
        cx: Float,
        cy: Float,
        baseRadius: Float,
        amplitude: Float,
        waves: Int,
        startAngleRad: Float,
        sweepAngleRad: Float,
    ): Path {
        val path = Path()
        val steps = maxOf(4, (16 * waves * (sweepAngleRad / (2f * Math.PI.toFloat()))).toInt())
        val dTheta = sweepAngleRad / steps
        for (i in 0..steps) {
            val currentAngle = startAngleRad + i * dTheta
            val phase = i * dTheta
            val r = baseRadius + amplitude * cos(waves * phase)
            val x = cx + r * cos(currentAngle)
            val y = cy + r * sin(currentAngle)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        return path
    }
}
