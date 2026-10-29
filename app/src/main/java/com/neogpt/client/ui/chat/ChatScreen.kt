package com.neogpt.client.ui.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neogpt.client.AppContainer
import com.neogpt.client.ui.ContainerViewModelFactory
import com.neogpt.client.ui.chat.components.MessageRow
import com.neogpt.client.ui.components.AttachmentSheet
import com.neogpt.client.ui.components.Composer
import com.neogpt.client.ui.components.LoadingDots
import com.neogpt.client.ui.components.NeoTopBar
import com.neogpt.client.util.AttachmentResolver

@Composable
fun ChatScreen(
    container: AppContainer,
    conversationId: String,
    modelLabel: String,
    onOpenDrawer: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onNewChat: () -> Unit,
    onConversationGone: () -> Unit = onNewChat,
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ContainerViewModelFactory { ChatViewModel(container, conversationId) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showAttachSheet by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(4),
    ) { uris ->
        uris.take(4).forEach { uri -> viewModel.addAttachment(AttachmentResolver.fromUri(context, uri)) }
    }
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        uris.take(4).forEach { uri -> viewModel.addAttachment(AttachmentResolver.fromUri(context, uri)) }
    }
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.setInput(listOf(state.input, spoken).filter { it.isNotBlank() }.joinToString(" "))
            }
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type")
        }
        runCatching { voiceLauncher.launch(intent) }
            .onFailure { Toast.makeText(context, "No speech recognizer available", Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(state.conversationRemoved) {
        if (state.conversationRemoved) onConversationGone()
    }

    val lastContentLength = state.messages.lastOrNull()?.content?.length ?: 0

    LaunchedEffect(state.messages.size, lastContentLength) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
        NeoTopBar(
            modelLabel = modelLabel,
            onMenuClick = onOpenDrawer,
            onModelClick = onOpenModelPicker,
            onNewChatClick = onNewChat,
        )

        Box(modifier = Modifier.weight(1f)) {
            if (state.loading) {
                LoadingDots(modifier = Modifier.align(Alignment.Center))
            } else if (state.messages.isEmpty()) {
                Text(
                    text = "Ask anything",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageRow(
                            message = message,
                            onFeedback = { value -> viewModel.setFeedback(message.id, value) },
                            onRegenerate = { viewModel.regenerate(message.id) },
                        )
                    }
                }
            }
        }

        Composer(
            value = state.input,
            onValueChange = viewModel::setInput,
            attachments = state.attachments,
            onAddAttachment = { showAttachSheet = true },
            onRemoveAttachment = viewModel::removeAttachment,
            onSend = viewModel::send,
            onVoiceInput = { startVoiceInput() },
            streaming = state.isStreaming,
            onStop = viewModel::stop,
            sendOnEnter = state.sendOnEnter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }

    if (showAttachSheet) {
        AttachmentSheet(
            onDismiss = { showAttachSheet = false },
            onPickPhotos = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickFiles = { filePicker.launch(arrayOf("*/*")) },
        )
    }
}
