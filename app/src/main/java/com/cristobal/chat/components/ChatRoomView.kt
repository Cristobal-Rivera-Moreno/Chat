package com.cristobal.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristobal.chat.R
import com.cristobal.chat.models.Message
import com.cristobal.chat.utils.AppUtils
import com.cristobal.chat.viewmodels.ChatRoomsViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChatRoomView(
    modifier: Modifier = Modifier,
    viewModel: ChatRoomsViewModel = viewModel()
) {
    val msgList by viewModel.messages.collectAsState()
    val infoVisibility by viewModel.infoDialogVisibility.collectAsState()
    val isJarvisThinking by viewModel.isJarvisThinking.collectAsState()
    BuildView(
        modifier = modifier,
        messages = msgList,
        onSend = { msg ->
            viewModel.sendMessageToAgent(msg)
        },
        onInfoIconClick = viewModel::setInfoDialogVisibility,
        isJarvisThinking = isJarvisThinking
    )
    if (infoVisibility)
        InfoDialog(
            onInfoIconClick = viewModel::setInfoDialogVisibility
        )
}

@Composable
private fun BuildView(
    modifier: Modifier = Modifier,
    messages: List<Message> = emptyList(),
    onSend: (String) -> Unit = {},
    onInfoIconClick: (Boolean) -> Unit = {},
    isJarvisThinking: Boolean = false
) {

    Column(
        modifier = modifier
            .background(Color.Black)
    ) {
        if (!AppUtils.isLandscape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleView(
                modifier = Modifier.align(Alignment.Center),
                title = stringResource(R.string.room_title)
            )

            InfoIcon (
                modifier = Modifier.align(
                    Alignment.CenterEnd
                ),
                onInfoIconClick = onInfoIconClick
            )
        }

        Spacer(modifier = Modifier.size(5.dp))

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            MessageList(
                modifier = Modifier.weight(1f),
                messages = messages
            )

            TypingBox(
                onSend = onSend,
                isJarvisThinking = isJarvisThinking,
                onInfoIconClick = onInfoIconClick
            )
        }
    }
}

@Composable
private fun InfoDialog(
    onInfoIconClick: (Boolean) -> Unit = {}
) {
    Dialog (
        onDismissRequest = {
            onInfoIconClick(false)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.charcoal)
                )
                .border(
                    0.1.dp,
                    colorResource(R.color.primary) ,
                    RoundedCornerShape(
                        8.dp
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.info),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun TitleView(
    modifier: Modifier = Modifier,
    title: String = ""
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center
        ),
        color = Color.White,
        modifier = modifier
            .padding(vertical = 16.dp)
    )
}

@Composable
private fun InfoIcon(
    modifier: Modifier = Modifier,
    onInfoIconClick: (Boolean) -> Unit = {}
) {
    IconButton(
        onClick = {
            onInfoIconClick(true)
        },
        modifier = modifier
            .padding(end = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_chat_info_24),
            contentDescription = "Info",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
private fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<Message> = emptyList()
) {

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(bottom = 8.dp),
        state = listState
    ) {
        items(messages, key = {it.id}) { message ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isMsgMine(message.role)) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            0.1.dp,
                            if (isMsgMine(message.role)) colorResource(R.color.primary) else colorResource(
                                R.color.purple_glow
                            ),
                            RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomEnd = if (isMsgMine(message.role)) 0.dp else 8.dp,
                                bottomStart = if (!isMsgMine(message.role)) 0.dp else 8.dp
                            )
                        )
                        .background(
                            if (isMsgMine(message.role)) colorResource(R.color.charcoal) else colorResource(
                                R.color.deep_purple
                            ),
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)

                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            if (!isMsgMine(message.role))
                                Text(
                                    text = stringResource(R.string.jarvis_name),
                                    color = colorResource(R.color.light_purple),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.Top)
                                )
                        }

                        MarkdownText(
                            markdown = message.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isMsgMine(
                                        message.role
                                    )
                                ) colorResource(R.color.white) else colorResource(R.color.light_purple)
                            ),
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                            isTextSelectable = true
                        )
                        /*Text(
                            text = message.content,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )*/
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                text = formatDate(message.timestamp),
                                color = if (isMsgMine(message.role)) colorResource(R.color.primary).copy(
                                    alpha = 0.3f
                                ) else Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Bottom)
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.padding(4.dp))
        }

    }
}

@Composable
private fun TypingBox(
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit = {},
    isJarvisThinking: Boolean = false,
    onInfoIconClick: (Boolean) -> Unit = {}

) {
    var input by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        if (AppUtils.isLandscape)
            InfoIcon (
                onInfoIconClick = onInfoIconClick
            )

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .height(70.dp)
                .padding(vertical = 10.dp)
                .border(
                    0.2.dp,
                    colorResource(R.color.primary),
                    RoundedCornerShape(8.dp)
                )
                .background(Color(0xFF23272F))
                .fillMaxWidth(0.83f)
        ) {
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    lineHeight = 35.sp,
                ),
                cursorBrush = SolidColor(colorResource(R.color.primary))
            )
            if (input.isEmpty()) {
                Text(
                    text = stringResource(R.string.type_a_message),
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterStart)
                )
            }

        }
        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            {
                if (input.isNotBlank()) {
                    onSend(input)
                    input = ""
                    keyboardController?.hide()
                }
            },
            enabled = !isJarvisThinking,
            modifier = Modifier
                .background(
                    colorResource(R.color.primary),
                    RoundedCornerShape(8.dp)
                )
                .alpha(
                    if (input.isNotBlank() && !isJarvisThinking) 1.0f else 0.5f
                )
        ) {
            if (isJarvisThinking) {
                DisplayLoadingIndicator()
            } else {
                Icon(
                    modifier = Modifier.size(50.dp),
                    painter = painterResource(R.drawable.outline_arrow_right_alt_24),
                    contentDescription = "Send",
                    tint = colorResource(R.color.background_black)
                )
            }
        }
    }
}


private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("hh:mm a")
    return format.format(date)
}

private fun isMsgMine(role: String): Boolean {
    return role == "user"
}

@Composable
private fun DisplayLoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(24.dp),
        color = colorResource(R.color.background_black),
        strokeWidth = 2.dp
    )
}

@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000
)
fun BuildViewPreview() {
    BuildView(
        messages = listOf(
            Message(
                id = "1",
                content = "Hello!",
                role = "me",
                timestamp = System.currentTimeMillis()
            ),
            Message(
                id = "2",
                content = "Hi, how are you?",
                role = "user",
                timestamp = System.currentTimeMillis()
            ),
            Message(
                id = "3",
                content = "I'm good, thanks for asking.",
                role = "Alice",
                timestamp = System.currentTimeMillis()
            )
        ),
        onSend = {}
    )
}

@Composable
@Preview (
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 660,
    heightDp = 440
)
fun BuildViewPreviewForTablets() {
    BuildView(
        messages = listOf(
            Message(
                id = "1",
                content = "Hello!",
                role = "me",
                timestamp = System.currentTimeMillis()
            ),
            Message(
                id = "2",
                content = "Hi, how are you?",
                role = "user",
                timestamp = System.currentTimeMillis()
            ),
            Message(
                id = "3",
                content = "I'm good, thanks for asking.",
                role = "Alice",
                timestamp = System.currentTimeMillis()
            )
        ),
        onSend = {}
    )
}