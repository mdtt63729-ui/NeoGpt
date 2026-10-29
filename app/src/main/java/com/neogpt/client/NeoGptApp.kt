package com.neogpt.client

import android.app.Application
import android.content.Context
import com.neogpt.client.data.local.AppDatabase
import com.neogpt.client.data.local.PreferencesRepository
import com.neogpt.client.data.repository.RoomConversationRepository
import com.neogpt.client.domain.repository.ChatCompletionClient
import com.neogpt.client.domain.repository.ConversationRepository
import com.neogpt.client.network.OpenAiCompatibleClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val preferences: PreferencesRepository = PreferencesRepository(context)
    val conversationRepository: ConversationRepository =
        RoomConversationRepository(AppDatabase.get(context))
    val chatClient: ChatCompletionClient = OpenAiCompatibleClient()

    /**
     * Text handed to the app through ACTION_SEND. Buffered until the UI drains it.
     */
    val shareInbox: Channel<String> = Channel(Channel.BUFFERED)
}

class NeoGptApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch { container.preferences.migrateToDefaultLightTheme() }
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as NeoGptApp).container
