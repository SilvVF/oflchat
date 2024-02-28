package io.silv.oflchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import io.silv.oflchat.ui.theme.OflchatTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

object ConversationItemDefaults {
    @Composable
    fun ProfileIcon(
        modifier: Modifier,
    ) {
        Box(modifier =  modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
        ) {
            Text("D",
                Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun ConversationItem(
    participants: List<String>,
    profileIcon: @Composable (String) -> Unit = {

    },
    unreadCountProvider: () -> Int,
    lastReceived: Long,
    lastMessage: String,
) {
    Row(
        Modifier
            .padding(horizontal = 4.dp)
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .padding(vertical = 4.dp),
    ) {
        Box(Modifier.wrapContentSize().align(Alignment.Top)) {
            ProfileIcons(
                hasUnread = remember(unreadCountProvider()) {
                    derivedStateOf { unreadCountProvider() >= 1 }
                }.value,
                participants = participants,
                profileIcon = profileIcon,
            )
        }
        Column(
            Modifier
                .padding(horizontal = 4.dp)
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = participants.joinToString(),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMessage,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.background.copy(
                        alpha = 0.78f
                    )
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        DateWithUnreadBadge(
            modifier = Modifier
                .padding(end = 12.dp)
                .wrapContentWidth()
                .align(Alignment.Top),
            lastReceived = lastReceived,
            unreadCount = unreadCountProvider,
        )
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
                color = MaterialTheme.colorScheme.background,
            ),
        )
        val unread = unreadCount()
        if (unread >= 1) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .size(16.dp)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
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

@Composable
fun ProfileIcons(
    hasUnread: Boolean,
    profileIcon: @Composable (String) -> Unit,
    participants: List<String>,
) {
    Layout(
        {
            participants.take(3).fastForEach {
                ConversationItemDefaults.ProfileIcon(modifier = Modifier.fillMaxSize())
            }
        },
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .sizeIn(maxHeight = 38.dp, maxWidth = 38.dp)
    ) {measurables, constraints ->

        val iconSize = when(participants.size) {
            1 -> constraints.maxWidth
            2 -> (constraints.maxWidth * 0.6f).roundToInt()
            3 -> (constraints.maxWidth * 0.33f).roundToInt()
            else -> (constraints.maxWidth * 0.25f).roundToInt()
        }

        val placeables = measurables.map {
            it.measure(constraints.copy(minWidth = 0, maxWidth = iconSize, minHeight = 0, maxHeight = iconSize))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            when (participants.size) {
                1 -> {
                    placeables.first().placeRelative(0, 0)
                }
                2 -> {
                    val first = placeables.first()
                    val second = placeables.last()

                    val centerY = constraints.maxHeight / 2
                    val centerX = constraints.maxWidth / 2


                    first.placeRelative(
                        x = (0),
                        y = 0
                    )
                    second.placeRelative(
                        x = (centerX),
                        y = 0 + (first.height * 0.66f).roundToInt()
                    )
                }
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
        val data = remember {
            buildList {
                repeat(100) {
                    val participants =
                        listOf("dafsdf", "dfasdf", "dfakdjfk", "dkjfaksjdf").take(Random.nextInt(1, 3))
                    val unread = listOf(0, 1).random()
                    val lastReceived = LocalDateTime.now()
                        .minusDays(Random.nextInt(0, 100).toLong())
                        .toEpochSecond(ZoneOffset.UTC)
                    add(
                        ConversationItemTestData(
                            participants,
                            { unread },
                            lastReceived,
                            "dkfjalksdjfkajsdf;klja;lsdfjkljldfadfasffdsf"
                        )
                    )
                }
            }
        }
        LazyColumn {
            items(data) { item ->
                ConversationItem(
                    participants = item.participants,
                    unreadCountProvider = item.unreadCountProvider,
                    lastReceived = item.lastReceived,
                    lastMessage = item.lastMessage
                )
            }
        }
    }
}

