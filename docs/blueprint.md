# **App Name**: SantheConnect

## Core Features:

- Interactive Local Discovery Map: Display an interactive map showing nearby local vendors (eateries, markets, craftspeople) with custom, culturally relevant category icons. Users can filter by category and the current day, viewing the distance from their location to each point of interest.
- Dynamic Santhe Market Calendar: Present a clear, weekly calendar for Santhe (village markets), automatically highlighting and filtering for markets active on the current day. Market data, including village name, day, GPS, and specialty goods, will be stored in Firestore.
- Community-Powered Review Wall: Enable users to contribute reviews for vendors either as voice notes (captured via browser mic) or photo uploads with optional captions, with all review metadata securely stored in Firestore and media in Firebase Storage.
- AI Voice Review Transcription Tool: Leverage Gemini AI to automatically transcribe submitted voice-note reviews into readable text, displayed seamlessly below the audio player for accessibility and discoverability.
- AI-Generated Specialty Tags and Recommendations Tool: Automatically generate descriptive specialty tags for vendors and provide personalized travel suggestions to users based on their interactions and preferences, powered by AI.
- GPS-Based Vendor Location Submission: Allow community organizers and users to easily add new vendor locations by accurately capturing and storing their GPS coordinates directly through the web app into Firestore.
- Detailed Vendor Information View: Upon selecting a vendor on the map, users can access a detailed card displaying essential information including high-quality photos, AI-generated specialty tags, and community reviews.
- Android Native Implementation: The application will be developed natively for Android using the Kotlin programming language.

## Style Guidelines:

- Primary color: A warm, inviting ochre (#ECAC2F), evoking earthy tones and vibrant cultural richness, providing clear contrast on a light background.
- Background color: A subtle, warm off-white cream (#FAF5ED), maintaining a light and natural feel while complementing the primary hue.
- Accent color: A muted olive green (#85B24C), an analogous hue that provides a harmonious, natural contrast and depth without being overly bold.
- Headlines: 'Alegreya' (humanist serif), chosen for its elegant and intellectual feel, reflecting cultural heritage.
- Body text: 'PT Sans' (humanist sans-serif), selected for its modern readability and a touch of warmth, ideal for clear practical information.
- Custom category icons for Food, Market, Craft, and Stay, designed to be intuitive, culturally relevant, and easily recognizable for new users, supporting quick discovery.
- A map-centric home screen with a clean, card-based design for vendor details, ensuring core functions like map, calendar, and reviews are accessible with minimal interaction for a seamless user experience.
- Subtle, performance-oriented transitions and animations that support quick data loading and user interaction, minimizing delays to provide a fluid browsing experience without distraction.