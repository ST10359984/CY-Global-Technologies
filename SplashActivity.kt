package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        CoroutineScope(Dispatchers.Main).launch {
            val currentUid = UserManager.getLoggedInUid()

            if (currentUid != null) {
                val isAdmin = withContext(Dispatchers.IO) {
                    UserManager.isAdmin()
                }

                if (isAdmin) {
                    navigateTo(AdminDashboardActivity::class.java)
                } else {
                    navigateTo(MainActivity::class.java)
                }
            } else {
                navigateTo(LoginActivity::class.java)
            }
        }
    }

    private fun navigateTo(target: Class<*>) {
        startActivity(Intent(this, target))
        finish()
    }
}