package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.spokenUnit

internal fun spokenNetworkBytes(bytes: Long, units: ByteUnits = ByteUnits.DECIMAL): String {
    val f = formatBytes(bytes, units)
    val displayVal = if (f.value.contains(".") && f.value.endsWith("0")) {
        f.value.trimEnd('0').removeSuffix(".")
    } else {
        f.value
    }
    return "$displayVal ${spokenUnit(f.unit)}"
}

/**
 * Expressive network usage bar visualizing the Mobile vs Wi-Fi split (Contract 13.1).
 *
 * Displays two rounded segments with a 3 dp gap proportional to byte counts, ensuring a minimum
 * visible width of 6 dp for any non-zero segment. Exposes spoken description for accessibility.
 */
@Composable
fun NetworkBar(
    mobileBytes: Long,
    wifiBytes: Long,
    mobileColor: Color,
    wifiColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    units: ByteUnits = ByteUnits.DECIMAL,
) {
    val spokenDesc = "Mobile ${spokenNetworkBytes(mobileBytes, units)}, Wi-Fi ${spokenNetworkBytes(wifiBytes, units)}"
    val safeMobile = mobileBytes.coerceAtLeast(0L)
    val safeWifi = wifiBytes.coerceAtLeast(0L)
    val hasMobile = safeMobile > 0L
    val hasWifi = safeWifi > 0L

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                contentDescription = spokenDesc
            },
    ) {
        val totalWidth = maxWidth
        val minSegmentWidth = 6.dp
        val gap = 3.dp

        if (!hasMobile && !hasWifi) {
            return@BoxWithConstraints
        }

        if (hasMobile && !hasWifi) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(CircleShape)
                    .background(mobileColor)
                    .testTag("network_bar_mobile")
            )
            return@BoxWithConstraints
        }

        if (!hasMobile && hasWifi) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(CircleShape)
                    .background(wifiColor)
                    .testTag("network_bar_wifi")
            )
            return@BoxWithConstraints
        }

        val availableWidth = (totalWidth - gap).coerceAtLeast(0.dp)
        val totalBytes = safeMobile + safeWifi
        val rawMobileWidth = availableWidth * (safeMobile.toDouble() / totalBytes.toDouble()).toFloat()
        val rawWifiWidth = availableWidth - rawMobileWidth

        var mobileWidth = rawMobileWidth
        var wifiWidth = rawWifiWidth

        if (availableWidth >= minSegmentWidth * 2) {
            if (mobileWidth < minSegmentWidth) {
                mobileWidth = minSegmentWidth
                wifiWidth = availableWidth - minSegmentWidth
            } else if (wifiWidth < minSegmentWidth) {
                wifiWidth = minSegmentWidth
                mobileWidth = availableWidth - minSegmentWidth
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(mobileWidth)
                    .height(height)
                    .clip(CircleShape)
                    .background(mobileColor)
                    .testTag("network_bar_mobile")
            )
            Box(
                modifier = Modifier
                    .width(wifiWidth)
                    .height(height)
                    .clip(CircleShape)
                    .background(wifiColor)
                    .testTag("network_bar_wifi")
            )
        }
    }
}
