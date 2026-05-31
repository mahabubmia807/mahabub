package com.example

import android.net.Uri

enum class DubbingStep(val label: String, val description: String) {
    UPLOAD("Uploading Video", "Uploading MP4 file to server..."),
    AUDIO_EXTRACT("Extracting Audio", "Extracting original audio track via FFmpeg..."),
    WHISPER_TRANSCRIBE("Whisper Transcription", "Transcribing speech from extracted audio..."),
    TRANSLATE("Translating Script", "Converting language while preserving context..."),
    XTTS_CLONING("XTTS Voice Cloning", "Generating target language voice with original voice reference..."),
    WAV2LIP_SYNC("Wav2Lip Lip-Syncing", "Matching video lip movements to cloned voice..."),
    DOWNLOAD("Downloading Result", "Retrieving final dubbed video file..."),
    COMPLETED("Completed", "Video dubbed successfully!")
}

sealed interface DubbingProcessState {
    object Idle : DubbingProcessState
    data class Processing(val currentStep: DubbingStep, val progress: Float = 0.0f) : DubbingProcessState
    data class Success(val localVideoUri: Uri, val infoMessage: String) : DubbingProcessState
    data class Error(val errorMessage: String) : DubbingProcessState
}

data class SelectedVideoInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val durationSeconds: Int? = null
) {
    val sizeMb: Double get() = sizeBytes / (1024.0 * 1024.0)
    val isOverLimit: Boolean get() = sizeBytes > 524288000 // 500 MB
}
