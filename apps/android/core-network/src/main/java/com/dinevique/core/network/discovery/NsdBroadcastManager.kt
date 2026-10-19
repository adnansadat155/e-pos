package com.dinevique.core.network.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo

/**
 * Android Network Service Discovery (mDNS) Broadcaster
 * Runs on the Master Terminal to announce its local IP and port to in-store peers.
 */
class NsdBroadcastManager(private val context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null

    companion object {
        const val SERVICE_TYPE = "_dinevique-pos._tcp."
        const val DEFAULT_SERVICE_NAME = "Dinevique-Master-POS"
    }

    fun registerService(port: Int = 8080, serviceName: String = DEFAULT_SERVICE_NAME) {
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                // Successfully registered service
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Handle registration error
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                // Service unregistered
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Handle unregistration error
            }
        }

        try {
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            // Log or report exception
        }
    }

    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager?.unregisterService(it)
            } catch (e: Exception) {
                // Already unregistered or error
            }
            registrationListener = null
        }
    }
}
