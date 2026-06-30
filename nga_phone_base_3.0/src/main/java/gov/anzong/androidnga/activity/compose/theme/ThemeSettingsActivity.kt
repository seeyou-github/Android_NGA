package gov.anzong.androidnga.activity.compose.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.justwen.androidnga.ui.compose.theme.AppTheme
import com.justwen.androidnga.ui.compose.theme.ThemeConfig
import com.justwen.androidnga.ui.compose.theme.ThemePreset
import com.justwen.androidnga.ui.compose.theme.ThemePresets
import com.justwen.androidnga.ui.compose.theme.readableOn
import sp.phone.theme.ThemeManager

class ThemeSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    ThemeSettingsPage(
                        onNavigateBack = { finish() },
                        onApplyTheme = {
                            ThemeManager.getInstance().markThemeDirty()
                            recreate()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSettingsPage(
    onNavigateBack: () -> Unit,
    onApplyTheme: () -> Unit,
) {
    val isNight = ThemeManager.getInstance().isNightMode()
    var nightMode by remember { mutableStateOf(isNight) }
    var selectedPresetIndex: Int? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "主题设置", onBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("夜间模式", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Switch(
                    checked = nightMode,
                    onCheckedChange = { enabled ->
                        nightMode = enabled
                        ThemeManager.getInstance().setNightMode(enabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val presets = if (nightMode) ThemePresets.DARK_PRESETS else ThemePresets.LIGHT_PRESETS
            Text("预设主题", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.chunked(3).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowPresets.forEach { preset ->
                            Box(modifier = Modifier.weight(1f)) {
                                PresetCard(
                                    preset = preset,
                                    onClick = {
                                        ThemeConfig.save(preset.config)
                                        onApplyTheme()
                                    },
                                    onLongClick = { selectedPresetIndex = presets.indexOf(preset) },
                                )
                            }
                        }
                        repeat(3 - rowPresets.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (nightMode) "当前为夜间模式，显示夜间预设" else "当前为日间模式，显示日间预设",
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }

    selectedPresetIndex?.let { index ->
        val presets = if (nightMode) ThemePresets.DARK_PRESETS else ThemePresets.LIGHT_PRESETS
        if (index in presets.indices) {
            ThemeDetailDialog(
                preset = presets[index],
                onDismiss = { selectedPresetIndex = null },
                onApply = { editedConfig ->
                    ThemeConfig.save(editedConfig)
                    selectedPresetIndex = null
                    onApplyTheme()
                }
            )
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val primary = MaterialTheme.colors.primary
    val onPrimary = primary.readableOn(primary)

    Surface(
        color = primary,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = onPrimary,
                    contentDescription = "返回"
                )
            }
            Text(
                text = title,
                color = onPrimary,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetCard(preset: ThemePreset, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bg = preset.config.bgColor
    val textColor = preset.config.textColor
    val topBar = preset.config.topBarColor

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, topBar.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(topBar)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(bg)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = preset.name,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ThemeDetailDialog(
    preset: ThemePreset,
    onDismiss: () -> Unit,
    onApply: (ThemeConfig) -> Unit,
) {
    var editedConfig by remember { mutableStateOf(preset.config) }
    var editingColorKey by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(preset.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ColorRow("文字颜色", editedConfig.textColor) { editingColorKey = "textColor" }
                Spacer(modifier = Modifier.height(8.dp))
                ColorRow("顶栏+底栏背景", editedConfig.topBarColor) { editingColorKey = "topBarColor" }
                Spacer(modifier = Modifier.height(8.dp))
                ColorRow("背景颜色", editedConfig.bgColor) { editingColorKey = "bgColor" }
                Spacer(modifier = Modifier.height(8.dp))
                ColorRow("强调色", editedConfig.accentColor) { editingColorKey = "accentColor" }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(editedConfig) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )

    editingColorKey?.let { key ->
        val currentColor = when (key) {
            "textColor" -> editedConfig.textColor
            "topBarColor" -> editedConfig.topBarColor
            "bgColor" -> editedConfig.bgColor
            else -> editedConfig.accentColor
        }
        HsvColorPickerDialog(
            initialColor = currentColor,
            onDismiss = { editingColorKey = null },
            onConfirm = { newColor ->
                editedConfig = when (key) {
                    "textColor" -> editedConfig.copy(textColor = newColor)
                    "topBarColor" -> editedConfig.copy(topBarColor = newColor)
                    "bgColor" -> editedConfig.copy(bgColor = newColor)
                    else -> editedConfig.copy(accentColor = newColor)
                }
                editingColorKey = null
            }
        )
    }
}

@Composable
private fun ColorRow(label: String, color: Color, onClick: () -> Unit) {
    val readable = color.readableOn(color)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = readable, fontSize = 14.sp)
        Text(color.toHex(), color = readable.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

private fun Color.toHex(): String {
    val argb = toArgb()
    return String.format("#%08X", argb)
}

@Composable
private fun HsvColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("调整颜色", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                val currentColor = Color.hsv(hue, saturation, value)
                PreviewBox(currentColor)
                Spacer(modifier = Modifier.height(16.dp))

                SatValPicker(hue, saturation, value) { s, v ->
                    saturation = s; value = v
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text("色相", fontSize = 12.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(4.dp))
                HueSlider(hue) { hue = it }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(currentColor) }) { Text("确定") }
                }
            }
        }
    }
}

@Composable
private fun PreviewBox(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(color.toHex(), color = color.readableOn(color), fontSize = 14.sp)
    }
}

@Composable
private fun HueSlider(hue: Float, onHueChange: (Float) -> Unit) {
    val gradientColors = listOf(
        Color.hsv(0f, 1f, 1f), Color.hsv(60f, 1f, 1f),
        Color.hsv(120f, 1f, 1f), Color.hsv(180f, 1f, 1f),
        Color.hsv(240f, 1f, 1f), Color.hsv(300f, 1f, 1f),
        Color.hsv(360f, 1f, 1f),
    )
    var viewWidthPx by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .onSizeChanged { viewWidthPx = it.width.coerceAtLeast(1).toFloat() }
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .pointerInput(viewWidthPx) {
                detectTapGestures { offset ->
                    val x = (offset.x / size.width).coerceIn(0f, 1f)
                    onHueChange(x * 360f)
                }
            }
            .pointerInput(viewWidthPx) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                    onHueChange(x * 360f)
                }
            }
    ) {
        val indicatorPos = (hue / 360f).coerceIn(0f, 1f)
        val indicatorPx = indicatorPos * viewWidthPx
        Box(
            modifier = Modifier
                .offset { IntOffset((indicatorPx - 12f).toInt(), 4) }
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .background(Color.hsv(hue, 1f, 1f))
        )
    }
}

@Composable
private fun SatValPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onSatValChange: (Float, Float) -> Unit,
) {
    Text("饱和度 / 明度", fontSize = 12.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
    Spacer(modifier = Modifier.height(4.dp))
    var viewSizePx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .onSizeChanged { viewSizePx = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
            .clip(RoundedCornerShape(8.dp))
            .background(Color.hsv(hue, 1f, 1f))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .pointerInput(viewSizePx) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onSatValChange(s, v)
                }
            }
            .pointerInput(viewSizePx) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onSatValChange(s, v)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        )
        val indicatorX = saturation * viewSizePx.width
        val indicatorY = (1f - value) * viewSizePx.height
        Box(
            modifier = Modifier
                .offset { IntOffset((indicatorX - 8f).toInt(), (indicatorY - 8f).toInt()) }
                .size(16.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .background(Color.hsv(hue, saturation, value))
        )
    }
}
