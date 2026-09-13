package com.jasongrech.carlocator.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jasongrech.carlocator.ui.theme.Accent
import com.jasongrech.carlocator.ui.theme.AccentPressed
import com.jasongrech.carlocator.ui.theme.AccentTint
import com.jasongrech.carlocator.ui.theme.Border
import com.jasongrech.carlocator.ui.theme.Danger
import com.jasongrech.carlocator.ui.theme.DangerPressed
import com.jasongrech.carlocator.ui.theme.OnAccent
import com.jasongrech.carlocator.ui.theme.Success
import com.jasongrech.carlocator.ui.theme.Surface
import com.jasongrech.carlocator.ui.theme.SurfaceRaised
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary

enum class ButtonVariant { Primary, Secondary, Danger }

/**
 * Pill button with a real press response: scales down and darkens on touch-down,
 * springs back on release. This is the "tactile" feel — a plain clickable Card
 * with a ripple reads as flat no matter the color underneath it.
 */
@Composable
fun TactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    val background: Color
    val contentColor: Color
    var borderColor: Color? = null
    when (variant) {
        ButtonVariant.Primary -> {
            background = if (pressed) AccentPressed else Accent
            contentColor = OnAccent
        }
        ButtonVariant.Secondary -> {
            background = if (pressed) SurfaceRaised else Surface
            contentColor = TextPrimary
            borderColor = Border
        }
        ButtonVariant.Danger -> {
            background = if (pressed) DangerPressed else Danger
            contentColor = TextPrimary
        }
    }

    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(background)
            .let { m -> if (borderColor != null) m.border(1.dp, borderColor, shape) else m }
            .clickable(
                interactionSource = interactionSource,
                // No ripple — the color shift + scale-down above *is* the press feedback;
                // layering a default ripple on top would fight the bespoke effect.
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun TactileButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
) {
    TactileButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled,
        fullWidth = fullWidth,
        contentPadding = contentPadding
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** A flat rounded block — the basic unit of the layout, no shadow, stepped one tone above the page. */
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

/** Tracked-out uppercase label, e.g. "PARKING DETECTION" — the section headers from the reference app. */
@Composable
fun SectionEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        modifier = modifier
    )
}

/**
 * A section whose body can be tapped closed — keeps settings that are usually set
 * once and forgotten out of the way, without hiding that they exist or their
 * current state (a one-line summary stays visible even while collapsed).
 */
@Composable
fun CollapsibleSection(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    initiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(modifier = modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionEyebrow(title)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!expanded && summary != null) {
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Text(
                    if (expanded) "▴" else "▾",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        }
    }
}

/** Small state dot — green means ready, amber means "needs a tap." Function, not decoration. */
@Composable
fun StatusDot(ready: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (ready) Success else Accent)
    )
}

/** The app's signature mark: a blocky rounded-square parking badge, reused on every saved spot. */
@Composable
fun ParkingBadge(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape((size.value * 0.3f).dp))
            .background(AccentTint),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "P",
            color = Accent,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
            fontSize = (size.value * 0.46f).sp
        )
    }
}
