package io.github.khaledbahaaeldin.emberbyte.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.common.weekdayBars
import io.github.khaledbahaaeldin.emberbyte.home.emberbyteAnimateItem
import io.github.khaledbahaaeldin.emberbyte.home.rememberRetainedWhen
import io.github.khaledbahaaeldin.emberbyte.data.BudgetRepository
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.SpikeRepository
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.util.DayClock
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppBudget
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
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
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.appicons.AppIconProvider
import io.github.khaledbahaaeldin.emberbyte.apps.detail.rankOf
import io.github.khaledbahaaeldin.emberbyte.apps.detail.planImpact
import io.github.khaledbahaaeldin.emberbyte.apps.detail.hourTotals
import io.github.khaledbahaaeldin.emberbyte.apps.detail.dailyBudgetProjection
import io.github.khaledbahaaeldin.emberbyte.apps.detail.PlanImpact
import io.github.khaledbahaaeldin.emberbyte.apps.detail.DetailRange
import io.github.khaledbahaaeldin.emberbyte.apps.detail.BudgetProjection
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.BudgetRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
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
    /** Local-time points of the chosen range (oldest first), one per day. */
    val dailySeries: List<UsagePoint> = emptyList(),
    val range: DetailRange = DetailRange.Week,
    /** Total of the equal-length range right before [range]; 0 when there is no data there. */
    val previousTotalBytes: Long = 0L,
    /** The app's place among all apps (0 when it is not in the list), how many apps there are, and its share. */
    val rank: Int = 0,
    val appCount: Int = 0,
    val shareOfAll: Float = 0f,
    /** Total per local hour of the day over the range (24 entries). */
    val hourTotals: LongArray = LongArray(24),
    val planMobileImpact: PlanImpact? = null,
    val budgetProjection: BudgetProjection? = null,
    val units: ByteUnits = ByteUnits.DECIMAL,
    val loaded: Boolean = false,
    val budget: BudgetUi? = null,
    val spikes: List<SpikeRowUi> = emptyList(),
    val planOptions: List<PlanOptionUi> = emptyList(),
    val editor: BudgetEditorUi? = null,
    val showBudgetRemovedUndo: Boolean = false,
) {
    val isLoading: Boolean get() = !loaded
}

internal fun buildAppDetailUiState(
    packageName: String,
    apps: List<AppUsage>,
    series: List<UsagePoint>,
    units: ByteUnits,
    zone: ZoneId,
    locale: Locale,
    range: DetailRange = DetailRange.Week,
    previousTotal: Long = 0L,
    hours: List<UsagePoint> = emptyList(),
): AppDetailUiState {
    val app = apps.firstOrNull { it.packageName == packageName }
    val rank = rankOf(packageName, apps)
    return AppDetailUiState(
        packageName = packageName,
        label = app?.label ?: packageName,
        totalBytes = app?.totalBytes ?: series.sumOf { it.totalBytes },
        mobileBytes = app?.mobileBytes ?: series.sumOf { it.mobileBytes },
        wifiBytes = app?.wifiBytes ?: series.sumOf { it.wifiBytes },
        dailySeries = series,
        range = range,
        previousTotalBytes = previousTotal,
        rank = rank.rank,
        appCount = rank.appCount,
        shareOfAll = rank.shareOfAll,
        hourTotals = hourTotals(hours, zone),
        units = units,
        loaded = true,
    )
}

