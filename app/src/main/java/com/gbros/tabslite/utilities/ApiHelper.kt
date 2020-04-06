package com.gbros.tabslite.utilities

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.gbros.tabslite.data.ServerTimestampType
import kotlinx.coroutines.*
import java.lang.Exception
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

object ApiHelper {
    lateinit var apiKey: String
    var apiInit = false  // flag for whether the api key has been set yet.  Useful for no internet access
    var updatingApiKey = true  // flag for initial start.  If things happen insanely fast, UgApi.authenticatedStream might try to get the api key before we're done generating the md5
    private lateinit var myDeviceId: String

    //we need to update the server time and api key whenever we get a 498 response code
    suspend fun updateApiKey(): String? {
        updatingApiKey = true
        val updaterJob = updaterJobAsync()
        updaterJob.start()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd:H", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val stringBuilder = StringBuilder(getDeviceId())


        val update = updaterJob.await()
        return if (update != null) {
            stringBuilder.append(simpleDateFormat.format(update.getServerTime().time))
            stringBuilder.append("createLog()")
            apiKey = getMd5(stringBuilder.toString())
            apiInit = true
            updatingApiKey = false
            apiKey
        } else {
            updatingApiKey = false
            null
        }
    }

    private fun updaterJobAsync() = GlobalScope.async(start = CoroutineStart.LAZY) {
        val devId = getDeviceId()
        val lastResult: ServerTimestampType
        val conn = URL("https://api.ultimate-guitar.com/api/v1/common/hello").openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.12 (")  // actual value "UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)". 5.10.11 is the app version.
        conn.setRequestProperty("x-ug-client-id", devId)                // stays constant over time; api key and client id are related to each other.

        try {
            val inputStream = conn.getInputStream()
            val jsonReader = JsonReader(inputStream.reader())
            val serverTimestampTypeToken = object : TypeToken<ServerTimestampType>() {}.type
            lastResult = Gson().fromJson(jsonReader, serverTimestampTypeToken)
            inputStream.close()
            lastResult
        } catch (ex: Exception){
            Log.e(javaClass.simpleName, "Error getting hello handshake.  We may not be connected to the internet.", ex)
            null
        }

    }

    private fun getMd5(paramString: String): String {
        var ret = paramString

        try {
            ret = BigInteger(1, MessageDigest.getInstance("MD5").digest(ret.toByteArray())).toString(16)
            while (ret.length < 32) {
                val stringBuilder = java.lang.StringBuilder()
                stringBuilder.append("0")
                stringBuilder.append(ret)
                ret = stringBuilder.toString()
            }
            return ret
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            val runtimeException = RuntimeException(noSuchAlgorithmException)
            throw runtimeException
        }
    }

    // generates a new device id each run.  todo: save the value so it is constant over runs
    fun getDeviceId(): String {
        if (! this::myDeviceId.isInitialized) {

            // generate a device id
            var newId = ""
            val charList = charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
            while(newId.length < 16) {
                newId += charList[Random.nextInt(0, 15)]
            }
            myDeviceId = newId
        }
        return myDeviceId
    }
}