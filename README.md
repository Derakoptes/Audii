<p align="center">
  <img src="https://github.com/user-attachments/assets/f9cfda6f-afbd-437e-97af-a28d9e893cf3" alt="Audii Icon" width="150" height="150" />
</p>

<h1 align="center">Audii 📚🎧</h1>

<p align="center">
  An open-source Android audiobook player focused on seamless offline playback.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin-blue" alt="Language">
  <img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="License">
</p>

---

## Features

* 🎵 **Local Playback**: Seamless support for popular audiobook formats (MP3, M4B, etc.)
* 📁 **File Management**: Organize audiobooks into collections
* 📌 **Progress Tracking**: Automatically saves your listening position
* 🔖 **Bookmarks**: Mark and return to key points in any audiobook
* 🚗 **Android Auto Support**: Listen while on the road
* 📦 **Offline-First**: Fully usable without an internet connection
* 🏠 **Built with**:

  * Kotlin
  * Room Database
  * Jetpack Components

---

## Installation

Clone the repo and build with Android Studio:

```bash
git clone https://github.com/derakoptes/audii.git
```

Open the project in **Android Studio** and run on your preferred device or emulator.

---

## How to Use

Audii offers flexible ways to add your audiobooks:

1.  **From a Single File:**
    *   Simply select an individual audiobook file (e.g., `.mp3`, `.m4b`). The cover image will be extracted from the embedded metadata if available.

2.  **From a Folder (Single Audiobook with Chapters):**
    *   Organize chapters of a single audiobook into a dedicated folder.
    *   **Important for proper parsing:** Name your chapter files sequentially, for example: `Chapter 1.mp3`, `Chapter 2.mp3`, etc.
    *   Place the cover image for the audiobook directly inside this folder (e.g., `cover.jpg`). If no image is found in the folder, Audii will attempt to use the embedded image from the first chapter file.

3.  **From a Folder (Multiple Audiobooks):**
    *   You can select a folder that contains multiple audiobooks. Each audiobook can either be a single file or a subfolder structured as described in point #2.
    *   Audii will scan the selected folder and add all valid audiobooks it finds.

---

## ScreenShots(Ongoing)


<img width="225" height="500" alt="image" src="https://github.com/user-attachments/assets/a15b7651-3a39-4e58-bdcd-edacd70fd288" />
<img width="225" height="500" alt="image" src="https://github.com/user-attachments/assets/4cde2b2a-a7b0-4398-923f-ea57916f92bb" />
<img width="225" height="500" alt="image" src="https://github.com/user-attachments/assets/51da9463-8d94-4dcf-9cb9-7dca681d81a7" />
<img width="225" height="500" alt="image" src="https://github.com/user-attachments/assets/c5e23074-f899-4f2f-af77-6c897f4afff3" />
<img width="225" height="500" alt="image" src="https://github.com/user-attachments/assets/6b0b3d18-63b0-4e17-86ba-6b3b25cfb63e" />


---

## Roadmap
* [x] Basic Audiobook Playback
* [X] Progress Storage
* [ ] Adding from different source types 
* [ ] Adding from a repository of sorts, so it auto syncs
* [ ] Playback speed control
* [ ] Bookmarks and collections
* [ ] Sleep timer
* [ ] Android Auto compatibility

---
