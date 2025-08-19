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
    *   This folder will serve as a repository and will be scanned for new Audiobooks when you launch the app.

---

## ScreenShots(Ongoing)


![Screenshot_20250819_020200_Audii](https://github.com/user-attachments/assets/ed186bea-d4f1-457f-bbed-6e93fb503026)
![Screenshot_20250819_020139_Audii](https://github.com/user-attachments/assets/452e787f-a21f-4525-8647-cf93d8a3ddcc)
![Screenshot_20250819_020333_Audii](https://github.com/user-attachments/assets/610007e9-5855-4451-aec7-68077a5e7cf6)
![Screenshot_20250819_020254_Audii](https://github.com/user-attachments/assets/fe323ce4-2b7d-4ec1-923a-7d49348937de)
![Screenshot_20250819_020250_Audii](https://github.com/user-attachments/assets/ddd23327-0887-4633-a568-3f7c64b868c2)
![Screenshot_20250819_020216_Audii](https://github.com/user-attachments/assets/24dc628e-0e5e-4acb-9434-0f69ebfa4761)
![Screenshot_20250819_020213_Audii](https://github.com/user-attachments/assets/6c853c07-9052-454b-af2b-42863306ec45)



---

## Roadmap
* [x] Basic Audiobook Playback
* [X] Progress Storage
* [x] Adding from different source types 
* [x] Adding from a repository of sorts, so it auto syncs
* [x] Playback speed control per audiobook
* [ ] Bookmarks and collections
* [ ] Sleep timer

---
