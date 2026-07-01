package com.aj.geminiproj.core.ai.firebase.common

import android.util.Log

/**
 * Logging abstraction that keeps domain classes off android.util.Log.
 * Production wires AndroidLogger; unit tests wire PrintLogger or SilentLogger
 *
 */
interface Logger {
    fun d(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun w(tag: String, msg: String)
    fun e(tag: String, msg: String, throwable: Throwable? = null)
}

class AndroidLogger : Logger {
    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun e(tag: String, msg: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, msg, throwable)
        }
        Log.e(tag, msg)
    }
}

class PrintLogger : Logger{
    override fun d(tag: String, msg: String) {
        println("D/$tag:$msg")
    }

    override fun i(tag: String, msg: String) {
        println("I/$tag:$msg")
    }

    override fun w(tag: String, msg: String) {
        println("W/$tag:$msg")
    }

    override fun e(tag: String, msg: String, throwable: Throwable?) {
        println("E/$tag:$msg $throwable")
    }

    object SilentLogger : Logger{
        override fun d(tag: String, msg: String) {
            Unit
        }

        override fun i(tag: String, msg: String) {
            Unit
        }

        override fun w(tag: String, msg: String) {
            Unit
        }

        override fun e(tag: String, msg: String, throwable: Throwable?) {
            Unit
        }

    }

}
