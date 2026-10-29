package com.cleaner.security

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnScan: Button
    private lateinit var btnClean: Button
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val detectedJunk = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
        }

        tvStatus = TextView(this).apply {
            text = "Tap Start Scan to clean junk & check security."
            textSize = 16f
        }
        btnScan = Button(this).apply {
            text = "Start Scan"
            setOnClickListener { checkStorageAndScan() }
        }
        btnClean = Button(this).apply {
            text = "Clean All Junk"
            isEnabled = false
            setOnClickListener { cleanJunkFiles() }
        }

        layout.addView(btnScan)
        layout.addView(btnClean)
        layout.addView(tvStatus)
        setContentView(layout)
    }

    private fun checkStorageAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:" + packageName)
                }
                startActivity(intent)
                return
            }
        }
        runScan()
    }

    private fun runScan() {
        tvStatus.text = "Scanning Storage & Apps..."
        detectedJunk.clear()
        
        scope.launch {
            val root = Environment.getExternalStorageDirectory()
            var junkBytes = 0L

            withContext(Dispatchers.IO) {
                root?.walkTopDown()?.forEach { file ->
                    val ext = file.extension.lowercase()
                    if (ext in listOf("tmp", "log", "bak", "cache")) {
                        detectedJunk.add(file)
                        junkBytes += file.length()
                    }
                }
            }

            var threatCount = 0
            val packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (pkg in packages) {
                val isSystem = (pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                if (!isSystem) {
                    val perms = pkg.requestedPermissions?.toList() ?: emptyList()
                    val hasOverlay = perms.contains(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
                    val hasSms = perms.contains(android.Manifest.permission.RECEIVE_SMS)
                    if (hasOverlay && hasSms) threatCount++
                }
            }

            val mb = junkBytes / (1024 * 1024)
            tvStatus.text = "Scan Complete!\n\nJunk Found: " + mb + " MB (" + detectedJunk.size + " files)\nRisky Apps: " + threatCount
            btnClean.isEnabled = detectedJunk.isNotEmpty()
        }
    }

    private fun cleanJunkFiles() {
        scope.launch {
            tvStatus.text = "Cleaning files..."
            var freedBytes = 0L
            withContext(Dispatchers.IO) {
                detectedJunk.forEach { file ->
                    val len = file.length()
                    if (file.delete()) freedBytes += len
                }
            }
            detectedJunk.clear()
            btnClean.isEnabled = false
            tvStatus.text = "Cleaned " + (freedBytes / (1024 * 1024)) + " MB successfully!"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}