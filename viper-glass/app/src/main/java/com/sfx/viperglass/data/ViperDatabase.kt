package com.sfx.viperglass.data

import androidx.room.*

@Entity(tableName = "device_settings")
data class DeviceSettingsEntity(
    @PrimaryKey val deviceId: String,
    val device: String,
    val deviceName: String,
    val enabled: Boolean,
    val isHeadphone: Boolean,
    val lastConnected: Long,
    val settingsJson: String,
    val isFavorite: Boolean = false
)

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val deviceId: String,
    val settingsJson: String,
    val artworkUri: String? = null,
    val tags: String = "",
    val likes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ViperDao {
    @Query("SELECT * FROM device_settings ORDER BY lastConnected DESC")
    suspend fun getDevices(): List<DeviceSettingsEntity>

    @Query("SELECT * FROM device_settings WHERE deviceId = :id")
    suspend fun getDevice(id: String): DeviceSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(e: DeviceSettingsEntity)

    @Query("SELECT * FROM presets ORDER BY createdAt DESC")
    suspend fun getPresets(): List<PresetEntity>

    @Insert suspend fun insertPreset(e: PresetEntity): Long
    @Delete suspend fun deletePreset(e: PresetEntity)
    @Query("SELECT * FROM presets WHERE id = :id") suspend fun getPreset(id: Long): PresetEntity?
}

@Database(entities = [DeviceSettingsEntity::class, PresetEntity::class], version = 3, exportSchema = true)
abstract class ViperDatabase : RoomDatabase() {
    abstract fun dao(): ViperDao
}
