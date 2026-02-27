package com.talsk.amadz

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.talsk.amadz.core.dial
import com.talsk.amadz.core.hasDefaultCallingSimConfigured
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.ui.MainNavGraph
import com.talsk.amadz.ui.onboarding.OnboardingActivity
import com.talsk.amadz.ui.theme.AmadzTheme
import com.talsk.amadz.util.PermissionChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var simInfoProvider: SimInfoProvider

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), {})

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            handleDialIntent(intent)
        }
        checkPermission()
        setContent {
            AmadzTheme {
                MainNavGraph()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDialIntent(intent)
    }

    private fun handleDialIntent(intent: Intent) {
        val phoneNumber = extractPhoneNumber(intent) ?: return

        when (intent.action) {
            Intent.ACTION_CALL,
            Intent.ACTION_VOICE_COMMAND,
            Intent.ACTION_CALL_BUTTON,
            Intent.ACTION_DIAL,
            Intent.ACTION_VIEW -> requestDial(phoneNumber)
        }
    }

    private fun requestDial(phone: String) {
        val sims = simInfoProvider.getSimsInfo().sortedBy { it.simSlotIndex }
        if (sims.size > 1 && !hasDefaultCallingSimConfigured()) {
            val items = sims.map {
                it.displayName?.takeIf(String::isNotBlank) ?: "SIM ${it.simSlotIndex + 1}"
            }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Choose SIM")
                .setItems(items) { dialog, which ->
                    val selected = sims.getOrNull(which)
                    if (selected != null) {
                        dial(phone = phone, accountId = selected.accountId)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            dial(phone)
        }
    }

    private fun extractPhoneNumber(intent: Intent): String? {
        val data: Uri? = intent.data
        val fromData = data?.schemeSpecificPart?.takeIf { it.isNotBlank() }
        if (fromData != null) return fromData

        val fromExtra = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)?.takeIf { it.isNotBlank() }
        if (fromExtra != null) return fromExtra

        return null
    }


    private fun checkPermission() {
        val defaultPhoneApp = PermissionChecker.isDefaultPhoneApp(this)
        if (defaultPhoneApp.not()) {
            startActivity(Intent(this, OnboardingActivity::class.java)).also { finish() }
        } else {
            if (PermissionChecker.hasAllPermissions(this).not()) {
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.CALL_PHONE,
                        android.Manifest.permission.READ_CALL_LOG,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS
                    )
                )
            }
        }
    }
}
