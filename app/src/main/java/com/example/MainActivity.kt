package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F1015)) // Deep luxury dark theme background
                ) { innerPadding ->
                    DubbingAppScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DubbingAppScreen(
    modifier: Modifier = Modifier,
    viewModel: DubViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val useMockMode by viewModel.useMockMode.collectAsState()
    val processState by viewModel.processState.collectAsState()
    val simulatedSubtitles by viewModel.simulatedSubtitles.collectAsState()

    // Video selector launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectVideo(context, uri)
        }
    }

    // Supported languages paired with flags/emojis & local display names
    val languages = listOf(
        Triple("bn", "Bengali", "🇧🇩"),
        Triple("en", "English", "🇺🇸"),
        Triple("es", "Spanish", "🇪🇸"),
        Triple("fr", "French", "🇫🇷"),
        Triple("de", "German", "🇩🇪"),
        Triple("hi", "Hindi", "🇮🇳"),
        Triple("it", "Italian", "🇮🇹"),
        Triple("pt", "Portuguese", "🇵🇹"),
        Triple("zh-cn", "Chinese", "🇨🇳")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Elegant header section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E2030))
                    .border(1.dp, Color(0xFF3B3E5B), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dub_logo),
                    contentDescription = "Dubbing Logo",
                    modifier = Modifier.size(38.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "AI Dubbing Engine",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Self-Voice Cloning & Lip-Sync Client",
                    color = Color(0xFFA0A5C0),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Section 1: Video File Selection Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("select_video_card")
                .clickable { videoPickerLauncher.launch("video/mp4") },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161722),
            ),
            border = borderStrokeForVideo(selectedVideo)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedVideo == null) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Upload Video Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "ভিডিও নির্বাচন করুন (Select Video)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Supported format: MP4 (Max 500MB as per engine rule)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF787E9E),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val video = selectedVideo!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video Asset",
                            tint = if (video.isOverLimit) Color(0xFFFF5252) else Color(0xFF3DDC84),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "ফাইল সাইজ: ${String.format("%.2f", video.sizeMb)} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (video.isOverLimit) Color(0xFFFF5252) else Color(0xFFA0A5C0)
                            )
                        }
                        if (!video.isOverLimit) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid size indicator",
                                tint = Color(0xFF3DDC84),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Strict limit validation warning
                    if (video.isOverLimit) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x1AFF5252),
                            modifier = Modifier.fillMaxWidth(),
                            border = borderStrokeForError()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error detail icon",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "ফাইল সাইজ অনেক বড়! অনুগ্রহ করে ৫০০ এমবি (500MB) এর নিচের ভিডিও আপলোড করুন।",
                                    color = Color(0xFFFF8B8B),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Choose Target Language
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Target Translation Language",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { (code, name, emoji) ->
                    val isSelected = targetLanguage == code
                    Box(
                        modifier = Modifier
                            .testTag("lang_chip_$code")
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E2030)
                            )
                            .clickable { viewModel.setTargetLanguage(code) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = name,
                                color = if (isSelected) Color.Black else Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Simple Environment Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161722)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Simulation Switch Icon",
                            tint = Color(0xFFA0A5C0),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Offline Demo (Simulation)",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Runs client mock of full pipeline out-of-the-box",
                                color = Color(0xFF787E9E),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Switch(
                        checked = useMockMode,
                        onCheckedChange = { viewModel.setUseMockMode(it) },
                        modifier = Modifier.testTag("mock_mode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                AnimatedVisibility(visible = !useMockMode) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Custom Server Endpoint Base URL",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { viewModel.setServerUrl(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("server_url_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E2030),
                                unfocusedContainerColor = Color(0xFF1E2030),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF3B3E5B)
                            ),
                            placeholder = { Text("http://10.0.2.2:8000/", color = Color(0xFF535777)) }
                        )
                        Text(
                            text = "Configure this to match your self-hosted FastAPI server. Cleartext HTTP is allowed.",
                            color = Color(0xFF787E9E),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Section 4: Process Step Tracker Visualizer
        if (processState is DubbingProcessState.Processing) {
            val state = processState as DubbingProcessState.Processing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1D2D)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ডাবিং প্রসেস চলছে (Processing)...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Stepper tracker
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DubbingStep.values().forEach { step ->
                            val isPassed = step.ordinal < state.currentStep.ordinal
                            val isActive = step == state.currentStep
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isPassed -> Color(0xFF3DDC84)
                                                isActive -> MaterialTheme.colorScheme.primary
                                                else -> Color(0xFF33364D)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPassed) {
                                        Text("✓", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text(
                                            text = (step.ordinal + 1).toString(),
                                            color = if (isActive) Color.Black else Color(0xFF787E9E),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = step.label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isActive || isPassed) Color.White else Color(0xFF535777),
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal
                                        )
                                    )
                                    if (isActive) {
                                        Text(
                                            text = step.description,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dynamic transcription subtitles console box
                    if (simulatedSubtitles.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF10111A),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF2C2E42))
                        ) {
                            Text(
                                text = simulatedSubtitles,
                                color = Color(0xFF3DDC84),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Completed result Video player
        if (processState is DubbingProcessState.Success) {
            val successState = processState as DubbingProcessState.Success
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151624)),
                border = BorderStroke(1.dp, Color(0xFF2C2E42))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Headline status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A382B))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF3DDC84)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = successState.infoMessage,
                            color = Color(0xFFE2F9EE),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Video view
                    VideoPlayerView(videoUri = successState.localVideoUri)

                    // Subtitles overlay representation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Translated Dub Dialogue Preview",
                            color = Color(0xFFA0A5C0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = Color(0xFF0F1015),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = simulatedSubtitles,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFFFFD54F),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Action Button
        val isButtonEnabled = selectedVideo != null && !selectedVideo!!.isOverLimit && processState !is DubbingProcessState.Processing
        val isProcessing = processState is DubbingProcessState.Processing

        Button(
            onClick = { viewModel.startDubbing(context) },
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_dubbing_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF1E2030),
                disabledContentColor = Color(0xFF535777)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ডাবিং হচ্ছে...",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                } else {
                    Icon(
                        imageVector = if (processState is DubbingProcessState.Success) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = "Submit",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (processState is DubbingProcessState.Success) "ডাবিং পুনরায় শুরু করুন" else "ডাবিং শুরু করুন (Start AI Dubbing)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Error log feedback
        if (processState is DubbingProcessState.Error) {
            val errState = processState as DubbingProcessState.Error
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0x1BFF5252),
                border = BorderStroke(1.dp, Color(0xFF4C2727))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error detail header",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডাবিং ব্যর্থ হয়েছে (Dubbing Failed)",
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = errState.errorMessage,
                        color = Color(0xFFFFBABA),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp)) // Safe visual spacing
    }
}

private fun borderStrokeForVideo(info: SelectedVideoInfo?): BorderStroke {
    return when {
         info == null -> BorderStroke(1.dp, Color(0xFF2C2E42))
         info.isOverLimit -> BorderStroke(2.dp, Color(0xFFFF5252))
         else -> BorderStroke(2.dp, Color(0xFF3DDC84))
    }
}

private fun borderStrokeForError() = BorderStroke(1.dp, Color(0x3DFF5252))
