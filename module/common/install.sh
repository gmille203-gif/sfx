################################################################
# SFX Glass — Universal Installer
# HIDL & AIDL dual support, vendor autodetect, 32/64 universal
################################################################

# Detect HAL type
IS_AIDL=false
IS_HIDL=false
if service list 2>/dev/null | grep -qi 'android.hardware.audio.effect.IFactory'; then
  IS_AIDL=true
  ui_print "  → AIDL audio HAL terdeteksi"
fi
if ls /vendor/etc/audio_effects*.xml /vendor/etc/audio_effects*.conf /system/etc/audio_effects* 2>/dev/null | grep -q .; then
  IS_HIDL=true
fi
if ! $IS_AIDL && ! $IS_HIDL; then
  ui_print "  ⚠ Tidak ada config audio_effects ditemukan, lanjut overlay generik"
  IS_HIDL=true
fi

# Simpan arch info
echo -n $LIBPATCH >$MODPATH/libpatch.txt
echo -n "$ABI $ABI32 $IS64BIT" >$MODPATH/arch.txt
[ "$IS_AIDL" = true ] && echo "AIDL" >$MODPATH/hal.txt || echo "HIDL" >$MODPATH/hal.txt

ui_print "    ABI: $ABI / $ABI32 (64-bit: $IS64BIT)"
ui_print "    HAL: $($IS_AIDL && echo AIDL || echo HIDL)"
ui_print "    Lib patch: $LIBPATCH"

# 1. Copy native libs universal
ui_print "    Copying native libs..."
# Pastikan folder ada
mkdir -p $MODPATH/system/vendor/lib/soundfx
mkdir -p $MODPATH/system/vendor/lib64/soundfx

# Copy sesuai ABI — fallback logic agar universal
if [ -f $MODPATH/common/files/libv4a_re_arm64-v8a.so ]; then
  cp_ch -n $MODPATH/common/files/libv4a_re_arm64-v8a.so $MODPATH/system/vendor/lib64/soundfx/libv4a_re.so
fi
if [ -f $MODPATH/common/files/libv4a_re_armeabi-v7a.so ]; then
  cp_ch -n $MODPATH/common/files/libv4a_re_armeabi-v7a.so $MODPATH/system/vendor/lib/soundfx/libv4a_re.so
  # Untuk device 32-only, lib64 tidak perlu; untuk 64, sediakan juga 32 compat
  if [ "$IS64BIT" = true ] && [ ! -f $MODPATH/system/vendor/lib64/soundfx/libv4a_re.so ]; then
    cp_ch -n $MODPATH/common/files/libv4a_re_armeabi-v7a.so $MODPATH/system/vendor/lib64/soundfx/libv4a_re.so
  fi
fi

# Legacy x86 support (emulator) — optional
if [ -f $MODPATH/common/files/libv4a_re_x86_64.so ]; then
  cp_ch -n $MODPATH/common/files/libv4a_re_x86_64.so $MODPATH/system/vendor/lib64/soundfx/libv4a_re.so 2>/dev/null || true
fi

