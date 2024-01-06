package sound.recorder.widget

import android.annotation.SuppressLint
import android.app.Application
import com.google.firebase.FirebaseApp
import sound.recorder.widget.internet.InternetAvailabilityChecker


@SuppressLint("Registered")
open class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
       // InternetAvailabilityChecker.init(this);
    }

    override fun onLowMemory() {
        super.onLowMemory()
       // InternetAvailabilityChecker.getInstance().removeAllInternetConnectivityChangeListeners()
    }
}