# Analisa Mendalam ViPER4Android RE v2.1.1 + Modul Magisk

> **Sumber**: `ViPER4Android-2.1.1.apk` (3.8 MB) & `ViPER4Android-RE-v2.1.1.zip` (618 KB) dari Google Drive kamu  
> **Tanggal analisa**: 1 Nov 2026  
> **Package**: `com.llsl.viper4android` | **VersionCode 1 / 2.1.1** | **Min SDK 28 (Android 9) | Target SDK 37 (Android 17/18)**

---

## 1. Ringkasan Eksekutif

ViPER4Android RE (Reverse Engineered) adalah **audio enhancing software** generasi baru yang di-reverse dari ViPER original. Versinya jauh lebih modern:
- **APK**: Kotlin + Jetpack Compose + Material 3 + Hilt + Room + DataStore + Navigation Compose
- **Driver**: Native library `libv4a_re.so` (ARMv7 + ARM64) yang dipasang sebagai **system audio effect** via Magisk/KernelSU/APatch
- **Arsitektur**: Client-Service, dimana UI (Compose) berkomunikasi dengan `ViperService` (Foreground Service) yang mengendalikan `android.media.audiofx.AudioEffect` di level sistem.

> Berbeda dengan ViPER4Android FX legacy yang memakai driver closed-source + UI XML kuno, RE ini sudah 100% Kotlin Compose, open-architecture, dan mendukung Android 9–15+.

---

## 2. Mekanisme Kerja — End to End

### 2.1 Diagram Alur

```
[User membuka APK] 
   → MainActivity (Compose)
   → ViperService (Foreground, SPECIAL_USE: audio_effect_processing)
   → Deteksi output device (Speaker / Headphone / Bluetooth A2DP/SCO / USB)
   → Load DeviceSettings dari Room DB (viper4android.db) + DataStore PB
   → Kirim parameter EffectState via binder viper.control
   → Native libv4a_re.so di AudioFlinger memproses PCM real-time
         |
         +-- EQ 10-band (EqState)
         +-- Convolver IRS (ConvolverState)
         +-- DDC / Speaker Correction
         +-- Surround / Headphone Surround / Field Surround / Diff Surround
         +-- Reverberation
         +-- Bass Boost & Bass Mono
         +-- Clarity, Psychoacoustic Bass
         +-- Cure, AnalogX, FET Compressor, Multiband Compressor
         +-- Playback Gain Control (LUFS / Limiter)
         +-- Dynamic System & Dynamic EQ
         +-- Spectrum Extension, Stereo Imager, Tube Simulator
         → Audio HAL output (audioserver)
```

### 2.2 Magisk Module — Instalasi Driver

**File inti**: `common/files/libv4a_re_{arm64-v8a, armeabi-v7a}.so`

| Langkah install.sh | Apa yang terjadi |
|---|---|
| **1. Cek AIDL** | Jika `android.hardware.audio.effect.IFactory` terdeteksi, abort — user harus pakai versi AIDL khusus. Artinya module ini **hanya untuk HIDL audio_effects**. |
| **2. Copy SO** | `libv4a_re.so` disalin ke `$MODPATH/system/vendor/lib{,64}/soundfx/` (DYNLIB=true untuk Android 8.0+). Permission `0755/0644` + `u:object_r:vendor_file:s0`. |
| **3. Patch audio_effects** | Script `find /odm /system /vendor -name *audio_effects*.conf/xml` lalu inject: <br> `v4a_standard_re { library v4a_re ; uuid 90380da3-8536-4744-a6a3-5731970e640f }` dan `v4a_re { path /vendor/lib/soundfx/libv4a_re.so }` <br> Mendukung **dua format** `.conf` (legacy) dan `.xml` (modern). |
| **4. service.sh** | Untuk KernelSU/APatch (`skip_mount`), melakukan `nsenter -t 1 -m mount --bind` secara live: bind-mount folder `soundfx` dan file `audio_effects.*` tanpa reboot. Lalu kill `audioserver` & `audiohalservice` agar reload config. |
| **5. Cleanup / uninstall.sh** | Menyimpan daftar file yang di-override di `$NVBASE/modules/.$MODID-files`, restore `file~` backup saat uninstall. |