# 2. Patch audio_effects — HIDL
if $IS_HIDL; then
  ui_print "    Patching audio_effects (HIDL)..."
  CFGS="$(find /odm /system /vendor -type f -name "*audio_effects*.conf" -o -name "*audio_effects*.xml" 2>/dev/null)"
  # Fallback: jika tidak ada di mirror, buat patch generik di vendor/etc
  if [ -z "$CFGS" ]; then
    CFGS="/vendor/etc/audio_effects.xml"
    ui_print "    → Config tidak ditemukan, buat overlay di $CFGS"
  fi

  for OFILE in ${CFGS}; do
    # Map path
    FILE="$MODPATH$(echo $OFILE | sed "s|^/vendor|/system/vendor|g")"
    # Jika OFILE tidak ada (fallback), skip copy, buat baru
    if [ -f "$OFILE" ]; then
      cp_ch -n $OFILE $FILE
    else
      mkdir -p $(dirname $FILE)
      echo '<?xml version="1.0" encoding="UTF-8"?><audio_effects_conf version="2.0"><libraries/><effects/></audio_effects_conf>' > $FILE
    fi

    case $FILE in
      *.conf)
        sed -i "/v4a_standard_re {/,/}/d" $FILE
        sed -i "/v4a_re {/,/}/d" $FILE
        sed -i "s/^effects {/effects {\n  v4a_standard_re {\n    library v4a_re\n    uuid 90380da3-8536-4744-a6a3-5731970e640f\n  }/g" $FILE
        sed -i "s/^libraries {/libraries {\n  v4a_re {\n    path $LIBPATCH\/lib\/soundfx\/libv4a_re.so\n  }/g" $FILE
        ui_print "      ✓ $OFILE (conf)"
        ;;
      *.xml)
        sed -i "/v4a_standard_re/d" $FILE
        sed -i "/v4a_re/d" $FILE
        sed -i "/<libraries>/ a\\        <library name=\"v4a_re\" path=\"libv4a_re.so\"/>" $FILE
        sed -i "/<effects>/ a\\        <effect name=\"v4a_standard_re\" library=\"v4a_re\" uuid=\"90380da3-8536-4744-a6a3-5731970e640f\"/>" $FILE
        ui_print "      ✓ $OFILE (xml)"
        ;;
    esac
  done
fi

# 3. Patch AIDL — audio_effects tidak dipakai, tapi butuh config di vendor/etc
if $IS_AIDL; then
  ui_print "    Patching AIDL audio config..."
  # AIDL pakai .xml di vendor/etc/audio_effects_config.xml atau vendor/etc/aidl
  # Kita tetap pasang lib + buat config stub agar service bisa load
  AIDL_CFG="$MODPATH/system/vendor/etc/audio_effects_config.xml"
  if [ ! -f "$AIDL_CFG" ]; then
    mkdir -p $(dirname $AIDL_CFG)
    cat > $AIDL_CFG << 'AIDL'
<?xml version="1.0" encoding="UTF-8"?>
<audioEffects>
    <library name="v4a_re" path="libv4a_re.so"/>
    <effect name="v4a_standard_re" library="v4a_re" uuid="90380da3-8536-4744-a6a3-5731970e640f"/>
</audioEffects>
AIDL
    ui_print "      ✓ Created AIDL config stub"
  else
    sed -i "/v4a_re/d" $AIDL_CFG 2>/dev/null || true
    sed -i "/<libraries>/ a\\        <library name=\"v4a_re\" path=\"libv4a_re.so\"/>" $AIDL_CFG 2>/dev/null || true
    sed -i "/<effects>/ a\\        <effect name=\"v4a_standard_re\" library=\"v4a_re\" uuid=\"90380da3-8536-4744-a6a3-5731970e640f\"/>" $AIDL_CFG 2>/dev/null || true
  fi
  # Flag untuk service.sh agar mount AIDL juga
  echo "AIDL" > $MODPATH/aidl_flag
fi

# 4. Sepolicy patch (jika magiskpolicy ada)
if command -v magiskpolicy >/dev/null 2>&1; then
  ui_print "    Patching sepolicy..."
  magiskpolicy --live "allow audioserver audioserver_tmpfs file { read write execute }" 2>/dev/null || true
  magiskpolicy --live "allow audioserver vendor_file file { read execute open }" 2>/dev/null || true
  magiskpolicy --live "allow hal_audio_default vendor_file file { read execute }" 2>/dev/null || true
fi

# 5. Buat prop untuk APK detection
cat > $MODPATH/system.prop << PROP
# SFX Glass — agar APK tahu module terpasang
ro.sfx.viper.version=3.0.0-Glass
ro.sfx.viper.hal=$($IS_AIDL && echo AIDL || echo HIDL)
PROP

# 6. Info untuk service.sh
echo "SFX Glass installed: $(date)" > $MODPATH/install.log
echo "ABI=$ABI IS64=$IS64BIT HAL=$($IS_AIDL && echo AIDL || echo HIDL)" >> $MODPATH/install.log

ui_print "    ✓ Universal install selesai"
