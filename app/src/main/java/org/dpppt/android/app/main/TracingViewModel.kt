/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.dpppt.android.app.debug.TracingStatusWrapper
import org.dpppt.android.app.debug.model.DebugAppState
import org.dpppt.android.app.main.model.AppState
import org.dpppt.android.app.sdk.DP3T
import org.dpppt.android.app.util.DeviceFeatureHelper
import org.dpppt.android.sdk.TracingStatus
import org.dpppt.android.sdk.TracingStatus.ErrorState
import javax.inject.Inject

class TracingViewModel @Inject constructor(
    private val DP3T: DP3T,
    private val application: Application
) : ViewModel() {

    private val tracingStatusLiveData = MutableLiveData<TracingStatus>()

    private val tracingEnabledLiveData = MutableLiveData<Boolean>()
    private val exposedLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    private val numberOfHandshakesLiveData = MutableLiveData(0)
    private val errorsLiveData = MutableLiveData<List<ErrorState>>(emptyList())
    private val appStateLiveData = MutableLiveData<AppState>()
    private val bluetoothEnabledLiveData = MutableLiveData<Boolean>()
    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                invalidateBluetoothState()
                invalidateTracingStatus()
            }
        }
    }
    private val tracingStatusWrapper = TracingStatusWrapper(DebugAppState.NONE)

    init {
        tracingStatusLiveData.observeForever { status: TracingStatus ->
            tracingEnabledLiveData.value = status.isAdvertising && status.isReceiving
            numberOfHandshakesLiveData.value = status.numberOfHandshakes
            tracingStatusWrapper.setStatus(status)
            exposedLiveData.value = Pair(tracingStatusWrapper.isReportedAsExposed, tracingStatusWrapper.wasContactExposed())
            errorsLiveData.value = status.errors
            appStateLiveData.setValue(tracingStatusWrapper.appState)
        }
        invalidateBluetoothState()
        invalidateTracingStatus()
        application.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        viewModelScope.launch {
            DP3T.statusUpdates.collect {
                invalidateTracingStatus(it)
            }
        }
    }

    fun resetSdk(onDeleteListener: Runnable) {
        if (tracingEnabledLiveData.value!!) DP3T.stop()
        tracingStatusWrapper.debugAppState = DebugAppState.NONE

        viewModelScope.launch {
            DP3T.clearData()
            onDeleteListener.run()
        }
    }

    @JvmOverloads
    fun invalidateTracingStatus(status: TracingStatus = DP3T.lastStatus) {
        tracingStatusLiveData.value = status
    }

    fun getTracingEnabledLiveData(): LiveData<Boolean> {
        return tracingEnabledLiveData
    }

    val selfOrContactExposedLiveData: LiveData<Pair<Boolean, Boolean>>
        get() = exposedLiveData

    fun getErrorsLiveData(): LiveData<List<ErrorState>> {
        return errorsLiveData
    }

    fun getAppStateLiveData(): LiveData<AppState> {
        return appStateLiveData
    }

    fun getTracingStatusLiveData(): LiveData<TracingStatus> {
        return tracingStatusLiveData
    }

    fun getBluetoothEnabledLiveData(): LiveData<Boolean> {
        return bluetoothEnabledLiveData
    }

    fun setTracingEnabled(enabled: Boolean) {
        if (enabled) {
            DP3T.start()
        } else {
            DP3T.stop()
        }
    }

    fun invalidateService() {
        if (tracingEnabledLiveData.value!!) {
            DP3T.stop()
            DP3T.start()
        }
    }

    private fun invalidateBluetoothState() {
        bluetoothEnabledLiveData.value = DeviceFeatureHelper.isBluetoothEnabled()
    }

    override fun onCleared() {
        application.unregisterReceiver(bluetoothReceiver)
    }

    var debugAppState: DebugAppState?
        get() = tracingStatusWrapper.debugAppState
        set(debugAppState) {
            tracingStatusWrapper.debugAppState = debugAppState
        }
}