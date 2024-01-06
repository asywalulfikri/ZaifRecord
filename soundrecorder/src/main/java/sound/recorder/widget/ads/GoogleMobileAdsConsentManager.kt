/*
package sound.recorder.widget.ads

import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

*/
/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 *//*

class GoogleMobileAdsConsentManager private constructor(context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    */
/** Interface definition for a callback to be invoked when consent gathering is complete. *//*

    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }




    open fun canRequestAds(): Boolean {
        return (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED
                || consentInformation.consentStatus == ConsentInformation.ConsentStatus.NOT_REQUIRED)
    }
    fun gatherConsent(
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener?
    ) {
    }


    companion object {
        @Volatile private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
                }
    }
}*/
