这是一个面向 Android、iOS、Web、桌面、服务器的 Kotlin 多平台项目。

* “/composeApp”适用于将在 Compose Multiplatform 应用程序之间共享的代码。
  它包含几个子文件夹：
  - “commonMain”适用于所有目标的通用代码。
  - 其他文件夹用于 Kotlin 代码，这些代码将仅针对文件夹名称中指示的平台进行编译。
    例如，如果您想将 Apple 的 CoreCrypto 用于 Kotlin 应用的 iOS 部分，
    “iosMain” 将是此类调用的正确文件夹。

* '/iosApp' 包含 iOS 应用程序。即使您与 Compose Multiplatform 共享 UI，
  您的 iOS 应用程序需要此入口点。这也是您应该为项目添加 SwiftUI 代码的地方。

* '/server' 用于 Ktor 服务器应用程序。

* '/shared' 用于将在项目中的所有目标之间共享的代码。
  最重要的子文件夹是 'commonMain'。如果愿意，您也可以在此处将代码添加到特定于平台的文件夹中。


进一步了解[Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

感谢您在公共 Slack 频道中对 Compose/Web 和 Kotlin/Wasm 提供反馈 [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
如果您遇到任何问题，请在[GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

您可以通过运行`:composeApp:wasmJsBrowserDevelopmentRun` Gradle 任务.