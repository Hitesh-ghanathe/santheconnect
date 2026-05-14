# 🛒 SantheConnect

> A native Android app that connects people with Karnataka's local street vendors, weekly *santhe* (village markets), and craftspeople — powered by AI.

Built as a college project to promote local businesses, help users discover nearby vendors, and preserve the culture of traditional Karnataka markets using modern technology.

---

## 📌 About the Project

**SantheConnect** is a community-driven marketplace discovery app built natively for Android using **Kotlin**. The name comes from **"Santhe"** — the Kannada word for the weekly open-air markets held across villages in Karnataka.

The app helps users:
- 🗺️ Find nearby local vendors (food stalls, craftspeople, market sellers) on an interactive map
- 📅 Check which *santhe* markets are open today using a weekly calendar
- ⭐ Read and write community reviews (text, photo, or voice note)
- 🤖 Chat with an AI assistant (**SantheGuide**) that knows about Karnataka's markets and culture
- 📍 Submit new vendor locations via GPS

---

## ✨ Features

### 🏠 Home Screen
- Beautiful banner with search bar
- Category filter chips (Food, Market, Craft, Stay)
- Today's active markets section
- Featured vendors carousel
- AI-powered daily Karnataka fact & personalized travel suggestion

### 🗺️ Interactive Map (Google Maps)
- Shows nearby vendors as custom markers with category icons
- Filter vendors by category
- Tap a marker to view vendor details
- Distance-aware — shows vendors relative to your location

### 📅 Santhe Market Calendar
- Weekly calendar view for all *santhe* across Karnataka
- Auto-highlights today's markets
- Each market card shows village name, specialty goods, and GPS location
- Task planner — add personal tasks for specific dates

### ⭐ Community Review Wall
- Browse all community reviews in a scrollable feed
- Submit reviews as:
  - 📷 Photo with caption
  - 🎙️ Voice note (recorded via mic)
- Voice notes are auto-transcribed to text using **Gemini AI**

### 🤖 AI Chat (SantheGuide)
- Powered by **Google Gemini 2.0 Flash**
- Ask anything about Karnataka markets, local specialties, travel tips
- Contextual, warm responses in a chat interface
- Handles offline/error states gracefully

### 👤 User Profile & Auth
- Firebase Authentication (Email/Password + Google Sign-In)
- User dashboard with profile info
- Splash screen with auto-login check

### 📍 Add New Vendor
- Community members can submit new vendor locations
- Auto-captures GPS coordinates
- Upload photos, set category, add description
- AI auto-generates specialty tags for the vendor

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | XML Layouts + Jetpack Compose (hybrid) |
| **Architecture** | MVVM (ViewModel + LiveData) |
| **Navigation** | Jetpack Navigation Component |
| **Backend/Database** | Firebase Firestore (cloud) + Room (local/offline) |
| **Storage** | Firebase Storage (photos, audio) |
| **Authentication** | Firebase Auth (Email + Google) |
| **Maps** | Google Maps SDK + Play Services Location |
| **AI** | Google Gemini 2.0 Flash (via `generativeai` SDK) |
| **Image Loading** | Coil |
| **Async** | Kotlin Coroutines + Flow |
| **Build** | Gradle (KTS) with KSP |

---

## 📁 Project Structure

```
SantheConnect/
├── android/app/src/main/java/com/hitesh/santheconnect/
│   ├── ai/                     # Gemini AI helper (tags, transcription, suggestions)
│   ├── data/
│   │   ├── model/              # Data classes (Vendor, Santhe, Review, ChatMessage)
│   │   ├── local/              # Room DB, DAOs, Entities, Converters
│   │   └── repository/         # SantheRepository (Firebase + Room)
│   ├── ui/
│   │   ├── auth/               # Login / Sign-up screen
│   │   ├── home/               # Home feed (categories, markets, vendors)
│   │   ├── map/                # Google Maps vendor discovery
│   │   ├── calendar/           # Weekly santhe calendar + tasks
│   │   ├── reviews/            # Community review wall
│   │   ├── chat/               # AI chatbot (SantheGuide)
│   │   ├── vendor/             # Vendor detail + Add vendor screens
│   │   ├── profile/            # User profile dashboard
│   │   └── splash/             # Splash / loading screen
│   ├── utils/                  # Extensions, Location, Network helpers
│   ├── MainActivity.kt
│   └── SantheConnectApp.kt     # Application class
├── android/app/src/main/res/   # Layouts, drawables, navigation, colors, themes
├── docs/blueprint.md           # Project design blueprint
└── .gitignore
```

