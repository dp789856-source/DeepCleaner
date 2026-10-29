package com.cleaner.security

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnScan: Button
    private lateinit var btnClean: Button
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val detectedJunk = mutableListOf<File>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkManageStoragePermission()
        } else {
            Toast.makeText(this, "Permissions required for deep cleaning", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F172A"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val header = TextView(this).apply {
            text = "SHIELD CLEANER & ANTIVIRUS"
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 70, 0, 30)
        }
        rootLayout.addView(header)

        val bodyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 20, 40, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        tvStatus = TextView(this).apply {
            text = "Ready to analyze system health, junk files, and background threats."
            textSize = 15f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 40)
        }

        btnScan = Button(this).apply {
            text = "START DEEP SCAN"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2563EB"))
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(30, 25, 30, 25)
            setOnClickListener { checkAllPermissionsAndRun() }
        }

        btnClean = Button(this).apply {
            text = "CLEAN ALL JUNK"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#16A34A"))
            textSize = 16f
            isEnabled = false
            setTypeface(null, Typeface.BOLD)
            setPadding(30, 25, 30, 25)
            setOnClickListener { cleanJunkFiles() }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 25, 0, 0) }
            layoutParams = params
        }

        bodyLayout.addView(btnScan)
        bodyLayout.addView(btnClean)
        bodyLayout.addView(tvStatus)
        rootLayout.addView(bodyLayout)

        val footerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 40)
            setBackgroundColor(Color.parseColor("#090D16"))
        }

        val tvSecurityBadge = TextView(this).apply {
            text = "🔒 100% PRIVATE • LOCAL ON-DEVICE ENGINE"
            textSize = 11f
            setTextColor(Color.parseColor("#38BDF8"))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val tvCopyright = TextView(this).apply {
            text = "Deep Cleaner & Security Core • Version 1.0.0"
            textSize = 12f
            setTextColor(Color.parseColor("#64748B"))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        footerLayout.addView(tvSecurityBadge)
        footerLayout.addView(tvCopyright)
        rootLayout.addView(footerLayout)

        setContentView(rootLayout)
        rpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63()
    }

    private fun rpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        } else {
            checkManageStoragePermission()
        }
    }

    private fun checkManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this)
                    .setTitle("Storage Access Permission")
                    .setMessage("Deep Cleaning requires All Files Access to scan and delete junk cache.")
                    .setPositiveButton("Allow") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:" + packageName)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun checkAllPermissionsAndRun() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            checkManageStoragePermission()
            return
        }
        runScan()
    }

    private fun runScan() {
        tvStatus.text = "Deep scanning in progress...\nAnalyzing junk files and app threats."
        btnScan.isEnabled = false
        detectedJunk.clear()

        scope.launch {
            val root = Environment.getExternalStorageDirectory()
            var junkBytes = 0L

            withContext(Dispatchers.IO) {
                root?.walkTopDown()?.forEach { file ->
                    val ext = file.extension.lowercase()
                    if (ext in listOf("tmp", "log", "bak", "cache", "dmp")) {
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
                    val hasOverlay = perms.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    val hasSms = perms.contains(Manifest.permission.RECEIVE_SMS)
                    if (hasOverlay && hasSms) threatCount++
                }
            }

            val mb = junkBytes / (1024 * 1024)
            tvStatus.text = "Scan Completed!\n\n🗑️ Junk Found: " + mb + " MB (" + detectedJunk.size + " files)\n🛡️ Potential Threats: " + threatCount
            btnScan.isEnabled = true
            btnClean.isEnabled = detectedJunk.isNotEmpty()
        }
    }

    private fun cleanJunkFiles() {
        scope.launch {
            tvStatus.text = "Cleaning junk files safely..."
            var freedBytes = 0L
            withContext(Dispatchers.IO) {
                detectedJunk.forEach { file ->
                    val len = file.length()
                    if (file.delete()) freedBytes += len
                }
            }
            detectedJunk.clear()
            btnClean.isEnabled = false
            tvStatus.text = "Cleanup Successful!\nFreed: " + (freedBytes / (1024 * 1024)) + " MB"
            Toast.makeText(this@MainActivity, "Cleaned successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}