**UUID penting**: `90380da3-8536-4744-a6a3-5731970e640f` — ID unik agar AudioFlinger mengenali effect ini sebagai `v4a_standard_re`.

**Kompatibilitas Root**:
- ✅ Magisk (systemless via overlay)
- ✅ KernelSU (`skip_mount` + ns_mount)
- ✅ APatch (`skip_mount` + skip_mountify)
- ❌ Recovery mode hanya uninstall

### 2.3 APK — Lapisan Aplikasi

**Teknologi Stack (dari META-INF & classes.dex)**:

| Kategori | Library / Versi |
|---|---|
| **UI** | Jetpack Compose (Material 3, animation-core, foundation), androidx.activity 1.8+, lifecycle-runtime-compose |
| **DI** | Dagger Hilt + hilt-navigation-compose |
| **Navigasi** | Navigation Compose + navigation-common/runtime |
| **Storage** | Room 2.6 (ViperDatabase, ViperDatabase_Impl), DataStore Preferences + Proto, SQLite |
| **Network** | OkHttp 4.x + conscrypt (OpenSSL) |
| **Async** | Kotlin Coroutines Android/core |
| **System** | ForegroundService, QuickSettings Tile, BootCompletedReceiver, FileProvider |

**Manifest Highlights**:
```xml
package="com.llsl.viper4android"
application name="ViPER4AndroidApp" (HiltApp)
activity: MainActivity (exported, MAIN/LAUNCHER + DISPLAY_AUDIO_EFFECT_CONTROL_PANEL + QS_TILE_PREFERENCES)
service: ViperService (foregroundServiceType=0x40000000 SPECIAL_USE - audio_effect_processing)
tile: MasterTileService (BIND_QUICK_SETTINGS_TILE)
receiver: BootCompletedReceiver (BOOT_COMPLETED)
permission: FOREGROUND_SERVICE, MODIFY_AUDIO_SETTINGS/ROUTING, POST_NOTIFICATIONS, INTERNET, RECEIVE_BOOT_COMPLETED
```

**Database Schema (Room)**:
- `device_settings` (device_id PK, device, deviceName, enabled, settings_json, last_connected)
- `presets` (id, name, settings_json)
- `eq_presets` / `ds_presets` (id, name_key) — builtin presets seperti AnalogX, DDC, dll.
- Query penting: `SELECT * FROM device_settings ORDER BY last_connected DESC` — auto-switch per device.

**Effect State Models (dari dex strings)** — inilah inti DSP:
```
EffectState(masterEnable: Boolean)
EqState(enable, gains[], bandEnables[])
ConvolverState(enable, kernel IRS)
DdcState(enable, device)
FieldSurroundState, DiffSurroundState, HeadphoneSurroundState
ReverbState, BassState, BassMonoState, ClarityState, PsychoacousticBassState
CureState, TubeSimulatorState, AnalogXState, FetCompressorState, MultibandCompressorState
SpeakerCorrectionState, SpectrumExtensionState, StereoImagerState
PlaybackGainControlState, LufsState, DynamicSystemState, DynamicEqState
OutputState(volume), AnimationState, etc.
```
Setiap State di-serialize ke `settingsJson` dan dikirim ke native via `AudioEffect.setParameter()` dengan reflection fallback (`AudioEffect constructor is null, reflection failed` handling).

**ViperService Flow**:
1. `onCreate()` → init Room + DataStore (`viper_preferences.preferences_pb`)
2. `onStartCommand()` via intent `START/STOP/TOGGLE_MASTER/EXTRA_MASTER_ENABLED`
3. Hook `AudioDeviceCallback` (deteksi `Output device added: headphone=` / `Output device removed`)
4. `getService(viper.control)` binder — komunikasi native. Jika null → toast `service(viper.control) is null`
5. `applyAndAckSettings` → iterate semua EffectState, panggil native, `kill_audio_procs` jika perlu.
6. `Master off: skipping per-app session` — optimasi tidak proses jika master disable.

