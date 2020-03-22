package com.gbros.tabslite.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.gbros.tabslite.data.ServerTimestampType
import kotlinx.coroutines.*
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

object ApiHelper {
    var apiKey: String = ""

    //we need to update the server time and api key whenever we get a 498 response code
    suspend fun updateApiKey() {
        val updaterJob = updaterJobAsync()
        updaterJob.start()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd:H", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val stringBuilder = StringBuilder(getDeviceId())

        stringBuilder.append(simpleDateFormat.format(updaterJob.await().getServerTime().time))
        stringBuilder.append("createLog()")
        apiKey = getMd5(stringBuilder.toString())
    }

    private fun updaterJobAsync() = GlobalScope.async(start = CoroutineStart.LAZY) {
        val lastResult: ServerTimestampType
        val conn = URL("https://api.ultimate-guitar.com/api/v1/common/hello").openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value "UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)". 5.10.11 is the app version.
        conn.setRequestProperty("x-ug-client-id", getDeviceId())                   // stays constant over time; api key and client id are related to each other.

        val inputStream = conn.getInputStream()
        val jsonReader = JsonReader(inputStream.reader())
        val serverTimestampTypeToken = object : TypeToken<ServerTimestampType>() {}.type
        lastResult = Gson().fromJson(jsonReader, serverTimestampTypeToken)
        inputStream.close()

        lastResult
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

    fun getDeviceId(): String {
        var str: String
        str = "f858b6156cb21e15" //todo: randomly generate this on first app startup.  Save somewhere
        while (str.length < 16) {
            val stringBuilder = java.lang.StringBuilder()
            stringBuilder.append("0")
            stringBuilder.append(str)
            str = stringBuilder.toString()
        }
        return str
    }
}