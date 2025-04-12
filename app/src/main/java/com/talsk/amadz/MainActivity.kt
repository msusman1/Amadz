package com.talsk.amadz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.talsk.amadz.ui.MainNavGraph
import com.talsk.amadz.ui.home.MainViewModel
import com.talsk.amadz.ui.onboarding.OnboardingActivity
import com.talsk.amadz.ui.theme.AmadzTheme
import com.talsk.amadz.util.PermissionChecker

class MainActivity : ComponentActivity() {

    private val vm by viewModels<MainViewModel>()
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), {})

    private var phoneNumberState = mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleDialIntent(intent)
        checkPermission()
        setContent {
            AmadzTheme {
                val contacts by vm.contacts.collectAsState()
                val callLogs by vm.callLogs.collectAsState()
                val phoneNumber = phoneNumberState.value
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainNavGraph(contacts, callLogs, vm::toggleFavourite, phoneNumber = phoneNumber)
                }
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
                phoneNumberState.value = phoneNumber
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.reloadData()
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
