#!/system/bin/sh
# SFX Glass Uninstall — restore backups

if [ -f $INFO ]; then
  while read LINE; do
    if [ "$(echo -n $LINE | tail -c 1)" == "~" ]; then
      continue
    elif [ -f "$LINE~" ]; then
      mv -f $LINE~ $LINE
    else
      rm -f $LINE
      while true; do
        LINE=$(dirname $LINE)
        [ "$(ls -A $LINE 2>/dev/null)" ] && break 1 || rm -rf $LINE
      done
    fi
  done <$INFO
  rm -f $INFO
fi

# Cleanup work_mount
rm -rf /data/local/tmp/sfx_viper.log
rm -rf /data/adb/modules/SFX-ViPER-Glass/work_mount 2>/dev/null || true

# Reset prop
resetprop --delete ro.sfx.viper.active 2>/dev/null || true
resetprop --delete ro.sfx.viper.version 2>/dev/null || true
