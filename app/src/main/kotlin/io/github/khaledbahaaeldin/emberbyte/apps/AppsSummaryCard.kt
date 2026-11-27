package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.MorphingNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/**
 * Total used by apps in the period, a Wi-Fi / Mobile split bar (primary for Wi-Fi, the green accent for Mobile) and the
 * two amounts. The label says "apps only": Home's hero total also counts system traffic.
 */
@Composable
fun AppsSummaryCard(
    total: Long,
    wifi: Long,
    mobile: Long,
    periodLabel: String,
    units: ByteUnits,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val shape = remember { mixedCorners(large = 28.dp, small = 10.dp, smallCorner = Corner.BottomStart) }
    val formatted = formatBytes(total, units)
    val wifiText = formatBytes(wifi, units)
    val mobileText = formatBytes(mobile, units)
    val (wifiWeight, mobileWeight) = splitWeights(wifi, mobile)
    val animatedWifi by animateFloatAsState(wifiWeight, if (reduce) snap() else motion.spatial(), label = "splitWifi")
    val animatedMobile by animateFloatAsState(mobileWeight, if (reduce) snap() else motion.spatial(), label = "splitMobile")
    val description = "Apps used $periodLabel, apps only: ${plainBytes(total, units)}. " +
        "Wi-Fi ${wifiText.value} ${wifiText.unit}, mobile ${mobileText.value} ${mobileText.unit}."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.surface2)
            .padding(16.dp)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .testTag("apps_summary_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Apps used $periodLabel (apps only)",
            style = EmberbyteType.grotesk(AppsType.SummaryLabel, 600),
            color = palette.muted,
            modifier = Modifier.testTag("apps_summary_label"),
        )
        ForceLtr {
            MorphingNumber(
                value = formatted.value,
                unit = formatted.unit,
                style = EmberbyteType.grotesk(AppsType.SummaryTotal, 700).copy(letterSpacing = (-0.02).em),
                unitStyle = EmberbyteType.grotesk(AppsType.SummaryUnit, 600),
                color = palette.text,
                contentDescription = "Total ${plainBytes(total, units)}",
                modifier = Modifier.testTag("apps_summary_total"),
            )
        }
        ForceLtr {
            Row(Modifier.fillMaxWidth().height(16.dp).testTag("apps_split_bar"), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (animatedWifi > 0.001f) {
                    Box(Modifier.weight(animatedWifi).height(16.dp).clip(CircleShape).background(palette.primary).testTag("apps_split_wifi"))
                }
                if (animatedMobile > 0.001f) {
                    Box(Modifier.weight(animatedMobile).height(16.dp).clip(CircleShape).background(palette.accent).testTag("apps_split_mobile"))
                }
            }
        }
        ForceLtr {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Wi-Fi ${wifiText.value} ${wifiText.unit}",
                    style = EmberbyteType.grotesk(AppsType.SummarySplit, 600),
                    color = palette.text,
                )
                Spacer(Modifier.width(0.dp))
                Text(
                    "Mobile ${mobileText.value} ${mobileText.unit}",
                    style = EmberbyteType.grotesk(AppsType.SummarySplit, 600),
                    color = palette.text,
                )
            }
        }
    }
}