**MainActivity (Compose)**:
- Single-Activity architecture, Navigation Compose
- Material 3 theming, dark/light, dynamic color
- Icons: Filled.Compress, Equalizer, Headphones, Speaker, PowerSettingsNew, etc.

### 2.4 Kelebihan vs Kekurangan Saat Ini

| ✅ Kelebihan | ❌ Kekurangan (Peluang Upgrade) |
|---|---|
| Modern Compose, bukan XML kuno | UI masih Material 3 generic, belum berkarakter (tidak iOS style, animasi minim) |
| Per-device auto profile (last_connected) | Tidak ada visualizer real-time (spectrum) |
| Support Magisk/KSU/APatch | Tidak ada cloud sync / backup enkripsi |
| Foreground Service stabil di Android 14/15 | Tidak ada per-app EQ (semua global) |
| Room + migration builtin presets | Preset management sederhana (CRUD saja) |
| QS Tile untuk toggle cepat | Tidak ada gesture, haptic, atau onboarding |
| Open RE, aktif di GitHub | Tidak ada AutoEQ / AI suggestion |
| Support Hi-Res USB, Bluetooth | Tidak ada A/B compare, tidak ada share preset |

---

## 3. Analisa UI/UX Saat Ini

### 3.1 Struktur Navigasi (disimpulkan dari dex & resources)

```
MainActivity
├── Header: Master ON/OFF (Power)
├── Tabs/Destination (kemungkinan):
│   ├── Speaker (Built-in)
│   ├── Headphones (Wired/Bluetooth)
│   ├── Bluetooth/USB (jika terdeteksi)
│   └── Presets / Settings
├── Per Device Panel:
│   ├── Master Enable (switch besar)
│   ├── FET Compressor
│   ├── Convolver (IRS picker)
│   ├── DDC / Speaker Correction
│   ├── Equalizer (10-band sliders)
│   ├── ViPER-DDC, ViPER Bass, Clarity, XHIFI, Cure, etc.
│   ├── Reverberation, Surround, Stereo Imager
│   └── Playback Gain / Limiter / Spectrum Extension
└── Presets: Save / Load / Delete / Builtin (Small Speakers, Common Headphone v1/v2, etc.)
```

**Resources**: `res/*.xml` (18 file, 0.5–2 KB) — sangat ramping, indikasi UI memang sepenuhnya Compose code-generated, bukan XML layout. `resources.arsc` 332 KB dengan string multi-lokal (en-rCA, fr-rCA, zh-rHK/CN/TW, pt-BR, es-US, etc.) — sudah i18n matang.

**Visual Saat Ini (Material 3 default)**:
- Palet default Android (Dynamic Color), card elevation, switch & slider bawaan.
- Icon Filled.* dari Material Icons Extended.
- Typography Roboto, tidak ada branding kuat.
- Animasi hanya default Compose (fade, slide) — tidak ada spring, Lottie, atau blur.

### 3.2 Pain Points UX

1. **Terlalu teknis**: Nama efek seperti `FET Compressor`, `Field Surround`, `Cure` tanpa tooltip / preview suara.
2. **Slider membosankan**: 10-band EQ slider vertikal generik, sulit presisi, tidak ada gesture pinch.
3. **Tidak ada feedback visual**: User tidak tahu efeknya bekerja — butuh spectrum analyzer / waveform live.
4. **Preset flat**: List text saja, tidak ada artwork, search, tag, atau rating.
5. **No onboarding**: User baru bingung bedanya Speaker vs Headphone profile.
6. **Responsiveness**: Layout belum optimal tablet / foldable (Compose ada tapi tidak di-custom).
7. **iOS vibe = 0**: Tidak ada blur, glassmorphism, haptic, atau animasi spring iOS.

---

## 4. Visi Upgrade: Clean Responsive iOS Style Full Animation

