package com.talsk.amadz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.talsk.amadz.ui.MainNavGraph
import com.talsk.amadz.ui.onboarding.OnboardingActivity
import com.talsk.amadz.ui.theme.AmadzTheme
import com.talsk.amadz.util.PermissionChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), {})

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleDialIntent(intent)
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
        if (intent.action == Intent.ACTION_DIAL || intent.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            val phoneNumber = data?.schemeSpecificPart
            if (!phoneNumber.isNullOrBlank()) {
            }
        }
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