---

## ⚙️ Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35
- A Firebase project with Firestore, Storage, and Auth enabled
- Google Maps API key
- Gemini API key

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/YOUR_USERNAME/SantheConnect.git
   cd SantheConnect
   ```

2. **Add your API keys**

   Create a `local.properties` file in the **project root** (this file is gitignored):
   ```properties
   sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
   GEMINI_API_KEY=your_gemini_api_key_here
   MAPS_API_KEY=your_maps_api_key_here
   ```

3. **Add Firebase config**

   Download `google-services.json` from your Firebase Console and place it at:
   ```
   android/app/google-services.json
   ```

4. **Set up Firestore collections**

   Create these collections in your Firebase Firestore:
   - `vendors` — with fields: `name`, `category`, `latitude`, `longitude`, `description`, `photoUrls`, `specialtyTags`, `averageRating`, `reviewCount`, `isActive`, `submittedBy`
   - `santhe` — with fields: `villageName`, `dayOfWeek`, `latitude`, `longitude`, `specialtyGoods`, `description`, `isActive`, `organizer`
   - `reviews` — with fields: `vendorId`, `userId`, `text`, `photoUrl`, `audioUrl`, `rating`, `timestamp`

5. **(Optional) Google Sign-In**

   If you want Google Sign-In to work, replace `"YOUR_WEB_CLIENT_ID"` in `AuthActivity.kt` with your OAuth 2.0 Web Client ID from the Firebase Console.

6. **Open in Android Studio and run**
   - Open the root `SantheConnect` folder in Android Studio
   - Let Gradle sync
   - Run on an emulator or physical device (API 26+)

---

## 🔐 Security

All API keys are stored in `local.properties` (gitignored) and injected at build time through `BuildConfig` and manifest placeholders. **No keys are hardcoded in source code.**

| File | Status |
|---|---|
| `local.properties` | ❌ Not tracked (gitignored) |
| `android/local.properties` | ❌ Not tracked (gitignored) |
| `kotlin/new.properties` | ❌ Not tracked (gitignored) |
| `android/app/google-services.json` | ❌ Not tracked (gitignored) |

---

## 📸 Screenshots

> *Screenshots will be added soon.*

<!-- Add your screenshots here like this:
![Home Screen](screenshots/home.png)
![Map View](screenshots/map.png)
![AI Chat](screenshots/chat.png)
-->

---

## 🎨 Design

The UI follows a warm, earthy color palette inspired by Karnataka's culture:

- **Primary**: Ochre `#ECAC2F` — warm and inviting
- **Background**: Cream `#FAF5ED` — soft and natural
- **Accent**: Olive Green `#85B24C` — harmonious contrast
- **Headlines**: *Alegreya* (serif) — elegant, cultural feel
- **Body text**: *PT Sans* (sans-serif) — clean and readable

---

## 🗂️ Firestore Data Model

### Vendor
```json
{
  "name": "Raju's Dosa Cart",
  "category": "FOOD",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "description": "Best masala dosa in the area",
  "photoUrls": ["https://..."],
  "specialtyTags": ["Masala Dosa", "Filter Coffee"],
  "averageRating": 4.5,
  "reviewCount": 23,
  "isActive": true,
  "submittedBy": "Community"
}
```

### Santhe
```json
{
  "villageName": "Tiptur",
  "dayOfWeek": "WEDNESDAY",
  "latitude": 13.2571,
  "longitude": 76.4747,
  "specialtyGoods": ["Coconuts", "Jaggery", "Spices"],
  "description": "Famous weekly market for coconut trade",
  "isActive": true,
  "organizer": "Local Panchayat"
}
```

---

## 🤝 Contributing

This is a student project, but contributions are welcome! Feel free to:
1. Fork the repo
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push and open a Pull Request

---

## 📄 License

This project is for educational purposes. Feel free to use it as reference for your own projects.

---

## 🙏 Acknowledgements

- [Firebase](https://firebase.google.com/) — Backend, Auth, and Storage
- [Google Maps Platform](https://developers.google.com/maps) — Maps SDK
- [Google Gemini AI](https://ai.google.dev/) — AI features (chat, transcription, tags)
- [Coil](https://coil-kt.github.io/coil/) — Image loading
- [Material Design 3](https://m3.material.io/) — UI components and theming

Developed By

Hitesh S Ghanathe
VTU Internship Project – MindMatrix
Santhe-Connect: Local Flavor Discovery App
