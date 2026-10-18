# 如何更改專案名稱

這個專案把「給人看的名稱」、「Gradle 專案名」和「Kotlin 套件名」分開存放。視窗標題、網頁標題與 About 讀的是顯示名稱。改名請用 `./configure.sh proj_name`，不要只改其中一個檔案。

## 三種名稱

| 用途 | 寫在哪裡 | 誰讀它 |
| --- | --- | --- |
| 顯示名稱 | `gradle.properties` 的 `app.displayName`，以及 `AppVersion.APP_NAME` | 視窗標題、網頁標題、About 面板 |
| Gradle 專案名 | `gradle.properties` 的 `app.rootName` | `settings.gradle.kts` 的 `rootProject.name`、JS 輸出模組名、zip 檔名 |
| 套件名 | `gradle.properties` 的 `app.group` | 原始碼目錄、desktop executable 的 `mainClass` |

目前顯示名稱是 kool_study。`com.neojou.tools` 是共用工具套件，不是產品套件，改名時不會動到它。

顯示名稱與程式裡的常數必須相同。常數在：

`koolApp/src/commonMain/kotlin/com/neojou/koolstudy/AppVersion.kt`

```kotlin
const val APP_NAME: String = "kool_study" // configure:app.displayName
```

行尾的 `configure:app.displayName` 是 `configure.sh` 的標記。腳本只改 Kotlin 原始碼裡這一行的字串，而且必須正好一處。上面的程式片段是說明用的範例，腳本不會把它當成第二處標記。

## 畫面上的讀取位置

這些地方都讀 `AppVersion.APP_NAME`，不各自寫死名稱。視窗與分頁標題用的是 `appTitle`，也就是 `APP_NAME` 後面空一格再接版號。

- Desktop 視窗標題：`koolApp/src/desktopMain/kotlin/com/neojou/koolstudy/Main.kt` 的 `KoolConfigJvm(windowTitle = appTitle)`
- About 第一行：`koolApp/src/commonMain/kotlin/com/neojou/koolstudy/AppUi.kt` 的 About 面板
- JS 分頁標題：`koolApp/src/jsMain/kotlin/com/neojou/koolstudy/JsMain.kt` 在啟動時設定 `document.title = appTitle`
- 靜態網頁標題：`koolApp/src/jsMain/resources/index.html` 的 `<title>`。這一行不是 Kotlin，所以 `proj_name` 會另外改它。

## macOS 選單

用 `./gradlew :koolApp:runDesktop` 啟動時，程式還沒包成 `.app`。視窗由 GLFW（Cocoa）建立，不是 AWT 視窗。

kool 在 macOS 上會把 AWT 設成 headless，所以沒有 `java.awt.Desktop.setAboutHandler`。系統選單的 About 是 macOS 標準面板，不是 app 裡的 About。可靠的 About 是視窗左上角的 About 按鈕，第一行是專案名稱，第二行是版號。

`koolApp/build.gradle.kts` 在 macOS 上為 desktop executable 加上這三個 JVM 參數，值來自 `app.displayName`（Dock 那兩個）或固定旗標：

- `-XstartOnFirstThread`。GLFW 必須跑在 macOS 主執行緒。
- `-Dapple.awt.application.name`
- `-Xdock:name`

後兩個是給 AWT 用的。視窗是 GLFW 時，系統選單名稱可能仍顯示 `java`，這兩個參數可能無效。請在 Mac 上實際確認。選單名稱在 JVM 啟動時就決定，所以若要生效，必須放在啟動參數。

目前沒有 `nativeDistributions`，也沒有 dmg 或 Dock 名稱設定。打包檔名用沒有空白的 `app.rootName`，產物是 `desktopDistZip`。以後若要 dmg，要另外用 jpackage。

## 用指令改名稱

```bash
./configure.sh proj_name "Stock Viewer"
./configure.sh proj_name MyApp
```

名稱可以含空白。不可包含 `"`、`\`、`$`、`#`、`=` 或換行。

腳本會：

- 把 `app.displayName` 與 `APP_NAME` 那一行改成新名稱
- 把 `index.html` 的 `<title>` 改成新名稱
- 把說明文件裡的舊顯示名稱換成新名稱
- 若名稱含英文或數字，另外產生 Gradle 專案名與套件最後一段。例如 Stock Viewer 變成 Gradle 名稱 StockViewer、套件最後一段 stockviewer。組織前綴保留，只換最後一段，並搬移對應的原始碼目錄
- 沒有英數內容的名稱（例如純中文）只改顯示字串，Gradle 專案名與套件維持不變

這個指令不會重新命名所在的資料夾。目錄名稱請在複製模板時自己取。

Kotlin、kool 與函式庫版號不在這裡改。產品版號見 `How-To-Change_Version_number.md`。