### 4.1 Design System — "ViPER Glass"

**Inspirasi**: iOS 17/18 Control Center + Apple Music + Liquid Glass

| Token | Value |
|---|---|
| **Font** | SF Pro Display (headings) + SF Pro Text (body) via `Inter` fallback; `SF Mono` untuk dB values |
| **Warna** | Dark-first: `#0A0A0F` (bg) → `#1C1C1E` (card) → `#2C2C2E` (raised) ; Accent: `ViPER Red #FF3B30` + `Electric Cyan #00D9FF` untuk aktif state ; Light mode: `#F2F2F7` grouped background |
| **Blur** | `BackdropFilter` iOS vibrancy: `ultraThinMaterial` blur 20px, translucency 0.7 |
| **Radius** | Card `24px`, Button `14px`, Sheet `36px top` (iOS sheet) |
| **Shadow** | iOS soft shadow `0 8 32 rgba(0,0,0,0.4)` + inner highlight |
| **Grid** | 4pt system, 16px horizontal padding, 12px card gap |
| **Icon** | SF Symbols style (outline→filled on active) + custom ViPER glyph |

**Prinsip**:
- **Clean**: Whitespace besar, 1 primary action per screen, grouping by iOS `GroupedList`
- **Responsive**: Adaptive layout — phone (single column), tablet (2-column masters-detail), foldable (flex), desktop (3-column)
- **iOS Style**: Large Title (`UINavigationBar.largeTitle`), swipe-back, context menu on long-press, haptic `UIImpactFeedback`, sheet detents (medium/large)
- **Full Animation**: Spring (damping 0.7, stiffness 300), shared-element transition, parallax, Lottie waveform, micro-interactions

### 4.2 Navigasi Baru (Bottom Tab iOS)

```
┌─────────────────────────────────────┐
│  ●●● ○○  ViPER Glass  [bell] [gear] │  ← Large Title + toolbar
├─────────────────────────────────────┤
│                                     │
│         [ HERO CARD ]               │  ← Master Power (glass, pulsating)
│         [ Spectrum Live ]           │  ← Real-time analyzer
│                                     │
├─────────────────────────────────────┤
│  [EQ]  [Space]  [Bass]  [Presets]    │  ← Horizontal effect pills (iOS segmented)
│                                     │
│  ┌──────────┐ ┌──────────┐           │
│  │ Convolver│ │   DDC    │           │  ← Bento grid 2x2
│  └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐           │
│  │ Reverb   │ │ Clarity  │           │
│  └──────────┘ └──────────┘           │
├─────────────────────────────────────┤
│ ♪  ◉  ○  ○  ○                        │  ← iOS PageControl
├─────────────────────────────────────┤
│ [Home] [EQ] [Lab] [Presets] [Me]    │  ← iOS TabBar (blur, 5 tabs)
└─────────────────────────────────────┘
```

**5 Tab**:
1. **Home** — Dashboard master, device switcher (Speaker/Headphone/BT/USB), spectrum, quick toggles
2. **EQ** — 10/15/31-band equalizer dengan gesture
3. **Lab** — Semua efek advanced dalam bento grid (Convolver, DDC, Surround, Reverb, Bass, Clarity, Compressor, etc.)
4. **Presets** — Gallery preset (cards dengan artwork, A/B compare)
5. **Me** — Profile, backup, settings, about, driver status

### 4.3 Detail Per Layar

#### Home — Dashboard
- **Master Power Button**: Lingkaran besar 120dp, glass + inner glow. Tap → spring scale 0.95 → 1.05 → 1.0 + haptic heavy. Status `DriverStatus(enabled=true)` → hijau pulsating, false → merah + action Install.
- **Device Switcher**: iOS Segmented Picker (`Speaker | Headphone | Bluetooth | USB`). Auto-detect via `AudioDeviceCallback`, animasi slide indicator.
- **Live Spectrum**: Canvas Compose dengan 64-bar FFT, 60fps, gradient cyan→red, merefleksikan EQ curve. Tap → expand fullscreen.
- **Effect Pills**: Horizontal scroll, chip iOS style (`SF Symbol + label`). Active → filled accent, inactive → ultraThinMaterial. Badge dot jika modified.

