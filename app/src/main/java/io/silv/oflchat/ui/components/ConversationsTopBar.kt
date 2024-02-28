package io.silv.oflchat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.DefaultModelEqualityDelegate
import coil.compose.EqualityDelegate
import io.silv.oflchat.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object ConversationsTopBarDefaults {

    @Composable
    fun scrollBehavior() = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    @Composable
    fun IconExpanded(painter: Painter, contentDescription: String? = null) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(2.dp)
                .size(72.0.dp)
        )
    }

    @Composable
    fun IconCollapsed(painter: Painter, contentDescription: String? = null) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(2.dp)
                .size(40.0.dp)
        )
    }

    @Composable
    fun TitleExpanded(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(2.dp),
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun TitleCollapsed(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(2.dp),
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun ProfileIcon(
        model: Any?,
        onClick: () -> Unit,
        contentDescription: String? = stringResource(id = R.string.profile_icon),
        onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
        onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
        onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
        alignment: Alignment = Alignment.Center,
        contentScale: ContentScale = ContentScale.Fit,
        alpha: Float = DefaultAlpha,
        colorFilter: ColorFilter? = null,
        filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
        clipToBounds: Boolean = true,
        modelEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate,
    ) {
        var hasError by remember { mutableStateOf(false) }
        val default = rememberVectorPainter(image = Icons.Rounded.Person)
        val surface = MaterialTheme.colorScheme.onSurface

        AsyncImage(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .padding(2.dp)
                .size(32.0.dp)
                .clip(CircleShape)
                .clickable {
                    onClick()
                },
            model = model,
            contentDescription = contentDescription,
            placeholder = default,
            error = default,
            fallback = default,
            onLoading = {
                hasError = false
                onLoading?.invoke(it)
            },
            onSuccess = {
                hasError = false
                onSuccess?.invoke(it)
            },
            onError = {
                hasError = true
                onError?.invoke(it)
            },
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = if (hasError) {
                ColorFilter.tint(surface, BlendMode.Color)
            } else {
                colorFilter
            },
            filterQuality = filterQuality,
            clipToBounds = clipToBounds,
            modelEqualityDelegate = modelEqualityDelegate
        )
    }
}

@Composable
fun ConversationsTopBar(
    iconExpanded: @Composable () -> Unit,
    iconCollapsed: @Composable () -> Unit,
    titleExpanded: @Composable () -> Unit,
    titleCollapsed: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.background,
        containerColor = MaterialTheme.colorScheme.background
    ),
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBarCopy(
        iconExpanded = iconExpanded,
        iconCollapsed = iconCollapsed,
        titleExpanded = titleExpanded,
        titleCollapsed = titleCollapsed,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarCopy(
    modifier: Modifier = Modifier,
    iconExpanded: @Composable () -> Unit,
    iconCollapsed: @Composable () -> Unit,
    titleExpanded: @Composable () -> Unit,
    titleCollapsed: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    // Sets the app bar's height offset to collapse the entire bar's height when content is
    // scrolled.
    val heightOffsetLimit = with(LocalDensity.current) { -64.dp.toPx() }
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior?.state?.heightOffsetLimit = heightOffsetLimit
        }
    }

    // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
    // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
    // This may potentially animate or interpolate a transition between the container-color and the
    // container's scrolled-color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
    val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    val appBarContainerColor by animateColorAsState(
        targetValue = containerColor(fraction, colors.containerColor, colors.scrolledContainerColor),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "appBarContainerColor"
    )

    // Wrap the given actions in a Row.
    val actionsRow = @Composable {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier = if (scrollBehavior != null && !scrollBehavior.isPinned) {
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffset + delta
            },
            onDragStopped = { velocity ->
                settleAppBar(
                    scrollBehavior.state,
                    velocity,
                    scrollBehavior.flingAnimationSpec,
                    scrollBehavior.snapAnimationSpec
                )
            }
        )
    } else {
        Modifier
    }

    // Compose a Surface with a TopAppBarLayout content.
    // The surface's background color is animated as specified above.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    Surface(modifier = modifier.then(appBarDragModifier), color = appBarContainerColor) {
        BoxWithConstraints {
            if (maxWidth > maxHeight) {
                val height = LocalDensity.current.run { 64.dp.toPx() }
                TopAppBarLayout(
                    modifier = Modifier
                        .windowInsetsPadding(windowInsets)
                        // clip after padding so we don't show the title over the inset area
                        .clipToBounds(),
                    heightPx = height,
                    navigationIconContentColor = colors.navigationIconContentColor,
                    titleContentColor = colors.titleContentColor,
                    actionIconContentColor = colors.actionIconContentColor,
                    title = titleCollapsed,
                    titleAlpha = 1f,
                    titleVerticalArrangement = Arrangement.Center,
                    titleHorizontalArrangement = Arrangement.Start,
                    titleBottomPadding = 0,
                    hideTitleSemantics = false,
                    navigationIcon = iconCollapsed,
                    actions = actionsRow,
                )
            } else {
                TwoRowsTopAppBar(
                    title = titleExpanded,
                    titleBottomPadding = MediumTitleBottomPadding,
                    smallTitle = titleCollapsed,
                    iconCollapsed = iconCollapsed,
                    iconExpanded = iconExpanded,
                    actions = actions,
                    windowInsets = windowInsets,
                    colors = colors,
                    maxHeight = 64.dp + 216.dp,
                    pinnedHeight = 64.dp,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoRowsTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    titleBottomPadding: Dp,
    smallTitle: @Composable () -> Unit,
    iconCollapsed: @Composable () -> Unit,
    iconExpanded: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    maxHeight: Dp,
    pinnedHeight: Dp,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    if (maxHeight <= pinnedHeight) {
        throw IllegalArgumentException(
            "A TwoRowsTopAppBar max height should be greater than its pinned height"
        )
    }
    val pinnedHeightPx: Float
    val maxHeightPx: Float
    val titleBottomPaddingPx: Int
    LocalDensity.current.run {
        pinnedHeightPx = pinnedHeight.toPx()
        maxHeightPx = maxHeight.toPx()
        titleBottomPaddingPx = titleBottomPadding.roundToPx()
    }

    // Sets the app bar's height offset limit to hide just the bottom title area and keep top title
    // visible when collapsed.
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != pinnedHeightPx - maxHeightPx) {
            scrollBehavior?.state?.heightOffsetLimit = pinnedHeightPx - maxHeightPx
        }
    }

    // Obtain the container Color from the TopAppBarColors using the `collapsedFraction`, as the
    // bottom part of this TwoRowsTopAppBar changes color at the same rate the app bar expands or
    // collapse.
    // This will potentially animate or interpolate a transition between the container color and the
    // container's scrolled color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val appBarContainerColor = containerColor(colorTransitionFraction, colors.containerColor, colors.scrolledContainerColor)

    // Wrap the given actions in a Row.
    val actionsRow = @Composable {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
    val topTitleAlpha = TopTitleAlphaEasing.transform(colorTransitionFraction)
    val bottomTitleAlpha =
        lerp(1f, 0f, FastOutLinearInEasing.transform(colorTransitionFraction / 0.5f))
    // Hide the top row title semantics when its alpha value goes below 0.5 threshold.
    // Hide the bottom row title semantics when the top title semantics are active.
    val hideTopRowSemantics = colorTransitionFraction < 0.5f
    val hideBottomRowSemantics = !hideTopRowSemantics

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier = if (scrollBehavior != null && !scrollBehavior.isPinned) {
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffset + delta
            },
            onDragStopped = { velocity ->
                settleAppBar(
                    scrollBehavior.state,
                    velocity,
                    scrollBehavior.flingAnimationSpec,
                    scrollBehavior.snapAnimationSpec
                )
            }
        )
    } else {
        Modifier
    }

    Surface(modifier = modifier.then(appBarDragModifier), color = appBarContainerColor) {
        Column {
            BackdropLayout(
                bottomTitleAlpha = bottomTitleAlpha,
                iconExpanded = iconExpanded,
                colors = colors,
                hideTopRowSemantics = hideTopRowSemantics,
                titleExpanded = title,
                maxHeightPx = maxHeightPx,
                pinnedHeightPx = pinnedHeightPx,
                scrollBehavior = scrollBehavior
            )
            TopAppBarLayout(
                modifier = Modifier
                    .windowInsetsPadding(windowInsets)
                    // clip after padding so we don't show the title over the inset area
                    .clipToBounds(),
                heightPx = with(LocalDensity.current) { 64.dp.toPx() },
                navigationIconContentColor =
                colors.navigationIconContentColor,
                titleContentColor = colors.titleContentColor,
                actionIconContentColor =
                colors.actionIconContentColor,
                title = smallTitle,
                titleAlpha = topTitleAlpha,
                titleVerticalArrangement = Arrangement.Center,
                titleHorizontalArrangement = Arrangement.Start,
                titleBottomPadding = 0,
                hideTitleSemantics = hideTopRowSemantics,
                navigationIcon = iconCollapsed,
                actions = actionsRow,
            )
        }
    }
}

