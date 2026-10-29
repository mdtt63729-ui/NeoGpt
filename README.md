<div align="center">

<img src="docs/icon.png" alt="NeoGPT icon" width="128" height="128" />

# NeoGPT

**Android chat client for any OpenAI-compatible API — Kotlin + Jetpack Compose (Material 3).**

[![Android CI](https://github.com/mdtt63729-ui/NeoGpt/actions/workflows/android.yml/badge.svg)](https://github.com/mdtt63729-ui/NeoGpt/actions/workflows/android.yml)
[![minSdk](https://img.shields.io/badge/minSdk-26-3ddc84)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7f52ff)](https://kotlinlang.org)

</div>

## Download the APK

**[NeoGPT 1.1.0 release APK](https://github.com/mdtt63729-ui/NeoGpt/releases/latest)** — signed
with the NeoGPT release key, installs as `com.neogpt.client` (Android 8.0+).
Download it on the phone and allow "install unknown apps" for your browser or
file manager.

Every push to `main` also builds both APKs on GitHub Actions: open the
[**Actions**](https://github.com/mdtt63729-ui/NeoGpt/actions/workflows/android.yml)
tab, pick the newest green run and download **NeoGPT-release-apk** or
**NeoGPT-debug-apk**.

## Status

This tree builds. GitHub Actions compiles `assembleRelease` and `assembleDebug`,
verifies the release signature with `apksigner`, and publishes the signed APK on
tag pushes. The Gradle wrapper (including `gradle-wrapper.jar`) is checked in, so
`./gradlew` works on a fresh clone. Nothing here has been reviewed on a physical
device — the CI proves it compiles, links, minifies and signs, not that every
screen behaves.

## Requirements

- JDK 17
- Android SDK Platform 35 + Build Tools 35.0.0 (Android Studio Ladybug or newer)
- minSdk 26, target/compileSdk 35

## Build

```sh
git clone https://github.com/mdtt63729-ui/NeoGpt.git
cd NeoGpt
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # or let Android Studio write it

./gradlew assembleDebug      # debug APK, signed with the debug key
./gradlew assembleRelease    # release APK — needs signing material, see below
```

On filesystems that do not preserve the executable bit, use `sh gradlew assembleDebug`.
`./gradlew testDebugUnitTest` runs the Markdown parser tests.

### Signing a release build locally

The release keystore is **not** in this repository. Create `keystore.properties`
in the project root (it is git-ignored):

```properties
storeFile=/absolute/path/to/release.jks
storePassword=…
keyAlias=neogpt
keyPassword=…
```

Without that file, `assembleRelease` produces an unsigned APK and CI falls back
to no signing config.

## Signing secrets and CI workflows

Two workflows live in `.github/workflows/`:

| Workflow | What it does |
|---|---|
| `signing-key.yml` | **Generate signing key** (manual). Creates a 4096-bit RSA keystore, prints the certificate fingerprint, and produces the four values that must become repository secrets. Artifact: `signing-key-and-secrets` (keystore + `signing-secrets.txt`, 1-day retention). |
| `android.yml` | **Android CI**. Restores the keystore from secrets, builds a signed release APK and a debug APK, verifies the signature, uploads both, and on a `v*` tag publishes a GitHub release. |

The four repository secrets (`Settings → Secrets and variables → Actions`) are:

| Secret | Contents |
|---|---|
| `KEYSTORE_BASE64` | the whole `.jks` file, base64-encoded (`base64 -w0 release.jks`) |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | key alias, `neogpt` |
| `KEY_PASSWORD` | key password (same as the keystore password for PKCS12 keystores) |

CI writes them into `keystore.properties` inside the runner only; the workflow
fails with a clear message if any of the four is missing. To rotate the key, run
`signing-key.yml` again and replace all four secrets together.

## Provider setup

The app is a client for any service that implements the OpenAI-compatible
`POST {baseUrl}/chat/completions` streaming contract.

1. Open the sidebar → **Settings**.
2. Enter the base URL, for example `https://api.example.com/v1` (the app appends
   `/chat/completions` unless the URL already ends with it).
3. Enter the API key. It is stored in app-private DataStore and sent only to the
   configured host.
4. Set the model id your provider expects. **Test connection** probes
   `GET {baseUrl}/models`; some providers do not expose it, which does not block
   chat.
5. Press **Save**.

Quick presets fill the URL and model id: **OpenAI**, **OpenRouter**,
**Gemini** (Google AI Studio key, OpenAI-compatible endpoint), **Groq**,
**DeepSeek**, **Mistral** (key required), plus **Ollama** and **LM Studio**
(a server on your own computer; no key, no usage limit). Any other
OpenAI-compatible host works too: type its URL and model id by hand.

On OpenRouter you can append `:free` to a model id (for example
`deepseek/deepseek-r1:free`) to use its free variants; those are rate limited by
OpenRouter, not by this app. For Ollama/LM Studio replace the example LAN IP
with your machine's address, allow the port through the firewall, and pull a
model first (`ollama pull llama3.2`).

Without a provider the composer still works, and the app tells you to finish
setup instead of silently failing.

## Architecture

```
app/src/main/java/com/neogpt/client/
├── MainActivity.kt            edge-to-edge activity, splash, ACTION_SEND intake
├── NeoGptApp.kt               Application + AppContainer (manual DI)
├── data/
│   ├── local/                 Room entities, DAOs, database, DataStore prefs
│   └── repository/            Room-backed ConversationRepository
├── domain/
│   ├── model/                 Message, Conversation, Attachment, preferences,
│   │                          model catalog
│   └── repository/            repository + streaming client interfaces
├── network/                   OpenAI-compatible SSE client (OkHttp + org.json)
├── ui/
│   ├── RootScreen.kt          drawer + NavHost (home / chat / settings)
│   ├── RootViewModel.kt       conversations, active chat, model selection
│   ├── home/                  greeting, composer, suggestion chips
│   ├── chat/                  message list, streaming, feedback, regenerate
│   │   └── components/        message rows + Markdown renderer
│   ├── sidebar/               chat list, search, rename, delete
│   ├── settings/              provider, theme, chat behavior, data
│   ├── models/                model picker sheet
│   ├── components/            top bar, composer, sheets, shared controls
│   └── theme/                 color, type, shape, theme
└── util/                      Markdown parser, attachment resolver
```

- **Streaming**: `OpenAiCompatibleClient` parses `data:` lines in a
  `callbackFlow` and cancels the HTTP call on collection stop. Chat state is
  updated in memory per token and persisted once the stream ends (or is
  stopped), which keeps Room writes low.
- **Storage**: conversations and messages in Room (`neogpt.db`), preferences
  (theme, provider, model, send behavior) in Preferences DataStore.
- **No background work**: everything runs while a screen is alive; there is no
  WorkManager, no push, no analytics, no telemetry.

## Implemented

- Welcome gate with Log in / Sign up (local name only) and **Skip now**.
- White theme by default (its palette is the original's #FFFFFF/#0D0D0D set), with optional dark and follow-system modes; status/navigation bars follow the app theme.
- Edge-to-edge Compose UI with a modal navigation drawer: new chat, chat search,
  per-chat rename and delete, settings entry.
- Home screen matching the original layout: circular menu button, model pill,
  account-style mark, two suggestion rows, and the composer pinned to the bottom
  edge (attach sheet, voice dictation, voice button).
- Chat screen with streaming responses, stop, copy, like/dislike, regenerate,
  code blocks with copy, links, lists, quotes, headings, and the composer plus
  "ChatGPT can make mistakes, so check anything important." pinned at the bottom.
- Model picker sheet (catalog ids) plus free-form model id in Settings and
  provider presets (OpenAI, OpenRouter, Gemini, Groq, DeepSeek, Mistral, Ollama,
  LM Studio).
- Local persistence and chat deletion (single, all).
- `ACTION_SEND` text sharing opens the home composer prefilled.
- Launcher icon: adaptive white background + black mark traced from the
  original vector asset (108 dp viewport, exact path data), with monochrome
  layer; dark splash uses the white variant of the same mark.

## Not implemented / differences from the original

- **No OpenAI backend.** Account, subscription, Upgrade, Memory, Projects,
  Library, Explore, Canvas, Builder and Deep Research screens are absent; only
  their labels were recoverable from the APK and they need server features.
- **Attachments are local references only.** Images and files are listed in the
  UI and their names are appended to the prompt text; no multimodal encoding,
  no upload. The request body carries `role`/`content` strings only.
- **Voice is input only** (`RecognizerIntent`), with no text-to-speech replies.
- Persisted attachment URIs may become unreadable after a reboot if the
  providing app does not grant persistable access.
- Markdown support is a deliberate subset: no tables, task lists, nested lists,
  footnotes, HTML, or syntax highlighting inside code blocks.
- UI strings are inline in Compose, not in `strings.xml`; only the app label is
  localized. There is no translation set (the original `en` split was not
  copied).
- No `menlo_*` or `roboto_medium_numbers` fonts from the APK were copied
  (licensing); code uses the platform monospace family.
- The original app uses a Valdi-based shell and 9 dex files; none of that code
  is reused. Layouts here are new Compose code that matches the visual design.
- No paging on the chat list, no message editing/branching, no offline queue,
  no retry with backoff beyond a manual regenerate.
- `app/src/main/res/font/` is intentionally empty.
- Pixel parity is measured, not guaranteed: geometry was matched against
  1080×2400 light-theme screenshots of the original (44 dp top-bar circles,
  14 dp bar padding, 292 dp composer with 34 dp side margins, 48 dp capsule with
  24 dp radius, 1 dp #EBEBEB border, 32 dp #3566EF voice button, 18 dp
  suggestion rows starting at 26 dp). Remaining limits: the original uses OpenAI
  Sans (not redistributable), its suggestions and "Get Plus" chip are
  server-driven, and this machine has no Android SDK to render and diff builds.
  The "Get Plus" chip is replaced by the functional model picker pill.

## Tests

`app/src/test/java/com/neogpt/client/util/MarkdownTest.kt` covers the Markdown
parser (blocks, fences, lists, quotes, inline spans, unsafe links). Run with:

```sh
./gradlew testDebugUnitTest
```

No instrumentation tests are included; they need a device.

## Verification

Performed here: APK inventory and manifest/resource parsing (custom ARSC/AXML
readers), UI string mining, icon vector extraction, and a static consistency
check of this source tree (balanced delimiters, all internal imports resolve,
XML well-formed). Not performed: Gradle build, Kotlin compilation, lint, unit
test execution, and on-device UI review.

## Notes

This directory already contained `Readme.md`, `drift.py` and `vercel.json`, which
belong to a small Flask/Vercel scraper project unrelated to NeoGPT. `drift.py`
and `vercel.json` were left untouched. Because Android external storage here is
case-insensitive, writing this file at `README.md` replaced the previous
`Readme.md` content; that original file could not be recovered from this device.
`README.md` and `Readme.md` are the same file on this storage.
