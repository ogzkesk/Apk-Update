package com.ogzkesk.test

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ogzkesk.test.ui.theme.TestTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var configService: ConfigService

    private val updateChannel = Channel<VersionConfig>()
    private val updateFlow = updateChannel.receiveAsFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initConfigService()

        setContent {

            LaunchedEffect(key1 = updateFlow) {
                updateFlow.collect { newVersion ->

                    // Convert to bottomSheet or dialog

                    try {
                        val downloadApk =       DownloadApk(this@MainActivity)
                        downloadApk.startDownloadingApk(newVersion.versionUrl)
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Exception: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

            TestTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(


                        ),
//                    containerColor = Color.Red
                ) {
                    it
                        .calculateTopPadding(


                        )
                    Greeting()
                }
            }
        }
    }

    private fun initConfigService() {
        configService = Retrofit.Builder()
            .baseUrl(ConfigService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConfigService::class.java)
    }

    override fun onResume() {
        super.onResume()
        if (askPermission() && checkUnknownResources()) {
            checkForNewVersion()
        }
    }

    private fun checkForNewVersion() {
        lifecycleScope.launch {
            try {
                val newVersion = configService.getConfig()
                if (newVersion.versionCode != BuildConfig.VERSION_CODE) {
                    println("newVersion found: $newVersion")
                    updateChannel.send(newVersion)
                } else {
                    println("no newVersion found: $newVersion")
                }
            } catch (e: HttpException) {
                println("HttpException: message: ${e.message()} code: ${e.code()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun askPermission(): Boolean {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            return true
        }
        return if (
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1001
            )
            false
        } else {
            true
        }
    }

    private fun checkUnknownResources(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    this,
                    "Please allow unknown resources to get app updates",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName")
                    )
                )
                false
            } else {
                true
            }
        } else {
            val isNonPlayAppAllowed = Settings.Secure.getInt(
                contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS
            ) == 1
            if (!isNonPlayAppAllowed) {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                Toast.makeText(
                    this,
                    "Please allowed unknown resources to get app updates",
                    Toast.LENGTH_LONG
                ).show()
                false
            } else {
                true
            }
        }
    }
}

@Composable
fun Greeting() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Version name: ${BuildConfig.VERSION_NAME}\n" +
                    "Version code: ${BuildConfig.VERSION_CODE}"
        )
    }
}
