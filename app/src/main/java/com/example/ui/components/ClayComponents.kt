package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

/**
 * Reusable Claymorphism Design System Components
 * Implements soft 3D clay-like cards, puffy elevated controls, gentle highlights,
 * and tactile pressed-depth animations.
 */

@Composable
fun Modifier.claySurface(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 6.dp,
    highlightBorderAlpha: Float = 0.7f
): Modifier {
    val isDark = MaterialTheme.colorScheme.background == ClayBackgroundDark
    val highlightColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = highlightBorderAlpha)
    val shadowAmbient = if (isDark) Color(0x60000000) else Color(0x18000000)
    val shadowSpot = if (isDark) Color(0x80000000) else Color(0x15000000)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowAmbient,
            spotColor = shadowSpot
        )
        .clip(shape)
        .background(backgroundColor)
        .border(
            width = 1.5.dp,
            color = highlightColor,
            shape = shape
        )
}

/**
 * 1. ClayCard: Soft 3D clay-like card with rounded corners and puffy elevation
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .claySurface(shape = shape, backgroundColor = backgroundColor, elevation = elevation)
            .then(clickModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * 2. ClayButton: Puffy tactile button with pressed depth animations
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(16.dp),
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "clay_btn_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(durationMillis = 100),
        label = "clay_btn_elevation"
    )

    val buttonBg = if (enabled) containerColor else containerColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
            .claySurface(shape = shape, backgroundColor = buttonBg, elevation = elevation)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (content != null) {
                content()
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    if (!text.isNullOrBlank()) Spacer(modifier = Modifier.width(8.dp))
                }
                if (!text.isNullOrBlank()) {
                    Text(
                        text = text,
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 3. ClayInput: Soft rounded input field
 */
@Composable
fun ClayInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .claySurface(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    elevation = 3.dp
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = if (placeholder != null) {
                    { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                } else null,
                leadingIcon = if (leadingIcon != null) {
                    { Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                } else null,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                maxLines = maxLines,
                isError = isError,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = ClayCoral,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ClayCoral,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 4. ClaySelect: Puffy dropdown selection component
 */
@Composable
fun <T> ClaySelect(
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .claySurface(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    elevation = 3.dp
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemLabel(selectedItem),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(item)) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 5. ClayToggle: Puffy clay pill toggle switch
 */
@Composable
fun ClayToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackBg by animateColorAsState(
        targetValue = if (checked) ClayMint else Color(0xFFCBD5E1),
        label = "clay_toggle_bg"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "clay_toggle_thumb"
    )

    Box(
        modifier = modifier
            .width(52.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(trackBg)
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(15.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color(0x30000000))
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0x15000000), CircleShape)
        )
    }
}

/**
 * 6. ClayBadge: Soft pastel status badge
 */
@Composable
fun ClayBadge(
    text: String,
    statusType: String = "info",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (statusType.lowercase()) {
        "success", "published", "passed", "approved" -> Pair(ClayMintContainer, ClayMint)
        "running", "warning", "pending", "researching", "writing" -> Pair(ClayPeachContainer, ClayPeach)
        "failed", "error", "rejected" -> Pair(ClayCoralContainer, ClayCoral)
        "draft", "purple" -> Pair(ClayLavenderContainer, ClayLavender)
        "cyan", "scheduled" -> Pair(ClayCyanContainer, ClayCyan)
        else -> Pair(ClayBlueContainer, ClayBlue)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 7. ClayProgress: Puffy rounded progress bar
 */
@Composable
fun ClayProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = clampedProgress)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
    }
}

/**
 * 8. ClayTabs: Puffy segment pill tab bar
 */
@Composable
fun ClayTabs(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .claySurface(
                shape = RoundedCornerShape(22.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                elevation = 2.dp
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val tabBg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .then(
                            if (isSelected) Modifier.claySurface(
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = tabBg,
                                elevation = 3.dp
                            ) else Modifier.clip(RoundedCornerShape(18.dp))
                        )
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = fontWeight
                    )
                }
            }
        }
    }
}

/**
 * 9. ClayStatCard: Puffy stat card for dashboard metrics
 */
@Composable
fun ClayStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    ClayCard(
        modifier = modifier,
        elevation = 5.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .claySurface(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = containerColor,
                        elevation = 3.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 10. ClayStatusIndicator: Pulsing animated status dot
 */
@Composable
fun ClayStatusIndicator(
    label: String,
    isActive: Boolean = true,
    activeColor: Color = ClayMint,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "clay_status_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) activeColor.copy(alpha = pulseAlpha)
                    else Color.Gray.copy(alpha = 0.4f)
                )
                .border(2.dp, Color.White, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 11. ClayModal: Puffy clay dialog/modal container
 */
@Composable
fun ClayModal(
    onDismissRequest: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .claySurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    elevation = 10.dp
                )
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                content()
            }
        }
    }
}

/**
 * 12. ClayTable: Mobile-responsive clay card list item (adapts table row to card on phone)
 */
@Composable
fun ClayTableRow(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeStatus: String,
    timeText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ClayCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 3.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                ClayBadge(text = badgeText, statusType = badgeStatus)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 13. ClayNavbar: Puffy top app bar
 */
@Composable
fun ClayNavbar(
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .claySurface(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                backgroundColor = MaterialTheme.colorScheme.surface,
                elevation = 4.dp
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (navigationIcon != null) {
                    navigationIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions()
                }
            }
        }
    }
}
