package io.github.khaledbahaaeldin.emberbyte.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/** Test tag of the soft edge drawn under the top bar. */
internal const val SCREEN_SCAFFOLD_SCRIM = "screen_scaffold_scrim"

/** How far the scrim fades below the top bar. */
private val ScrimHeight = 24.dp

/** Default bottom room for the floating navigation pill (the larger of the old 112 dp and 120 dp values). */
private val DefaultFloatingToolbarReserve = 120.dp

/**
 * One scaffold for every scrolling screen, so none of them lets content slide under the clock or hard-clips it.
 *
 * - A flexible Material 3 Expressive top app bar that collapses on scroll and always keeps the title visible. It draws
 *   its own status-bar inset, so nothing is ever drawn under the clock and battery.
 * - A [ScrimHeight] soft edge under the bar so rows fade out instead of being cut.
 * - [content] receives padding that clears the top bar and reserves [floatingToolbarReserve] at the bottom for the
 *   floating navigation pill. Pass it to the scrolling list as `contentPadding`; do not add your own
 *   `statusBarsPadding()`.
 *
 * [onBack] adds the back arrow (48 dp target); leave it null on top-level screens. [actions] are trailing icons.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmberbyteScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingToolbarReserve: Dp = DefaultFloatingToolbarReserve,
    content: @Composable (PaddingValues) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = palette.background,
        contentColor = palette.text,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = palette.text),
                            modifier = Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Back" },
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = actions,
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background,
                    scrolledContainerColor = palette.background,
                    titleContentColor = palette.text,
                    navigationIconContentColor = palette.text,
                    actionIconContentColor = palette.text,
                ),
            )
        },
        bottomBar = bottomBar,
    ) { inner ->
        val top = inner.calculateTopPadding()
        Box(Modifier.fillMaxSize()) {
            content(
                PaddingValues(
                    top = top,
                    bottom = inner.calculateBottomPadding() + floatingToolbarReserve,
                ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = top)
                    .fillMaxWidth()
                    .height(ScrimHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(palette.background, palette.background.copy(alpha = 0f)),
                        ),
                    )
                    .testTag(SCREEN_SCAFFOLD_SCRIM),
            )
        }
    }
}
