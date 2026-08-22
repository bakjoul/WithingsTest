# Image Search App

A small Android application built as part of a technical assessment.

The application allows users to search for images, select multiple images, and view their selection in a dedicated detail screen.

## Features

* 🔎 Search for images
* 💡 Search suggestions
* 🖼️ Display search results in a grid
* 📄 Pagination when scrolling through results
* ☑️ Select and deselect images
* 🧹 Clear the current selection
* 📱 View selected images in a dedicated detail screen
* ▶️ Automatic image scrolling
* 👆 Manual navigation between images
* ⏳ Loading states
* ⚠️ Error handling
* 🔍 Empty state when no results are found
* 💬 Snackbar notifications for pagination errors

## Tech Stack

* **Kotlin**
* **Jetpack Compose** — UI
* **Kotlin Coroutines & Flow** — asynchronous operations and state management
* **Ktor Client** — API communication
* **Coil** — image loading
* **Koin** — dependency injection
* **Navigation 3** — navigation
* **JUnit** — unit testing
* **Mokkery** — mocking
* **Turbine** — Flow testing

## Architecture

The application follows an **MVVM architecture** with a separation between the presentation, domain, and data layers.

```text
UI
 ↓
ViewModel
 ↓
Use Case
 ↓
Repository
 ↓
API
 ↓
Ktor Client
```

## Testing

Unit tests cover the main ViewModel behaviours, including:

* Successful searches
* Search errors
* Empty search queries
* Pagination
* Pagination errors
* Image selection and deselection
* Clearing the selection
* Image loading state on the detail screen

## Running the Project

Open the project in **Android Studio** and run the application on an Android device or emulator.

The project requires a configured API key for the image search API.
