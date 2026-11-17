# DeepSeek-App-Demo

A third-party, open-source Android client that recreates the chat experience of the official DeepSeek App, built from scratch with Jetpack Compose. The project's internal codename is **DeepFish**.

> **Disclaimer:** This is a community project for learning purposes only. It is **not** affiliated with, endorsed by, or connected to DeepSeek. The name "DeepSeek" is used only to describe the API service it connects to. If DeepSeek has any concern about this project, please open an issue and I will respond promptly.

## Features

- Streaming chat with smooth text animation, stop / regenerate / edit / continue, message branches, auto titles.
- Deep thinking with an official-style collapsible thinking panel and in-line search steps.
- Web search (`deepseek_web_search`), link reading (`deepseek_read_url`), source panel and inline citations.
- Attachments via the Files API (images, PDF, Office documents), camera/gallery/file pickers.
- Voice input (ASR) and read-aloud (TTS) with four voices.
- Markdown, syntax highlighting, tables, LaTeX, Mermaid.
- Local SQLite storage, session rename/pin/multi-select/delete, full-text search, ZIP import/export.
- Light/dark themes, font size, language, thinking auto-fold, data management.

## Build

Requirements: JDK 17 and Android SDK 36.

```sh
git clone https://github.com/Sadami3066/DeepSeek-App-Demo.git
cd DeepSeek-App-Demo/android
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

Release signing is optional: without `android/keystore.properties` the build falls back to the debug signing config.

## License

[MIT](LICENSE).
