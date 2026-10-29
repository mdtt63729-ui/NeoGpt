package com.neogpt.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.neogpt.client.ui.RootScreen
import com.neogpt.client.ui.theme.NeoGptTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NeoGptApp
        handleShare(app, intent)

        setContent {
            NeoGptTheme(container = app.container) {
                RootScreen(container = app.container)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(application as NeoGptApp, intent)
    }

    private fun handleShare(app: NeoGptApp, intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (!text.isNullOrEmpty()) {
            app.container.shareInbox.trySend(text)
        }
    }
}
