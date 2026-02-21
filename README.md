# Workout Partner Planner

Android application for collaborative workout planning between training partners.

This project is built as a structured learning project focused on:

- Modern Android architecture (MVVM + Compose)
- Firebase backend integration
- Clean Git workflows
- AI-assisted development practices
- Structured collaboration between human and coding agents

---

## 🎯 Project Purpose

The core idea of the app is to allow two training partners to:

- Connect accounts
- Manage work schedules
- Propose and accept workout times
- Create structured workout templates
- Track active workout sessions with notes
- View upcoming sessions and workout history

The long-term goal is to design a scalable collaborative workout platform.

---

## 🎓 Educational Focus

This project is built in an educational context with a strong emphasis on:

- Learning structured development workflows
- Understanding AI-assisted coding (Codex)
- Practicing clean architecture
- Writing professional commits and pull requests
- Working ticket-driven with Linear
- Reviewing and validating AI-generated code

The objective is not just to build an app, but to learn how to build software professionally.

---

## 🤖 AI Workflow

This repository follows a structured AI-first development workflow.

See `AGENTS.md` for:

- Architecture rules (MVVM)
- Layering constraints
- Commit message format
- Branch naming conventions
- Pull request standards
- AI usage boundaries

Codex is used for code generation and refactoring.  
ChatGPT is used for design discussions, planning, and guidance.  
All AI-generated code is manually reviewed before merge.

---

## 🧱 Architecture

The app follows a strict MVVM structure:

UI → ViewModel → Repository → Firebase

Key principles:

- No business logic in Composables
- No direct Firebase calls in UI
- ViewModels manage state via StateFlow
- Repositories handle all data operations
- Clear separation of concerns

---

## 🛠 Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- MVVM Architecture
- Kotlin Coroutines + StateFlow
- Firebase Authentication
- Cloud Firestore
- Linear (project management)

---

## 🌿 Git & Workflow Standards

- Feature branches follow:  
  `codex/<TICKET-ID>-short-description`

- All commits are prefixed with:  
  `[codex]`

- Pull requests follow a structured template
- Linear tickets are the source of truth

The workflow is designed to simulate a professional development environment, even in a solo project setting.

---

## 🚀 Running the Project

1. Open in Android Studio
2. Sync Gradle
3. Run on emulator or physical device

Ensure Firebase configuration is set up properly before running.

---

## 📌 Status

This project is under active development as part of an ongoing learning process.

Architecture and workflows may evolve as the project matures.
