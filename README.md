# Marketplace Notification

An Android application that captures sales notifications from marketplace apps (Tokopedia, Shopee, Lazada, Bukalapak, etc.) and forwards them to configurable external systems — so you never miss an order again.

## Features

- **Notification Listener** — Reads notifications from any configured marketplace app using Android's `NotificationListenerService`
- **Configurable Actions** — Forward each captured notification to one or more of the following:
  - 🌐 **API Request** — HTTP POST/GET to a custom endpoint (configurable URL, method, headers, and body template)
  - 📁 **SCP File** — Create a JSON file on a remote server via SCP (SSH)
  - 📧 **Email** — Send an email via SMTP (Gmail, Outlook, etc.)
  - 💬 **WhatsApp** — Send a WhatsApp message via WhatsApp Business API, or open a wa.me link as fallback
- **Read & Acknowledge** — Mark notifications as Read or Acknowledged; swipe left to acknowledge, swipe right to delete
- **Re-trigger** — If a notification is not acknowledged within the configured delay (default 30 min), all enabled actions are triggered again automatically (powered by WorkManager)
- **Clean Material UI** — Cards, status badges (NEW / READ / DONE), snackbars, and swipe gestures

## Screenshots

| Notification List | Config — Actions | Config — Apps |
|---|---|---|
| (notification cards with status) | (add/edit API, SCP, Email, WhatsApp actions) | (monitored apps with re-trigger delay) |

## Architecture

```
app/
├── data/            # Room database: entities, DAOs, repository
├── action/          # Action executors: API, SCP, Email, WhatsApp
├── service/         # MarketplaceNotificationService (NotificationListenerService)
├── worker/          # ReTriggerWorker + ReTriggerScheduler (WorkManager)
└── ui/              # Activities, Fragments, ViewModels, Adapters
```

- **Language**: Kotlin
- **Database**: Room (SQLite)
- **Background Work**: WorkManager (re-trigger every 15 min)
- **Architecture**: MVVM with LiveData + ViewModel
- **UI**: Material Components, RecyclerView, ViewPager2

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26+
- JDK 17

### Build
```bash
./gradlew assembleDebug
```

### Run Tests
```bash
./gradlew test
```

## Adding a Marketplace App to Monitor

1. Open the app → tap the ⚙ FAB or the Settings menu
2. Go to the **Monitored Apps** tab
3. Tap **+** to browse installed apps
4. Select the marketplace app (e.g., Tokopedia)
5. Optionally adjust the re-trigger delay via the edit button on the app row

## Configuring a Forwarding Action

1. Open Settings → **Actions** tab
2. Tap **+**, enter a name, and select the action type:

### API Request
| Field | Description |
|---|---|
| URL | Endpoint to POST/GET to |
| Method | GET / POST / PUT |
| Headers | JSON object, e.g. `{"Authorization":"******"}` |
| Body Template | Optional. Use `{app}`, `{title}`, `{text}`, `{timestamp}`. Leave empty for default JSON payload. |

### SCP File
| Field | Description |
|---|---|
| Host | SSH server hostname |
| Port | SSH port (default 22) |
| Username / Password | SSH credentials |
| Remote Path | Target directory (e.g. `/home/user/notifications`) |

A JSON file named `notification_<id>_<timestamp>.json` will be created per notification.

### Email (SMTP)
| Field | Description |
|---|---|
| SMTP Host | e.g. `smtp.gmail.com` |
| SMTP Port | e.g. `587` |
| Username / Password | SMTP credentials |
| From / To | Sender and recipient addresses |
| Subject | Email subject prefix |

### WhatsApp
| Field | Description |
|---|---|
| Recipient | Phone number with country code, e.g. `+6281234567890` |
| API URL | (Optional) WhatsApp Business API endpoint |
| API Key | (Optional) Authentication token (Bearer) for the WhatsApp Business API |

If no API URL/key is set, the app falls back to opening a `https://wa.me/` link (user must tap to send).

## CI/CD

This project uses **GitHub Actions** to build a debug APK automatically on every push to `main`:

```
.github/workflows/android.yml
```

The workflow:
1. Sets up JDK 17 and Gradle 8.6
2. Generates the Gradle wrapper
3. Runs `./gradlew assembleDebug`
4. Runs `./gradlew test`
5. Uploads the APK and test results as workflow artifacts

## Permissions

| Permission | Purpose |
|---|---|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read notifications from marketplace apps |
| `INTERNET` | Send API requests, emails, and WhatsApp messages |
| `FOREGROUND_SERVICE` | Keep the listener alive |
| `QUERY_ALL_PACKAGES` | Browse installed apps in the selection screen |

## License

MIT
