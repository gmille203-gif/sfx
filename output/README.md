# SFX Glass v3.0 — Distribusi Siap Pakai

## Isi
- `SFX-Glass-v3.0.apk` (18 MB) — ViPER4Android RE 3.0 iOS Glass (universal, dark/light, full animation) + assets Glass
- `SFX-Player-v3.0.apk` (1.8 MB) — SFX Music Player terintegrasi ViPER (Hi-Res, gapless, audition)
- `SFX-Glass-Universal-Module-v3.0.zip` (599 KB) — Module Magisk/KSU/APatch universal (HIDL+AIDL, semua device, Android 9-15)
- `SFX-Glass-Player-Standalone-PWA.zip` — PWA music player (install via Chrome Add to Home)

## Install
1. Flash module: Magisk/KSU → Install `SFX-Glass-Universal-Module-v3.0.zip` → Reboot
2. Install APKs: `adb install SFX-Glass-v3.0.apk` + `adb install SFX-Player-v3.0.apk` atau tap langsung di HP
3. Buka SFX Glass → cek Driver Active (hijau) → atur EQ → buka SFX Player → play lagu → dengar ViPER realtime

## Source Code
Full Kotlin Compose + Media3 project ada di `viper-glass/` — buka di Android Studio Hedgehog, sync Gradle, Run.
GitHub Actions akan build APK release otomatis di setiap push (lihat .github/workflows/build.yml)

## Tema
iOS Liquid Glass: blur vibrancy, SF Pro, spring animation, dark/light auto, responsive (phone/tablet/foldable)

## KSU/Magisk/APatch
Module universal: auto-detect HAL, auto-patch audio_effects.conf/xml, sepolicy, self-mount untuk KSU/APatch

## Dukungan
- Device: semua ARM64/ARM (universal libs)
- Android: 9 - 15
- Root: Magisk 24+, KernelSU, APatch, KSU-Next
