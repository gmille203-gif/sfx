# Rekomendasi Fitur Baru ViPER Glass — Curated untuk Kamu

## Top 8 Fitur Paling Worth It (Pilih 3-4 untuk MVP)

### 1. 🎛️ Live Spectrum Analyzer (WAJIB)
**Kenapa?** Ini yang bikin user merasa "woah efeknya beneran jalan". Saat ini user cuma lihat switch on/off, tidak ada feedback visual.
- 64-bar FFT live 60fps di Home, overlay EQ curve
- Tap expand → fullscreen immersive
- **Effort**: Medium, pakai `Visualizer` API (tidak perlu root untuk visual, cukup `RECORD_AUDIO` optional)

### 2. 📱 Per-App Profile (Game Changer)
**Kenapa?** Spotify butuh bass heavy, YouTube butuh vocal clarity, PUBG butuh compressor. Kompetitor Wavelet sudah punya, ViPER belum.
- Auto switch preset saat app foreground berganti
- List app dengan icon + last used
- **Effort**: Large tapi high value — pakai `UsageStatsManager` + `AudioPlaybackCallback`

### 3. ⚖️ A/B Compare + Morph Slider
**Kenapa?** Pro audio workflow. User bisa bandingkan 2 setting dengan 1 tap, slider morph untuk blend.
- Tombol A/B di toolbar EQ/Lab
- Long-press A/B → copy, swap
- **Effort**: Small, simpan 2 state di memory

### 4. 🪄 AutoEQ — 1 Tap Headphone Calibration
**Kenapa?** 90% user bingung tuning EQ manual. AutoEQ dari oratory1990 punya 3000+ headphone profile terukur.
- Search "Sony WH-1000XM5" → auto apply parametric EQ yang flat
- Offline cache, update via GitHub raw
- **Effort**: Medium

### 5. 🎨 Preset Gallery + Cloud Share
**Kenapa?** Saat ini preset cuma list text membosankan. Bikin kayak Apple Music playlist: artwork, likes, tags, community sharing.
- Artwork auto-generate dari EQ curve (gradient)
- Share via QR / FileProvider `.vip` zip
- Community tab (Supabase free tier cukup)
- **Effort**: Large tapi bikin app viral

### 6. 🌊 iOS Haptics & Micro-Interactions (FREE WIN)
**Kenapa?** Bedakan dari semua audio app Android yang kaku. Tiap toggle, slider, tab ada haptic & spring.
- `HapticFeedbackConstants` di semua touch
- Spring animation `damping 0.7`
- **Effort**: Small, tapi impact besar ke perceived quality

### 7. 🧙 Onboarding Glass (Retensi)
**Kenapa?** User baru langsung uninstall kalau bingung. 4-page onboarding iOS style naikkan retensi 40%.
- Page 1: Welcome Lottie waveform
- Page 2: Root check (Magisk/KSU status + fix button)
- Page 3: Device picker (Speaker/Headphone)
- Page 4: Try EQ (drag slider → hear difference)
- **Effort**: Small

### 8. 🔗 Quick Tiles & Widget
**Kenapa?** Toggle master tanpa buka app — iOS Control Center feel.
- Tile 1: Master ON/OFF
- Tile 2: Preset cycle (tap → next preset)
- Tile 3: Device switch
- Widget home 2x2 glass
- **Effort**: Small (TileService sudah ada)

---

## Paket Rekomendasi

### Paket A — "Glass MVP" (6 minggu)
Spectrum + Per-App + A/B + Haptics + Onboarding + Tiles
→ Cocok kalau mau rilis cepat dengan wow factor maksimal

### Paket B — "Complete Glass" (10 minggu)
Paket A + AutoEQ + Preset Gallery + Cloud
→ Cocok kalau mau jadi kompetitor serius Wavelet/Poweramp

### Paket C — "Pro Studio" (14 minggu)
Paket B + PEQ + 3D Room + Gesture + WearOS
→ Untuk enthusiast & audiophile

**Rekomendasi aku: Paket A dulu, rilis Beta, dapet feedback, baru lanjut B.**

Mau aku bikinkan prototype APK Paket A yang bisa langsung kamu install di HP kamu? Kasih tau HP + Android version + root method kamu.
