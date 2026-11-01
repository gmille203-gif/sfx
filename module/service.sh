#!/system/bin/sh
# SFX Glass Universal Service — KSU/APatch self-mount + Magisk overlay
# Support HIDL + AIDL, semua device

MODDIR=${0%/*}
SELF_MOUNT=false
[ -f "$MODDIR/skip_mount" ] && SELF_MOUNT=true

# Logging
LOGFILE="/data/local/tmp/sfx_viper.log"
echo "[SFX] $(date) service.sh start SELF_MOUNT=$SELF_MOUNT" >> $LOGFILE

ns_mount() {
  if nsenter -t 1 -m -- mount -o bind "$1" "$2" 2>/dev/null; then
    echo "[SFX] ns_mount $1 -> $2 ok" >> $LOGFILE
    return 0
  fi
  if mount -o bind "$1" "$2" 2>/dev/null; then
    echo "[SFX] mount $1 -> $2 ok" >> $LOGFILE
    return 0
  fi
  echo "[SFX] mount $1 -> $2 FAIL" >> $LOGFILE
  return 1
}

mount_effect_lib() {
  local target_dir="$1"
  local source_dir="$2"
  local work_dir="$MODDIR/work_mount$target_dir"

  [ -d "$target_dir" ] || return 0
  [ -f "$source_dir/libv4a_re.so" ] || return 0
  # Jika sudah ada (misal Magisk sudah mount), skip
  [ -f "$target_dir/libv4a_re.so" ] && return 0

  [ -d "$work_dir" ] && rm -rf "$work_dir"
  mkdir -p "$work_dir"
  cp -af "$target_dir"/* "$work_dir/" 2>/dev/null
  cp -f "$source_dir/libv4a_re.so" "$work_dir/libv4a_re.so"
  chcon -R --reference="$target_dir" "$work_dir" 2>/dev/null
  ns_mount "$work_dir" "$target_dir"
}

mount_config() {
  local original="$1"
  local module_path="$2"
  [ -f "$original" ] || return 0
  [ -f "$module_path" ] || return 0
  cmp -s "$original" "$module_path" && return 0
  ns_mount "$module_path" "$original"
}

kill_audio_procs() {
  echo "[SFX] killing audioserver" >> $LOGFILE
  for proc in audioserver audiohalservice soundserver; do
    pids=$(ps -A 2>/dev/null | grep "$proc" | grep -v grep | awk '{ if ($1 ~ /^[0-9]+$/) print $1; else if ($2 ~ /^[0-9]+$/) print $2 }')
    for pid in $pids; do
      kill "$pid" 2>/dev/null && echo "[SFX] killed $proc $pid" >> $LOGFILE
    done
  done
  # Alternative via killall
  killall audioserver 2>/dev/null || true
  killall vendor.qti.hardware.audiohalservice 2>/dev/null || true
}

wait_boot() {
  # Tunggu boot selesai agar mount stabil
  while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 1; done
  sleep 2
}

# Main
if [ "$SELF_MOUNT" = true ]; then
  wait_boot
  echo "[SFX] Self-mount mode (KSU/APatch)" >> $LOGFILE

  for arch in lib lib64; do
    mount_effect_lib "/vendor/$arch/soundfx" "$MODDIR/system/vendor/$arch/soundfx"
    mount_effect_lib "/system/$arch/soundfx" "$MODDIR/system/$arch/soundfx"
    # ODM juga
    mount_effect_lib "/odm/$arch/soundfx" "$MODDIR/system/vendor/$arch/soundfx" 2>/dev/null || true
  done

  # Mount configs HIDL + AIDL
  for original in $(find /odm /system /vendor -type f \( -name 'audio_effects*.conf' -o -name 'audio_effects*.xml' -o -name 'audio_effects_config.xml' \) 2>/dev/null); do
    case "$original" in
      /vendor/*) module_path="$MODDIR/system$original" ;;
      *) module_path="$MODDIR$original" ;;
    esac
    mount_config "$original" "$module_path"
  done

  # AIDL stub jika ada tapi original tidak ada, bind ke /vendor/etc
  if [ -f "$MODDIR/system/vendor/etc/audio_effects_config.xml" ] && [ ! -f "/vendor/etc/audio_effects_config.xml" ]; then
    mkdir -p /vendor/etc 2>/dev/null || true
    ns_mount "$MODDIR/system/vendor/etc/audio_effects_config.xml" "/vendor/etc/audio_effects_config.xml" 2>/dev/null || true
  fi

  kill_audio_procs

  # Jaga agar prop terbaca
  resetprop ro.sfx.viper.active 1 2>/dev/null || setprop ro.sfx.viper.active 1 2>/dev/null || true

  echo "[SFX] service.sh done" >> $LOGFILE
else
  echo "[SFX] Magisk mode — overlay handled by Magic Mount, nothing to self-mount" >> $LOGFILE
  # Tetap kill audioserver agar reload (sekali saja)
  ( sleep 5; kill_audio_procs ) &
fi

# Always: start ViPER Glass service if apk terpasang
sleep 3
am start-foreground-service -n com.sfx.viperglass/.service.ViperGlassService 2>/dev/null || \
am startservice -n com.sfx.viperglass/.service.ViperGlassService 2>/dev/null || true
