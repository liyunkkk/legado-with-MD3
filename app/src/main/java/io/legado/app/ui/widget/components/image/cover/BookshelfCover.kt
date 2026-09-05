package io.legado.app.ui.widget.components.image.cover

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.legado.app.ui.theme.LegadoTheme
import io.legado.app.ui.widget.components.card.TextCard
import io.legado.app.ui.widget.components.progressIndicator.AppLinearProgressIndicator

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookshelfCover(
    name: String?,
    author: String?,
    path: String?,
    radius: Dp = 4.dp,
    modifier: Modifier = Modifier,
    coverModifier: Modifier = Modifier.fillMaxWidth(),
    isUpdating: Boolean = false,
    badgeText: String? = null,
    showBadgeDot: Boolean = false,
    leftBottomText: String? = null,
    sourceOrigin: String? = null,
    onLoadFinish: (() -> Unit)? = null,
    showLoadingPlaceholder: Boolean = true,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedCoverKey: String? = null,
    iosStyle: Boolean = false,
) {
    Box(modifier = modifier) {
        val bookCover: @Composable (Modifier) -> Unit = { contentModifier ->
            CoilBookCover(
                name = name,
                author = author,
                path = path,
                radius = radius,
                modifier = contentModifier,
                sourceOrigin = sourceOrigin,
                onLoadFinish = onLoadFinish,
                showLoadingPlaceholder = showLoadingPlaceholder,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedCoverKey = sharedCoverKey,
            )
        }
        if (iosStyle) {
            BookshelfCoverFrame(
                radius = radius,
                modifier = coverModifier,
                animatedVisibilityScope = animatedVisibilityScope,
                content = bookCover
            )
        } else {
            bookCover(coverModifier)
        }

        // 使用 animatedVisibilityScope 的 animateEnterExit 为叠加层添加同步动画
        val overlayModifier = Modifier.then(
            if (animatedVisibilityScope != null) {
                with(animatedVisibilityScope) {
                    Modifier.animateEnterExit(
                        enter = fadeIn(),
                        exit = fadeOut()
                    )
                }
            } else Modifier
        )

        if (!badgeText.isNullOrEmpty()) {
            TextCard(
                text = badgeText,
                icon = if (showBadgeDot) Icons.Default.Update else null,
                iconSize = 12.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .then(overlayModifier),
                cornerRadius = 4.dp,
                horizontalPadding = 4.dp,
                verticalPadding = 2.dp
            )
        }

        if (!leftBottomText.isNullOrEmpty()) {
            TextCard(
                text = leftBottomText,
                backgroundColor = LegadoTheme.colorScheme.cardContainer,
                contentColor = LegadoTheme.colorScheme.onCardContainer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(2.dp)
                    .then(overlayModifier),
                cornerRadius = 4.dp,
                horizontalPadding = 4.dp,
                verticalPadding = 2.dp
            )
        }

        if (isUpdating) {
            AppLinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .height(3.dp)
                    .then(overlayModifier)
            )
        }
    }
}

/**
 * iOS Books 风格的轻量封面框。
 * 图片本身仍由 CoilBookCover 独立承载 sharedElement；这里仅绘制页边、环境阴影和书脊，
 * 并随 AnimatedVisibility 一起淡入淡出，避免干扰阅读页转场。
 */
@Composable
fun BookshelfCoverFrame(
    radius: Dp,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    content: @Composable (Modifier) -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    val decorationModifier = Modifier.then(
        if (animatedVisibilityScope != null) {
            with(animatedVisibilityScope) {
                Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())
            }
        } else {
            Modifier
        }
    )
    Box(
        modifier = modifier.padding(end = 2.dp, bottom = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .shadow(
                    elevation = 7.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.28f)
                )
                .then(decorationModifier)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 1.5.dp)
                .clip(shape)
                .background(if (LegadoTheme.isDark) Color(0xFFB8B5AE) else Color(0xFFF3F0E8))
                .drawWithCache {
                    val lineColor = if (LegadoTheme.isDark) {
                        Color.White.copy(alpha = 0.10f)
                    } else {
                        Color.Black.copy(alpha = 0.10f)
                    }
                    onDrawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = lineColor,
                            style = Stroke(width = 0.6.dp.toPx())
                        )
                    }
                }
                .then(decorationModifier)
        )

        content(Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = radius, bottomStart = radius))
                .drawWithCache {
                    val spineBrush = Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.26f),
                        0.20f to Color.Black.copy(alpha = 0.13f),
                        0.34f to Color.White.copy(alpha = 0.22f),
                        0.42f to Color.Black.copy(alpha = 0.17f),
                        1f to Color.Transparent,
                        endX = size.width * 0.10f
                    )
                    onDrawBehind {
                        drawRect(spineBrush)
                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(size.width * 0.034f, radius.toPx()),
                            end = Offset(size.width * 0.034f, size.height - radius.toPx()),
                            strokeWidth = 0.55.dp.toPx()
                        )
                    }
                }
                .then(decorationModifier)
        )
    }
}