#### EQ — Equalizer Studio
- **Curve Editor**: Grafik Bezier interaktif — drag control point (iOS spring), pinch zoom frequency, double-tap reset. Grid 20Hz–20kHz log scale, dB -12 to +12.
- **Sliders**: Vertical `UISlider` iOS clone — track gradient, thumb haptic on detent (0dB). Long-press → numeric input (SF Mono).
- **Presets Row**: Horizontal carousel `BuiltinEqPreset` cards (Small Speakers, Common Headphone v1/v2, Extreme Headphone, etc.) dengan artwork abstrak.
- **A/B**: Tombol A/B iOS toggle di toolbar — bandingkan 2 setting instan, swipe untuk morph.
- **AutoEQ**: Tombol `Magic Wand` → fetch dari AutoEQ GitHub (oratory1990) berdasar headphone model.

#### Lab — Effects Bento
- **Grid 2x2 adaptive**: Setiap card = glass + SF Symbol + toggle + mini preview (mis. Convolver → waveform, Reverb → room 3D).
- **Sheet Detail**: Tap card → iOS sheet (detents `.medium/.large`) dengan konten spesifik:
  - *Convolver*: IRS picker (search, audition, import WAV), visual impulse response.
  - *Reverb*: Room size slider + 3D room preview (SceneView).
  - *Bass*: Knob iOS (rotary) untuk `BassState` + `BassMonoState` + `PsychoacousticBass`.
  - *Compressor Family*: FET / Multiband dengan threshold curve.
  - *Surround*: Field/Surround/Headphone → spatial puck.
- **Animations**: Card masuk staggered (delay 40ms, spring), toggle → Lottie check, sheet → fluid spring.

#### Presets — Gallery
- **Masonry Grid**: Card preset dengan cover art (generated gradient berdasar EQ curve), name, author, tags, likes.
- **Actions**: Long-press → iOS context menu (Share, Duplicate, Delete, Export).
- **Search & Filter**: iOS search bar + chips (Bass Heavy, Vocal, Gaming, etc.).
- **Import/Export**: Drag-drop `.vip` / `.irs`, share via FileProvider.
- **Cloud**: Tab `Community` — browse preset online (Firebase/Supabase), download, rating.

#### Me / Settings
- **Grouped List iOS**: Sections — Driver, Backup, Appearance, About.
- **Driver Status Card**: `DriverStatus`, version `v2.1.1`, `libv4a_re.so` path, audio_effects patch status, button `Reinstall / Check Root`.
- **Appearance**: Light/Dark/System, Accent picker, Reduce Motion toggle (respect Android).
- **Haptics**: Toggle `UIImpactFeedback`.

### 4.4 Animasi — Full Motion Spec

| Elemen | Animasi | Param |
|---|---|---|
| Tab switch | SharedAxis X | duration 300ms, easing `EmphasizedDecelerate` |
| Card appear | Stagger + fade + scale | delay 40ms per card, spring 0.7/300 |
| Master toggle | Scale + glow + haptic | 0.95→1.05, 200ms spring |
| Slider drag | Haptic tick per 1dB, track fill morph | `snapTo` + `animateTo` |
| Sheet present | Spring up | damping 0.8, stiffness 400, blur bg |
| Spectrum | 60fps `withFrameNanos`, bar height `animateFloatAsState` | FFT 64, lerp 0.3 |
| Preset save | Confetti Lottie + haptic success | `LottieCompletion` |
| Device switch | Indicator slide spring | 250ms |
| Empty state | Lottie illustration + parallax | loop |

**Teknik**: Compose `AnimatedVisibility`, `AnimatedContent`, `Crossfade`, `graphicsLayer`, `Haze` blur library (Chris Banes), `Lottie Compose`, `androidx.compose.animation:animation` spring.

---

