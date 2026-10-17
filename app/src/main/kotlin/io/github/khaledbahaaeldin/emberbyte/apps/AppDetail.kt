package io.github.khaledbahaaeldin.emberbyte.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.common.ScreenHeader
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.common.weekdayBars
import io.github.khaledbahaaeldin.emberbyte.data.BudgetRepository
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.SpikeRepository
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.util.DayClock
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppBudget
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.BudgetPeriod
import io.github.khaledbahaaeldin.emberbyte.engine.model.BudgetStatus
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.SpikeEvent
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.bytesOf
import io.github.khaledbahaaeldin.emberbyte.plans.textAndUnitOf
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.appicons.AppIconProvider
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.BudgetRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UsageBarRow
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppDetailUiState(
    val packageName: String,
    val label: String = "",
    val totalBytes: Long = 0L,
    val mobileBytes: Long = 0L,
    val wifiBytes: Long = 0L,
    val foregroundBytes: Long = 0L,
    val backgroundBytes: Long = 0L,
    val bars: List<BarUi> = emptyList(),
    val units: ByteUnits = ByteUnits.DECIMAL,
    val loaded: Boolean = false,
    val budget: BudgetUi? = null,
    val spikes: List<SpikeRowUi> = emptyList(),
    val planOptions: List<PlanOptionUi> = emptyList(),
    val editor: BudgetEditorUi? = null,
)

internal fun buildAppDetailUiState(
    packageName: String,
    apps: List<AppUsage>,
    series: List<UsagePoint>,
    units: ByteUnits,
    zone: ZoneId,
    locale: Locale,
): AppDetailUiState {
    val app = apps.firstOrNull { it.packageName == packageName }
    return AppDetailUiState(
        packageName = packageName,
        label = app?.label ?: packageName,
        totalBytes = app?.totalBytes ?: series.sumOf { it.totalBytes },
        mobileBytes = app?.mobileBytes ?: series.sumOf { it.mobileBytes },
        wifiBytes = app?.wifiBytes ?: series.sumOf { it.wifiBytes },
        bars = weekdayBars(series, units, zone, locale),
        units = units,
        loaded = true,
    )
}

private data class BudgetAndSpikesState(
    val statuses: List<BudgetStatus>,
    val spikes: List<SpikeEvent>,
    val plans: List<Plan>,
    val editor: BudgetEditorUi?,
)

