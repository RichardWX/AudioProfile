package com.rjw.audioprofile.activity

import android.os.Bundle
import android.view.View
import com.rjw.audioprofile.BuildConfig
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivityAboutBinding
import com.rjw.audioprofile.utils.Alerts
import com.rjw.audioprofile.utils.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AboutActivity : AudioActivity() {
    private lateinit var binding: ActivityAboutBinding

    /**
     * Create the about box.
     * @param savedInstanceState State information from last time the activity was run.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setWindowRatios(0.8f, 0.3f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        binding = ActivityAboutBinding.bind(view)

        val pInfo = packageManager.getPackageInfo(packageName, 0)
        binding.textVersion.text = String.format(getString(R.string.about_version), pInfo.versionName)
        val buildDate = Calendar.getInstance()
        buildDate.timeInMillis = BuildConfig.TIMESTAMP
        val builtDate = StringBuilder(DateFormat.getDateInstance(DateFormat.SHORT).format(buildDate.time))
        if(BuildConfig.DEBUG) {
            builtDate.append(" ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(buildDate.time))
        }
        binding.textBuilt.text = String.format(getString(R.string.about_built), builtDate)
        binding.textCopyright.text =
            String.format(getString(R.string.about_copyright), SimpleDateFormat("yyyy", Locale.getDefault()).format(buildDate.time))
        binding.textLog.text = Log.readLog()

        binding.log.setOnClickListener {
            binding.layoutControls.visibility = View.INVISIBLE
            binding.layoutLog.visibility = View.VISIBLE
            binding.deleteLog.visibility = View.VISIBLE
        }
        binding.deleteLog.setOnClickListener {
            Log.clearLog()
            finish()
        }
        binding.buttonClose.setOnClickListener {
            finish()
        }
    }
}