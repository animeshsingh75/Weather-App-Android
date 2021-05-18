package com.project.weatherapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun convertKelvinToCelsius(number: Number): Double {
    return DecimalFormat().run {
        applyPattern(".##")
        parse(format(number.toDouble().minus(273))).toDouble()
    }
}
fun converttoMilesPerHour(number: Number):Double{
    return DecimalFormat().run{
        applyPattern(".#")
        var numberDouble=number.toDouble()
        numberDouble *= 2.237
        parse(format(numberDouble)).toDouble()
    }

}
fun convertCelsiusToFahrenheit(number: Number): Double {
    return DecimalFormat().run {
        applyPattern(".##")
        var numberDouble=number.toDouble()
        numberDouble=((numberDouble*9)/5)+32
        parse(format(numberDouble)).toDouble()
    }
}
inline fun <T : View> T.showIf(condition: (T) -> Boolean) {
    visibility = if (condition(this)) {
        View.VISIBLE
    } else {
        View.GONE
    }
}


fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(
        lifecycleOwner,
        object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        }
    )
}
fun isNetworkConnected(context: Context):Boolean{
    return try {
        val mConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT > +Build.VERSION_CODES.M) {
            val mNetworkInfo = mConnectivityManager.activeNetwork ?: return false
            val capabilities =
                mConnectivityManager.getNetworkCapabilities(mNetworkInfo) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> return false
            }
        } else {
            mConnectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    } catch (e: NullPointerException) {
        false
    }
}
@SuppressLint("SimpleDateFormat")
fun currentSystemTime(): String {
    val currentTime = System.currentTimeMillis()
    val date = Date(currentTime)
    val dateFormat = SimpleDateFormat("EEEE MMM d, hh:mm aaa")
    return dateFormat.format(date)
}

fun convertToDate(time:Long): String {
    val timeChanged=time/10000
    val date = Date(timeChanged)
    val dateFormat = SimpleDateFormat("YYYY-MM-dd")
    return dateFormat.format(date)
}