## 5. Saran Fitur Baru — Prioritized Backlog

### Tier 1 — Must Have (MVP 2.2.0)

| # | Fitur | Deskripsi | Value | Teknis |
|---|---|---|---|---|
| 1 | **Live Spectrum Analyzer** | 64-bar FFT real-time dari `Visualizer` API / `AudioRecord` loopback (jika root, baca PCM). Overlay EQ curve. | User lihat efek bekerja, WOW factor | Compose Canvas + `android.media.audiofx.Visualizer` (capture 1024, rate 20ms) ; fallback dummy animated jika permission ditolak |
| 2 | **Per-App Profile** | EQ berbeda per app (Spotify bass heavy, YouTube vocal). Deteksi foreground app via `UsageStatsManager` + `AudioPlaybackConfiguration` | Killer feature, kompetitor (Wavelet) punya | Room table `app_profiles (packageName, deviceId, settingsJson)` ; ViperService listen `onPlaybackConfigChanged` |
| 3 | **A/B Compare** | Tombol A/B di semua layar untuk toggle 2 setting instan, plus slider morph | Pro workflow, mixing | Simpan `stateA/stateB` di memory, crossfade parameter via `snap` + haptic |
| 4 | **Quick Settings Tiles++** | 3 tiles: Master, Preset Cycle, Device Switch | Akses cepat tanpa buka app | `TileService` sudah ada, tambah 2 service, `requestTileUpdate` |
| 5 | **Onboarding Glass** | 4-page iOS onboarding (Welcome → Root Check → Device Select → Try EQ) dengan Lottie | Retensi new user +40% | Navigation Compose onboarding graph, DataStore `hasSeenOnboarding` |
| 6 | **Haptic & Sound Feedback** | Haptic `HapticFeedbackType` di toggle/slider, click sound `SoundEffectConstants` | iOS feel | `LocalHapticFeedback.current.performHapticFeedback` |

### Tier 2 — High Value (2.3.0)

| # | Fitur | Deskripsi |
|---|---:|---|
| 7 | **AutoEQ Integration** | Search headphone model (oratory1990 / AutoEQ GitHub) → 1-tap apply parametric EQ. Cache offline. | Via `https://raw.githubusercontent.com/jaakkopasanen/AutoEq/master/results/` JSON |
| 8 | **Convolver Cloud + IRS Editor** | Browse IRS library (ch.com, user upload), audition, trim, normalize, export `.irs` / `.wav` | Room + download manager, waveform view `Canvas` |
| 9 | **Preset Marketplace** | Community preset gallery: upload, download, like, comment, report. Moderation. | Supabase/Firebase: `presets_community` table, paginated, offline cache |
| 10 | **Import/Export v2** | Export `.vip` (zip: settingsJson + irs + ddc), share via FileProvider, QR code share | `FileProvider` + QR `zxing` |
| 11 | **Widget iOS Style** | Home widget (glanceable EQ preset + master) + lockscreen widget Android 14 | `Glance` API |
| 12 | **Adaptive EQ (AI)** | Analisa genre via MediaSession metadata → auto suggest preset (ML on-device TFLite) | TFLite classifier, `MediaController` |

### Tier 3 — Delight (2.4.0+)

| # | Fitur | Deskripsi |
|---|---|---|
| 13 | **3D Spatial Preview** | Visual 3D room untuk Reverb/Surround (SceneView / Filament), drag to change room size | `io.github.sceneview:sceneview` |
| 14 | **Gesture EQ** | Pinch zoom frequency, 2-finger drag curve, shake to reset | `pointerInput` + `transformable` |
| 15 | **Parametric EQ (PEQ)** | Tambah mode PEQ: Freq, Gain, Q per band (pro) selain graphic EQ | New `PeqState` model, 10→15 band |
| 16 | **Loudness History** | Grafik LUFS history per session, limiter GR meter | `LufsState` + Canvas meter, Room `loudness_logs` |
| 17 | **App Shortcuts** | Long-press launcher icon → Preset 1/2/3 + Toggle Master | `ShortcutManager` |
| 18 | **Tasker / MacroDroid Plugin** | Intent plugin untuk automation: `com.llsl.viper4android.SET_PRESET` | `BroadcastReceiver` exported |
| 19 | **Wear OS Companion** | Watch tile untuk toggle master / preset cycle | Wear Compose |
| 20 | **Desktop Companion (Web)** | PWA untuk edit preset di desktop, sync via QR | Web (Next.js) + Supabase Realtime |

