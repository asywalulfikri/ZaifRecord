package sound.recorder.widget.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import sound.recorder.widget.R
import sound.recorder.widget.RecordingSDK
import sound.recorder.widget.util.DataSession
import sound.recorder.widget.util.Toastic

open class BaseFragmentWidget : Fragment(){

    private var dataSession : DataSession? =null
    var mInterstitialAd: InterstitialAd? = null
    private var isLoad = false

    var fileName =  ""
    var dirPath = ""
    val LOG_TAG = "AudioRecordTest"
    var sharedPreferences : SharedPreferences? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSession = DataSession(activity as Context)
    }

    fun isInternetConnected(): Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    private fun getDataSession(context: Context) : DataSession{
        return DataSession(context)
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


    fun setupInterstitial(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            adRequest.let {
                InterstitialAd.load(context, getDataSession(context).getInterstitialId(), it,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                            isLoad = true
                            Log.d("AdMob Inter","success")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            mInterstitialAd = null
                            Log.d("AdMob Inter",loadAdError.message)
                        }
                    })
            }
        }catch (e : Exception){
            setLog(e.message.toString())
        }

    }


    fun showAllowPermission(){
        try {
            setToastInfo(activity,requireActivity().getString(R.string.allow_permission))
        }catch (e : Exception){
            setLog(e.message)
        }

    }

    fun setupFragment(view : Int, fragment : Fragment?){
        if(activity!=null){
            try {
                // some code
                if (fragment != null) {
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.add(view, fragment)
                        ?.commit()
                }
            } catch (e: Exception) {
                setToastError(activity,e.message.toString())
            }
        }
    }

    fun openFragment(view : Int, fragment : Fragment){
        if(activity!=null){
            try {
                // some code
                activity?.supportFragmentManager?.beginTransaction()
                    ?.add(view, fragment)
                    ?.commit()
            } catch (e: Exception) {
                setToastError(activity,e.message.toString())
            }
        }
    }

    fun setLog(message : String? =null){
        Log.e("error", "$message.")
    }

    fun setToastError(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = "Error : $message",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.ERROR,
                isIconAnimated = true
            ).show()
        }
    }

    fun setToastWarning(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.WARNING,
                isIconAnimated = true
            ).show()
        }
    }

    fun setToastSuccess(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(
                activity,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.SUCCESS,
                isIconAnimated = true
            ).show()
        }
    }

    fun setToastInfo(activity: Activity?,message : String){
        if(activity!=null){
            Toastic.toastic(activity,
                message = "$message.",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.INFO,
                isIconAnimated = true
            ).show()
        }
    }

    @SuppressLint("NewApi")
    fun showSettingsDialog(context: Context?) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle("Permission")
        builder.setMessage(HtmlCompat.fromHtml("You need allow Permission Record Audio", HtmlCompat.FROM_HTML_MODE_LEGACY))
        builder.setPositiveButton("Setting") { dialog, _ ->
            dialog.cancel()
            openSettings(context)
        }
        builder.show()
    }

    private fun openSettings(activity: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity?.packageName.toString(), null)
        activity?.startActivity(intent)
    }




    fun setupAds(activity: Activity?) {
        if(activity!=null){
            val adRequestInterstitial = AdRequest.Builder().build()
            adRequestInterstitial.isTestDevice(activity)
            InterstitialAd.load(activity as Context,dataSession?.getInterstitialId().toString(), adRequestInterstitial,
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
    }

    fun showInterstitial(activity: Activity?){
        if(activity!=null){
            if(isLoad){
                mInterstitialAd?.show(activity)
            }
        }
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

    fun openPlayStoreForMoreApps() {
        val appPackageName = "com.example.myapp" // Replace with your app's package name

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=$appPackageName"))
            intent.setPackage("com.android.vending") // Specify the Play Store app package name

            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=$appPackageName"))

            startActivity(intent)
        }
    }


    @SuppressLint("SetTextI18n")
    fun showDialogEmail(appName : String ,info : String) {

        // custom dialog
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_input_email)
        dialog.setCancelable(true)

        // set the custom dialog components - text, image and button
        val etMessage = dialog.findViewById<View>(R.id.etMessage) as EditText
        val btnSend = dialog.findViewById<View>(R.id.btnSend) as Button
        val btnCancel = dialog.findViewById<View>(R.id.btnCancel) as Button


        // if button is clicked, close the custom dialog
        btnSend.setOnClickListener {
            var message = etMessage.text.toString().trim()
            if(message.isEmpty()){
                setToastWarning(requireActivity(),getString(R.string.message_cannot_empty))
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
        RecordingSDK.openEmail(requireActivity(),subject,body)
    }

}