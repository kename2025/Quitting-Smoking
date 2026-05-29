package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HabitTrigger
import com.example.data.QuitSmokingConfig
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuitSmokingDashboard(
    viewModel: QuitSmokingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.configState.collectAsStateWithLifecycle()
    val triggers by viewModel.triggersState.collectAsStateWithLifecycle()
    val cravingLogs by viewModel.cravingsState.collectAsStateWithLifecycle()
    val breathingState by viewModel.breathingState.collectAsStateWithLifecycle()

    val isEng = config?.useEnglish == true
    fun t(zh: String, en: String): String = if (isEng) en else zh

    // Real-time ticking time state
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(config?.quitDateTimestamp) {
        if (config?.quitDateTimestamp != null) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    // Active Tab state: 0 = deconstruct, 1 = scripts, 2 = MVS 7-step, 3 = milestones
    var activeTab by remember { mutableStateOf(0) }

    // Dialog control states
    var showCustomScriptDialog by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showCommitDialog by remember { mutableStateOf(false) }
    var showDeconstructDetail by remember { mutableStateOf<DeconstructLayer?>(null) }
    var showWeChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand Section
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t("第一性原理戒烟", "First-Principles Quit"),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "DECONSTRUCT ADDICTION, REGAIN FREEDOM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextDarkSec,
                                letterSpacing = 1.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Toggle Button
                        Button(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = OxygenGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp).testTag("lang_toggle_button")
                        ) {
                            Text(
                                text = if (isEng) "中文" else "EN",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OxygenGreen,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // WeChat Contact Button
                        Button(
                            onClick = { showWeChatDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = HealingCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp).testTag("wechat_contact_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "WeChat Contact",
                                modifier = Modifier.size(14.dp),
                                tint = HealingCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t("联系作者", "Contact"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HealingCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // S.O.S Breathing Button
                        Button(
                            onClick = { viewModel.startBreathingExercise() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp).testTag("sos_breath_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "S.O.S Breathing",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t("S.O.S 吸氧", "S.O.S Breath"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Plan Card / Stat Board
                val hasActivePlan = config?.quitDateTimestamp != null
                if (!hasActivePlan) {
                    // Empty Setup State
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BorderSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Plan",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = t("你的神经回路复原计划尚未开启", "Your Neural Recovery Plan is Not Yet Active"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = t(
                                    "我们已经习惯把戒烟归因为「意志力不足」——本质上，这是由于大脑奖赏通路被尼古丁重配置了。第一性原理戒烟用物理和心理双重视角，帮助你科学、无痛且高效地回归本真无烟状态。",
                                    "We are accustomed to blaming failure on \"lack of willpower.\" In reality, nicotine has re-wired your brain's reward networks. First-Principles Quit Smoking leverages clinical & cognitive science to help you return painlessly and scientifically to your authentic, smoke-free self."
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { showSetupDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("init_plan_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = t("开启第一性原理戒断计划", "Launch First-Principles System"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                } else {
                    // Stats / Real-time Tracker Card
                    val localConfig = config!!
                    val diff = currentTime - localConfig.quitDateTimestamp!!

                    val elapsedDays = diff / (24 * 60 * 60 * 1000)
                    val elapsedHours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
                    val elapsedMinutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
                    val elapsedSeconds = (diff % (60 * 1000)) / 1000

                    // Calculations
                    val msPerDay = 24.0 * 60.0 * 60.0 * 1000.0
                    val daysDecimal = diff.toDouble() / msPerDay
                    val cigarettesAvoided = (daysDecimal * localConfig.cigarettesPerDay).toInt()
                    val totalPacks = (cigarettesAvoided.toDouble() / 20.0)
                    val moneySaved = totalPacks * localConfig.pricePerPack
                    // Life oxygen regained (1 cigarette costs 11 minutes of life normally)
                    val lifeMinutesSaved = (cigarettesAvoided * 11)
                    val hoursRegained = lifeMinutesSaved / 60
                    val remMinutesRegained = lifeMinutesSaved % 60

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BorderSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(OxygenGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = t("神经系统重置中", "Neural Re-wiring in Progress"),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = OxygenGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                TextButton(
                                    onClick = { showSetupDialog = true },
                                    modifier = Modifier.testTag("edit_settings_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = t("修改参数", "Settings"),
                                        modifier = Modifier.size(16.dp),
                                        tint = TextGray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(t("设置", "Settings"), style = MaterialTheme.typography.bodySmall.copy(color = TextGray))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Counter Display
                            Text(
                                text = t("已持续清爽呼吸", "Clean air breathed for"),
                                style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "%02d".format(elapsedDays),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 42.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = t("天 ", "d "),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextGray,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
                                )

                                Text(
                                    text = "%02d".format(elapsedHours),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 32.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = t("时 ", "h "),
                                    style = MaterialTheme.typography.titleSmall.copy(color = TextGray),
                                    modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
                                )

                                Text(
                                    text = "%02d".format(elapsedMinutes),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 32.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = t("分 ", "m "),
                                    style = MaterialTheme.typography.titleSmall.copy(color = TextGray),
                                    modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
                                )

                                Text(
                                    text = "%02d".format(elapsedSeconds),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 22.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = t("秒 ", "s "),
                                    style = MaterialTheme.typography.titleSmall.copy(color = TextGray),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = BorderSlate, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(18.dp))

                            // Stats Grid: Money Saved, Cigarettes Saved, Life Regained
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = t("减少吸入毒素", "Toxic intake avoided"),
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$cigarettesAvoided " + t("支", "cigs"),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = t("节省金钱支出", "Money Saved"),
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${localConfig.currencySymbol} %.2f".format(moneySaved),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = HealingCyan
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = t("挽回生命时间", "Life span regained"),
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (hoursRegained > 0) "${hoursRegained}${t("时", "h")}${remMinutesRegained}${t("分", "m")}" else "${remMinutesRegained}${t("分钟", "m")}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        ),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pillar Selection Tabs (Premium Pill Design)
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {},
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabItems = listOf(
                        t("原理解构", "Deconstruct"),
                        t("习惯重组", "Habit Loops"),
                        t("最小系统", "MVS 7-Step"),
                        t("生理里程", "Milestones")
                    )
                    tabItems.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(
                                    if (activeTab == index) MaterialTheme.colorScheme.primaryContainer
                                    else CardSlate
                                )
                                .border(
                                    1.dp,
                                    if (activeTab == index) MaterialTheme.colorScheme.primary else BorderSlate,
                                    RoundedCornerShape(30.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTab == index) MaterialTheme.colorScheme.primary else TextWhite
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Section Render
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                    },
                    label = "SectionAnimated"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> DeconstructPillar(isEng = isEng, onLayerClick = { showDeconstructDetail = it })
                        1 -> HabitRewritingPillar(
                            isEng = isEng,
                            triggers = triggers,
                            onExecute = { viewModel.executeTriggerAction(it) },
                            onDelete = { viewModel.removeTrigger(it) },
                            onAddClick = { showCustomScriptDialog = true }
                        )
                        2 -> MvsChecklistPillar(
                            isEng = isEng,
                            config = config,
                            triggers = triggers,
                            onSetDateClick = { showSetupDialog = true },
                            onCommitClick = { showCommitDialog = true },
                            onToggleStep = { step, checked -> viewModel.updateChecklistStep(step, checked) }
                        )
                        3 -> ClinicalMilestonesPillar(
                            isEng = isEng,
                            quitTimestamp = config?.quitDateTimestamp,
                            cravingLogs = cravingLogs
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // BREATHING COACH OVERLAY
            if (breathingState is BreathingState.Ongoing) {
                val state = breathingState as BreathingState.Ongoing
                Dialog(onDismissRequest = { /* Force completing or close warning */ }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CarbonDarkBg),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        BreathingCoachPanel(
                            ongoingState = state,
                            onNextPhase = { phase, cycle, rem ->
                                viewModel.updateBreathingPhase(phase, cycle, rem)
                            },
                            onComplete = { viewModel.completeBreathingExercise() },
                            onCancel = { viewModel.resetBreathingState() }
                        )
                    }
                }
            } else if (breathingState is BreathingState.Completed) {
                Dialog(onDismissRequest = { viewModel.resetBreathingState() }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(OxygenGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = CarbonDarkBg,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = t("此轮渴望阻断成功", "Synapse Restored Successfully"),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OxygenGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = t(
                                    "第一性原理指出：「吸烟能缓解焦虑」是一个因果归因错误。深呼吸 and 注意力暂停能够触发同样的副交感神经反射释放，提供真实的物理放松。你刚刚完成了一次神经突触重构实验，干得漂亮！",
                                    "First-Principles analysis states that \"smoking relieves anxiety\" is a causal attribution error. Deep breathing & sensory pause trigger the exact same parasympathetic reflex, providing real physiological relaxation. You have successfully completed a synaptic re-wiring experiment. Well done!"
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.resetBreathingState() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(t("继续保持清爽", "Continue Staying Fresh"))
                            }
                        }
                    }
                }
            }

            // DIALOGS
            if (showWeChatDialog) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val contextLocal = LocalContext.current
                Dialog(onDismissRequest = { showWeChatDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "WeChat Contact",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = t("联系作者", "Contact Author"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OxygenGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = t(
                                    "你好！我是本应用的独立作者。如果你有 APP开发、程序开发、系统定制业务需求，或任何反馈，欢迎随时加我微信咨询交流！",
                                    "Hello! I am the independent creator of this app. If you have any APP development, custom software building, or system crafting needs, feel free to add me on WeChat!"
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // WeChat ID block
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSlateElevated),
                                border = BorderStroke(1.dp, BorderSlate),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = t("微信账号", "WeChat ID"),
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                                        )
                                        Text(
                                            text = "kename",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = HealingCyan,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append("kename") })
                                            val toastText = t("微信号已复制到剪贴板", "WeChat ID copied to clipboard")
                                            android.widget.Toast.makeText(contextLocal, toastText, android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(t("复制", "Copy"), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Business information
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardSlateElevated),
                                border = BorderStroke(1.dp, BorderSlate),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = t("主要业务承接", "Services We Offer"),
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextGray)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = t(
                                            "• 品牌 APP 客户端开发\n• 前后端各类型软件/程序开发定制\n• 微信小程序及公众号搭建开发",
                                            "• Premium Native/Hybrid App Development\n• Custom Software & Technical Architectures\n• Seamless Fullstack Systems & Web Apps"
                                        ),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhite,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { showWeChatDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlateElevated),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, BorderSlate),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(t("确认", "Close"), color = TextWhite)
                            }
                        }
                    }
                }
            }

            // DIALOGS
            if (showSetupDialog) {
                val currentConfig = config ?: QuitSmokingConfig()
                var cigarettesStr by remember { mutableStateOf(currentConfig.cigarettesPerDay.toString()) }
                var priceStr by remember { mutableStateOf(currentConfig.pricePerPack.toString()) }
                var currencySymbol by remember { mutableStateOf(currentConfig.currencySymbol) }

                Dialog(onDismissRequest = { showSetupDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = if (config?.quitDateTimestamp == null) t("设定复原计划", "Setup Recovery Plan") else t("参数校正与重组", "Calibrate Parameters"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OxygenGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Date Setup
                            Text(
                                text = t("何时开始停止将大脑租给毒品？", "When will you stop leasing your brain to nicotine?"),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            var chosenTimestamp by remember { mutableStateOf<Long?>(currentConfig.quitDateTimestamp ?: System.currentTimeMillis()) }

                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply {
                                        chosenTimestamp?.let { timeInMillis = it }
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            calendar.set(Calendar.YEAR, year)
                                            calendar.set(Calendar.MONTH, month)
                                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            chosenTimestamp = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlateElevated),
                                border = BorderStroke(1.dp, BorderSlate),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home, // placeholder
                                    contentDescription = "Date",
                                    tint = OxygenGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = chosenTimestamp?.let { sdf.format(Date(it)) } ?: t("选择精确戒断日期", "Select exact stop date/time"),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cigarettes Pre Day
                            Text(
                                text = t("原日吸烟量 (支)", "Daily Smoking (cigarettes)"),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = cigarettesStr,
                                onValueChange = { cigarettesStr = it.filter { c -> c.isDigit() } },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cigarettes_input"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Cost Pre Pack
                            Text(
                                text = t("平均每包烟的价格 (20支)", "Average price per pack (20 cigs)"),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = currencySymbol,
                                    onValueChange = { currencySymbol = it.take(2) },
                                    modifier = Modifier
                                        .width(60.dp)
                                        .padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = priceStr,
                                    onValueChange = { priceStr = it.filter { c -> c.isDigit() || c == '.' } },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("price_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showSetupDialog = false }) {
                                    Text(t("取消", "Cancel"), color = TextGray)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        val cigs = cigarettesStr.toIntOrNull() ?: 15
                                        val price = priceStr.toDoubleOrNull() ?: 25.0
                                        viewModel.setQuitDate(chosenTimestamp)
                                        viewModel.updateSavingsConfig(cigs, price, currencySymbol)
                                        showSetupDialog = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("confirm_settings_btn")
                                ) {
                                    Text(t("确认启动", "Confirm Launch"))
                                }
                            }
                        }
                    }
                }
            }

            if (showCommitDialog) {
                var p1 by remember { mutableStateOf(config?.notifiedPerson1 ?: "") }
                var p2 by remember { mutableStateOf(config?.notifiedPerson2 ?: "") }
                var p3 by remember { mutableStateOf(config?.notifiedPerson3 ?: "") }

                Dialog(onDismissRequest = { showCommitDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = t("搭建社会承诺（契约网络）", "Build Social Commitments"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OxygenGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = t(
                                    "第一性原理第五步：通知至少三个人。社会监督是一条坚固的外部隐形约束，能显著提高前72小时突破率。",
                                    "Step 5: Notify at least 3 supporters. Social accountability creates an invisible external boundary that significantly boosts your success rule during the first crucial 72 hours."
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = p1,
                                onValueChange = { p1 = it },
                                placeholder = { Text(t("支持者1 名称或留言", "Supporter 1: Name or Note"), color = TextDarkSec) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_p1"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = p2,
                                onValueChange = { p2 = it },
                                placeholder = { Text(t("支持者2 名称或留言", "Supporter 2: Name or Note"), color = TextDarkSec) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_p2"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = p3,
                                onValueChange = { p3 = it },
                                placeholder = { Text(t("支持者3 名称或留言", "Supporter 3: Name or Note"), color = TextDarkSec) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_p3"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showCommitDialog = false }) {
                                    Text(t("取消", "Cancel"), color = TextGray)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.setPublicCommitments(p1, p2, p3)
                                        showCommitDialog = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("confirm_commitment_btn")
                                ) {
                                    Text(t("生成公开誓词", "Generate Oath"))
                                }
                            }
                        }
                    }
                }
            }

            if (showCustomScriptDialog) {
                var cue by remember { mutableStateOf("") }
                var behavior by remember { mutableStateOf("") }

                Dialog(onDismissRequest = { showCustomScriptDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = t("编写“如果X，我就Y”防守脚本", "Write an IF/THEN Counter-Script"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OxygenGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = t("如果（设定特定触发情境/渴望线索）：", "IF (specific trigger cue or temptation window):"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextGray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = cue,
                                onValueChange = { cue = it },
                                placeholder = { Text(t("例如：饭后和同事聚在茶水间...", "e.g., hanging out with colleagues post-meal..."), color = TextDarkSec) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("script_cue_input"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = t("我就（立刻执行无需思考的物理替代行为）：", "THEN (immediate physical replacement actions):"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextGray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = behavior,
                                onValueChange = { behavior = it },
                                placeholder = { Text(t("例如：立刻喝一杯冰柠檬水，大口吞咽...", "e.g., swallow cold lemon water block by block..."), color = TextDarkSec) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("script_behavior_input"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showCustomScriptDialog = false }) {
                                    Text(t("取消", "Cancel"), color = TextGray)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.addNewTrigger(cue, behavior)
                                        showCustomScriptDialog = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("confirm_add_script_btn")
                                ) {
                                    Text(t("植入反射回路", "Inject Reflex Loop"))
                                }
                            }
                        }
                    }
                }
            }

            if (showDeconstructDetail != null) {
                val layer = showDeconstructDetail!!
                Dialog(onDismissRequest = { showDeconstructDetail = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = layer.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(CardSlateElevated, CircleShape)
                                        .clickable { showDeconstructDetail = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = TextWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = t("生理机制深层剖析：", "Deep Physiological Mechanisms:"),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = layer.mechanismDetail,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite),
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = t("第一性原理解法：", "First-Principles Solutions:"),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = layer.solutionDetail,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = HealingCyan,
                                    fontWeight = FontWeight.Bold
                                ),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// PILLAR 1: MECHANISM DECONSTRUCT
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeconstructPillar(isEng: Boolean, onLayerClick: (DeconstructLayer) -> Unit) {
    val layers = listOf(
        DeconstructLayer(
            title = if (isEng) "🧬 Biological: Receptor Hijacking" else "🧬 生理层: 受体劫持",
            summary = if (isEng) "Nicotine hijacks acetylcholine receptors. Smoking fills a fake chemical void, rather than bringing genuine joy." else "尼古丁与大脑乙酰胆碱受体结合激发多巴胺，吸烟并非创造真正快感，而是填补化学虚空。",
            mechanismDetail = if (isEng) "Within 7 seconds, inhaled nicotine crosses the blood-brain barrier and binds to acetylcholine receptors, forcing your brain to dump large amounts of dopamine. However, this quick relief fades rapidly, driving continuous 'hunger signals' that hijack your baseline biology." else "当身体吸入第一口尼古丁，仅需7秒它就能穿越血脑屏障并连接乙酰胆碱受体。这会欺骗神经元释放大量非正常额度的多巴胺。然而这种快感是极度短暂的，它消退极快，迫使大脑释放“饥饿信号”再次渴求尼古丁，由此完成奖励机制的物理劫持。",
            solutionDetail = if (isEng) "Realize that 'enjoying smoking' is merely the relief of withdrawal symptoms. By stopping intake for 72 hours, receptors return to baseline health, ending this artificial chemical cycle." else "理解所谓的“吸烟很享受”只是戒断反应被消除的错觉。你需要切断机械式吸烟动作，让身体在72小时内断绝摄入，清退多巴胺伪电刺激，乙酰胆碱受体便会自降活性，回归自然健康基准。"
        ),
        DeconstructLayer(
            title = if (isEng) "📈 Adaptation: Neural Desensitization" else "📈 适应层: 神经元去敏感化",
            summary = if (isEng) "Due to high intake, receptor sensitivity is actively down-regulated. Brain falls into restlessness when idle." else "由于摄入增加，受体敏感度被动下调，大脑在不抽烟时陷入了失衡性的烦躁与空虚。",
            mechanismDetail = if (isEng) "Your default dopamine pathways were perfectly self-sufficient. Constant nicotine storms force the brain to decrease native dopamine production and receptor numbers. This is why you feel constantly empty in standard settings without a cigarette." else "大脑中原有的多巴胺系统本身是自给自足的。但长期遭受外源性尼古丁的大暴雨滋润后，作为自我保护，大脑下调了多巴胺的分泌和自身受体敏感度（去敏感化）。这就是为什么烟瘾越来越大，因为天然环境完全无法填饱这些受体，你必须不停抽烟才能维持在普通人的基本心理状态。",
            solutionDetail = if (isEng) "This down-regulation will automatically reverse! Give the cellular system 2-4 weeks without nicotine, and baseline receptors will recover completely, restoring full joy from natural interactions and daily life." else "这是大脑重新配置的过渡失衡期。坚信这是一个纯粹的化学自净过程。断掉尼古丁2～4周后，大脑会完美地反向修正化学反馈浓度，你会重新在普通生活的食物、风景与交往中体验到饱满的多巴胺快乐！"
        ),
        DeconstructLayer(
            title = if (isEng) "⚡ Triggers: Automatic Behavioral Locking" else "⚡ 触发层: 习惯全自动咬合",
            summary = if (isEng) "Nicotine habits seamlessly connect with situational cues like post-meals or stress, bypassing conscious thought." else "吸烟动作与某些外界情境高度绑定（如饭后、焦虑社交等），跳过大脑逻辑直接下达点烟信号。",
            mechanismDetail = if (isEng) "A short nicotine half-life of 2 hours prompts frequent, dense smoking habits. This repeated association forms thick conditional loops in the cerebellum. Cues like dining automatically activate motor programs to ignite a cigarette." else "尼古丁的超快代谢（半衰期约2小时）导致抽烟变成高频密集习惯（饭后必抽、写代码必抽、遇到压力必抽）。这种长期的特定触发情境刺激与行为连接，在小脑内打磨出了极度平滑的化学条件反射链路。一旦外部场景（饭后）被激活，小脑就会自发绕过前额叶理性，直接调起肌肉点燃打火机。",
            solutionDetail = if (isEng) "Do not fight this conditional loop with empty willpower alone. Replace the actions! Code an 'Implementation Intention': 'When cue X occurs, I will immediately execute action Y instead without hesitation.'" else "永远不要单凭抽象的意志力去对抗场景绑定。必须进行脚本改写！采用“执行意图”方法：“当X情境发生，我将不予犹豫，直接去启动Y替代方案”。"
        ),
        DeconstructLayer(
            title = if (isEng) "🧠 Cognitive: Dismantling the Illusion of Calm" else "🧠 认知层: 放松幻觉解构",
            summary = if (isEng) "Smokers falsely believe cigarettes aid focus & relieve stress, whereas smoking merely pacifies its own withdrawal pain." else "错误归因相信“香烟能帮我思考、缓解压力”。事实是香烟只缓解了它本身所带来的戒断痛苦。",
            mechanismDetail = if (isEng) "True, smoking feels highly relaxing on the spot. But this is not because nicotine possesses relaxing biochemistry. Smoking simply reverses the acute, nervous withdrawal state created by the previous cigarette. Non-smokers enjoy this peace 24/7 for free." else "很多吸烟者真切地感知到“抽烟那一刻全身舒展、脑子松开了”。第一性原理告诉我们：这并不代表尼古丁本身有任何物理催眠或镇静功效。真相非常戏谑——你体验到的仅仅是“本轮尼古丁浓度见底产生的烦躁焦虑，由于重新续烟而终于得到了消退”。普通未抽烟的人每天24小时都在自由地享受这种完美的平静，而你被迫每半小时吸一次毒来换取瞬间的平和。",
            solutionDetail = if (isEng) "Reconstruct your perspective. Every craving is a biological healing signal in progress. Quitting is not 'giving up a pleasure' - it is releasing yourself from deep captivity." else "重塑认知。每一次渴望的产生都是排毒的生理尖兵在打扫战场。戒烟绝非“放弃某种生活享受”，而是无罪开释，将身体的所有权从跨国烟草资本及尼古丁锁链中重新“赎回并重获自由”！"
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isEng) "Addiction Mechanism Analysis (Click to explore)" else "成瘾机制物理拆解 (点击剖析)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        layers.forEach { layer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onLayerClick(layer) },
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = layer.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = layer.summary,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                            lineHeight = 18.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = OxygenGreen,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

data class DeconstructLayer(
    val title: String,
    val summary: String,
    val mechanismDetail: String,
    val solutionDetail: String
)

// PILLAR 2: HABIT REWRITING (IF/THEN CUSTOM LOGIC)
@Composable
fun HabitRewritingPillar(
    isEng: Boolean,
    triggers: List<HabitTrigger>,
    onExecute: (HabitTrigger) -> Unit,
    onDelete: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val tLocal = { zh: String, en: String -> if (isEng) en else zh }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = tLocal("防守反击习惯脚本", "Counter-Reflex Scripts"),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                )
                Text(
                    text = tLocal("如果【特定线索】，我就【物理替代】", "IF [Specific Cue], THEN [Physical Alternative]"),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSec)
                )
            }

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_custom_script_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Script",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(tLocal("植入反射", "Inject Reflex"), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (triggers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(CardSlate, RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, BorderSlate), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(tLocal("暂无防守反击脚本，去编写一个吧", "No reflex script yet. Let's write one!"), color = TextGray)
            }
        } else {
            triggers.forEach { trigger ->
                // Translate built-in triggers on the fly if English is active
                val displayedCue = if (isEng && trigger.isBuiltIn) {
                    when (trigger.cue) {
                        "饭后完成进食" -> "Finished dining"
                        "感到工作/生活压力大" -> "Feeling high stress from work/life"
                        "早起配咖啡/茶时" -> "Waking up with coffee/tea"
                        "社交聚会有人递烟" -> "Offered a cigarette at a social gathering"
                        else -> trigger.cue
                    }
                } else {
                    trigger.cue
                }

                val displayedBehavior = if (isEng && trigger.isBuiltIn) {
                    when (trigger.behavior) {
                        "立刻起身做5分钟拉伸，或出去散步10分钟" -> "Immediately stand up for a 5-min stretch, or take a 10-min walk"
                        "做5次深呼吸（使用SOS呼吸教练），不让自己握住烟盒" -> "Do 5 deep breaths (use SOS coach), do not touch the casing"
                        "换成柠檬温水，改用茶匙搅拌，打乱原本的机械习惯配对" -> "Switch to warm lemon water, stir with a teaspoon, disruption of habit mechanics"
                        "礼貌微笑并坚定拒绝：“谢了，我正在把身体奖励系统恢复原状呢”，嚼一颗无糖口香糖" -> "Smile & decline firmly: \"Thanks, I'm recovering my body's reward system to baseline scale.\" Chew sugar-free gum"
                        else -> trigger.behavior
                    }
                } else {
                    trigger.behavior
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, BorderSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (trigger.isBuiltIn) MaterialTheme.colorScheme.primaryContainer else CardSlateElevated,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (trigger.isBuiltIn) tLocal("精选原理解", "Built-In") else tLocal("自定义防守", "Custom"),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (trigger.isBuiltIn) OxygenGreen else TextWhite
                                            )
                                        )
                                    }

                                    if (trigger.executionCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.secondaryContainer,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tLocal("成功避坑 ${trigger.executionCount} 次", "Avoided trigger successfully ${trigger.executionCount} times"),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = HealingCyan
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tLocal("当：", "IF: "),
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = displayedCue,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = tLocal("那就：", "THEN: "),
                                        fontWeight = FontWeight.Bold,
                                        color = OxygenGreen,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = displayedBehavior,
                                        fontWeight = FontWeight.Medium,
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            if (!trigger.isBuiltIn) {
                                IconButton(
                                    onClick = { onDelete(trigger.id) },
                                    modifier = Modifier.size(32.dp).testTag("delete_script_${trigger.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = DopamineCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Execution confirmation button
                        Button(
                            onClick = { onExecute(trigger) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("execute_trigger_btn_${trigger.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardSlateElevated,
                                contentColor = OxygenGreen
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tLocal("刚才这个场景我克制了毒瘾！", "I conquered the urge under this situation!"),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

// PILLAR 3: MINIMUM VIABLE SYSTEM (7-STEP CHECKLIST)
@Composable
fun MvsChecklistPillar(
    isEng: Boolean,
    config: QuitSmokingConfig?,
    triggers: List<HabitTrigger>,
    onSetDateClick: () -> Unit,
    onCommitClick: () -> Unit,
    onToggleStep: (Int, Boolean) -> Unit
) {
    if (config == null) return

    val tLocal = { zh: String, en: String -> if (isEng) en else zh }

    val totalSteps = 7
    var completedCount = 0

    val step1Completed = config.quitDateTimestamp != null
    if (step1Completed) completedCount++

    val step2Completed = config.stepCleanEnvironment
    if (step2Completed) completedCount++

    val step3Completed = config.stepNrtSetup
    if (step3Completed) completedCount++

    val step4Completed = config.isCommitted
    if (step4Completed) completedCount++

    // Step 5: Survive 72h physiological peak
    val elapsedSinceQuit = if (config.quitDateTimestamp != null) {
        System.currentTimeMillis() - config.quitDateTimestamp
    } else 0L
    val step5Completed = elapsedSinceQuit >= (72 * 60 * 60 * 1000L)
    if (step5Completed) completedCount++

    // Step 6: Rewrite situations scripts
    val step6Completed = triggers.isNotEmpty()
    if (step6Completed) completedCount++

    // Step 7: Saved Money Track (True if saving progress set and they survived at least some time)
    val step7Completed = step1Completed && elapsedSinceQuit > 0L
    if (step7Completed) completedCount++

    val progressPercent = (completedCount.toFloat() / totalSteps.toFloat())

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            border = BorderStroke(1.dp, BorderSlate),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tLocal("最小可行戒烟系统 (MVS)", "Minimum Viable System (MVS)"),
                        color = TextWhite,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = tLocal("$completedCount / $totalSteps 已完成", "$completedCount / $totalSteps Done"),
                        color = OxygenGreen,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = OxygenGreen,
                    trackColor = CardSlateElevated
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RENDERING 7 STEPS OVERVIEW
        // Step 1
        val sdf = SimpleDateFormat(tLocal("yyyy年MM月dd日 HH:mm", "yyyy-MM-dd HH:mm"), Locale.getDefault())
        MvsStepItem(
            stepNumber = 1,
            title = tLocal("选定确定戒烟日 (而非从明天起)", "Determine Exact Target Date"),
            completed = step1Completed,
            desc = if (step1Completed) {
                tLocal("已定于 ${sdf.format(Date(config.quitDateTimestamp!!))}", "Scheduled on ${sdf.format(Date(config.quitDateTimestamp!!))}")
            } else tLocal("给自己一个神圣的开始，而不是无限拖延的明天", "Set a rigid, sacred entry point in time, and reject procrastination."),
            actionButton = {
                TextButton(onClick = onSetDateClick, modifier = Modifier.testTag("step_1_btn")) {
                    Text(if (step1Completed) tLocal("修改时间", "Update Time") else tLocal("立刻选定", "Select Now"), color = OxygenGreen, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        )

        // Step 2
        MvsStepItem(
            stepNumber = 2,
            title = tLocal("清除物理环境 (制造触发阻力)", "Purge Physical Environment"),
            completed = step2Completed,
            desc = tLocal("扔掉房间和车内所有的打火机、香烟、未拆封烟灰缸。彻底断绝在不经历痛苦阻力的情况下接触到烟草的可能。", "Throw away all matches, lighters, and remaining cigarette packs in your room & vehicle. Create strong physical barriers."),
            checkboxClick = { onToggleStep(2, !step2Completed) },
            tag = "step_2_checkbox"
        )

        // Step 3
        MvsStepItem(
            stepNumber = 3,
            title = tLocal("配备无毒替代品 (配备防御战备)", "Equip Non-Toxic Alternatives"),
            completed = step3Completed,
            desc = tLocal("准备薄荷口香糖、柠檬片或医生推荐的非吞入尼古丁贴剂。给手部或口腔肌肉一个健康的“假目标”。", "Prepare mint gums, deep sour lemon water, or medical patches. Provide physical target substitutions for hand/mouth muscles."),
            checkboxClick = { onToggleStep(3, !step3Completed) },
            tag = "step_3_checkbox"
        )

        // Step 4
        MvsStepItem(
            stepNumber = 4,
            title = tLocal("告知 3 个关心你的人 (社会契约网络)", "Notify 3 Close Supporters"),
            completed = step4Completed,
            desc = if (step4Completed) {
                tLocal("已建立相互监督的外部约束圈", "Established external accountability network with supporters")
            } else tLocal("将誓言转化为真实的外部锚点。公开宣告后，你的戒除承诺将绑定一部分群体自尊。", "Turn vows into external social anchors. Group esteem will naturally assist physical determination."),
            actionButton = {
                TextButton(onClick = onCommitClick, modifier = Modifier.testTag("step_4_btn")) {
                    Text(if (step4Completed) tLocal("更新契约", "Update Oath") else tLocal("写入承诺", "Write Oath"), color = OxygenGreen, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        )

        // Step 5
        val peakProgress = min(elapsedSinceQuit.toFloat() / (72 * 60 * 60 * 1000f), 1f)
        MvsStepItem(
            stepNumber = 5,
            title = tLocal("坚挺支撑度过前 72 小时 (生理脱瘾期)", "Survive First 72 Hours"),
            completed = step5Completed,
            desc = tLocal("排毒尖峰时刻。72小时后，体内尼古丁被100%完全代谢排空，生理依赖将进入彻底退巢期。", "Peak clearing phase. After 72 hours, nicotine is 100% metabolized out of your blood, sending physical dependence into decline."),
            customWidget = {
                if (step1Completed && !step5Completed) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        LinearProgressIndicator(
                            progress = { peakProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = WarningAmber,
                            trackColor = CardSlateElevated
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tLocal(
                                "生理脱瘾进度: %.1f%% (%d / 72小时)".format(peakProgress * 100f, elapsedSinceQuit / (60 * 60 * 1000)),
                                "Physical detox progress: %.1f%% (%d / 72hr)".format(peakProgress * 100f, elapsedSinceQuit / (60 * 60 * 1000))
                            ),
                            color = WarningAmber,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        )

        // Step 6
        MvsStepItem(
            stepNumber = 6,
            title = tLocal("改写习惯条件反射链路", "Rewrite Habit Conditions"),
            completed = step6Completed,
            desc = tLocal("列出引发吸烟的最高频场景（如饭后书房、朋友开烟），必须预设好「如果场景发生，我就开启何种物理行为抵抗」的逻辑重连公式。", "List situations representing high temptation. You must pre-program conditional reflexes: 'IF situation occurs, THEN launch physical counter-defense.'"),
            customWidget = {
                if (!step6Completed) {
                    Text(
                        text = tLocal("⚠️ 尚未在 【习惯重组】 版块中为自己设置反击脚本！", "⚠️ Under-equipped: Refresher script not yet programmed in Habit Rehab section!"),
                        color = DopamineCoral,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        )

        // Step 7
        MvsStepItem(
            stepNumber = 7,
            title = tLocal("持续监测节省资金与清肺收益", "Monitor Financial & Health Earnings"),
            completed = step7Completed,
            desc = tLocal("用真实的经济积累和生物里程碑（例如，皮肤光泽度上升、体能回春），给大脑前额叶不断灌顶正向奖励，覆盖被截断的虚无空洞。", "Use tactile economics gain & health metrics to reward the prefrontal cortex with positive reinforcement, effectively writing off artificial sensory decay."),
            customWidget = {
                if (step7Completed) {
                    Text(
                        text = tLocal("💡 您的数据已在主面板和生理里程牌版块中实施了自动追踪绑定。", "💡 Progress tracking is fully synchronized on your dashboard."),
                        color = OxygenGreen,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun MvsStepItem(
    stepNumber: Int,
    title: String,
    completed: Boolean,
    desc: String,
    checkboxClick: (() -> Unit)? = null,
    actionButton: (@Composable () -> Unit)? = null,
    customWidget: (@Composable () -> Unit)? = null,
    tag: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) CardSlateElevated else CardSlate
        ),
        border = BorderStroke(1.dp, if (completed) OxygenDarkGreen else BorderSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Circle Number
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (completed) OxygenGreen else CardSlateElevated,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = CarbonDarkBg,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = stepNumber.toString(),
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (completed) OxygenGreen else TextWhite
                        )
                    )

                    // Optional Switch/Checkbox
                    if (checkboxClick != null) {
                        Checkbox(
                            checked = completed,
                            onCheckedChange = { checkboxClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = OxygenGreen,
                                uncheckedColor = TextGray
                            ),
                            modifier = if (tag != null) Modifier.size(24.dp).testTag(tag) else Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                    lineHeight = 16.sp
                )

                actionButton?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    it()
                }

                customWidget?.let {
                    it()
                }
            }
        }
    }
}

// PILLAR 4: CLINICAL PROGRESS & LOGS
@Composable
fun ClinicalMilestonesPillar(
    isEng: Boolean,
    quitTimestamp: Long?,
    cravingLogs: List<com.example.data.CravingLog>
) {
    val elapsed = if (quitTimestamp != null) {
        System.currentTimeMillis() - quitTimestamp
    } else 0L

    val tLocal = { zh: String, en: String -> if (isEng) en else zh }

    val milestones = listOf(
        MilestoneItem(
            tLocal("20 分钟", "20 Mins"),
            tLocal("心率、血压开始下降，手脚温度向健康人靠拢。", "Heart rate and blood pressure begin to decrease; extremity temperatures approach baseline health."),
            20 * 60 * 1000L
        ),
        MilestoneItem(
            tLocal("12 小时", "12 Hours"),
            tLocal("血液中剧毒的一氧化碳浓度剧降至正常水平，血氧浓度回复高点。", "Toxic carbon monoxide in blood drops to normal range. Blood oxygen scales back to healthy levels."),
            12 * 60 * 60 * 1000L
        ),
        MilestoneItem(
            tLocal("24 小时", "24 Hours"),
            tLocal("冠心病突发猝死风险开始出现轻微反转，肺部开启排异防御机制。", "Risk of coronary heart disease drops slightly. Clean lungs begin defensive clearance of hazardous residues."),
            24 * 60 * 60 * 1000L
        ),
        MilestoneItem(
            tLocal("72 小时", "72 Hours"),
            tLocal("体内尼古丁完全排干代谢！呼吸支气管放松并扩张，呼吸极大清爽化。", "Nicotine perfectly cleared from blood! Bronchiole tubes relax and dilate, giving a refreshing airway."),
            72 * 60 * 60 * 1000L
        ),
        MilestoneItem(
            tLocal("2 星期", "2 Weeks"),
            tLocal("全身血液微循环回春级提升，肺功能、通气吞吐量最高上升达 30%。", "Total physical blood micro-circulation rises. Lung capacity & respiration levels shoot up by up to 30%."),
            14 * 24 * 60 * 60 * 1000L
        ),
        MilestoneItem(
            tLocal("1 个月", "1 Month"),
            tLocal("咳嗽咳嗽、呼吸不上急促情况大退。肺内清洁纤毛复活，毒痰排出提速。", "Coughing and shortness of breath decreases drastically. Lung cilia activate to speed up self-cleaning."),
            30 * 24 * 60 * 60 * 1000L
        ),
        MilestoneItem(
            tLocal("1 年", "1 Year"),
            tLocal("由于吸烟造成的过剩性心肌梗塞心脏血管硬化风险减少一半，重获心安。", "Risk of coronary heart disease and cardiovascular stiffness drops to half that of a current smoker."),
            365 * 24 * 60 * 60 * 1000L
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = tLocal("肺部及心血管机能自愈里碑", "Cardiovascular & Pulmonary Repair Milestones"),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
        )
        Text(
            text = tLocal("由于受体敏感下调及排毒机制完全复苏", "Triggered by receptor normalization & biological detox"),
            style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSec)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (quitTimestamp == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSlate, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tLocal("开启戒断日程后，在此处追踪身体各大器官的物理重构历程。", "Define a stop date to track the organs' physical recovery program on this timeline."),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            milestones.forEach { item ->
                val progress = if (elapsed >= item.timeNeededMs) 1.0f else (elapsed.toFloat() / item.timeNeededMs.toFloat())
                val isUnlocked = elapsed >= item.timeNeededMs

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, if (isUnlocked) OxygenDarkGreen else BorderSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.time,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) OxygenGreen else TextWhite
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isUnlocked) OxygenDarkGreen else CardSlateElevated,
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isUnlocked) tLocal("已达成", "Unlocked") else tLocal("修复中", "Healing"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color.White else TextGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.benefit,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isUnlocked) OxygenGreen else HealingCyan,
                            trackColor = CardSlateElevated
                        )
                        if (!isUnlocked) {
                            val hoursLeft = ((item.timeNeededMs - elapsed) / (1000 * 60 * 60)).toInt()
                            val minsLeft = (((item.timeNeededMs - elapsed) % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                            val remainingStr = if (hoursLeft > 24) {
                                tLocal("剩余约 ${(hoursLeft / 24)} 天", "Approx. ${(hoursLeft / 24)} days left")
                            } else {
                                tLocal("剩余约 ${hoursLeft}时${minsLeft}分", "$hoursLeft hr $minsLeft min left")
                            }
                            Text(
                                text = remainingStr,
                                color = TextDarkSec,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RECENT CRASHED CRAVINGS logs
        Text(
            text = "战胜化学多巴胺渴求日记",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (cravingLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSlate, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "每击溃一次尼古丁求救信号，这里都会添上一道勋章！",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSec),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val sdfLog = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
            cravingLogs.take(8).forEach { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, BorderSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Win",
                            tint = DopamineCoral,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.associatedCue ?: "自主阻断防线",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                Text(
                                    text = sdfLog.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSec)
                                )
                            }
                            log.notes?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MilestoneItem(
    val time: String,
    val benefit: String,
    val timeNeededMs: Long
)

// BREATHING COACH PANEL WITH PULSING CIRCLE SCALING ANIMATION
@Composable
fun BreathingCoachPanel(
    ongoingState: BreathingState.Ongoing,
    onNextPhase: (BreathPhase, cycle: Int, rem: Int) -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    // Phase update clock ticking down every 1 second
    LaunchedEffect(ongoingState.phase, ongoingState.secondsRemaining) {
        delay(1000)
        val nextSec = ongoingState.secondsRemaining - 1
        if (nextSec > 0) {
            onNextPhase(ongoingState.phase, ongoingState.cycle, nextSec)
        } else {
            // Transitions to the next phase
            when (ongoingState.phase) {
                BreathPhase.IN -> {
                    onNextPhase(BreathPhase.HOLD, ongoingState.cycle, BreathPhase.HOLD.durationSec)
                }
                BreathPhase.HOLD -> {
                    onNextPhase(BreathPhase.OUT, ongoingState.cycle, BreathPhase.OUT.durationSec)
                }
                BreathPhase.OUT -> {
                    val nextCycle = ongoingState.cycle + 1
                    if (nextCycle > 5) {
                        onComplete()
                    } else {
                        onNextPhase(BreathPhase.IN, nextCycle, BreathPhase.IN.durationSec)
                    }
                }
            }
        }
    }

    // Interactive Pulsing Animation based on Phase
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingBreath")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsePulse"
    )

    // Base target scales: IN = expands, HOLD = stays large, OUT = shrinks
    val scaleFactor by animateFloatAsState(
        targetValue = when (ongoingState.phase) {
            BreathPhase.IN -> 1.4f
            BreathPhase.HOLD -> 1.4f
            BreathPhase.OUT -> 0.8f
        },
        animationSpec = tween(4000, easing = FastOutSlowInEasing),
        label = "BreathSmoothScaler"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Progress indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "生理突触阻断实验 (循环 %d/5)".format(ongoingState.cycle),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = OxygenGreen,
                    fontWeight = FontWeight.Bold
                )
            )

            IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = TextGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Breathing Circle Container with animated Canvas
        Box(
            modifier = Modifier
                .size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Simple concentric visual waves
            val colorPrimary = when (ongoingState.phase) {
                BreathPhase.IN -> OxygenGreen
                BreathPhase.HOLD -> HealingCyan
                BreathPhase.OUT -> WarningAmber
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val baseRadius = (size.minDimension / 3.4f) * scaleFactor * pulseScale
                        // Glowing center
                        drawCircle(
                            color = colorPrimary.copy(alpha = 0.15f),
                            radius = baseRadius + 24.dp.toPx()
                        )
                        // Precise Stroke Ring
                        drawCircle(
                            color = colorPrimary,
                            radius = baseRadius,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
            )

            // Realtime countdown clock
            Text(
                text = "%d".format(ongoingState.secondsRemaining),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Dynamic guidelines text explanation
        Text(
            text = ongoingState.phase.text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.height(48.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlateElevated),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "💡 第一性原理提示：空虚烦躁是神经元受体空仓求助的正常生理电信号。由于半衰期机制，此种强烈化学渴求将在3分钟后达到生理衰减阀值。放轻松并深呼吸，专注于本轮氧气充沛的呼吸动作，信号很快会被安全释放。",
                style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                lineHeight = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