/** @param dates tests MUST pass a finite flow. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailViewModel(
    private val packageName: String,
    usage: UsageRepository,
    settings: SettingsRepository,
    private val budgets: BudgetRepository,
    private val spikes: SpikeRepository,
    private val plans: PlanRepository,
    private val is24Hour: () -> Boolean = { true },
    private val clock: Clock = SystemZoneClock(),
    private val locale: Locale = Locale.getDefault(),
    dates: Flow<LocalDate> = DayClock(clock).dates(),
) : ViewModel() {
    private val _editor = MutableStateFlow<BudgetEditorUi?>(null)
    @Volatile private var currentBudgetStatus: BudgetStatus? = null
    @Volatile private var currentSettings: Settings = Settings()

    val uiState: StateFlow<AppDetailUiState> = dates.flatMapLatest { date ->
        val zone = clock.zone
        val range = DateRange(
            date.minusDays(6).atStartOfDay(zone).toInstant(),
            date.plusDays(1).atStartOfDay(zone).toInstant(),
        )
        val usageAndSettings = combine(
            usage.observeApps(range),
            usage.observeAppSeries(packageName, range, Granularity.DAY),
            settings.observe(),
        ) { apps, series, s ->
            currentSettings = s
            Triple(apps, series, s)
        }
        val budgetAndSpikes = combine(
            budgets.observeStatuses(),
            spikes.observeSpikes(includeDismissed = false),
            plans.observePlans(includeArchived = false),
            _editor,
        ) { statuses, spikeList, planList, editorState ->
            BudgetAndSpikesState(statuses, spikeList, planList, editorState)
        }
        combine(usageAndSettings, budgetAndSpikes) { (apps, series, s), bState ->
            val units = s.unitSystem.toByteUnits()
            val baseState = buildAppDetailUiState(packageName, apps, series, units, zone, locale)
            val matchedStatus = bState.statuses.firstOrNull { it.budget.packageName == packageName }
            currentBudgetStatus = matchedStatus
            val planName = matchedStatus?.budget?.planId?.let { pid -> bState.plans.firstOrNull { it.id == pid }?.name }
            val budgetUiModel = matchedStatus?.let { budgetUi(it, planName, units) }
            val appSpikes = bState.spikes.filter { it.packageName == packageName && !it.dismissed }
                .map { spikeRowUi(it, units, zone, locale, is24Hour()) }
            val planOptionList = bState.plans.map { PlanOptionUi(it.id, it.name) }
            baseState.copy(
                budget = budgetUiModel,
                spikes = appSpikes,
                planOptions = planOptionList,
                editor = bState.editor,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppDetailUiState(packageName))

    fun onEvent(event: AppDetailEvent) {
        when (event) {
            AppDetailEvent.OpenBudgetEditor -> {
                val current = currentBudgetStatus?.budget
                if (current != null) {
                    val (limitText, unit) = textAndUnitOf(current.limitBytes, currentSettings.unitSystem)
                    _editor.value = BudgetEditorUi(
                        limitText = limitText,
                        unit = unit,
                        period = current.period,
                        planId = current.planId,
                        warnAtPercent = current.warnAtPercent,
                        errors = emptyMap(),
                        saving = false,
                        isEdit = true,
                    )
                } else {
                    _editor.value = BudgetEditorUi(
                        limitText = "",
                        unit = CapUnit.GB,
                        period = BudgetPeriod.DAILY,
                        planId = null,
                        warnAtPercent = 80,
                        errors = emptyMap(),
                        saving = false,
                        isEdit = false,
                    )
                }
            }
            AppDetailEvent.CloseBudgetEditor -> {
                _editor.value = null
            }
            is AppDetailEvent.SetBudgetLimit -> {
                _editor.value = _editor.value?.copy(limitText = event.text, errors = emptyMap())
            }
            is AppDetailEvent.SetBudgetUnit -> {
                _editor.value = _editor.value?.copy(unit = event.unit, errors = emptyMap())
            }
            is AppDetailEvent.SetBudgetPeriod -> {
                _editor.value = _editor.value?.copy(period = event.period, errors = emptyMap())
            }
            is AppDetailEvent.SetBudgetPlan -> {
                _editor.value = _editor.value?.copy(planId = event.planId, errors = emptyMap())
            }
            is AppDetailEvent.SetBudgetWarn -> {
                _editor.value = _editor.value?.copy(warnAtPercent = event.percent, errors = emptyMap())
            }
            AppDetailEvent.SaveBudget -> {
                val ed = _editor.value ?: return
                val bytes = bytesOf(ed.limitText, ed.unit, currentSettings.unitSystem)
                if (bytes == null) {
                    _editor.value = ed.copy(errors = mapOf("limitBytes" to "Enter a number greater than zero"))
                    return
                }
                viewModelScope.launch {
                    _editor.value = ed.copy(saving = true)
                    val targetBudget = AppBudget(
                        id = currentBudgetStatus?.budget?.id ?: 0L,
                        packageName = packageName,
                        limitBytes = bytes,
                        period = ed.period,
                        planId = ed.planId.takeIf { ed.period == BudgetPeriod.CYCLE },
                        warnAtPercent = ed.warnAtPercent,
                        enabled = true,
                    )
                    when (val result = budgets.upsert(targetBudget)) {
                        is Outcome.Success -> {
                            _editor.value = null
                        }
                        is Outcome.Failure -> {
                            when (val error = result.error) {
                                is EmberbyteError.Invalid -> {
                                    _editor.value = ed.copy(saving = false, errors = mapOf(error.field to error.reason))
                                }
                                EmberbyteError.NotFound -> {
                                    _editor.value = ed.copy(saving = false, errors = mapOf("planId" to "That plan no longer exists"))
                                }
                                is EmberbyteError.Storage -> {
                                    _editor.value = ed.copy(saving = false, errors = mapOf("form" to error.cause))
                                }
                                else -> {
                                    _editor.value = ed.copy(saving = false, errors = mapOf("form" to "Could not save budget"))
                                }
                            }
                        }
                    }
                }
            }
            AppDetailEvent.RemoveBudget -> {
                val current = currentBudgetStatus?.budget ?: return
                viewModelScope.launch {
                    budgets.delete(current.id)
                }
            }
            is AppDetailEvent.DismissSpike -> {
                viewModelScope.launch {
                    spikes.dismiss(event.id)
                }
            }
        }
    }
}

@Composable
fun AppDetailContent(
    state: AppDetailUiState,
    onEvent: (AppDetailEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null,
    icon: ImageBitmap? = null,
) {
    val palette = LocalEmberbytePalette.current
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with Back button, AppIcon, Title & Package
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.text,
                    )
                }
                AppIcon(
                    label = state.label.ifEmpty { state.packageName },
                    icon = icon,
                    shape = shapeFor(state.packageName),
                    size = 56.dp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.label.ifEmpty { state.packageName },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (state.label.isNotEmpty() && state.label != state.packageName) {
                        Text(
                            text = state.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // Hero Number section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Last 7 days",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.muted,
                )
                val totalFmt = formatBytes(state.totalBytes, state.units)
                HeroNumber(
                    value = totalFmt.value,
                    unit = totalFmt.unit,
                    contentDescription = "${spokenBytes(state.totalBytes, state.units)} used in the last 7 days",
                    size = 64.sp,
                    color = palette.primary,
                )
            }
        }

        // Network split bar & legend
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NetworkBar(
                    mobileBytes = state.mobileBytes,
                    wifiBytes = state.wifiBytes,
                    mobileColor = palette.warn,
                    wifiColor = palette.primary,
                    units = state.units,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val mobileFmt = formatBytes(state.mobileBytes, state.units)
                    val wifiFmt = formatBytes(state.wifiBytes, state.units)
                    ForceLtr {
                        Text(
                            text = "• Mobile ${mobileFmt.value} ${mobileFmt.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.warn,
                        )
                    }
                    ForceLtr {
                        Text(
                            text = "• Wi-Fi ${wifiFmt.value} ${wifiFmt.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.primary,
                        )
                    }
                }
            }
        }

        // Foreground vs Background split as two BentoTiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val fgFmt = formatBytes(state.foregroundBytes, state.units)
                val bgFmt = formatBytes(state.backgroundBytes, state.units)
                BentoTile(
                    title = "Foreground",
                    modifier = Modifier.weight(1f),
                    container = TileContainer.Standard,
                ) {
                    ForceLtr {
                        Text(
                            text = "${fgFmt.value} ${fgFmt.unit}",
                            style = EmberbyteType.value(20.sp),
                            color = palette.text,
                        )
                    }
                }
                BentoTile(
                    title = "Background",
                    modifier = Modifier.weight(1f),
                    container = TileContainer.Standard,
                ) {
                    ForceLtr {
                        Text(
                            text = "${bgFmt.value} ${bgFmt.unit}",
                            style = EmberbyteType.value(20.sp),
                            color = palette.text,
                        )
                    }
                }
            }
        }

        // Daily chart (bars use primary, today's bar uses accent)
        item {
            val weekSummary = run {
                val highest = state.bars.maxByOrNull { it.bytes }
                if (highest != null) "Daily usage, last 7 days, highest ${highest.description}"
                else "Daily usage, last 7 days"
            }
            BentoTile(
                title = "Daily usage",
                modifier = Modifier.fillMaxWidth(),
                container = TileContainer.Standard,
            ) {
                UsageBarRow(
                    points = state.bars,
                    selectedIndex = selectedBarIndex,
                    onSelect = { selectedBarIndex = if (selectedBarIndex == it) null else it },
                    summaryDescription = weekSummary,
                    barColor = { index, _ ->
                        if (index == state.bars.lastIndex) palette.accent
                        else palette.primary
                    },
                )
            }
        }

        // Spikes if present
        if (state.spikes.isNotEmpty()) {
            item {
                BentoTile(title = "Spikes", modifier = Modifier.fillMaxWidth(), container = TileContainer.Warn) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.spikes.forEach { spike ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    ForceLtr {
                                        Text(spike.whenText, style = MaterialTheme.typography.bodyMedium, color = palette.text)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ForceLtr {
                                            Text(
                                                spike.bytesText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.muted,
                                            )
                                        }
                                        Text(
                                            "·",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.muted,
                                        )
                                        ForceLtr {
                                            Text(
                                                spike.multipleText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.muted,
                                            )
                                        }
                                    }
                                }
                                TextButton(
                                    onClick = { onEvent(AppDetailEvent.DismissSpike(spike.id)) },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                                ) {
                                    Text("Dismiss", color = palette.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Data budget
        item {
            BentoTile(title = "Data budget", modifier = Modifier.fillMaxWidth()) {
                val b = state.budget
                if (b == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "No budget set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted,
                        )
                        TextButton(
                            onClick = { onEvent(AppDetailEvent.OpenBudgetEditor) },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Text("Set a budget", color = palette.primary)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BudgetRing(
                            fraction = b.fraction,
                            state = b.ringState,
                            percentText = b.percentText,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            ForceLtr {
                                Text(b.usedText, style = MaterialTheme.typography.titleMedium, color = palette.text)
                            }
                            ForceLtr {
                                Text(
                                    b.periodText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.muted,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { onEvent(AppDetailEvent.OpenBudgetEditor) },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                                ) {
                                    Text("Edit", color = palette.primary)
                                }
                                TextButton(
                                    onClick = { onEvent(AppDetailEvent.RemoveBudget) },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                                ) {
                                    Text("Remove", color = palette.danger)
                                }
                            }
                        }
                    }
                }
            }
        }

        // App settings
        if (!state.packageName.startsWith("uid:") && onOpenSettings != null) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    TextButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Text("App settings", color = palette.primary)
                    }
                }
            }
        }
    }

    if (state.editor != null) {
        BudgetEditorDialog(
            editor = state.editor,
            planOptions = state.planOptions,
            onEvent = onEvent,
        )
    }
}

@Composable
fun AppDetailScreen(
    viewModel: AppDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    iconProvider: AppIconProvider? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, state.packageName, iconProvider) {
        value = iconProvider?.icon(state.packageName)
    }
    AppDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenSettings = { openAppSettings(context, state.packageName) },
        modifier = modifier,
        icon = icon,
    )
}

private fun openAppSettings(context: Context, packageName: String) {
    val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No settings screen to open; nothing to do.
    }
}