### Quick Wins (bisa minggu ini)

- **Dark/Light toggle** di toolbar
- **Search preset** (filter by name)
- **Copy settings** antar device (Speaker → Headphone, 1-tap)
- **Driver status** card yang lebih jelas (hijau/merah + log `logcat -s ViPER4Android`)
- **Backup to GDrive** via `Storage Access Framework` (manual export `.db` + `.pb`)

---

## 6. Riset Kompetitor & Inspirasi

| App | Kelebihan | Pelajaran untuk ViPER Glass |
|---|---|---|
| **Wavelet** | AutoEQ, per-app, simple | AutoEQ must-have |
| **Poweramp EQ** | Parametric EQ, spectrum, skins | PEQ + spectrum |
| **Apple Music EQ** | iOS native feel, minimal | iOS design reference |
| **Dolby On** | Live visualizer, glass | Visualizer + glass |
| **JamesDSP** | Open source, banyak efek | Efek completeness |

**Inspirasi UI iOS**: 
- **iOS Control Center** (glass, haptics)
- **Apple Music Classical** (bento, large title)
- **Linear.app** (clean, motion)

---

## 7. Roadmap Implementasi

### Phase 0 — Setup (Minggu 1)
- Fork repo `likelikeslike/ViPERFX_RE` → `gmille203/ViPER-Glass`
- Setup Android Studio Hedgehog, Compose BOM 2024.10, Kotlin 2.0
- Tambah dependencies: `Haze` blur, `Lottie`, `Coil`, `Supabase`, `TFLite`
- Buat Design System module `:designsystem` (tokens, components)

### Phase 1 — Design System (Minggu 2–3)
- Implement `ViPER Glass` tokens: color, typography (Inter/SF), shape, elevation
- Komponen: `GlassCard`, `IOSSwitch`, `IOSSlider`, `IOSSegmentedControl`, `BottomTabBar`, `LargeTitleScaffold`
- Animasi: `ViPERMotion` (spring presets)
- Storybook: `@Preview` semua komponen, screenshot test

### Phase 2 — Home & EQ (Minggu 4–6)
- Refactor `MainActivity` → `GlassMainActivity` + NavHost (5 tabs)
- Bangun Home dashboard + Device switcher + Master button
- Spectrum Analyzer (Visualizer + Canvas)
- EQ Graph editor (Bezier) + sliders iOS

### Phase 3 — Lab & Presets (Minggu 7–9)
- Bento Grid Lab + Sheet detail per effect
- Preset Gallery (masonry) + A/B + import/export
- Per-App profile + Tile++

### Phase 4 — Polish & Features (Minggu 10–12)
- Onboarding, Haptics, Widget, AutoEQ, Cloud
- Tablet/foldable responsive, a11y (TalkBack, large font), RTL
- Performance: `remember`/`derivedStateOf`, `LazyColumn` key, `Baseline Profiles`

### Phase 5 — Release (Minggu 13)
- Beta via Firebase App Distribution → Play Internal Testing
- Screenshots iOS style untuk Play Store (glass mockups)
- Changelog, docs, video demo
- Module zip tetap `ViPER4Android-RE-v2.1.1-Glass.zip` (backward compat)

---

## 8. Teknis Upgrade — Code-Level

### 8.1 Modul Baru yang Dibuat

```
:designsystem      → tokens, components iOS
:feature:home       → Home dashboard
:feature:eq        → Equalizer studio
:feature:lab       → Effects bento + sheets
:feature:presets   → Gallery + marketplace
:feature:onboarding→ Onboarding
:core:audio        → AudioEngine wrapper (AudioEffect, Visualizer)
:core:data         → Room + DataStore + Supabase
```

