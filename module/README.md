# SFX Glass Universal Module

**Universal ViPER4Android RE + SFX Player — Magisk / KernelSU / APatch / KSU-Next**

- ✅ Android 9 (Pie) → 15 (VanillaIceCream)
- ✅ HIDL & AIDL auto-detect
- ✅ arm64-v8a + armeabi-v7a + x86_64 (emulator)
- ✅ Magisk 24+ / KSU / APatch
- ✅ Vendor autodetect (ODM/System/Vendor)
- ✅ iOS Glass APK terintegrasi

## Flash
1. Buka Magisk / KSU Manager → Modules → Install from storage → pilih `SFX-Glass-Universal-Module-v3.0.zip`
2. Reboot
3. Install APK `SFX-Glass-v3.0.apk` jika belum auto-install
4. Buka SFX Glass → cek Driver Active (hijau)

## Uninstall
Magisk Manager → hapus module → reboot, atau flash uninstall zip.

## Log
`adb shell cat /data/local/tmp/sfx_viper.log`
`adb shell logcat -s SFX-Glass:* ViPER4Android:*`
