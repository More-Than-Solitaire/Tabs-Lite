package com.gbros.tabslite.utilities

/**
 * Gets the class name for use as a tag for logs. Since API 24 there's no length restriction on log
 * tags, so this returns the full name.
 */
val Any.TAG: String
    get() {
        return if (!javaClass.isAnonymousClass) {
            "tabslite.${javaClass.simpleName}"
        } else {
            "tabslite.${javaClass.name}"
        }
    }