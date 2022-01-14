package org.odk.collect.geo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.analytics.Analytics
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.geo.analytics.AnalyticsEvents
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class GeoPointActivity : LocalizedActivity(), GeoPointDialogFragment.Listener {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setTranslucent(true)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        (application as GeoDependencyComponentProvider).geoDependencyComponent.inject(this)

        val viewModel =
            ViewModelProvider(this, geoPointViewModelFactory).get(GeoPointViewModel::class.java)

        viewModel.acceptedLocation.observe(this) {
            if (it != null) {
                Analytics.log(AnalyticsEvents.SAVE_POINT_AUTO)
                ExternalAppUtils.returnSingleValue(this, GeoUtils.formatLocationResultString(it))
            }
        }

        DialogFragmentUtils.showIfNotShowing(
            GeoPointDialogFragment::class.java,
            supportFragmentManager
        )

        viewModel.start(
            retainMockAccuracy = this.intent.getBooleanExtra(
                EXTRA_RETAIN_MOCK_ACCURACY,
                false
            ),
            accuracyThreshold = (this.intent.extras?.get(EXTRA_ACCURACY_THRESHOLD) as? Float)
        )
    }

    override fun onCancel() {
        finish()
    }

    companion object {
        const val EXTRA_ACCURACY_THRESHOLD = "extra_accuracy_threshold"
    }
}