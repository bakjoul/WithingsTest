package com.bakjoul.testwithings.ui.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 0.dp,
    @DrawableRes icon: Int? = null,
    iconEndPadding: Dp = 0.dp,
    text: String,
    textSize: TextUnit = 13.sp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .alpha(if (enabled) 1f else 0.5f),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = iconEndPadding)
                )
            }

            Text(
                text = text,
                fontSize = textSize
            )
        }
    }
}

@Preview
@Composable
fun SmallButtonPreview() {
    SmallButton(
        text = "Button",
        onClick = {}
    )
}
