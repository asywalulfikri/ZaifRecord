package sound.recorder.widget.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import org.json.JSONObject
import sound.recorder.widget.R
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.ads.GoogleMobileAdsConsentManager
import sound.recorder.widget.notes.Note
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.Toastic
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sound.recorder.widget.animation.ParticleSystem
import sound.recorder.widget.animation.modifiers.ScaleModifier
import kotlin.time.Duration.Companion.seconds

open class BaseActivityWidget : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    var id: String? = null
    private var isLoad = false
    private var rewardedAd: RewardedAd? = null
    private var isLoadReward = false
    private var isLoadInterstitialReward = false
    private var rewardedInterstitialAd : RewardedInterstitialAd? =null

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val initialLayoutComplete = AtomicBoolean(false)
    private lateinit var adView: AdManagerAdView
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private lateinit var consentInformation: ConsentInformation
    private var TAG = "GDPR_App"

    private var isPrivacyOptionsRequired: Boolean = false

    private lateinit var appUpdateManager: AppUpdateManager       // in app update
    private val updateType = AppUpdateType.FLEXIBLE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            MobileAds.initialize(this) {}
            val testDeviceIds = listOf("D48A46E523E6A96C8215178502423686")
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(configuration)

        }catch (e : Exception){
            setLog(e.message.toString())
        }

        try {
            val languageCode = Locale.getDefault().language
            getDataSession().saveDefaultLanguage(languageCode)
            setLocale(getDataSession().getDefaultLanguage())
        }catch (e : Exception){
            setToastError(e.message.toString())
        }
    }


    protected fun setupFragment(id : Int, fragment : Fragment?){
        try {
            if(fragment!=null){
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(id, fragment)
                fragmentTransaction.commit()
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    protected fun checkUpdate(){
        try {
            appUpdateManager = AppUpdateManagerFactory.create(this)

            if (updateType == AppUpdateType.FLEXIBLE) {
                appUpdateManager.registerListener(installStateUpdatedListener)
            }

            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                val isUpdateAllowed = when (updateType) {
                    AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                    AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                    else -> false
                }

                if (isUpdateAvailable && isUpdateAllowed) {

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            info,
                            updateType,
                            this,
                            123
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        // Handle the exception, log, or display an error message
                        //e.printStackTrace()
                        Log.d("not","support")
                        // You can also perform additional error handling here
                    }
                }
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }

    fun onDestroyUpdate() {
        try {
            if (updateType == AppUpdateType.FLEXIBLE) {
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            }
        }catch (e : Exception){
            Log.d("not","support")
        }
    }


    private val installStateUpdatedListener = InstallStateUpdatedListener{ state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            try {
                setToastInfo(getString(R.string.download_success))
                lifecycleScope.launch {
                    delay(5.seconds)
                    appUpdateManager.completeUpdate()
                }
            }catch (e : Exception){
                Log.d("not","support")
            }
        }
    }


    fun setupGDPR(){
        try {
            // Set tag for under age of consent. false means users are not under age
            // of consent.

            /*  val debugSettings = ConsentDebugSettings.Builder(this)
                  .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                  .addTestDeviceHashedId("0c302266-17a0-4f2a-a11a-10ca1ad1abe1")
                  .build()*/

            val params = ConsentRequestParameters
                .Builder()
                // .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false)
                .build()

            consentInformation = UserMessagingPlatform.getConsentInformation(this)
            isPrivacyOptionsRequired  = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

            consentInformation.requestConsentInfoUpdate(
                this,
                params, {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) {

                            loadAndShowError -> run {
                            Log.w(
                                TAG, String.format(
                                    "%s: %s",
                                    loadAndShowError?.errorCode,
                                    loadAndShowError?.message
                                )
                            )

                        }
                        if (isPrivacyOptionsRequired) {
                            // Regenerate the options menu to include a privacy setting.
                            UserMessagingPlatform.showPrivacyOptionsForm(this) { formError ->
                                formError?.let {
                                    setToastError(it.message.toString())
                                }
                            }
                        }
                    }
                },
                {
                        requestConsentError ->
                    // Consent gathering failed.
                    Log.w(TAG, String.format("%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message))
                })

            if (consentInformation.canRequestAds()) {
                MobileAds.initialize(this) {}
            }
        }catch (e :Exception){
            Log.d("message",e.message.toString())
        }
    }


    private fun getDataSession() : DataSession{
        return DataSession(this)
    }

    private fun loadBanner(adViewContainer: FrameLayout,unitId : String) {
        try {
            adView.adUnitId = unitId
            adView.setAdSizes(getSize(adViewContainer))
            val adRequest = AdManagerAdRequest.Builder().build()
            adView.loadAd(adRequest)
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    private fun getSize(adViewContainer: FrameLayout): AdSize{
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

    private fun initializeMobileAdsSdk(adViewContainer: FrameLayout,unitId: String) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        // Load an ad.
        if (initialLayoutComplete.get()) {
            loadBanner(adViewContainer,unitId)
        }
    }

    fun newBannerSetup(adViewContainer: FrameLayout,unitId: String){
        adView = AdManagerAdView(this)
        adViewContainer.addView(adView)


        /* googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
         googleMobileAdsConsentManager.gatherConsent(this) { error ->
             if (error != null) {
                 // Consent not obtained in current session.
                // Log.d(TAG, "${error.errorCode}: ${error.message}")
             }

             if (googleMobileAdsConsentManager.canRequestAds) {
                 initializeMobileAdsSdk()
             }

             if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                 // Regenerate the options menu to include a privacy setting.
                 invalidateOptionsMenu()
             }
         }*/

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk(adViewContainer,unitId)
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete.getAndSet(true) && googleMobileAdsConsentManager.canRequestAds()) {
                loadBanner(adViewContainer,unitId)
            }
        }

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("0c302266-17a0-4f2a-a11a-10ca1ad1abe1")).build()
        )
    }

    fun showArrayLanguage(){
        val languageArray = resources.getStringArray(R.array.language_array)
        val languageArrayCode = resources.getStringArray(R.array.language_code)
        val selectedLanguages = BooleanArray(languageArray.size) // Untuk melacak status CheckBox

        var selectedLanguage = "" // Untuk melacak bahasa yang dipilih

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.choose_language)


        builder.setSingleChoiceItems(languageArray, -1) { _, which ->
            selectedLanguage = languageArrayCode[which]
        }


        builder.setPositiveButton(getString(R.string.colorpicker_dialog_ok)) { _, _ ->
            if (selectedLanguage.isNotEmpty()) {
                getDataSession().saveDefaultLanguage(selectedLanguage)
                changeLanguage(selectedLanguage)
                // Lakukan sesuatu dengan bahasa yang dipilih
                // Toast.makeText(this, "Anda memilih bahasa: $selectedLanguage", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    @SuppressLint("SetTextI18n")
    fun showDialogLanguage() {
        val language = getDataSession().getLanguage()
        // custom dialog
        var type = ""
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_choose_language)
        dialog.setCancelable(true)

        // set the custom dialog components - text, image and button
        val rbDefault = dialog.findViewById<View>(R.id.rbDefaultLanguage) as RadioButton
        val rbEnglish = dialog.findViewById<View>(R.id.rbEnglish) as RadioButton
        val btnSave = dialog.findViewById<View>(R.id.btn_submit) as AppCompatTextView


        if(getDataSession().getLanguage()=="en"){
            rbEnglish.isChecked = true
        }else{
            rbDefault.isChecked = true
        }


        // if button is clicked, close the custom dialog
        btnSave.setOnClickListener {

            if(rbDefault.isChecked){
                type = getDataSession().getDefaultLanguage()
            }

            if(rbEnglish.isChecked){
                type = "en"
            }


            if(type.isNotEmpty()&&type!=getDataSession().getLanguage()){
                getDataSession().setLanguage(type)
                changeLanguage(type)
            }
            dialog.dismiss()
        }

        dialog.show()
    }


    fun openPlayStoreForMoreApps(devName : String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=$devName"))
            intent.setPackage("com.android.vending") // Specify the Play Store app package name

            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=$devName"))

            startActivity(intent)
        }
    }


    @SuppressLint("SetTextI18n")
    fun showDialogEmail(appName : String ,info : String) {

        // custom dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_input_email)
        dialog.setCancelable(true)

        // set the custom dialog components - text, image and button
        val etMessage = dialog.findViewById<View>(R.id.etMessage) as EditText
        val btnSend = dialog.findViewById<View>(R.id.btnSend) as Button
        val btnCancel = dialog.findViewById<View>(R.id.btnCancel) as Button


        // if button is clicked, close the custom dialog
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if(message.isEmpty()){
                setToastWarning(getString(R.string.message_cannot_empty))
                return@setOnClickListener
            }else{
                sendEmail("Feed Back $appName", "$message\n\n\n\nfrom $info")
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sendEmail(subject: String, body: String) {
        try {
            RecordingSDK.openEmail(this,subject,body)
        }catch (e : Exception){
            setToastError(e.message.toString())
        }

    }


    private fun changeLanguage(type : String) {
        val locale = Locale(type) // Ganti "en" dengan kode bahasa yang diinginkan
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale

        resources.updateConfiguration(configuration, resources.displayMetrics)
        this.recreate()
    }


    private fun getCurrentLanguage(): String {
        /* val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             this.resources.configuration.locales[0]
         } else {
             this.resources.configuration.locale
         }
         return locale.displayLanguage*/
        val currentLocale: Locale = Locale.getDefault()
        return currentLocale.language
    }

    private fun setLocale(language : String) {
        try {
            val locale = Locale(language) // Ganti "en" dengan kode bahasa yang diinginkan
            Locale.setDefault(locale)

            val config = Configuration()
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }



    fun showLoadingLayout(context: Context,long : Long){
        try {
            showLoadingProgress(context,long)
        } catch (e: Exception) {
            setLog(e.message.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    fun showLoadingProgress(context: Context,long : Long) {

        try {
            var dialogLoading: Dialog? = Dialog(context)
            dialogLoading?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogLoading?.setContentView(R.layout.loading_layout)
            dialogLoading?.setCancelable(false)

            dialogLoading?.show()

            val handler = Handler()
            handler.postDelayed({
                val dialog = dialogLoading
                if (dialog != null && dialog.isShowing) {
                    dialog.dismiss()
                    dialogLoading = null // Release the dialog instance
                }
            }, long)
        }catch (e : Exception){
            Log.d("message",e.message.toString())
        }
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                // for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


    fun getNoteValue(note: Note) : String{
        val valueNote = try {
            val jsonObject = JSONObject(note.note.toString())
            val value = Gson().fromJson(note.note, Note::class.java)
            // The JSON string is valid
            value.note.toString()

        } catch (e: Exception) {
            // The JSON string is not valid
            note.note
        }

        return  valueNote
    }

    fun getTitleValue(note: Note) : String{
        var valueNote = ""
        valueNote = try {
            val jsonObject = JSONObject(note.note.toString())
            val value = Gson().fromJson(note.title, Note::class.java)
            // The JSON string is valid
            value.note.toString()

        } catch (e: Exception) {
            // The JSON string is not valid
            "No title"
        }

        return  valueNote
    }

    fun setupBanner(mAdView: AdView){
        try {
            val adRequest = AdRequest.Builder().build()
            //RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("D48A46E523E6A96C8215178502423686"))
            mAdView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("AdMob", "Ad loaded successfully")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.d("AdMob", "Ad failed to load:"+ p0.message)
                }

                override fun onAdOpened() {
                    Log.d("AdMob", "Ad opened")
                }

                override fun onAdClicked() {
                    Log.d("AdMob", "Ad clicked")
                }

                override fun onAdClosed() {
                    Log.d("AdMob", "Ad closed")
                }
            }

            mAdView.loadAd(adRequest)
        }catch (e : Exception){
            setLog(e.message.toString())

        }
    }

    private fun permissionNotification(){
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Pass any permission you want while launching
                    requestPermissionNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }catch (e : Exception){
           setLog(e.message.toString())
        }

    }

    private val requestPermissionNotification = registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    fun setupInterstitial() {
        try {
            val adRequest = AdRequest.Builder().build()
            adRequest.let {
                InterstitialAd.load(this, getDataSession().getInterstitialId(), it,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                            isLoad = true
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            mInterstitialAd = null
                        }
                    })
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }


    fun setupRewardInterstitial(){
        try {
            RewardedInterstitialAd.load(this, DataSession(this).getRewardInterstitialId(),
                AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        //Log.d(TAG, "Ad was loaded.")
                        rewardedInterstitialAd = ad
                        isLoadInterstitialReward = true
                        rewardedInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                            override fun onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d("yametere", "Ad was clicked.")
                            }

                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d("yametere", "Ad dismissed fullscreen content.")
                                rewardedInterstitialAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                // Called when ad fails to show.
                                Log.d("yametere", "Ad failed to show fullscreen content.")
                                rewardedInterstitialAd = null
                            }

                            override fun onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d("yametere", "Ad recorded an impression.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d("yametere","Ad showed fullscreen content.")
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Log.d(TAG, adError?.toString())
                        Log.d("yameterex",adError.message.toString())
                        rewardedInterstitialAd = null
                    }
                })

        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    fun showRewardInterstitial(){
        try {
            if(isLoadInterstitialReward){
                Log.d("yametere", "show")
                rewardedInterstitialAd?.let { ad ->
                    ad.show(this) { rewardItem ->
                        // Handle the reward.
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        Log.d("yametere", "User earned the reward.$rewardAmount--$rewardType")
                    }
                } ?: run {
                    Log.d("yametere", "The rewarded ad wasn't ready yet.")
                    showInterstitial()
                }
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    fun setupReward(){
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(this,DataSession(this).getRewardId(), adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadReward = true
                    rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d("yametere", "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d("yametere", "Ad dismissed fullscreen content.")
                            rewardedAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            // Called when ad fails to show.
                            Log.d("yametere", "Ad failed to show fullscreen content.")
                            rewardedAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d("yametere", "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d("yametere","Ad showed fullscreen content.")
                        }
                    }
                }
            })
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    fun showRewardAds(){
        try {
            if(isLoadReward){
                Log.d("yametere", "show")
                rewardedAd?.let { ad ->
                    ad.show(this) { rewardItem ->
                        // Handle the reward.
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        Log.d("yametere", "User earned the reward.$rewardAmount--$rewardType")
                    }
                } ?: run {
                    Log.d("yametere", "The rewarded ad wasn't ready yet.")
                    showInterstitial()
                }
            }else{
                Log.d("yametere", "nall")
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }
    protected open fun getFirebaseToken(): String? {
        val tokens = AtomicReference("")
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w("response", "Fetching FCM registration token failed", task.exception)
                    getFirebaseToken()
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val tokenFirebase = task.result
                tokens.set(tokenFirebase)
                Log.d("tokenFirebase",tokenFirebase.toString())

            }
        return tokens.get()
    }


    fun showInterstitial(){
        try {
            if(isLoad){
                mInterstitialAd?.show(this)
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    protected fun showReward(){
        try {
            if(isLoadReward){
                rewardedAd?.let { ad ->
                    ad.show(this) { rewardItem ->
                        // Handle the reward.
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                    }
                } ?: run {
                    showInterstitial()
                }
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }


    protected fun setToastError(message : String){
        try {
            Toastic.toastic(
                context = this,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.ERROR,
                isIconAnimated = true
            ).show()
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }

    protected fun setToastWarning(message : String){
        try {
            Toastic.toastic(
                context = this,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.WARNING,
                isIconAnimated = true
            ).show()
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    protected fun setToastSuccess(message : String){
        try {
            Toastic.toastic(
                context = this,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    fun setToastInfo(message : String){
        try {
            Toastic.toastic(
                context = this,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.INFO,
                isIconAnimated = true
            ).show()
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }


    fun setLog(message: String){
        Log.d("response", "$message - ")
    }

    protected fun simpleAnimation(view: View , drawable:Int? = null) {
        try {
            var icon  = R.drawable.star_pink
            if(drawable!=null){
                icon = drawable
            }
            ParticleSystem(this, 100, icon, 800)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(view, 100)
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }

    protected fun advanceAnimation(view: View , drawable:Int? = null) {
        // Launch 2 particle systems one for each image
        try {
            var icon  = R.drawable.star_white_border
            if(drawable!=null){
                icon = drawable
            }
            val ps = ParticleSystem(this, 100, icon, 800)
            ps.setScaleRange(0.7f, 1.3f)
            ps.setSpeedRange(0.1f, 0.25f)
            ps.setAcceleration(0.0001f, 90)
            ps.setRotationSpeedRange(90f, 180f)
            ps.setFadeOut(200, AccelerateInterpolator())
            ps.oneShot(view, 100)
        }catch (e : Exception){
            setLog(e.message.toString())
        }
    }

    open fun starAnimation(view: View , drawable:Int? = null) {

        try{
            var icon  = R.drawable.star_white_border
            if(drawable!=null){
                icon = drawable
            }
            ParticleSystem(this, 10, icon, 3000)
                .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                .setAcceleration(0.000003f, 90)
                .setInitialRotationRange(0, 360)
                .setRotationSpeed(120f)
                .setFadeOut(2000)
                .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                .oneShot(view, 10)
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }

    open fun getActivity(): BaseActivityWidget? {
        return this
    }

    open fun rating(){
        val appPackageName = packageName

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    fun isDarkTheme(): Boolean {
        return resources?.configuration?.uiMode!! and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }



    fun showKeyboard(view: View) {
        try {
            if (view.requestFocus()) {
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }


    fun hideKeyboard(view: View) {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }

}