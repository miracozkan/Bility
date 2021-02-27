package com.mihanitylabs.bility

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.SkuDetails
import com.mihanitylabs.bilitylib.Bility
import com.mihanitylabs.bilitylib.data.model.BillingConfig
import com.mihanitylabs.bilitylib.util.Resource
import com.mihanitylabs.bilitylib.util.response.BillingClientResponse
import com.mihanitylabs.bilitylib.util.response.PurchaseResponse

class MainActivity : AppCompatActivity() {

    private val billingConfig by lazy { BillingConfig("") }
    private val bility by lazy { Bility.getInstance(this, billingConfig) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun startBility() {
        bility.startBillingConnection(::billingClientListener)
    }

    private fun billingClientListener(resource: BillingClientResponse) {
        when (resource) {
            BillingClientResponse.Connected -> {
                bility.setPurchaseListener(::purchaseListener)
                bility.setInAppSkuListener(::inAppListener)
                bility.setSubSkuListener(::subListener)
            }
            BillingClientResponse.ServiceNotReady -> TODO()
            BillingClientResponse.Loading -> TODO()
            BillingClientResponse.ServerDisconnected -> TODO()
            BillingClientResponse.ServiceTimeOut -> TODO()
            BillingClientResponse.ServiceUnavailable -> TODO()
            is BillingClientResponse.Error -> TODO()
        }
    }

    private fun purchaseListener(response: PurchaseResponse) {
        when (response) {
            is PurchaseResponse.Success -> TODO()
            is PurchaseResponse.Pending -> TODO()
            is PurchaseResponse.Error -> TODO()
            PurchaseResponse.FeatureNotSupported -> TODO()
            PurchaseResponse.UserCancelled -> TODO()
            PurchaseResponse.BillingNotAvailable -> TODO()
            PurchaseResponse.ItemAlreadyOwned -> TODO()
        }
    }

    private fun subListener(resource: Resource<List<SkuDetails>>) {
        when (resource) {
            is Resource.Success -> TODO()
            is Resource.Error -> TODO()
            Resource.Loading -> TODO()
        }
    }

    private fun inAppListener(resource: Resource<List<SkuDetails>>) {
        when (resource) {
            is Resource.Success -> TODO()
            is Resource.Error -> TODO()
            Resource.Loading -> TODO()
        }
    }

    fun purchaseHistoryListener() {
        bility.setPurchaseHistoryListener(
            inAppListener = {

            },
            subListener = {

            }
        )
    }

    override fun onDestroy() {
        bility.endBillingConnection()
        super.onDestroy()
    }
}
