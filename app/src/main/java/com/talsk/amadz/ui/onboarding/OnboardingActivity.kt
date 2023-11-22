package com.talsk.amadz.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.talsk.amadz.MainActivity
import com.talsk.amadz.ui.theme.AmadzTheme
import com.talsk.amadz.util.PermissionChecker

class OnboardingActivity : ComponentActivity() {


    private val dialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleActivityResult
    )

    private fun handleActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java)).also { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmadzTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    OnboardingScreen(onRequestDialerPermission = {
                        dialerLauncher.launch(PermissionChecker.changeDialogRequestUiIntent(this))
                    })
                }
            }
        }
    }


}

