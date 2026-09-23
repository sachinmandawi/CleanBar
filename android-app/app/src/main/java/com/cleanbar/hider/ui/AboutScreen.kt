package com.cleanbar.hider.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cleanbar.hider.R
import com.cleanbar.hider.ui.theme.*
import com.cleanbar.hider.util.UpdateChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEVELOPER_EMAIL = "sachinmandawi@gmail.com"
private const val GITHUB_REPO_URL = "https://github.com/sachinmandawi/CleanBar"
private const val GITHUB_ISSUES_URL = "https://github.com/sachinmandawi/CleanBar/issues"
private const val UPI_ID = "darkcaptain@ybl"
private const val BINANCE_UID = "1138545342"

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateChecker.UpdateResult?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Bottom Toast State
    var bottomToastMessage by remember { mutableStateOf<String?>(null) }

    fun triggerBottomToast(message: String) {
        bottomToastMessage = message
        coroutineScope.launch {
            delay(2500)
            if (bottomToastMessage == message) {
                bottomToastMessage = null
            }
        }
    }

    // Handle Android native back gesture
    BackHandler(enabled = true) {
        if (showUpdateDialog) {
            showUpdateDialog = false
        } else {
            doVibrate(context)
            onBack()
        }
    }

    // Update Available Dialog
    if (showUpdateDialog && updateResult != null) {
        Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2F3A))
                            .border(1.dp, Color(0xFF2B527E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = CleanBarBlueText,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Update Available 🚀",
                        color = CleanBarTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "CleanBar ${updateResult?.latestVersion} is ready to download.",
                        color = CleanBarTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showUpdateDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanBarDarkBg,
                                contentColor = CleanBarTextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanBarDarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Later", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showUpdateDialog = false
                                doVibrate(context)
                                openBrowser(context, updateResult?.downloadUrl ?: GITHUB_REPO_URL)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanBarBlueText,
                                contentColor = Color(0xFF191919)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Download", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBarDarkBg)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // Top Navigation Bar
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CleanBarDarkSurface)
                        .border(1.dp, CleanBarDarkBorder, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            doVibrate(context)
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CleanBarTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "About",
                    color = CleanBarTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF15261E))
                        .border(1.dp, Color(0xFF224E3A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "v1.1.0",
                        color = CleanBarGreenText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ==========================================
            // Developer Card (Ultra Minimal)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.dev_avatar),
                        contentDescription = "Sachin Mandawi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF3E3E3E), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Sachin Mandawi",
                            color = CleanBarTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Developer",
                            color = CleanBarTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // Support Section
            // ==========================================
            MinimalSectionTitle(title = "SUPPORT")

            Spacer(modifier = Modifier.height(6.dp))

            // UPI
            MinimalCard(
                drawableId = R.drawable.ic_upi_logo,
                iconBg = Color(0xFF19251E),
                title = "UPI • $UPI_ID",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy UPI ID",
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    clipboardManager.setText(AnnotatedString(UPI_ID))
                    triggerBottomToast("✓ Copied: $UPI_ID")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Binance
            MinimalCard(
                drawableId = R.drawable.ic_binance_logo,
                iconBg = Color(0xFF292416),
                title = "Binance • $BINANCE_UID",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Binance UID",
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    clipboardManager.setText(AnnotatedString(BINANCE_UID))
                    triggerBottomToast("✓ Copied: $BINANCE_UID")
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // Links Section
            // ==========================================
            MinimalSectionTitle(title = "LINKS")

            Spacer(modifier = Modifier.height(6.dp))

            // Check Updates
            MinimalCard(
                icon = Icons.Default.SystemUpdate,
                iconTint = CleanBarBlueText,
                iconBg = Color(0xFF18222E),
                title = "Check for Updates",
                trailingContent = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            color = CleanBarBlueText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Text(
                            text = "v1.1.0",
                            color = CleanBarTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                onClick = {
                    doVibrate(context)
                    if (isCheckingUpdate) return@MinimalCard
                    isCheckingUpdate = true
                    coroutineScope.launch {
                        val res = UpdateChecker.checkForUpdates(UpdateChecker.CURRENT_VERSION)
                        isCheckingUpdate = false
                        res.onSuccess { update ->
                            if (update.hasUpdate) {
                                updateResult = update
                                showUpdateDialog = true
                            } else {
                                triggerBottomToast("CleanBar is up to date 🎉")
                            }
                        }.onFailure {
                            triggerBottomToast("CleanBar is up to date 🎉")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gmail (With Copy icon on the left of Launch icon)
            MinimalCard(
                icon = Icons.Default.Email,
                iconTint = CleanBarRedText,
                iconBg = Color(0xFF261919),
                title = "Gmail",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Copy Email Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    doVibrate(context)
                                    clipboardManager.setText(AnnotatedString(DEVELOPER_EMAIL))
                                    triggerBottomToast("✓ Copied: $DEVELOPER_EMAIL")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = CleanBarTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Launch Email Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    doVibrate(context)
                                    sendEmail(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Gmail",
                                tint = CleanBarTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                },
                onClick = {
                    doVibrate(context)
                    sendEmail(context)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GitHub
            MinimalCard(
                icon = Icons.Default.Code,
                iconTint = CleanBarGreenText,
                iconBg = Color(0xFF17241F),
                title = "GitHub",
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    openBrowser(context, GITHUB_REPO_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Report Bug
            MinimalCard(
                icon = Icons.Default.BugReport,
                iconTint = Color(0xFFFFB86C),
                iconBg = Color(0xFF261F17),
                title = "Report Bug",
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    openBrowser(context, GITHUB_ISSUES_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Share App
            MinimalCard(
                icon = Icons.Default.Share,
                iconTint = Color(0xFFC678DD),
                iconBg = Color(0xFF231926),
                title = "Share App",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    shareCleanBar(context)
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ==========================================
        // FLOATING BOTTOM TOAST (Neeche me Toast)
        // ==========================================
        AnimatedVisibility(
            visible = bottomToastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF252525))
                    .border(1.dp, Color(0xFF3E3E3E), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CleanBarGreenText,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = bottomToastMessage ?: "",
                        color = CleanBarTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalSectionTitle(title: String) {
    Text(
        text = title,
        color = CleanBarTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun MinimalCard(
    icon: ImageVector? = null,
    drawableId: Int? = null,
    iconTint: Color = Color.Unspecified,
    iconBg: Color,
    title: String,
    trailingContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CleanBarDarkSurface)
            .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (drawableId != null) {
                        Icon(
                            painter = painterResource(id = drawableId),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    color = CleanBarTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            trailingContent()
        }
    }
}

/**
 * Triggers intent to open Gmail / default email client.
 */
private fun sendEmail(context: Context) {
    val subject = "[CleanBar] Feedback"
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$DEVELOPER_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(emailIntent, "Open with..."))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Developer Email", DEVELOPER_EMAIL)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Email copied: $DEVELOPER_EMAIL", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Opens a web URL in browser.
 */
private fun openBrowser(context: Context, url: String) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Shares CleanBar link.
 */
private fun shareCleanBar(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "CleanBar")
            putExtra(Intent.EXTRA_TEXT, GITHUB_REPO_URL)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share CleanBar"))
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}
