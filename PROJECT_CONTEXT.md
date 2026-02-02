# PROJECT_CONTEXT

## App Name
Lock-In

## One-Line Summary
Lock-In is an offline-first Android app that helps users enter deep focus states by eliminating distractions, enforcing time-boxed commitment, and guiding users back on track when they deviate.

---

## Core Problem
Users suffer from:
- Dopamine addiction (scrolling, shorts, porn, random browsing)
- Inconsistent discipline despite motivation
- Relapse after small failures
- Over-complex productivity systems that are abandoned

Lock-In exists to **force commitment**, not motivate endlessly.

---

## Core Philosophy
- Discipline > Motivation
- Systems > Willpower
- Environment design beats self-control
- One committed action is better than ten planned ones

The app should feel like a **calm but strict manager**, not a coach or a friend.

---

## Target User
- Age: 16–25
- Student / self-learner / early career
- High ambition, low consistency
- Comfortable with minimal UI
- Prefers structure and constraints over flexibility

---

## Non-Negotiable Design Principles
- Low dopamine UI (no flashy colors, no infinite scroll)
- Minimal animations (only functional)
- No social features
- No gamification points, streak fireworks, or badges
- No constant notifications
- Dark mode first
- Typography-centric UI

---

## Primary Use Case (Critical Path)
1. User decides to "Lock-In"
2. User selects:
    - Task (single objective)
    - Duration (e.g., 25 / 45 / 90 minutes)
3. Lock-In session starts:
    - No task switching
    - No browsing inside app
    - Minimal UI interaction
4. If user breaks the session:
    - App records deviation
    - Shows neutral, non-shaming intervention
5. Session ends:
    - User reflects briefly
    - App stores session outcome

---

## Core Features
- Lock-In focus sessions (time-boxed)
- Single-task enforcement
- Session persistence across app restarts
- Deviation / relapse detection
- Minimal reflection logging
- Pattern recognition over time (local only)

---

## Tech Stack
- Platform: Android
- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM
- State Management: StateFlow
- Dependency Injection: Hilt
- Local Storage: DataStore + Room
- No backend (offline-only)
- No ads
- No analytics SDKs

---

## Architecture Rules (Strict)
- UI layer:
    - Compose only
    - Stateless where possible
    - Observes ViewModel state only
- ViewModel layer:
    - Holds UI state
    - No direct persistence calls
- Domain layer:
    - UseCases contain business logic
    - Pure Kotlin where possible
- Data layer:
    - Repositories abstract storage
    - DataStore for preferences
    - Room for historical session data
- No circular dependencies
- No logic inside Composables

---

## State Management Rules
- All screen state must be represented as immutable data classes
- No mutable shared state across ViewModels
- One ViewModel per screen

---

## Persistence Rules
- Session state must survive:
    - App backgrounding
    - Process death
- Partial sessions are never silently discarded
- All session outcomes are explicit:
    - Completed
    - Aborted
    - Timed out

---

## Intervention Tone Rules
- No shame language
- No hype or motivational clichés
- Calm, factual, firm
- Example:
    - "You exited the session early. Resume or end intentionally."

---

## Anti-Patterns (Do NOT implement)
- Social feeds
- Streak obsession
- Pop-ups for motivation
- Reward animations
- AI chatbots inside app
- Complex dashboards

---

## Future (Out of Scope for Now)
- Cloud sync
- Multi-device support
- AI personalization
- Community features

---

## Developer Intent
This project is:
- A long-term discipline system
- Built for maintainability over speed
- Architected for clarity
- Explicitly resistant to feature creep

Copilot suggestions must respect:
- Existing architecture
- Minimalism
- Psychological intent of restraint