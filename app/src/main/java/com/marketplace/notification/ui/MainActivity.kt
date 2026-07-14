package com.marketplace.notification.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.marketplace.notification.R
import com.marketplace.notification.databinding.ActivityMainBinding
import com.marketplace.notification.service.MarketplaceNotificationService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter
    private lateinit var database: com.marketplace.notification.data.AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        database = com.marketplace.notification.data.AppDatabase.getDatabase(this)
        setupRecyclerView()
        observeNotifications()
        checkNotificationPermission()

        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission on resume in case user just granted it
        updatePermissionBanner()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(
            lifecycleOwner = this,
            database = database,
            onRead = { viewModel.markAsRead(it.id) },
            onAcknowledge = { viewModel.markAsAcknowledged(it.id) },
            onDelete = { notification ->
                viewModel.deleteNotification(notification)
                Snackbar.make(binding.root, R.string.notification_deleted, Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }

        // Swipe left → acknowledge, swipe right → delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val notification = adapter.getNotification(viewHolder.adapterPosition)
                if (direction == ItemTouchHelper.LEFT) {
                    viewModel.markAsAcknowledged(notification.id)
                    Snackbar.make(binding.root, R.string.acknowledged, Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.deleteNotification(notification)
                    Snackbar.make(binding.root, R.string.notification_deleted, Snackbar.LENGTH_SHORT).show()
                }
            }
        }).attachToRecyclerView(binding.recyclerNotifications)
    }

    private fun observeNotifications() {
        viewModel.notifications.observe(this) { notifications ->
            adapter.submitList(notifications)
            binding.tvEmptyState.visibility =
                if (notifications.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun checkNotificationPermission() {
        if (!isNotificationListenerEnabled()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.notification_access_message)
                .setPositiveButton(R.string.grant_access) { _, _ ->
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun updatePermissionBanner() {
        if (isNotificationListenerEnabled()) {
            binding.bannerPermission.visibility = android.view.View.GONE
        } else {
            binding.bannerPermission.visibility = android.view.View.VISIBLE
            binding.bannerPermission.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, MarketplaceNotificationService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, ConfigActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
