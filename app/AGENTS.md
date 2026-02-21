# 🤖 AGENTS.md — AI-First Workflow
Workout Partner Planner (Android)

> This document defines how Codex should work in this repository.
> Linear is the source of truth.
> Human review is mandatory before merge.

---

# 📱 Project Overview

Android app for collaborative workout planning.

Core features:
- Connect with a training partner
- Manage work schedule
- Send training requests (accept / decline)
- Create workout templates
- Track active workout sessions
- View schedule overview & history

---

# 🛠 Tech Stack

- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Architecture: MVVM
- Async: Kotlin Coroutines + StateFlow
- Backend: Firebase Auth + Firestore
- Project Management: Linear
- AI Agent: Codex (primary)

---

# 🧱 Architecture Rules (STRICT)

## Layering

UI → ViewModel → Repository → Firebase

### UI (Compose)
- No business logic.
- No Firebase calls.
- Only communicates with ViewModel.
- Must support:
    - Loading state
    - Error state
    - Empty state (when applicable)

### ViewModel
- Owns UiState (StateFlow).
- Handles user events.
- Catches repository exceptions.
- Converts errors to user-friendly messages.

### Repository
- Only place that communicates with Firebase.
- Returns domain models.
- Throws exceptions on failure.

---

# 📦 State Pattern

Each screen must define a UiState:

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

Prefer immutable state.

For one-time events (navigation, snackbars), use:
- SharedFlow
- Or event wrapper pattern

---

# 🔥 Firebase Conventions

Example collections:

- users/{uid}
- users/{uid}/workShifts/{shiftId}
- users/{uid}/workoutTemplates/{templateId}
- users/{uid}/workoutSessions/{sessionId}
- trainingRequests/{requestId}
- bookings/{bookingId}

Rules:
- Do not duplicate IDs unnecessarily.
- Do not change schema without documenting it in PR.
- Validate input before writing.
- Do not mutate data on read.

---

# 🎫 Linear Workflow (MANDATORY)

Linear is the single source of truth for feature scope and status.

## Implementation Rules

When implementing a ticket:

1. Read the full Linear ticket.
2. Follow the Acceptance Criteria exactly.
3. Do not expand scope without explicit instruction.
4. Confirm Acceptance Criteria coverage in the Pull Request.

If a ticket is marked **"UI only"**:
- Use placeholder or mock data.
- Add TODOs for future backend wiring.
- Do not simulate business logic.

---

## Status Rules

1. When starting implementation, move the ticket to **In Progress**.
2. Keep the ticket in **In Progress** until the Pull Request is merged.
3. After merge and verification, move the ticket to **Done**.
4. Do not move tickets between statuses without explicit confirmation.

---

## 🔁 Post-Push Control (MANDATORY)

After each push to the remote feature branch:

1. The agent must checkout `main`.
2. The agent must ask whether to pull latest changes:

"Pull latest changes on main? (Y/n)"

- If **Y** or Enter → run `git pull`.
- If **n** → skip pull.

3. The agent must then ask for confirmation before starting a new ticket:

"Do you want me to proceed with the next prioritized story? (Y/n)"

- If **Y** or Enter → run `new ticket`.
- If **n** → wait for next comand.

Rules:
- Never start a new ticket automatically without confirmation.
- Keep chat messages brief; PR description is the source of truth for implementation details.

---

# 🌿 Branch Naming

Format:

```
codex/<TICKET-ID>-short-description
```

Examples:

- codex/US6-training-request-flow
- codex/US9-active-workout-session

---

# 💬 Commit Rules

All commits must start with:

```
[codex]
```

### First commit in branch:

```
[codex] US6 Implement training request flow
```

### Follow-up commits:

```
[codex] Add TrainingRequest model
[codex] Implement accept logic
[codex] Add conflict validation
[codex] Improve error handling
```

Rules:
- Imperative tense.
- Be specific.
- No vague messages like "fix stuff".

---

# 📦 Pull Request Rules

## PR Title

```
[codex] <TICKET-ID> Title Case Summary
```

Example:

```
[codex] US6 Training Request Accept/Decline + Sync
```

---

## PR Description Template (REQUIRED)

```markdown
## ✅ <TICKET-ID> Implementation Complete

Linear: <full ticket URL>

### Summary
Short explanation of what was implemented.

---

### What Was Done

1. **Data Layer**
   - ...

2. **ViewModel Layer**
   - ...

3. **UI Layer**
   - ...

---

### Acceptance Criteria Coverage

- [ ] AC1:
- [ ] AC2:
- [ ] AC3:

---

### Edge Cases Handled

- ...
- ...

---

### Manual Test Steps

1.
2.
3.

---

### Notes / Tradeoffs

- ...
```

---

# 🚫 AI Restrictions

Codex must NOT:

- Modify authentication logic without explicit instruction.
- Change Firestore schema silently.
- Introduce new dependencies without documenting why in PR.

Generated code must be reviewed manually before merge.

---

# 📘 Documentation Rules

Add KDoc for non-trivial logic:

```kotlin
/**
 * Explains what the function does.
 *
 * Example use:
 * val result = function(input)
 *
 * Why:
 * Explains reasoning behind logic.
 */
```

Prefer over-documentation over under-documentation.

---

# ✅ Pre-Merge Checklist

- [ ] Follows MVVM strictly
- [ ] No Firebase calls inside Composables
- [ ] Loading / Empty / Error states implemented
- [ ] Acceptance Criteria fully covered
- [ ] Commits formatted correctly
- [ ] PR description complete

---

# 🎯 Long-Term Goal

This codebase should be:
- Easy to refactor
- Easy to extend
- AI-assisted but human-controlled