@Composable
private fun BackdropLayout(
    bottomTitleAlpha: Float,
    iconExpanded: @Composable () -> Unit,
    colors: TopAppBarColors,
    hideTopRowSemantics: Boolean,
    titleExpanded: @Composable () -> Unit,
    maxHeightPx: Float,
    pinnedHeightPx: Float,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    Layout(
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = bottomTitleAlpha)
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .layoutId("navigationIcon")
                        .padding(start = TopAppBarHorizontalPadding)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.navigationIconContentColor,
                        content = iconExpanded
                    )
                }
                Box(
                    Modifier
                        .layoutId("title")
                        .padding(horizontal = TopAppBarHorizontalPadding)
                        .then(if (hideTopRowSemantics) Modifier.clearAndSetSemantics { } else Modifier)
                ) {
                    titleExpanded()
                }
            }
        },
    ) {measurables, constraints ->

        val backdropPlaceable = measurables.first().measure(constraints.copy(maxHeight = maxHeightPx.roundToInt()))
        val layoutHeight =  maxHeightPx - pinnedHeightPx + (scrollBehavior?.state?.heightOffset ?: 0f)

        val scrollOffset = (scrollBehavior?.state?.heightOffset ?: 0f) * 0.6f

        layout(constraints.maxWidth, layoutHeight.roundToInt()) {
            backdropPlaceable.placeRelative(
                0,
                (0 + (scrollOffset).roundToInt()).coerceAtLeast(-64.dp.roundToPx())
            )
        }
    }
}

