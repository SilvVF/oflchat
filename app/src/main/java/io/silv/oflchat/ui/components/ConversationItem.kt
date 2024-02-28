package io.silv.oflchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import io.silv.oflchat.R
import io.silv.oflchat.ui.theme.OflchatTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
) {
    Row(
        Modifier
            .padding(horizontal = 12.dp)
            .padding(vertical = 22.dp)
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Box(
            Modifier
                .align(Alignment.Top)) {
            ProfileIcons(
                hasUnread = remember(unreadCountProvider()) {
                    derivedStateOf { unreadCountProvider() >= 1 }
                }.value,
                participants = participants,
            )
        }
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
) {
    val modifier =  Modifier
        .sizeIn(maxHeight = 56.dp, maxWidth = 56.dp)
    val color = MaterialTheme.colorScheme.onBackground
    when(participants.size) {
        1 -> {
            Box(
                modifier = modifier
            ) {
                participants.take(1).fastForEach {
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
                modifier = modifier)
        }
        3 -> {
            ThreeCircleParticipantLayout(
                participants = participants,
                modifier = modifier)
        }
        4 -> {
            Box(modifier = modifier) {
                FlowRow(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 2,
                ) {
                    participants.take(4).fastForEach {
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
}

@Composable
fun TwoCircleParticipantLayout(
    participants: List<String>,
    modifier: Modifier
) {
    Layout(
        {
            participants.take(2).fastForEach {
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

        val height = constraints.maxHeight
        val width = constraints.maxWidth


        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    maxWidth = (width / 1.75).roundToInt(),
                    maxHeight = (height / 1.75).roundToInt()
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
                x = constraints.maxWidth / 2,
                y = (constraints.maxHeight - bottom.height)
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
            participants.take(3).fastForEach {
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

        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    maxWidth = (constraints.maxWidth / 2),
                    maxHeight = (constraints.maxHeight / 2)
                )
            )
        }


        val triangleHeight = sqrt(3f) / 2f * (constraints.maxHeight - placeables.maxOf { it.height / 2 })

        val size = maxOf(constraints.maxWidth, constraints.maxHeight)

        val top = placeables.first()
        val bottomStart = placeables[1]
        val bottomEnd = placeables.last()

        layout(size, size) {

            top.placeRelative(
                y = (size - triangleHeight).roundToInt() - (top.height / 2),
                x = constraints.maxWidth / 2 - top.width / 2
            )

            bottomStart.placeRelative(
                y = size - bottomStart.height,
                x = 0
            )
            bottomEnd.placeRelative(
                x = size - bottomEnd.width,
                y = size - bottomEnd.height
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