private data class BudgetAndSpikesState(
    val statuses: List<BudgetStatus>,
    val spikes: List<SpikeEvent>,
    val plans: List<Plan>,
    val editor: BudgetEditorUi?,
    val showBudgetUndo: Boolean,
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
    private val _range = MutableStateFlow(DetailRange.Week)
    private val _showBudgetUndo = MutableStateFlow(false)
    @Volatile private var currentBudgetStatus: BudgetStatus? = null
    @Volatile private var lastRemovedBudget: AppBudget? = null
    @Volatile private var currentSettings: Settings = Settings()

    val uiState: StateFlow<AppDetailUiState> = combine(dates, _range) { date, detailRange -> date to detailRange }
        .flatMapLatest { (date, detailRange) ->
        val zone = clock.zone
        val days = detailRange.days
        val range = DateRange(
            date.minusDays((days - 1).toLong()).atStartOfDay(zone).toInstant(),
            date.plusDays(1).atStartOfDay(zone).toInstant(),
        )
        // One daily query for twice the range: the second half is the range, the first half the period before it.
        val doubled = DateRange(
            date.minusDays((2 * days - 1).toLong()).atStartOfDay(zone).toInstant(),
            date.plusDays(1).atStartOfDay(zone).toInstant(),
        )
        val usageAndSettings = combine(
            usage.observeApps(range),
            usage.observeAppSeries(packageName, doubled, Granularity.DAY),
            usage.observeAppSeries(packageName, range, Granularity.HOUR),
            settings.observe(),
        ) { apps, both, hours, s ->
            currentSettings = s
            val series = both.takeLast(days)
            val previous = both.dropLast(days).takeLast(days)
            Triple(apps, series to previous.sumOf { it.totalBytes }, hours to s)
        }
        val editorAndUndo = combine(_editor, _showBudgetUndo) { editorState, showUndo ->
            editorState to showUndo
        }
        val budgetAndSpikes = combine(
            budgets.observeStatuses(),
            spikes.observeSpikes(includeDismissed = false),
            plans.observePlans(includeArchived = false),
            editorAndUndo,
        ) { statuses, spikeList, planList, (editorState, showUndo) ->
            BudgetAndSpikesState(statuses, spikeList, planList, editorState, showUndo)
        }
        combine(usageAndSettings, budgetAndSpikes) { (apps, seriesAndPrevious, hoursAndSettings), bState ->
            val (series, previousTotal) = seriesAndPrevious
            val (hours, s) = hoursAndSettings
            val units = s.unitSystem.toByteUnits()
            val baseState = buildAppDetailUiState(
                packageName, apps, series, units, zone, locale,
                range = detailRange, previousTotal = previousTotal, hours = hours,
            )
            val matchedStatus = bState.statuses.firstOrNull { it.budget.packageName == packageName }
            currentBudgetStatus = matchedStatus
            val planName = matchedStatus?.budget?.planId?.let { pid -> bState.plans.firstOrNull { it.id == pid }?.name }
            val budgetUiModel = matchedStatus?.let { budgetUi(it, planName, units) }
            val appSpikes = bState.spikes.filter { it.packageName == packageName && !it.dismissed }
                .map { spikeRowUi(it, units, zone, locale, is24Hour()) }
            val planOptionList = bState.plans.map { PlanOptionUi(it.id, it.name) }
            val mobileImpact = planImpact(baseState.mobileBytes, days, bState.plans.firstOrNull())
            val projection = matchedStatus?.takeIf { it.budget.period == BudgetPeriod.DAILY }?.let {
                val now = clock.instant().atZone(zone)
                dailyBudgetProjection(it.usedBytes, it.budget.limitBytes, now.hour * 60 + now.minute)
            }
            baseState.copy(
                planMobileImpact = mobileImpact,
                budgetProjection = projection,
                budget = budgetUiModel,
                spikes = appSpikes,
                planOptions = planOptionList,
                editor = bState.editor,
                showBudgetRemovedUndo = bState.showBudgetUndo,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppDetailUiState(packageName))

    fun onEvent(event: AppDetailEvent) {
        when (event) {
            is AppDetailEvent.SetRange -> _range.value = event.range
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
                lastRemovedBudget = current.copy(id = 0L)
                _showBudgetUndo.value = true
                viewModelScope.launch {
                    budgets.delete(current.id)
                }
            }
            AppDetailEvent.UndoRemoveBudget -> {
                val restored = lastRemovedBudget ?: return
                lastRemovedBudget = null
                _showBudgetUndo.value = false
                viewModelScope.launch {
                    budgets.upsert(restored)
                }
            }
            AppDetailEvent.DismissBudgetUndo -> {
                _showBudgetUndo.value = false
            }
            is AppDetailEvent.DismissSpike -> {
                viewModelScope.launch {
                    spikes.dismiss(event.id)
                }
            }
        }
    }
}

/** The App detail screen content; the redesigned sections live in the `detail` package. */
@Composable
fun AppDetailContent(
    state: AppDetailUiState,
    onEvent: (AppDetailEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null,
    icon: ImageBitmap? = null,
) = io.github.khaledbahaaeldin.emberbyte.apps.detail.AppDetailContent(
    state = state,
    onEvent = onEvent,
    onBack = onBack,
    modifier = modifier,
    onOpenSettings = onOpenSettings,
    icon = icon,
)

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

@Composable
internal fun NetworkLegendItem(
    label: String,
    dotColor: Color,
    textColor: Color,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    textModifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape),
        )
        ForceLtr {
            Text(
                text = label,
                style = textStyle,
                color = textColor,
                modifier = textModifier,
            )
        }
    }
}