@Composable
private fun TopAppBarLayout(
    modifier: Modifier,
    heightPx: Float,
    navigationIconContentColor: Color,
    titleContentColor: Color,
    actionIconContentColor: Color,
    title: @Composable () -> Unit,
    titleAlpha: Float,
    titleVerticalArrangement: Arrangement.Vertical,
    titleHorizontalArrangement: Arrangement.Horizontal,
    titleBottomPadding: Int,
    hideTitleSemantics: Boolean,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    Layout(
        {
            Box(
                Modifier
                    .layoutId("navigationIcon")
                    .padding(start = TopAppBarHorizontalPadding)
                    .graphicsLayer(alpha = titleAlpha)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides navigationIconContentColor,
                    content = navigationIcon
                )
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = TopAppBarHorizontalPadding)
                    .then(if (hideTitleSemantics) Modifier.clearAndSetSemantics { } else Modifier)
                    .graphicsLayer(alpha = titleAlpha)
            ) {
                title()
            }
            Box(
                Modifier
                    .layoutId("actionIcons")
                    .padding(end = TopAppBarHorizontalPadding)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides actionIconContentColor,
                    content = actions
                )
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables.fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0))
        val actionIconsPlaceable =
            measurables.fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0))

        val maxTitleWidth = if (constraints.maxWidth == Constraints.Infinity) {
            constraints.maxWidth
        } else {
            (constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width)
                .coerceAtLeast(0)
        }
        val titlePlaceable =
            measurables.fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = maxTitleWidth))

        // Locate the title's baseline.
        val titleBaseline =
            if (titlePlaceable[LastBaseline] != AlignmentLine.Unspecified) {
                titlePlaceable[LastBaseline]
            } else {
                0
            }

        val layoutHeight = if (heightPx.isNaN()) 0 else heightPx.roundToInt()

        layout(constraints.maxWidth, layoutHeight) {
            // Navigation icon
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = (layoutHeight - navigationIconPlaceable.height) / 2
            )

            // Title
            titlePlaceable.placeRelative(
                x = when (titleHorizontalArrangement) {
                    Arrangement.Center -> {
                        var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
                        if (baseX < navigationIconPlaceable.width) {
                            // May happen if the navigation is wider than the actions and the
                            // title is long. In this case, prioritize showing more of the title by
                            // offsetting it to the right.
                            baseX += (navigationIconPlaceable.width - baseX)
                        } else if (baseX + titlePlaceable.width >
                            constraints.maxWidth - actionIconsPlaceable.width
                        ) {
                            // May happen if the actions are wider than the navigation and the title
                            // is long. In this case, offset to the left.
                            baseX += ((constraints.maxWidth - actionIconsPlaceable.width) -
                                    (baseX + titlePlaceable.width))
                        }
                        baseX
                    }

                    Arrangement.End ->
                        constraints.maxWidth - titlePlaceable.width - actionIconsPlaceable.width
                    // Arrangement.Start.
                    // An TopAppBarTitleInset will make sure the title is offset in case the
                    // navigation icon is missing.
                    else -> max(TopAppBarTitleInset.roundToPx(), navigationIconPlaceable.width)
                },
                y = when (titleVerticalArrangement) {
                    Arrangement.Center -> (layoutHeight - titlePlaceable.height) / 2
                    // Apply bottom padding from the title's baseline only when the Arrangement is
                    // "Bottom".
                    Arrangement.Bottom ->
                        if (titleBottomPadding == 0) layoutHeight - titlePlaceable.height
                        else layoutHeight - titlePlaceable.height - max(
                            0,
                            titleBottomPadding - titlePlaceable.height + titleBaseline
                        )
                    // Arrangement.Top
                    else -> 0
                }
            )

            // Action icons
            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = (layoutHeight - actionIconsPlaceable.height) / 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}


// An easing function used to compute the alpha value that is applied to the top title part of a
// Medium or Large app bar.
/*@VisibleForTesting*/
internal val TopTitleAlphaEasing = CubicBezierEasing(.8f, 0f, .8f, .15f)

private val MediumTitleBottomPadding = 24.dp
private val LargeTitleBottomPadding = 28.dp
private val TopAppBarHorizontalPadding = 4.dp

// A title inset when the App-Bar is a Medium or Large one. Also used to size a spacer when the
// navigation icon is missing.
private val TopAppBarTitleInset = 16.dp - TopAppBarHorizontalPadding

private fun containerColor(colorTransitionFraction: Float, containerColor: Color, scrolledContainerColor: Color): Color {
    return lerp(
        containerColor,
        scrolledContainerColor,
        FastOutLinearInEasing.transform(colorTransitionFraction)
    )
}
