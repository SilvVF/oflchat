package io.silv.oflchat.core.call

/*
 * Copyright 2023 Stream.IO, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.silv.oflchat.d
import io.silv.oflchat.helpers.PayloadHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber



class SignalingClient(
    private val endpointId: String,
) {
    private val logger = Timber
    private val signalingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // session flow to send information about the session state to the subscribers
    private val _sessionStateFlow = MutableStateFlow(WebRTCSessionState.Offline)
    val sessionStateFlow: StateFlow<WebRTCSessionState> = _sessionStateFlow

    // signaling commands to send commands to value pairs to the subscribers
    private val _signalingCommandFlow = MutableSharedFlow<Pair<SignalingCommand, String>>()
    val signalingCommandFlow: SharedFlow<Pair<SignalingCommand, String>> = _signalingCommandFlow

    fun sendCommand(signalingCommand: SignalingCommand, message: String) {
        logger.d { "[sendCommand] $signalingCommand $message" }
        PayloadHelper.sendRtcSignal(endpointId, "$signalingCommand $message")
    }

    fun handleCommand(text: String) {
        when {
            text.startsWith(SignalingCommand.STATE.toString(), true) ->
                handleStateMessage(text)
            text.startsWith(SignalingCommand.OFFER.toString(), true) ->
                handleSignalingCommand(SignalingCommand.OFFER, text)
            text.startsWith(SignalingCommand.ANSWER.toString(), true) ->
                handleSignalingCommand(SignalingCommand.ANSWER, text)
            text.startsWith(SignalingCommand.ICE.toString(), true) ->
                handleSignalingCommand(SignalingCommand.ICE, text)
        }
    }

    private fun handleStateMessage(message: String) {
        val state = getSeparatedMessage(message)
        _sessionStateFlow.value = WebRTCSessionState.valueOf(state)
    }

    private fun handleSignalingCommand(command: SignalingCommand, text: String) {
        val value = getSeparatedMessage(text)
        logger.d { "received signaling: $command $value" }
        signalingScope.launch {
            _signalingCommandFlow.emit(command to value)
        }
    }

    private fun getSeparatedMessage(text: String) = text.substringAfter(' ')

    fun dispose() {
        _sessionStateFlow.value = WebRTCSessionState.Offline
        signalingScope.cancel()
    }
}

enum class WebRTCSessionState {
    Active, // Offer and Answer messages has been sent
    Creating, // Creating session, offer has been sent
    Ready, // Both clients available and ready to initiate session
    Impossible, // We have less than two clients connected to the server
    Offline // unable to connect signaling server
}

enum class SignalingCommand {
    STATE, // Command for WebRTCSessionState
    OFFER, // to send or receive offer
    ANSWER, // to send or receive answer
    ICE // to send and receive ice candidates
}