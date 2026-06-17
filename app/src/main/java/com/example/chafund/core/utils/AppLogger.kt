package com.example.chafund.core.utils

import timber.log.Timber

object AppLogger {

    fun init(isDebug: Boolean) {
        if (isDebug && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun d(tag: String, msg: String) = Timber.tag(tag).d(msg)
    fun i(tag: String, msg: String) = Timber.tag(tag).i(msg)
    fun w(tag: String, msg: String, t: Throwable? = null) = Timber.tag(tag).w(t, msg)
    fun e(tag: String, msg: String, t: Throwable? = null) = Timber.tag(tag).e(t, msg)
}
