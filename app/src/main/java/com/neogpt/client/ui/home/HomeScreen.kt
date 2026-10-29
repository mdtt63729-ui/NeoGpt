package com.neogpt.client.ui.home

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neogpt.client.AppContainer
import com.neogpt.client.ui.ContainerViewModelFactory
import com.neogpt.client.ui.components.AttachmentSheet
import com.neogpt.client.ui.components.Composer
import com.neogpt.client.ui.components.NeoTopBar
import com.neogpt.client.ui.theme.NeoTheme
import com.neogpt.client.ui.theme.PillShape
import com.neogpt.client.util.AttachmentResolver
import kotlinx.coroutines.launch

private data class Suggestion(
    val label: String,
    val prompt: String,
    val icon: ImageVector,
)

private val suggestions = listOf(
    Suggestion(
        label = "Create an image or sticker",
        prompt = "Create an image of a calm mountain lake at sunrise.",
        icon = Icons.Outlined.Image,
    ),
    Suggestion(
        label = "Write or edit",
        prompt = "Help me write a short, friendly email to reschedule a meeting.",
        icon = Icons.Outlined.Edit,
    ),
)

private val HomeBackground = Brush.verticalGradient(
    0f to Color(0xFFF6F6F6),
    0.15f to Color(0xFFFCFCFC),
    0.8f to Color(0xFFFCFCFC),
    1f to Color(0xFFF0F0F0),
)

@Composable
fun HomeScreen(
    container: AppContainer,
    modelLabel: String,
    onOpenDrawer: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onConversationCreated: (String) -> Unit,
    onOpenSettings: () -> Unit,
    sharedDraft: String? = null,
    onSharedDraftConsumed: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel(
        factory = ContainerViewModelFactory { HomeViewModel(container) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAttachSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sharedDraft) {
        if (!sharedDraft.isNullOrBlank()) {
            viewModel.setDraft(sharedDraft)
            onSharedDraftConsumed()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(4),
    ) { uris ->
        uris.take(4).forEach { uri ->
            viewModel.addAttachment(AttachmentResolver.fromUri(context, uri))
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        uris.take(4).forEach { uri ->
            viewModel.addAttachment(AttachmentResolver.fromUri(context, uri))
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.setDraft(listOf(state.draft, spoken).filter { it.isNotBlank() }.joinToString(" "))
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

    fun submit() {
        if (state.draft.isBlank() && state.attachments.isEmpty()) return
        scope.launch {
            val id = viewModel.send()
            if (id != null) onConversationCreated(id)
        }
    }

    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Bottom)
                        .union(WindowInsets.ime)
                ),
        ) {
            NeoTopBar(
                modelLabel = modelLabel,
                onMenuClick = onOpenDrawer,
                onModelClick = onOpenModelPicker,
                onNewChatClick = viewModel::clearDraft,
            )

            Spacer(Modifier.weight(1f))

            if (!imeVisible) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    suggestions.forEach { suggestion ->
                        SuggestionRow(
                            suggestion = suggestion,
                            onClick = { viewModel.setDraft(suggestion.prompt) },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            if (!state.providerConfigured) {
                Row(
                    modifier = Modifier
                        .padding(start = 34.dp, bottom = 8.dp)
                        .clip(PillShape)
                        .clickable(onClick = onOpenSettings)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Connect a provider in Settings to chat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Composer(
                value = state.draft,
                onValueChange = viewModel::setDraft,
                attachments = state.attachments,
                onAddAttachment = { showAttachSheet = true },
                onRemoveAttachment = viewModel::removeAttachment,
                onSend = { submit() },
                onVoiceInput = { startVoiceInput() },
                streaming = false,
                onStop = {},
                sendOnEnter = state.sendOnEnter,
                modifier = Modifier.padding(horizontal = 34.dp),
            )

            Spacer(Modifier.height(if (imeVisible) 12.dp else 8.dp))
        }
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

@Composable
private fun SuggestionRow(
    suggestion: Suggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(onClick = onClick)
            .padding(start = 26.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = suggestion.icon,
            contentDescription = null,
            tint = NeoTheme.colors.suggestionTint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(17.dp))
        Text(
            text = suggestion.label,
            fontSize = 17.sp,
            color = NeoTheme.colors.suggestionTint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
