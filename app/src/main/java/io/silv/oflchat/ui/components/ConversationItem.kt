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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteOutline
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import kotlin.random.Random

private val ConversationItemHeight = 50.dp

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
        val id = remember { DarkColors.list.random(Random(text.hashCode())) }
        val c = colorResource(id = id)

        val char = remember(text) { text.first().uppercase() }

        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(c)
        ) {
            Text(
                text = char,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.background,
                style = textStyle
            )
        }
    }
}


private fun Modifier.conversationItemModifiers(
    progress: Float,
    targetValue: SwipeToDismissBoxValue,
    maxHeight: Dp,
    onClick: () -> Unit,
): Modifier = this.composed {
    this
        .clip(
            RoundedCornerShape(
                percent = if (targetValue != SwipeToDismissBoxValue.Settled) {
                    lerp(0f, 30f, progress).roundToInt()
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
            onClick()
        }
        .padding(12.dp)
        .heightIn(max = maxHeight)
}

@Composable
fun ConversationItem(
    participants: List<String>,
    unreadCountProvider: () -> Int,
    lastMessageProvider: () -> String,
    lastReceivedProvider: () -> Long,
    state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState()
) {
    SwipeToDismissContainer(state = state) {
        Row(
            Modifier
                .conversationItemModifiers(
                    progress = state.progress,
                    targetValue = state.targetValue,
                    maxHeight = ConversationItemHeight,
                    onClick = {

                    }
                )
        ) {
            ProfileIcons(
                unreadCountProvider = unreadCountProvider,
                participants = participants,
                modifier = Modifier
                    .align(Alignment.Top)
                    .fillMaxHeight()
                    .aspectRatio(1f / 1f)
            )
            MessagePreview(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .weight(1f),
                lastMessageProvider = lastMessageProvider,
                participants = participants
            )
            DateWithUnreadBadge(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .align(Alignment.Top),
                lastReceivedProvider = lastReceivedProvider,
                unreadCount = unreadCountProvider,
            )
        }
    }
}

@Composable
private fun MessagePreview(
    participants: List<String>,
    modifier: Modifier = Modifier,
    lastMessageProvider: () -> String
) {

    val participantPreview = remember(participants) {
        participants.take(5).joinToString() +
                if (participants.size > 5) ", ${participants.size - 5} more" else ""
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = participantPreview,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = lastMessageProvider(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.78f
                )
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SwipeToDismissContainer(
    state: SwipeToDismissBoxState,
    content: @Composable RowScope.() -> Unit
) {
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                label = "dismiss-color",
                targetValue = when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background
                }
            )
            Surface(
                color = backgroundColor,
                modifier = Modifier.fillMaxSize()
            ) {
                val dismissBackground = @Composable { icon: ImageVector, description: String ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                }
                when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        dismissBackground(
                            Icons.Rounded.DeleteOutline,
                            stringResource(id = R.string.delete)
                        )
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        dismissBackground(
                            Icons.Rounded.Archive,
                            stringResource(id = R.string.archive)
                        )
                    }

                    SwipeToDismissBoxValue.Settled -> Unit
                }
            }
        }
    ) {
        content()
    }
}

@Composable
fun DateWithUnreadBadge(
    lastReceivedProvider: () -> Long,
    unreadCount: () -> Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End
    ) {
        val lastReceived = lastReceivedProvider()
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
    unreadCountProvider: () -> Int,
    participants: List<String>,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onBackground
    val unreadCount = unreadCountProvider()
    val hasUnread by remember(unreadCount) { derivedStateOf {   unreadCount >= 1 } }

    when(participants.size) {
        1 -> Box(
            modifier = modifier
        ) {
            participants.fastForEach {
                ConversationItemDefaults.ProfileIcon(
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = color
                    ),
                    text = it
                )
            }
        }
        2 -> TwoCircleAlignedMaxFillLayout(
            modifier = modifier,
            circles = participants.map {
                {
                    ConversationItemDefaults.ProfileIcon(
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        text = it
                    )
                }
            }
        )
        3 ->  ThreeCircleAlignedTangentLayout(
            circles =  participants.map {
                {
                    ConversationItemDefaults.ProfileIcon(
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        text = it
                    )
                }
            },
            modifier = modifier
        )
        4 -> FlowRow(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 2
        ) {
            participants.fastForEach {
                ConversationItemDefaults.ProfileIcon(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f / 1f),
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        color = color
                    ),
                    text = it
                )
            }
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
            lastReceivedProvider =  { item.lastReceived },
            lastMessageProvider =  { item.lastMessage }
        )
    }
}

