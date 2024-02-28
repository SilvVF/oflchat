package io.silv.oflchat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.lerp
import io.silv.oflchat.R
import io.silv.oflchat.ui.theme.OflchatTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object DarkColors {

    val list: List<Int> = listOf(
        R.color.green,
        R.color.pink,
        R.color.orange,
        R.color.orange_alternate,
        R.color.blue,
        R.color.blue_mid,
        R.color.brown,
        R.color.green_mid,
        R.color.pink_high
    )
}

object ConversationItemDefaults {
    @Composable
    fun ProfileIcon(
        modifier: Modifier,
        text: String,
        textStyle: androidx.compose.ui.text.TextStyle,
    ) {
        val id = remember {
            DarkColors.list.random()
        }
        val c = colorResource(id = id)
        Box(modifier = modifier
            .clip(CircleShape)
            .background(c)
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.background,
                style = textStyle
            )
        }
    }
}

@Composable
fun ConversationItem(
    participants: List<String>,
    unreadCountProvider: () -> Int,
    lastReceived: Long,
    lastMessage: String,
    state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState()
) {
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Surface(
                color = animateColorAsState(
                    label = "dismiss-color",
                    targetValue = when (state.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background
                    }
                ).value,
                modifier = Modifier.fillMaxSize()
            ) {
                 when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = "")
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ){
                            Icon(imageVector = Icons.Outlined.Archive, contentDescription = "")
                        }
                    }
                    SwipeToDismissBoxValue.Settled -> Unit
                }
            }
        }
    ) {
        LaunchedEffect(state.currentValue) {
            if (state.currentValue in listOf(SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd)) {
                state.reset()
            }
        }
        Row(
            Modifier
                .clip(
                    RoundedCornerShape(
                        percent = if (state.targetValue != SwipeToDismissBoxValue.Settled) {
                            lerp(0f, 30f, state.progress).roundToInt()
                        } else {
                            0
                        }
                    )
                )
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 8.dp)
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                    role = Role.Button
                ) {

                }
                .padding(12.dp)
                .heightIn(max = 52.dp),
        ) {
            ProfileIcons(
                hasUnread = remember(unreadCountProvider()) {
                    derivedStateOf { unreadCountProvider() >= 1 }
                }.value,
                participants = participants,
                modifier = Modifier
                    .align(Alignment.Top)
                    .fillMaxHeight()
                    .aspectRatio(1f / 1f)
            )
            Box(
                Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .weight(1f),
            ) {
                Text(
                    text = participants.joinToString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                )
                Text(
                    text = lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.78f
                        )
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                )
            }
            DateWithUnreadBadge(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .align(Alignment.Top),
                lastReceived = lastReceived,
                unreadCount = unreadCountProvider,
            )
        }
    }
}

@Composable
fun DateWithUnreadBadge(
    lastReceived: Long,
    unreadCount: () -> Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = remember(lastReceived) {

                val instant = Instant.ofEpochSecond(lastReceived)
                val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

                val today = LocalDateTime.now().toLocalDate().toEpochDay()

                if (today - localDateTime.toLocalDate().toEpochDay() > 7) {
                    val formatter = DateTimeFormatter.ofPattern("MMM d")
                    localDateTime.format(formatter)
                } else {
                    val formatter = DateTimeFormatter.ofPattern("E")
                    localDateTime.format(formatter)
                }
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
            val unread = unreadCount()
            if (unread >= 1) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(16.dp)
                        .aspectRatio(1f)
                ) {
                    Text(
                        text = remember(unread) { unread.toString() },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.background,
                            fontSize = 8.sp
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileIcons(
    hasUnread: Boolean,
    participants: List<String>,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onBackground
    when(participants.size) {
        1 -> {
            Box(
                modifier = modifier
            ) {
                participants.fastForEach {
                    ConversationItemDefaults.ProfileIcon(
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            color = color
                        ),
                        text = it.first().toString().uppercase()
                    )
                }
            }
        }
        2 -> {
            TwoCircleParticipantLayout(
                participants = participants,
                modifier = modifier
            )
        }
        3 -> {
            ThreeCircleParticipantLayout(
                participants = participants,
                modifier = modifier
            )
        }
        4 -> {
            FlowRow(
                modifier,
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                maxItemsInEachRow = 2
            ) {
                participants.fastForEach {
                    ConversationItemDefaults.ProfileIcon(modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f / 1f),
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = color
                        ),
                        text = it.first().toString().uppercase()
                    )
                }
            }
        }
    }
}

