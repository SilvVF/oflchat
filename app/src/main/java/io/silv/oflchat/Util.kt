package io.silv.oflchat

import android.content.Context
import timber.log.Timber

val applicationContext: Context
    get() = OflChatApp.instance.applicationContext

fun Timber.Forest.v(messageLazy: () -> String) = v(messageLazy())
fun Timber.Forest.d(messageLazy: () -> String) = d(messageLazy())
fun Timber.Forest.e(messageLazy: () -> String) = e(messageLazy())
fun Timber.Forest.i(messageLazy: () -> String) = i(messageLazy())
fun Timber.Forest.w(messageLazy: () -> String) = w(messageLazy())
