package sound.recorder.widget.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import sound.recorder.widget.R

open class DataSession(private val mContext: Context) {

    companion object {
        const val TAG = "Preferences"
        lateinit var sharedPref: SharedPreferences
    }

    var bannerIdName = "bannerId"
    var admobIdName = "admobId"
    var interstitialIdName = "interstitialIdName"
    var rewardInterstitialName = "rewardInterstitialId"
    var rewardName = "rewardId"
    var nativeName = "nativeId"

    init {
        sharedPref = mContext.getSharedPreferences("recordingWidget", 0)
    }

    fun getShared(): SharedPreferences{
        return mContext.getSharedPreferences("recordingWidget", 0)
    }

    fun getSharedUpdate(): SharedPreferences {
        return mContext.getSharedPreferences("recordingWidget", 0)
    }

    fun getAnimation(): Boolean{
        return sharedPref.getBoolean(Constant.keyShared.animation, false)
    }

    fun isCoverSong(): Boolean{
        return sharedPref.getBoolean("showSong", false)
    }

    fun getVersionCode(): Int{
        return sharedPref.getInt("versionCode", 0)
    }

    fun getVersionName(): String?{
        return sharedPref.getString("versionName", "")
    }

    fun getSplashScreenColor(): String?{
        return sharedPref.getString("backgroundSplashScreen","")
    }

    fun getAppName(): String?{
        return sharedPref.getString("appName","")
    }

    fun getShowNote(): Boolean{
        return sharedPref.getBoolean("showNote",false)
    }

    fun getBackgroundRecord(): String{
        return sharedPref.getString("llRecordBackground","#FFF9AA").toString()
    }

    fun getJsonName(): String?{
        return sharedPref.getString("jsonName","")
    }

    fun getVolume(): Int{
        return sharedPref.getInt(Constant.keyShared.volume, 100)
    }

    fun setupAds(status : Boolean,admobId : String, bannerId : String, interstitialId : String,rewardInterstitialId : String,rewardId : String, nativeId : String){
        val editor = sharedPref.edit()
        editor.putBoolean("initiate", status)
        editor.putString(admobIdName, admobId)
        editor.putString(bannerIdName, bannerId)
        editor.putString(interstitialIdName, interstitialId)
        editor.putString(rewardName,rewardId)
        editor.putString(rewardInterstitialName,rewardInterstitialId)
        editor.putString(nativeName,nativeId)

        editor.apply()
    }

    fun setInfoApp(versionCode : Int,versionName : String,appId : String,appName: String, jsonName : String,splashScreenType: String,showNote: Boolean,showSong : Boolean, llRecordBackground : String){
        val editor = sharedPref.edit()
        editor.putInt("versionCode", versionCode)
        editor.putString("backgroundSplashScreen", splashScreenType)
        editor.putString("appName",appName)
        editor.putString("appId",appId)
        editor.putString("versionName",versionName)
        editor.putString("jsonName",jsonName)
        editor.putBoolean("showNote",showNote)
        editor.putBoolean("showSong",showSong)
        editor.putString("llRecordBackground",llRecordBackground)

        editor.apply()
    }


    fun addColor(colorWidget : Int, colorRunningText: Int){
        val editor = sharedPref.edit()
        if(colorWidget!=0){
            editor.putInt(Constant.keyShared.colorWidget, colorWidget)
        }
        if(colorRunningText!=0){
            editor.putInt(Constant.keyShared.colorRunningText, colorRunningText)
        }
        editor.apply()
    }


    fun getLanguage(): String{
        return sharedPref.getString("languageCode","").toString()
    }

    fun getDefaultLanguage() : String{
        return sharedPref.getString("defaultLanguage","").toString()
    }

    fun getAppId(): String{
        return sharedPref.getString("appId","").toString()
    }

    fun getColorWidget(): Int{
        return sharedPref.getInt(Constant.keyShared.colorWidget,R.color.color7)
    }

    fun getColorRunningText(): Int{
        return sharedPref.getInt(Constant.keyShared.colorRunningText, R.color.white)
    }

    fun isContainSong(): Boolean {
        return sharedPref.getBoolean("addSong", false)
    }

    fun initiateSong(status: Boolean){
        val editor = sharedPref.edit()
        editor.putBoolean("addSong", status)
        editor.apply()
    }

    fun setLanguage(language : String){
        val editor = sharedPref.edit()
        editor.putString("languageCode",language)
        editor.apply()
    }

    fun saveColor(color : Int,name : String){
        val editor = sharedPref.edit()
        editor.putInt(name,color)
        editor.apply()
    }

    fun saveFirstLanguage(value : String){
        val editor = sharedPref.edit()
        editor.putString("firstLanguage",value)
        editor.apply()
    }

    fun saveDefaultLanguage(idLanguage : String){
        try {
            val editor = sharedPref.edit()
            editor.putString("defaultLanguage",idLanguage)
            editor.apply()
        }catch (e : Exception){
            setLog(e.message)
        }
    }

    fun saveAnimation(value : Boolean){
        val editor = sharedPref.edit()
        editor.putBoolean(Constant.keyShared.animation,value)
        editor.apply()
    }

    fun saveVolume(value : Int){
        val editor = sharedPref.edit()
        editor.putInt(Constant.keyShared.volume,value)
        editor.apply()
    }

    fun getBackgroundColor(): Int{
        return sharedPref.getInt(Constant.keyShared.backgroundColor, -1)
    }


    fun resetBackgroundColor(){
        val editor = sharedPref.edit()
        editor.putInt(Constant.keyShared.backgroundColor,-1)
        editor.apply()
    }


    fun updateBackgroundColor(color:Int){
        val editor = sharedPref.edit()
        editor.putInt(Constant.keyShared.backgroundColor,color)
        editor.apply()
    }



    fun getBannerId(): String {
        return sharedPref.getString(bannerIdName, "").toString()
    }

    fun getInterstitialId(): String {
        return sharedPref.getString(interstitialIdName, "").toString()
    }

    fun getRewardInterstitialId(): String {
        return sharedPref.getString(rewardInterstitialName, "").toString()
    }

    fun getRewardId(): String {
        return sharedPref.getString(rewardName, "").toString()
    }

    fun getFirstLanguage(): String {
        return sharedPref.getString("firstLanguage", "").toString()
    }

    fun getNativeId(): String {
        return sharedPref.getString(nativeName, "").toString()
    }

    fun getAdmobId(): String {
        return sharedPref.getString(admobIdName, "").toString()
    }

    fun setLog(message : String?=null){
        Log.d("error",message.toString()+".")
    }
}
