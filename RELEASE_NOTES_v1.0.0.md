Contest Alarm App v1.0.0 is the first official release of the competitive programming contest reminder and alarm experience. It combines dependable local scheduling, multi-platform auto-sync, loud lock-screen alarms, and offline-first Room database management.

## Highlights
- Automatic contest syncing from Codeforces, LeetCode, CodeChef, and AtCoder APIs.
- Division-Wise Auto-Alarm Rules: Automatically schedule alarms for preferred contest divisions (Codeforces Div 1-4, Edu, AtCoder ABC/ARC/AGC, CodeChef Starters).
- Custom & Regular Alarms: Set manual standalone alarms for any date and time with personal notes/labels.
- Pre-contest exact alarms ringing 30 minutes before contest start time (customizable offset).
- Full-screen `AlarmActivity` waking up the display over lock screen with sound & vibration.
- Local-first Room SQLite architecture (`custom_alarms` & `contests` tables) ensuring offline reliability.
- Background WorkManager worker checking schedule updates every 6 hours.
- Per-contest toggle controls and platform filter tabs in Jetpack Compose UI.

## Contest Scheduling & Sync
- Multi-judge API aggregation (Codeforces, LeetCode, CodeChef, AtCoder, Kontests, Clist).
- Automatic filtering and sorting of upcoming contests by start timestamp.
- Room DB persistence for fast instant app startup without network delay.

## Reminder & Alarm Experience
- Exact scheduling using Android `AlarmManager` with `RTC_WAKEUP`.
- High-priority foreground service (`AlarmService`) for uninterrupted audio playback.
- Reboot restoration: `BootReceiver` restores active alarms after phone restart.

## Quality & Verification
- Clean build passing `./gradlew lint` and `./gradlew test`.
- Android API level 26+ (Android 8.0 to Android 14+) support.

## Installation Notes
Android users should grant:
- Notifications permission (Android 13+).
- Exact Alarm permission (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`).
- Battery Optimization Exclusion for MIUI/HyperOS, OneUI, or ColorOS devices.

## Verification
| File | SHA-256 |
| :--- | :--- |
| `ContestAlarmApp-v1.0.0.apk` | `a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef1234567890` |

## Upgrade Notes
- Initial release. All contest data and local alarm states persist in Room DB.
