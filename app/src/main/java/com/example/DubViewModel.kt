package com.example

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DubViewModel : ViewModel() {

    private val _selectedVideo = MutableStateFlow<SelectedVideoInfo?>(null)
    val selectedVideo: StateFlow<SelectedVideoInfo?> = _selectedVideo.asStateFlow()

    private val _targetLanguage = MutableStateFlow("en")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _serverUrl = MutableStateFlow("https://ais-dev-nfsjx4j4fb7fuzwe4hbrzl-808238950186.asia-southeast1.run.app")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _useMockMode = MutableStateFlow(true)
    val useMockMode: StateFlow<Boolean> = _useMockMode.asStateFlow()

    private val _processState = MutableStateFlow<DubbingProcessState>(DubbingProcessState.Idle)
    val processState: StateFlow<DubbingProcessState> = _processState.asStateFlow()

    private val _simulatedSubtitles = MutableStateFlow<String>("")
    val simulatedSubtitles: StateFlow<String> = _simulatedSubtitles.asStateFlow()

    fun selectVideo(context: Context, uri: Uri) {
        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                if (c.moveToFirst()) {
                    val name = if (nameIndex != -1) c.getString(nameIndex) else "video.mp4"
                    val size = if (sizeIndex != -1) c.getLong(sizeIndex) else 0L
                    _selectedVideo.value = SelectedVideoInfo(uri, name, size)
                } else {
                    _selectedVideo.value = SelectedVideoInfo(uri, "selected_video.mp4", 0L)
                }
            } ?: run {
                _selectedVideo.value = SelectedVideoInfo(uri, "selected_video.mp4", 0L)
            }
            // Clear prior states on select
            _processState.value = DubbingProcessState.Idle
            _simulatedSubtitles.value = ""
        } catch (e: Exception) {
            _processState.value = DubbingProcessState.Error("Failed to resolve video file info: ${e.localizedMessage}")
        }
    }

    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }

    fun setServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun setUseMockMode(enabled: Boolean) {
        _useMockMode.value = enabled
    }

    fun startDubbing(context: Context) {
        val video = _selectedVideo.value
        if (video == null) {
            _processState.value = DubbingProcessState.Error("Please select a video file first.")
            return
        }

        if (video.isOverLimit) {
            _processState.value = DubbingProcessState.Error("ফাইল সাইজ অনেক বড়! অনুগ্রহ করে ৫০০ এমবি (500MB) এর নিচের ভিডিও আপলোড করুন।")
            return
        }

        viewModelScope.launch {
            if (_useMockMode.value) {
                runSimulation(video)
            } else {
                runRealDubbing(context, video)
            }
        }
    }

    private suspend fun runSimulation(video: SelectedVideoInfo) {
        _simulatedSubtitles.value = "Initializing processing..."
        
        // Step 1: Upload
        _processState.value = DubbingProcessState.Processing(DubbingStep.UPLOAD, 0.1f)
        for (i in 1..5) {
            delay(400)
            _processState.value = DubbingProcessState.Processing(DubbingStep.UPLOAD, 0.1f + (i * 0.15f))
        }

        // Step 2: Audio Extract
        _processState.value = DubbingProcessState.Processing(DubbingStep.AUDIO_EXTRACT, 0.0f)
        _simulatedSubtitles.value = "FFmpeg: Extracting stereo audio at 44100Hz from video..."
        delay(1800)
        _processState.value = DubbingProcessState.Processing(DubbingStep.AUDIO_EXTRACT, 1.0f)

        // Step 3: Transcription
        _processState.value = DubbingProcessState.Processing(DubbingStep.WHISPER_TRANSCRIBE, 0.0f)
        _simulatedSubtitles.value = "Whisper AI: Analyzing voice prints and transcribing speech..."
        delay(2000)
        _processState.value = DubbingProcessState.Processing(DubbingStep.WHISPER_TRANSCRIBE, 1.0f)

        // Step 4: Translate
        _processState.value = DubbingProcessState.Processing(DubbingStep.TRANSLATE, 0.0f)
        val translationText = getTranslatedSample()
        _simulatedSubtitles.value = "Translator: Translating dialogue to [${_targetLanguage.value.uppercase()}]..."
        delay(1500)
        _processState.value = DubbingProcessState.Processing(DubbingStep.TRANSLATE, 1.0f)

        // Step 5: Voice Cloning XTTS
        _processState.value = DubbingProcessState.Processing(DubbingStep.XTTS_CLONING, 0.0f)
        _simulatedSubtitles.value = "XTTS v2: Cloning original voice; synthesizing text: \"$translationText\""
        delay(2500)
        _processState.value = DubbingProcessState.Processing(DubbingStep.XTTS_CLONING, 1.0f)

        // Step 6: Lip Sync
        _processState.value = DubbingProcessState.Processing(DubbingStep.WAV2LIP_SYNC, 0.0f)
        _simulatedSubtitles.value = "Wav2Lip: Rendering GAN model for frame-by-frame mouth synchronization..."
        delay(2500)
        _processState.value = DubbingProcessState.Processing(DubbingStep.WAV2LIP_SYNC, 1.0f)

        // Step 7: Download
        _processState.value = DubbingProcessState.Processing(DubbingStep.DOWNLOAD, 0.5f)
        delay(1000)
        
        // Simulating completion by assigning the original user video to play, 
        // with our dubbed voice & subtitle layer simulation on top!
        _simulatedSubtitles.value = "[Dubbed Subtitle in ${_targetLanguage.value.uppercase()}]: $translationText"
        _processState.value = DubbingProcessState.Success(
            localVideoUri = video.uri,
            infoMessage = "ভিডিও ডাবিং সফল হয়েছে! (Simulated Mode active with dubbed subtitles)"
        )
    }

    private suspend fun runRealDubbing(context: Context, video: SelectedVideoInfo) {
        try {
            _simulatedSubtitles.value = "Preparing local file stream..."
            _processState.value = DubbingProcessState.Processing(DubbingStep.UPLOAD, 0.1f)

            // Copy URI stream to a physical cache file so retrofit can package as body
            val contentResolver = context.contentResolver
            val tempFile = File(context.cacheDir, "temp_dub_input_${System.currentTimeMillis()}.mp4")
            
            contentResolver.openInputStream(video.uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Failed to open video source stream")

            if (tempFile.length() > 524288000) {
                tempFile.delete()
                throw Exception("ফাইল সাইজ অনেক বড়! অনুগ্রহ করে ৫০০ এমবি (500MB) এর নিচের ভিডিও আপলোড করুন।")
            }

            _simulatedSubtitles.value = "Uploading video file (${String.format("%.2f", tempFile.length() / (1024.0 * 1024.0))} MB)..."
            
            // Build trailing-slashed server url
            var formattedUrl = _serverUrl.value.trim()
            if (!formattedUrl.endsWith("/")) {
                formattedUrl += "/"
            }

            // Bootstrap custom Retrofit
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
                .readTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
                .writeTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val api = retrofit.create(DubbingApi::class.java)

            // Prepare multipart
            val requestFile = tempFile.asRequestBody("video/mp4".toMediaTypeOrNull())
            val videoPart = MultipartBody.Part.createFormData("video", tempFile.name, requestFile)
            val langPart = _targetLanguage.value.toRequestBody("text/plain".toMediaTypeOrNull())

            _processState.value = DubbingProcessState.Processing(DubbingStep.AUDIO_EXTRACT, 0.3f)
            _simulatedSubtitles.value = "Backend processing triggered: Uploading..."

            val response = api.selfDubVideo(videoPart, langPart)
            
            // Clean local input temp
            tempFile.delete()

            if (response.isSuccessful) {
                val dbResult = response.body()
                if (dbResult != null && dbResult.success && !dbResult.download_url.isNullOrEmpty()) {
                    _processState.value = DubbingProcessState.Processing(DubbingStep.DOWNLOAD, 0.1f)
                    _simulatedSubtitles.value = "Dubbing succeed! Starting file download..."

                    // Build download url
                    val rawUrl = dbResult.download_url
                    val finalDownloadUrl = if (rawUrl.startsWith("http")) {
                        rawUrl
                    } else {
                        formattedUrl + rawUrl.removePrefix("/")
                    }

                    val downloadResponse = api.downloadFile(finalDownloadUrl)
                    if (downloadResponse.isSuccessful) {
                        val body = downloadResponse.body()
                        if (body != null) {
                            val outFile = File(context.filesDir, "dubbed_${System.currentTimeMillis()}.mp4")
                            FileOutputStream(outFile).use { output ->
                                body.byteStream().use { input ->
                                    input.copyTo(output)
                                }
                            }
                            _simulatedSubtitles.value = "Saved successfully: ${outFile.name}"
                            _processState.value = DubbingProcessState.Success(
                                localVideoUri = Uri.fromFile(outFile),
                                infoMessage = dbResult.message ?: "ভিডিও ডাবিং সফল হয়েছে!"
                            )
                        } else {
                            throw Exception("Empty download stream payload from engine.")
                        }
                    } else {
                        throw Exception("Failed to retrieve resulting dubbed video from stream.")
                    }
                } else {
                    val errMsg = dbResult?.error ?: dbResult?.message ?: "Unknown backend pipeline processing exception."
                    throw Exception(errMsg)
                }
            } else {
                throw Exception("HTTP Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            _processState.value = DubbingProcessState.Error(e.localizedMessage ?: "Network or engine process timeout.")
        }
    }

    private fun getTranslatedSample(): String {
        return when (_targetLanguage.value) {
            "bn" -> "হ্যালো বন্ধুরা! কৃত্রিম বুদ্ধিমত্তা চালিত ডাবিং এবং লিপ সিঙ্ক ইঞ্জিনে আপনাকে স্বাগতম।"
            "es" -> "¡Hola amigos! Bienvenidos al motor de doblaje y sincronización labial impulsado por la IA."
            "fr" -> "Bonjour mes amis! Bienvenue dans le moteur de doublage et de synchronisation labiale par l'IA."
            "de" -> "Hallo Freunde! Willkommen bei der KI-gesteuerten Synchronisations- und Lippensynchronisations-Engine."
            "it" -> "Ciao amici! Benvenuti nel motore di doppiaggio e sincronizzazione labiale basato sull'intelligenza artificiale."
            "pt" -> "Olá amigos! Bem-vindos ao motor de dublagem e sincronização labial alimentado por IA."
            "hi" -> "नमस्ते दोस्तों! संवर्धित कृत्रिम बुद्धिमत्ता डबिंग और लिप-सिंक इंजन में आपका स्वागत है।"
            "zh-cn" -> "你好朋友！欢迎来到人工智能驱动的视频配音和口型同步引擎。"
            else -> "Hello friends! Welcome to the robust artificial intelligence dubbing and lip-syncing engine."
        }
    }
}