@Composable
fun TwoCircleParticipantLayout(
    participants: List<String>,
    modifier: Modifier
) {
    Layout(
        {
            participants.fastForEach {
                ConversationItemDefaults.ProfileIcon(modifier =
                Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                    text = it.first().toString().uppercase())
            }
        },
        modifier = modifier,
    ) {measurables, constraints ->

        val radius = constraints.maxWidth * 0.292893218813

        val diameter = (radius * 2).roundToInt()

        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxWidth = diameter,
                    maxHeight = diameter
                )
            )
        }

        val top = placeables.first()
        val bottom = placeables.last()

        layout(constraints.maxWidth, constraints.maxHeight) {
            top.placeRelative(
                y = 0,
                x = 0
            )
            bottom.placeRelative(
                x = constraints.maxWidth - diameter,
                y = constraints.maxHeight - diameter
            )
        }
    }
}



@Composable
fun ThreeCircleParticipantLayout(
    participants: List<String>,
    modifier: Modifier
) {
    Layout(
        {
            participants.fastForEach {
                ConversationItemDefaults.ProfileIcon(
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    text = it.first().toString().uppercase()
                )
            }
        },
        modifier = modifier,
    ) {measurables, constraints ->

        val radius = constraints.maxHeight / 4


        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    maxWidth = radius * 2,
                    maxHeight = radius * 2,
                    minHeight = 0,
                    minWidth = 0,
                )
            )
        }


        val top = placeables.first()
        val bottomStart = placeables[1]
        val bottomEnd = placeables.last()

        layout(constraints.maxWidth, constraints.maxHeight) {

            top.placeRelative(
                y = (constraints.maxHeight * (1f - 0.6875f) - radius).roundToInt(),
                x = constraints.maxWidth / 2 - radius
            )

            bottomStart.placeRelative(
                x = 0,
                y = constraints.maxHeight - bottomStart.height
            )
            bottomEnd.placeRelative(
                x = constraints.maxWidth - bottomEnd.width,
                y = constraints.maxHeight - bottomEnd.height
            )
        }
    }
}

private data class ConversationItemTestData(
    val participants: List<String>,
    val unreadCountProvider: () -> Int,
    val lastReceived: Long,
    val lastMessage: String
)

@Preview
@Composable
fun ConversationItemPreview() {
    OflchatTheme {

        LazyColumn {
           conversationTestData()
        }
    }
}

fun LazyListScope.conversationTestData() {
    val data =
        buildList {
            repeat(100) {
                val participants =
                    listOf("dafsdf", "dfasdf", "dfakdjfk", "dkjfaksjdf").take(kotlin.random.Random.nextInt(1, 5))
                val unread = listOf(0, 1).random()
                val lastReceived = java.time.LocalDateTime.now()
                    .minusDays(kotlin.random.Random.nextInt(0, 100).toLong())
                    .toEpochSecond(java.time.ZoneOffset.UTC)
                add(
                    ConversationItemTestData(
                        participants,
                        { unread },
                        lastReceived,
                        "dkfjalksdjfkasdfasdfasfsdfasfjsdf;klja;lsdfjkljldfadfasffdsf"
                    )
                )
            }
        }

    items(data) { item ->
        ConversationItem(
            participants = item.participants,
            unreadCountProvider = item.unreadCountProvider,
            lastReceived = item.lastReceived,
            lastMessage = item.lastMessage
        )
    }
}

