package com.sfx.viperglass.data

// Mirror dari ViPER4Android RE original, diperluas untuk Glass
data class DeviceSettings(
    val deviceId: String,
    val deviceName: String,
    val device: String, // speaker / headphone / bluetooth_a2dp / usb
    val enabled: Boolean = true,
    val isHeadphone: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis(),
    val settingsJson: String = EffectState().toJson(),
    val isFavorite: Boolean = false
)

data class EffectState(
    val masterEnable: Boolean = true,
    val playbackGain: PlaybackGainState = PlaybackGainState(),
    val fetCompressor: FetCompressorState = FetCompressorState(),
    val convolver: ConvolverState = ConvolverState(),
    val ddc: DdcState = DdcState(),
    val spectrumExtension: SpectrumExtensionState = SpectrumExtensionState(),
    val eq: EqState = EqState(),
    val bass: BassState = BassState(),
    val clarity: ClarityState = ClarityState(),
    val cure: CureState = CureState(),
    val reverb: ReverbState = ReverbState(),
    val fieldSurround: FieldSurroundState = FieldSurroundState(),
    val headphoneSurround: HeadphoneSurroundState = HeadphoneSurroundState(),
    val diffSurround: DiffSurroundState = DiffSurroundState(),
    val analogX: AnalogXState = AnalogXState(),
    val speakerCorrection: SpeakerCorrectionState = SpeakerCorrectionState(),
    val limiter: LimiterState = LimiterState(),
    val crossfeed: CrossfeedState = CrossfeedState(),
) {
    fun toJson(): String = """{"master":$masterEnable}""" // simplified, real uses kotlinx.serialization
}

data class EqState(val enable: Boolean = true, val gains: List<Float> = List(10){0f}, val bandEnables: List<Boolean> = List(10){true})
data class ConvolverState(val enable: Boolean = false, val kernel: String = "")
data class DdcState(val enable: Boolean = false, val device: String = "")
data class ReverbState(val enable: Boolean = false, val roomSize: Int = 50, val width: Int = 50, val damping: Int = 50)
data class BassState(val enable: Boolean = false, val freq: Int = 80, val gain: Int = 0)
data class BassMonoState(val enable: Boolean = false)
data class ClarityState(val enable: Boolean = false, val gain: Float = 6f)
data class FetCompressorState(val enable: Boolean = false, val threshold: Int = -20)
data class PlaybackGainState(val enable: Boolean = false, val maxGain: Int = 0)
data class CureState(val enable: Boolean = false)
data class AnalogXState(val enable: Boolean = false)
data class FieldSurroundState(val enable: Boolean = false)
data class HeadphoneSurroundState(val enable: Boolean = false)
data class DiffSurroundState(val enable: Boolean = false)
data class SpectrumExtensionState(val enable: Boolean = false)
data class SpeakerCorrectionState(val enable: Boolean = false)
data class LimiterState(val enable: Boolean = false, val threshold: Float = -1f)
data class CrossfeedState(val enable: Boolean = false, val preset: String = "Natural")

data class Preset(val id: Long = 0, val name: String, val deviceId: String, val settingsJson: String, val artworkUri: String? = null, val tags: String = "", val likes: Int = 0)
data class EqPreset(val id: Long=0, val name: String, val gains: List<Float>)
data class AppProfile(val id: Long=0, val packageName: String, val deviceId: String, val settingsJson: String)

enum class ViPERDevice(val id: String, val label: String, val icon: String) {
    SPEAKER("speaker","Speaker","🔊"),
    HEADPHONE("headphone","Headphone","🎧"),
    BLUETOOTH("bluetooth_a2dp","Bluetooth","󰂰"),
    USB("usb","USB Audio","🔌")
}
