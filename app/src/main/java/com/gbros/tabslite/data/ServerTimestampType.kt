package com.gbros.tabslite.data

import java.util.*

class ServerTimestampType(var timestamp: Long) {
    fun getServerTime(): Calendar {
        val date = Date(timestamp * 1000L)
        val gregorianCalendar = GregorianCalendar()
        gregorianCalendar.time = date
        gregorianCalendar.timeZone = TimeZone.getTimeZone("UTC")
        return gregorianCalendar
    }
}