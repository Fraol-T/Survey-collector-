# Survey Collector

Survey Collector is a mobile-first survey management and data collection system. It allows administrators to create surveys, customize questions (text, single-choice, multiple-choice), and generate unique shareable links. Respondents can fill out these surveys via a web browser form, and their responses are automatically synchronized back to the Android admin app.

## Project Setup

1. **Clone the Repository:**
   ```bash
   git clone <repository-url>
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Choose **File > Open** and select the cloned project folder `SC`.
3. **Sync Gradle:**
   - Click the **Sync Project with Gradle Files** button in the toolbar, or select **File > Sync Project with Gradle Files**.
   - Wait for Gradle configuration to complete successfully.

## Firebase Configuration

This project uses **Firebase Realtime Database** for data storage and syncing, and **Firebase Hosting** to host the web-based response form.

### Realtime Database Settings
1. Go to your [Firebase Console](https://console.firebase.google.com/).
2. Create or select a Firebase Project.
3. Open **Realtime Database** from the left navigation and create a database (select your preferred region, e.g., Europe West).
4. Note your Database URL (e.g., `https://[YOUR-PROJECT-ID]-default-rtdb.europe-west1.firebasedatabase.app/`).
5. Open `app/src/main/java/com/example/surveycollector/model/remote/ApiClient.java` and paste your Database URL as the `BASE_URL`:
   ```java
   private static final String BASE_URL = "https://[YOUR-PROJECT-ID]-default-rtdb.europe-west1.firebasedatabase.app/";
   ```
6. Set the same base URL in the web form file at `public/survey.html`:
   ```javascript
   const DB = 'https://[YOUR-PROJECT-ID]-default-rtdb.europe-west1.firebasedatabase.app/';
   ```

### Database Security Rules
To allow the mobile app and the web form to write/read database entries, go to the **Rules** tab in the Firebase Realtime Database console and paste:
```json
{
  "rules": {
    ".read": true,
    ".write": true,
    "surveys": {
      ".indexOn": ["isActive"]
    },
    "surveylinks": {
      ".indexOn": ["token", "surveyId"]
    },
    "questions": {
      ".indexOn": ["surveyId"]
    },
    "questionoptions": {
      ".indexOn": ["questionId"]
    },
    "responses": {
      ".indexOn": ["surveyId"]
    }
  }
}
```

### Firebase Hosting
To deploy the web response form to Firebase Hosting:
1. Make sure you have the Firebase CLI installed:
   ```bash
   npm install -g firebase-tools
   ```
2. Log in to your Firebase account:
   ```bash
   firebase login
   ```
3. Initialize hosting in the project directory (optional, configurations are already provided in `firebase.json` and `.firebaserc`):
   ```bash
   firebase use [YOUR-PROJECT-ID]
   ```
4. Deploy the contents of the `public/` directory (which contains `survey.html`):
   ```bash
   firebase deploy --only hosting
   ```

---

## Default Admin Credentials

When the app is started for the first time, it automatically seeds a default administrator account into the local SQLite database.

* **Username:** `admin`
* **Password:** `admin123`

---

## How to Run the App

1. Connect a physical Android device via USB debugging or start an Android Emulator.
2. In Android Studio, select `app` in the run configuration dropdown on the toolbar.
3. Click the green **Run** (Play) button or press `Shift + F10`.
4. Enter the default credentials to log in.

---

## How to Use the System

1. **Create a Survey:**
   - Go to the **Surveys** tab and tap the `+` Floating Action Button (FAB).
   - Enter a title, description, and decide whether user registration (Name and Email) is required.
   - Tap **Save** to save locally and sync with Firebase.
2. **Add Questions:**
   - Tap the options menu on the survey card and select **Manage Questions**.
   - Tap the `+` FAB to add a question. Select its type: `TEXT`, `SINGLE_CHOICE`, or `MULTIPLE_CHOICE`. For choice questions, add at least two options.
   - Tap **Add**. These questions are immediately uploaded to Firebase.
3. **Get & Share Link:**
   - Tap the options menu on the survey card and select **Get Link**.
   - An active link will be generated containing a unique UUID token.
   - Tap **Copy** to copy the URL to the clipboard or tap **Share** to share via the Android system share sheet.
   - The link status can be toggled on/off to pause or resume accepting responses.
4. **Collect Responses:**
   - The respondent opens the link in their web browser (e.g. `https://survaycollector.web.app/survey.html?token=UUID`).
   - If registration is enabled, they enter their details before answering the questions.
   - Upon submitting the form, responses and answers are POSTed directly to Firebase Realtime Database.
5. **Sync & View Responses:**
   - The mobile application listens to network connection changes via a broadcast receiver. When internet is active, it runs `SyncService` in the background to sync any locally created surveys/links and fetch new responses from Firebase.
   - Go to **Dashboard** to see live stats (total surveys, active links, total responses).
   - Tap **View Responses** on any survey card or recent survey list to inspect individual responses, names of respondents (or 'Anonymous'), and their question-by-question answers.

---

## Architecture Overview

This application is built around standard Android Architecture Components, following the MVVM pattern and Repository pattern.

* **View (UI Layer):** Fragments (`SurveyListFragment`, `DashboardFragment`, `ProfileFragment`, `SurveyLinkFragment`) and Activities (`LoginActivity`, `MainActivity`, `QuestionEditorActivity`, `ResponseViewerActivity`) observing LiveData.
* **ViewModel:** Holds UI state and delegates operations to repositories (`SurveyViewModel`, `DashboardViewModel`, `QuestionViewModel`, `ResponseViewModel`, `SurveyLinkViewModel`).
* **Model (Repository & Storage):**
  * **Room (SQLite):** Acts as the single source of truth for the local database (`AppDatabase`). Includes DAOs for Admins, Surveys, Questions, Responses, and Links.
  * **Retrofit (Cloud API):** Synchronizes database records to Firebase Realtime Database via REST endpoints.
  * **SyncService:** Background sync service that uploads offline responses and updates when online.
  * **SurveyContentProvider:** Exposes survey counts/details to external applications.

---

## Third-Party Libraries

The following dependencies are used in the application (defined in `gradle/libs.versions.toml`):

* **Android Room Persistence Library** (version `2.6.1`)
* **Retrofit HTTP Client** (version `2.9.0`)
* **Retrofit Gson Converter** (version `2.9.0`)
* **Android Lifecycle LiveData & ViewModel** (version `2.7.0`)
* **Google Material Components** (version `1.11.0`)
* **RecyclerView** (version `1.3.2`)
* **CardView** (version `1.0.0`)
* **Android Navigation Components** (version `2.7.7`)
