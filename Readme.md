
# 📞 Amadz – Modern Open Source Calling App

Amadz is a fully modern Android calling application built with Jetpack Compose, Navigation 3, Hilt, and Paging 3.
It is designed as a powerful alternative to the default Phone app with a modular, scalable architecture and production-grade call handling.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.talsk.amadz/)

Or download the latest APK from the [Releases Section](https://github.com/msusman1/Amadz/releases/latest).

![App Screenshot](https://github.com/msusman1/Amadz/blob/master/media/app_screenshot.png)

![App Demo](https://github.com/msusman1/Amadz/blob/master/media/amdaz_calling_app-demo.webm)

---

# ✨ Key Features

## 📲 Core Calling

* Full incoming, outgoing, and ongoing call handling
* Foreground service–based call management
* Full-screen incoming call notifications
* Missed call notifications
* Chronometer support for active calls
* Proximity sensor support during calls
* Multi-SIM support with SIM label display
* DTMF tone playback on dial pad

---

## 📚 Call Logs & History

* Complete call history screen
* Grouping by date with sticky headers
* Pagination using Paging 3
* Rejected call type support
* Add unsaved numbers directly to contacts
* Delete call log entries
* Intelligent refresh strategy
* Block / Unblock directly from history

---

## 👤 Contacts

* Sync favourites with Android starred contacts
* Add / Remove favourites
* Grid-based favourites layout
* Contact detail navigation
* Search by:

    * Name
    * Phone number
    * Email
    * Address
* Proper handling of contacts with multiple numbers

---

## 🚫 Call Blocking

* Block and unblock numbers
* Automatic rejection of blocked incoming calls
* Persistent blocked number storage

---

## 🔍 Smart Search & Dial Pad

* Integrated search + dial pad experience
* State-driven search bar (Collapsed / Expanded / Dial Pad mode)
* Frequently called contacts suggestion
* BackHandler-aware navigation behavior
* Animated FAB and bottom bar transitions

---

## 🔔 Advanced Notification System

* Separate notification channels:

    * High priority for incoming calls
    * Low priority for ongoing calls
* Silent ongoing notifications (no vibration)
* Dynamic notification channel handling
* BroadcastReceiver-based call actions
* Clean ringtone management via dedicated controller

---

# 🏗 Architecture

Amadz now uses a modern Android stack:

* **Jetpack Compose (BOM 2026)**
* **Navigation 3**
* **Hilt Dependency Injection**
* **Paging 3**
* **Kotlin 2.x**
* **KSP (Kotlin Symbol Processing)**
* Foreground service–based call orchestration
* Domain-driven call state management
* Modular separation of:

    * UI
    * Domain
    * Data
    * Call orchestration
    * Audio handling
    * Notification handling

### Major Architectural Improvements

* Replaced legacy call adapter with `CallOrchestrator`
* Introduced session-aware coroutine scope management
* Decoupled ringtone, audio, and notification logic
* Extracted UI effects into dedicated handlers
* Migrated to Version Catalog (TOML)
* Improved lifecycle cleanup to prevent leaks
* Modernized navigation stack (Nav3)

---

# ⚙ Requirements

To enable full functionality:

1. Set **Amadz** as your default Phone app
   **Settings → Apps → Default Apps → Phone App → Amadz**

2. Grant required permissions:

    * Phone
    * Contacts
    * Notifications
    * Foreground service

---

# 🤝 Contributing

Contributions are welcome.

You can help by:

* Reporting bugs
* Improving UI/UX
* Enhancing call features
* Optimizing architecture
* Improving documentation

### Steps

```bash
git checkout -b feature/your-feature
git commit -m "Add your feature"
git push origin feature/your-feature
```

Then open a Pull Request.

---

# 📜 License

Apache License 2.0 © 2023–2026 msusman1

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

---
