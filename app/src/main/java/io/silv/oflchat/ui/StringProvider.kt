package io.silv.oflchat.ui

import android.content.Context
import androidx.annotation.StringRes

interface StringProvider {

    fun getString(@StringRes id: Int, vararg formatArgs: Any): String

    fun getString(@StringRes id: Int): String

    companion object {
        fun create(context: Context) = object : StringProvider {
            override fun getString(id: Int) = context.getString(id)
            override fun getString(id: Int, vararg formatArgs: Any) = context.getString(id, formatArgs)
        }
    }
}