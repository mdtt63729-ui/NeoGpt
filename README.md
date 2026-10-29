<div align="center">

<img src="docs/icon.png" alt="NeoGPT icon" width="128" height="128" />

# NeoGPT

**A native Android chat client for any OpenAI-compatible API — Kotlin + Jetpack Compose (Material 3).**

[![Android CI](https://github.com/mdtt63729-ui/NeoGpt/actions/workflows/android.yml/badge.svg)](https://github.com/mdtt63729-ui/NeoGpt/actions/workflows/android.yml)
[![minSdk](https://img.shields.io/badge/minSdk-26-3ddc84)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7f52ff)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-not%20set-lightgrey)](#disclaimer)

</div>

## What this is

NeoGPT is a self-contained Android client for services that implement the
OpenAI-compatible `POST {baseUrl}/chat/completions` streaming contract. Point it
at your own endpoint (Ollama, LM Studio, vLLM, OpenRouter, Groq, a private
gateway — anything that speaks that API), paste a key, pick a model id, and chat.

The interface is a source-only recreation of the ChatGPT Android look: dark-first
palette, modal navigation drawer with chat history, streaming answers, Markdown
rendering, model picker, and a settings screen. It is an independent client — it
contains no OpenAI code, does not talk to OpenAI's account backend, and is not
affiliated with or endorsed by OpenAI. "ChatGPT" is a trademark of OpenAI.

- Package id: `com.neogpt.client`
- Project name: `NeoGPT`
- App label: `ChatGPT` (edit `app/src/main/res/values/strings.xml` to change it)

## Download the APK

**Latest build:** [NeoGPT-1.0.0-debug.apk](https://github.com/mdtt63729-ui/NeoGpt/releases/download/v1.0.0/NeoGPT-1.0.0-debug.apk)
from the [v1.0.0 release](https://github.com/mdtt63729-ui/NeoGpt/releases/latest) —
download it on the phone, then allow "install unknown apps" for your browser or
file manager.

Every push to `main` also builds a fresh APK on GitHub Actions: open the
[**Actions**](https://github.com/mdtt63729-ui/NeoGpt/actions/workflows/android.yml)
tab, pick the newest green **Android CI** run, and download the
**NeoGPT-debug-apk** artifact.

The APK is signed with the standard debug key, so it installs beside any other
app but is not suitable for the Play Store.

## Build locally

Requirements: JDK 17 and the Android SDK (Platform 35, Build Tools 35.0.0).
The Gradle wrapper is checked in, so no separate Gradle install is needed.

```sh
git clone https://github.com/mdtt63729-ui/NeoGpt.git
cd NeoGpt

# Point Gradle at your SDK (Android Studio writes this for you)
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

On filesystems that do not keep the executable bit, use `sh gradlew assembleDebug`.
`./gradlew testDebugUnitTest` runs the Markdown parser tests.

## First run — provider setup

1. Open the drawer → **Settings**.
2. **Base URL** — e.g. `https://api.example.com/v1`. The app appends
   `/chat/completions` unless the URL already ends with it.
3. **API key** — stored in app-private DataStore, sent only to the host you configured.
4. **Model** — the model id your provider expects (`gpt-4o-mini`, `llama3.1:8b`, …).
5. **Test connection** probes `GET {baseUrl}/models`. Some providers do not expose
   it; that does not block chat.
6. **Save**.

Without a provider the composer still works and the app tells you to finish setup
instead of failing silently.

## Architecture

```
app/src/main/java/com/neogpt/client/
├── MainActivity.kt            edge-to-edge activity, splash, ACTION_SEND intake
├── NeoGptApp.kt               Application + AppContainer (manual DI)
├── data/
│   ├── local/                 Room entities, DAOs, database, DataStore prefs
│   └── repository/            Room-backed ConversationRepository
├── domain/
│   ├── model/                 Message, Conversation, Attachment, preferences, catalog
│   └── repository/            repository + streaming client interfaces
├── network/                   OpenAI-compatible SSE client (OkHttp + org.json)
├── ui/
│   ├── RootScreen.kt          drawer + NavHost (home / chat / settings)
│   ├── RootViewModel.kt       conversations, active chat, model selection
│   ├── home/                  greeting, composer, suggestion chips
│   ├── chat/                  message list, streaming, feedback, regenerate
│   │   └── components/        message rows + Markdown renderer
│   ├── sidebar/               chat list, search, rename, delete
│   ├── settings/              provider, theme, chat behaviour, data
│   ├── models/                model picker sheet
│   ├── components/            top bar, composer, sheets, shared controls
│   └── theme/                 colour, type, shape, theme
└── util/                      Markdown parser, attachment resolver
```

- **Streaming** — `OpenAiCompatibleClient` parses `data:` lines in a `callbackFlow`
  and cancels the HTTP call when collection stops. Chat state updates in memory
  per token and is persisted once when the stream ends or is stopped.
- **Storage** — conversations and messages in Room (`neogpt.db`); theme, provider,
  model and send behaviour in Preferences DataStore.
- **No background work** — no WorkManager, no push, no analytics, no telemetry.

## Implemented

- Dark-first palette with a light theme and system/light/dark switch.
- Edge-to-edge Compose UI, modal drawer: new chat, search, rename, delete, settings.
- Home screen with greeting, rounded composer, attach sheet (photos/files), voice
  dictation via the system recogniser, suggestion chips.
- Chat screen with streaming responses, stop, copy, like/dislike, regenerate,
  and code blocks with copy, links, lists, quotes, headings.
- Model picker sheet plus a free-form model id in Settings.
- Local persistence; delete one chat or all chats.
- `ACTION_SEND` text sharing opens the composer prefilled.
- **Launcher icon** — a NeoGPT mark (rounded "N") on an indigo→violet gradient:
  adaptive icon with monochrome layer for Android 13+ themed icons, plus PNG
  mipmaps from mdpi to xxxhdpi and a round variant. Master art lives in
  `docs/icon.png`.

## Known limits

- **No OpenAI backend.** Account, subscription, Upgrade, Memory, Projects, Library,
  Explore, Canvas, Builder and Deep Research screens are absent — they need server
  features this client deliberately does not call.
- **Attachments are local references only.** File and image names are listed in the
  UI and appended to the prompt text; there is no multimodal encoding or upload.
- **Voice is input only** (`RecognizerIntent`); no text-to-speech replies.
- Persisted attachment URIs may become unreadable after a reboot if the providing
  app does not grant persistable access.
- Markdown is a deliberate subset: no tables, task lists, nested lists, footnotes,
  HTML, or syntax highlighting inside code blocks.
- UI strings are inline in Compose; only the app label is in `strings.xml`.
- No paging on the chat list, no message editing/branching, no offline queue, no
  retry with backoff beyond manual regenerate.

## Project layout notes

```
.github/workflows/android.yml   CI: builds the debug APK and uploads it as an artifact
app/src/main/res/mipmap-*/      launcher icon PNGs (mdpi … xxxhdpi) + round variant
app/src/main/res/mipmap-anydpi-v26/  adaptive icon (background + foreground + monochrome)
docs/icon.png                   icon master used in this README
gradle/wrapper/                 checked-in wrapper, so `./gradlew` works out of the box
```

## Disclaimer

Unofficial, unaffiliated project. "ChatGPT" and the OpenAI name are trademarks of
OpenAI; this repository ships original code and original artwork only. Use it with
providers and keys you are entitled to use.
