package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsScreenTest {
    @get:Rule val rule = createComposeRule()

    private fun show(
        settings: Settings = Settings(),
        onUpdate: ((Settings) -> Settings) -> Unit = {},
        onBack: () -> Unit = {},
        onOpenBatterySettings: () -> Unit = {},
        onOpenAppearance: () -> Unit = {},
        onOpenSource: () -> Unit = {},
        onOpenLicenses: () -> Unit = {},
        onExportCsv: (ExportRange) -> Unit = {},
        onExportJson: () -> Unit = {},
        onCreateBackup: (CharArray) -> Unit = {},
        onRestoreChosen: () -> Unit = {},
        fontScale: Float = 1f,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) {
        rule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density, fontScale = fontScale),
                LocalLayoutDirection provides layoutDirection,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.size(360.dp, 2400.dp)) {
                        SettingsContent(
                            settings = settings,
                            onUpdate = onUpdate,
                            onBack = onBack,
                            versionName = "1.0.0",
                            onOpenSource = onOpenSource,
                            onOpenLicenses = onOpenLicenses,
                            onOpenBatterySettings = onOpenBatterySettings,
                            onOpenAppearance = onOpenAppearance,
                            dataUiState = DataUiState(),
                            onExportCsv = onExportCsv,
                            onExportJson = onExportJson,
                            onCreateBackup = onCreateBackup,
                            onRestoreChosen = onRestoreChosen,
                        )
                    }
                }
            }
        }
    }

    private fun scrollTo(text: String) {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
    }

    @Test
    fun settings_group_and_row_icons_do_not_render_placeholder_letters() {
        show()
        rule.onAllNodesWithText("N", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText("L", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText("S", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText("A", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun every_setting_row_is_one_node_with_label_and_state() {
        show(
            settings = Settings(
                liveNotificationEnabled = true,
                notificationShowsSpeed = false,
                spikeAlertsEnabled = true,
                amoledBlack = false,
                themeId = "dusk",
            ),
        )

        val density = rule.density
        val switchRows = listOf(
            Triple("Live notification", "Today's usage in the notification shade", true),
            Triple("Show speed", "Add your current speed to the notification", false),
            Triple("Spike alerts", "Tell me when an app suddenly uses a lot of mobile data", true),
            Triple("AMOLED black", "Pure black backgrounds in dark theme", false),
        )

        for ((title, subtitle, expectedOn) in switchRows) {
            scrollTo(title)
            // Must be a single merged node containing both title and subtitle
            rule.onAllNodesWithText(title).assertCountEquals(1)
            val node = rule.onNodeWithText(title)
            node.assertIsDisplayed()
            node.assert(hasText(subtitle, substring = true))
            node.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            if (expectedOn) {
                node.assertIsOn()
            } else {
                node.assertIsOff()
            }
            val heightDp = with(density) { node.fetchSemanticsNode().size.height.toDp() }
            assertTrue("Row '$title' height ($heightDp) must be >= 56.dp", heightDp >= 55.9.dp)
        }

        // In the unmerged tree, there are exactly 4 Switch role nodes (the 4 SettingsSwitchRows, not duplicate inner switches)
        val unmergedSwitches = rule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        assertEquals(4, unmergedSwitches.size)

        // Appearance entry row is one merged node with label "Appearance" and theme value "Dusk"
        scrollTo("Appearance")
        val appearanceNode = rule.onNode(hasText("Appearance") and hasClickAction())
        appearanceNode.assertIsDisplayed()
        appearanceNode.assert(hasText("Dusk", substring = true))
        val appearanceHeightDp = with(density) { appearanceNode.fetchSemanticsNode().size.height.toDp() }
        assertTrue("Appearance row height ($appearanceHeightDp) must be >= 56.dp", appearanceHeightDp >= 55.9.dp)

        // Action rows in Data and backup are each one merged node with label and subtitle and >= 56.dp height
        val actionRows = listOf(
            "Export usage (CSV)" to "Detailed hourly usage for a spreadsheet",
            "Export everything (JSON)" to "Plans, budgets and usage as standard JSON",
            "Create backup" to "Save an encrypted copy of your data",
            "Restore backup" to "Replace data from an encrypted backup",
        )
        for ((title, subtitle) in actionRows) {
            scrollTo(title)
            val rowNode = rule.onNodeWithText(title)
            rowNode.assertIsDisplayed()
            rowNode.assert(hasText(subtitle, substring = true))
            val rowHeightDp = with(density) { rowNode.fetchSemanticsNode().size.height.toDp() }
            assertTrue("Action row '$title' height ($rowHeightDp) must be >= 56.dp", rowHeightDp >= 55.9.dp)
        }
    }

    @Test
    fun switch_rows_toggle_on_whole_row_click() {
        val updates = mutableListOf<(Settings) -> Settings>()
        show(
            settings = Settings(liveNotificationEnabled = true, spikeAlertsEnabled = true),
            onUpdate = { updates += it },
        )

        // Verify grouped heading exists
        rule.onNode(
            hasText("Notifications") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
        ).assertIsDisplayed()

        // Click on the far start (leading text/icon side) of "Live notification", not on the switch thumb
        val liveNode = rule.onNodeWithText("Live notification")
        liveNode.performTouchInput {
            click(Offset(x = 24f, y = centerY))
        }
        assertEquals(1, updates.size)
        assertFalse(updates[0](Settings(liveNotificationEnabled = true)).liveNotificationEnabled)

        // Click on the far start of "Spike alerts"
        scrollTo("Spike alerts")
        val spikeNode = rule.onNodeWithText("Spike alerts")
        spikeNode.performTouchInput {
            click(Offset(x = 24f, y = centerY))
        }
        assertEquals(2, updates.size)
        assertFalse(updates[1](Settings(spikeAlertsEnabled = true)).spikeAlertsEnabled)
    }

    @Test
    fun group_headings_are_headings_for_talkback() {
        show()
        val expectedHeadings = listOf(
            "Notifications",
            "Alerts",
            "Units",
            "Data and backup",
            "About",
        )
        for (headingText in expectedHeadings) {
            scrollTo(headingText)
            val node = rule.onNode(
                hasText(headingText) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
            )
            node.assertIsDisplayed()
        }
    }

    @Test
    fun appearance_row_shows_the_current_theme_name_and_navigates() {
        var navigated = 0
        show(
            settings = Settings(themeId = "catppuccin"),
            onOpenAppearance = { navigated++ },
        )
        scrollTo("Appearance")
        val appearanceRow = rule.onNode(hasText("Appearance") and hasClickAction())
        appearanceRow.assertIsDisplayed()
        appearanceRow.assert(hasText("Catppuccin", substring = true))
        appearanceRow.assert(hasText("12 themes", substring = true))
        appearanceRow.performClick()
        assertEquals(1, navigated)
    }

    @Test
    fun all_existing_actions_are_still_reachable() {
        val updates = mutableListOf<(Settings) -> Settings>()
        var exportedJson = false
        var exportedCsv: ExportRange? = null
        var batteryOpened = false
        var sourceOpened = false
        var licensesOpened = false

        show(
            settings = Settings(liveNotificationEnabled = true),
            onUpdate = { updates += it },
            onExportJson = { exportedJson = true },
            onExportCsv = { exportedCsv = it },
            onOpenBatterySettings = { batteryOpened = true },
            onOpenSource = { sourceOpened = true },
            onOpenLicenses = { licensesOpened = true },
        )

        // Notification toggles
        scrollTo("Live notification")
        rule.onNodeWithText("Live notification").assertIsDisplayed().performClick()
        assertFalse(updates.last()(Settings(liveNotificationEnabled = true)).liveNotificationEnabled)

        scrollTo("Show speed")
        rule.onNodeWithText("Show speed").assertIsDisplayed().performClick()
        assertFalse(updates.last()(Settings(notificationShowsSpeed = true)).notificationShowsSpeed)

        // Spike alerts toggle inside Alerts group
        scrollTo("Alerts")
        rule.onNodeWithText("Alerts").assertIsDisplayed()
        scrollTo("Spike alerts")
        rule.onNodeWithText("Spike alerts").assertIsDisplayed().performClick()
        assertFalse(updates.last()(Settings(spikeAlertsEnabled = true)).spikeAlertsEnabled)

        // Units connected button group
        scrollTo("Binary (GiB)")
        rule.onNodeWithText("Decimal (GB)").assertIsDisplayed()
        rule.onNodeWithText("Binary (GiB)").assertIsDisplayed().performClick()
        assertEquals(UnitSystem.BINARY, updates.last()(Settings()).unitSystem)

        // Data and backup group actions
        scrollTo("Data and backup")
        rule.onNodeWithText("Data and backup").assertIsDisplayed()

        // Export CSV -> opens range dialog
        scrollTo("Export usage (CSV)")
        rule.onNodeWithText("Export usage (CSV)").assertIsDisplayed().performClick()
        rule.onNodeWithText("Last 7 days").assertIsDisplayed().performClick()
        assertEquals(ExportRange.LAST_7_DAYS, exportedCsv)

        // Export JSON
        scrollTo("Export everything (JSON)")
        rule.onNodeWithText("Export everything (JSON)").assertIsDisplayed().performClick()
        assertTrue(exportedJson)

        // Create backup -> opens passphrase dialog
        scrollTo("Create backup")
        rule.onNodeWithText("Create backup").assertIsDisplayed().performClick()
        rule.onNodeWithText("Passphrase").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()

        // Restore backup -> opens warning dialog
        scrollTo("Restore backup")
        rule.onNodeWithText("Restore backup").assertIsDisplayed().performClick()
        rule.onNodeWithText("Continue").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()

        // Battery settings
        scrollTo("Battery settings")
        rule.onNodeWithText("Battery settings").assertIsDisplayed().performClick()
        assertTrue(batteryOpened)

        // About actions
        scrollTo("Source code")
        rule.onNodeWithText("Source code").assertIsDisplayed().performClick()
        assertTrue(sourceOpened)

        scrollTo("Open-source licences")
        rule.onNodeWithText("Open-source licences").assertIsDisplayed().performClick()
        assertTrue(licensesOpened)
    }

    @Test
    @Config(qualifiers = "w360dp-h2400dp")
    fun font_scale_2_no_clip() {
        show(fontScale = 2f)
        val density = Density(rule.density.density, fontScale = 2f)
        val maxWidthPx = with(density) { 360.dp.toPx() }

        val checkItems = listOf(
            "Settings",
            "Notifications",
            "Live notification",
            "Alerts",
            "Spike alerts",
            "Appearance",
            "Decimal (GB)",
            "Binary (GiB)",
            "Data and backup",
            "Export usage (CSV)",
            "Create backup",
            "Restore backup",
            "Battery settings",
            "Emberbyte 1.0.0",
            "Open-source licences",
        )

        for (label in checkItems) {
            scrollTo(label)
            val node = rule.onNodeWithText(label, substring = true).fetchSemanticsNode()
            val b = node.boundsInRoot
            assertTrue(
                "Node '$label' right (${b.right}) exceeds 360dp width ($maxWidthPx)",
                b.right <= maxWidthPx + 1f,
            )
            assertTrue(
                "Node '$label' left (${b.left}) is negative",
                b.left >= -1f,
            )
            assertTrue(
                "Node '$label' has non-positive size (${b.width} x ${b.height})",
                b.width > 0f && b.height > 0f,
            )
        }
    }

    @Test
    fun rtl() {
        show(layoutDirection = LayoutDirection.Rtl)

        // In RTL, the Back button is on the right side of the header
        val backBounds = rule.onNodeWithContentDescription("Back").fetchSemanticsNode().boundsInRoot
        val titleBounds = rule.onNodeWithText("Settings").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "In RTL, Back button (${backBounds.left}) should be to the right of Settings title (${titleBounds.left})",
            backBounds.left > titleBounds.left,
        )

        // In RTL, Units ConnectedButtonGroup mirrors so "Decimal (GB)" is to the right of "Binary (GiB)"
        scrollTo("Decimal (GB)")
        val checkIcons = rule.onAllNodesWithTag("connected_button_group_check_icon", useUnmergedTree = true).fetchSemanticsNodes()
        assertEquals(1, checkIcons.size)
        val decimalBounds = rule.onNodeWithText("Decimal (GB)").fetchSemanticsNode().boundsInRoot
        val binaryBounds = rule.onNodeWithText("Binary (GiB)").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "In RTL, first option 'Decimal (GB)' (${decimalBounds.left}) must be to the right of 'Binary (GiB)' (${binaryBounds.left})",
            decimalBounds.left > binaryBounds.left,
        )
    }
}