### 8.2 DB Migration

```kotlin
// ViperDatabase v2 → v3
@Database(entities = [DeviceSettings, Preset, DsPreset, EqPreset, AppProfile], version = 3)
MIGRATION_2_3 = {
  createTable("app_profiles", id PK, packageName, deviceId FK, settingsJson, createdAt)
  addColumn("device_settings", "isFavorite BOOLEAN DEFAULT 0")
  addColumn("presets", "artworkUri TEXT, tags TEXT, likes INT")
}
```

### 8.3 iOS Blur di Compose

```kotlin
// Haze (Chris Banes) — iOS vibrancy
@OptIn(ExperimentalHazeApi::class)
@Composable fun GlassCard(...) {
  val hazeState = rememberHazeState()
  Box {
    // content behind blur
    Card(
      modifier = Modifier.hazeEffect(hazeState, style = HazeMaterials.ultraThinMaterial)
        .clip(RoundedCornerShape(24.dp))
        .background(Color.White.copy(0.07f))
    ) { ... }
  }
}
```

### 8.4 Animasi Spring

```kotlin
val spring = spring<Float>(dampingRatio = 0.7f, stiffness = 300f)
val scale by animateFloatAsState(if (pressed) 0.95f else 1f, spring)
Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
  .hapticsOnPress(HapticFeedbackType.LongPress)
```

---

## 9. Estimasi Effort & Risiko

| Item | Effort | Risiko | Mitigasi |
|---|:---:|---|---|
| Design System | M | Low | Figma → Compose tokens, preview |
| Home + Spectrum | L | Medium (Visualizer permission) | Fallback dummy + permission rationale |
| EQ Graph | L | Medium (precise gesture) | Unit test + haptic |
| Lab Bento | M | Low | Reuse GlassCard |
| AutoEQ | M | Low (network) | Cache + offline |
| Per-App | L | High (UsageStats, bg) | Foreground service + docs |
| Cloud Marketplace | XL | High (backend, mod) | Supabase MVP, manual mod awal |
| Root/Driver compat | M | High (AIDL vs HIDL) | Keep existing install.sh, test di Pixel/Samsung/Xiaomi |

**Total**: ~3 bulan dengan 1 dev full-time + 1 designer part-time.

---

## 10. Next Steps untuk Kamu

1. **Setujui visi**: Pilih Tier 1/2 yang paling kamu mau duluan (aku rekomendasikan: Spectrum + Per-App + A/B + Onboarding).
2. **Aku bikinkan**: 
   - Figma file `ViPER Glass` (5 tab, light/dark)
   - Prototype APK demo (Home + EQ glass) yang bisa kamu install langsung
   - Branch `feature/glass-ui` di repo ini
3. **Test device**: Sebutkan HP + Android version + root (Magisk/KSU) yang kamu pakai biar aku test driver flow.
4. **Branding**: Mau nama tetap `ViPER4Android` atau rebrand jadi `ViPER Glass` / `SFX`?

---

## Lampiran — File Inventory

| File | Ukuran | Hash (awal) | Deskripsi |
|---|---|---|---|
| `ViPER4Android-2.1.1.apk` | 3.7 MB | classes.dex 3.2 MB, resources.arsc 332 KB, 134 files | APK modern Kotlin Compose, 1 activity, 2 services, 1 receiver |
| `ViPER4Android-RE-v2.1.1.zip` | 604 KB | 19 files, 2 SO (armeabi-v7a 548 KB, arm64-v8a 653 KB) | Magisk module MMT Extended, DYNLIB=true, patch audio_effects |

**Repo GitHub upstream**: `github.com/likelikeslike/ViPERFX_RE` (referensi updateJson di module.prop)

---

*Dokumen ini siap jadi blueprint untuk upgrade. Mau aku langsung generate mockup Figma-style + prototype APK glass-nya?*
