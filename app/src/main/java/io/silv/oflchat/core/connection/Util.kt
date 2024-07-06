package io.silv.oflchat.core.connection

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

val ConnectionInfo.uuid: String
    get() = endpointName.split("|").last()

val ConnectionInfo.name: String
    get() = endpointName.split("|").first()

val DiscoveredEndpointInfo.uuid: String
    get() = endpointName.split("|").last()

val DiscoveredEndpointInfo.name: String
    get() = endpointName.split("|").first()