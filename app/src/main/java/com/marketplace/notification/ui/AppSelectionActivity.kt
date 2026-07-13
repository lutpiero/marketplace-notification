package com.marketplace.notification.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.marketplace.notification.R
import com.marketplace.notification.data.AppConfig
import com.marketplace.notification.databinding.ActivityAppSelectionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private val viewModel: ConfigViewModel by viewModels()
    private lateinit var selectionAdapter: AppSelectionAdapter
    private var allApps: List<AppInfo> = emptyList()

    data class AppInfo(val packageName: String, val appName: String, var isAdded: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectionAdapter = AppSelectionAdapter { appInfo ->
            val config = AppConfig(
                packageName = appInfo.packageName,
                appName = appInfo.appName,
                enabled = true,
                retriggerDelayMinutes = 30
            )
            viewModel.saveAppConfig(config)
            finish()
        }

        binding.recyclerAppSelection.apply {
            layoutManager = LinearLayoutManager(this@AppSelectionActivity)
            adapter = selectionAdapter
        }

        binding.searchApps.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText.orEmpty())
                return true
            }
        })

        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = packageManager
                val addedPackages = viewModel.allApps.value
                    ?.map { it.packageName }?.toSet() ?: emptySet()

                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { app ->
                        // Only show user-installed apps (not system apps without launcher)
                        val isSystem = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
                        val hasLauncher = pm.getLaunchIntentForPackage(app.packageName) != null
                        !isSystem || hasLauncher
                    }
                    .map { app ->
                        AppInfo(
                            packageName = app.packageName,
                            appName = pm.getApplicationLabel(app).toString(),
                            isAdded = app.packageName in addedPackages
                        )
                    }
                    .sortedBy { it.appName }
            }
            allApps = apps
            selectionAdapter.submitList(apps)
        }
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
        selectionAdapter.submitList(filtered)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
