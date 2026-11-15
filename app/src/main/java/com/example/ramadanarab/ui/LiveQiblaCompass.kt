package com.example.ramadanarab.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

@Composable
fun LiveQiblaCompass(qiblaAngle: Double) {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            hasSensor = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val orientation = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        val normalized = (degrees + 360f) % 360f
                        azimuth += 0.15f * shortestAngleDiff(azimuth, normalized)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val diffToQibla = abs(shortestAngleDiff(azimuth, qiblaAngle.toFloat()))
    val isAligned = diffToQibla < 5f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
        if (!hasSensor) {
            Text(
                "هاتفك لا يدعم حساس البوصلة، الزاوية المحسوبة: ${String.format(Locale.ENGLISH, "%.1f°", qiblaAngle)}",
                color = Color.Gray
            )
            return@Column
        }

        Text(
            text = if (isAligned) "أنت متجه نحو القبلة الآن ✓" else "أدر الهاتف حتى تطابق الإبرة الذهبية الأعلى",
            color = if (isAligned) EmeraldDark else Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                drawCircle(
                    color = if (isAligned) EmeraldDark else Color.LightGray,
                    radius = radius,
                    style = Stroke(width = 8f)
                )

                rotate(-azimuth, pivot = center) {
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius * 0.75f),
                        strokeWidth = 6f
                    )
                }

                val needleRotation = qiblaAngle.toFloat() - azimuth
                rotate(needleRotation, pivot = center) {
                    val path = Path().apply {
                        moveTo(center.x, center.y - radius * 0.85f)
                        lineTo(center.x - 18f, center.y)
                        lineTo(center.x + 18f, center.y)
                        close()
                    }
                    drawPath(path = path, color = if (isAligned) EmeraldDark else RamadanGold)
                    drawCircle(color = EmeraldDark, radius = 14f, center = center)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "زاوية القبلة: ${String.format(Locale.ENGLISH, "%.1f°", qiblaAngle)}",
            color = RamadanGold,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun shortestAngleDiff(from: Float, to: Float): Float {
    return ((to - from + 540f) % 360f) - 180f
}
