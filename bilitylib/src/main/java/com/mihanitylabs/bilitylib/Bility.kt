package com.mihanitylabs.bilitylib

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import com.mihanitylabs.bilitylib.data.model.BillingConfig
import com.mihanitylabs.bilitylib.data.repository.BillingRepository
import com.mihanitylabs.bilitylib.util.Resource
import com.mihanitylabs.bilitylib.util.logDebug
import com.mihanitylabs.bilitylib.util.logError
import com.mihanitylabs.bilitylib.util.response.BillingClientResponse
import com.mihanitylabs.bilitylib.util.response.PurchaseResponse

//  Code with ❤️
// ┌─────────────────────────────┐
// │ Created by Mirac Ozkan      │
// │ ─────────────────────────── │
// │ mirac.ozkan123@gmail.com    │            
// │ ─────────────────────────── │
// │ 2/27/2021 - 1:30 PM         │
// └─────────────────────────────┘

class Bility private constructor(
    context: Context,
    private val billingConfig: BillingConfig
) {

    private val billingRepository by lazy { BillingRepository(context, billingConfig) }

    fun startBillingConnection(clientResponse: (BillingClientResponse) -> Unit) {
        billingRepository.startDataSourceConnections()
        billingRepository.setBillingClientListener(clientResponse)
    }

    fun setPurchaseListener(purchaseListener: (PurchaseResponse) -> Unit) {
        billingRepository.setPurchaseListener(purchaseListener)
            .also { billingRepository.queryPurchasesAsync() }
    }

    fun setInAppSkuListener(skuListener: (Resource<List<SkuDetails>>) -> Unit) {
        billingRepository.setSkuDetailListener(skuListener)
            .also { billingRepository.getSkuDetail() }
    }

    fun setSubSkuListener(subListener: (Resource<List<SkuDetails>>) -> Unit) {
        billingRepository.setSubDetailListener(subListener)
            .also { billingRepository.getSubscriptionDetail() }
    }

    fun onMakePurchase(activity: Activity, sku: SkuDetails) {
        billingRepository.startBillingFlow(activity, sku)
    }

    fun setPurchaseHistoryListener(
        inAppListener: (List<PurchaseHistoryRecord>?) -> Unit,
        subListener: (List<PurchaseHistoryRecord>?) -> Unit
    ) {
        billingRepository.getPurchaseHistory(
            inAppListener = inAppListener,
            subListener = subListener
        )
    }

    private fun billingClientListener(billingClientResponse: BillingClientResponse) {
        when (billingClientResponse) {
            BillingClientResponse.Connected -> {
                setPurchaseListener {}
                if (!billingConfig.inAppSkuList.isNullOrEmpty()) setInAppSkuListener {}
                if (!billingConfig.subsSkuList.isNullOrEmpty()) setSubSkuListener {}
                logDebug("Connected")
            }
            BillingClientResponse.Loading -> logDebug("BillingClientResponse.Loading")
            BillingClientResponse.ServerDisconnected -> logError("ServerDisconnected")
            BillingClientResponse.ServiceTimeOut -> logError("ServiceTimeOut")
            BillingClientResponse.ServiceUnavailable -> logError("ServiceUnavailable")
            is BillingClientResponse.Error -> logError(billingClientResponse.exception?.localizedMessage)
            BillingClientResponse.ServiceNotReady -> logError("ServiceNotReady")
        }
    }

    private fun purchaseListener(purchaseResponse: PurchaseResponse) {
        when (purchaseResponse) {
            is PurchaseResponse.Success -> logDebug(purchaseResponse.purchaseList.toString())
            is PurchaseResponse.Pending -> logError(purchaseResponse.pendingItem.toString())
            PurchaseResponse.FeatureNotSupported -> logError("PurchaseResponse.FeatureNotSupported")
            PurchaseResponse.UserCancelled -> logError("PurchaseResponse.UserCancelled")
            is PurchaseResponse.Error -> logError(purchaseResponse.exception?.message.toString())
            PurchaseResponse.BillingNotAvailable -> logError("PurchaseResponse.BillingNotAvailable")
            PurchaseResponse.ItemAlreadyOwned -> logError("ItemAlreadyOwned")
        }
    }

    private fun skuListener(resource: Resource<List<SkuDetails>>) {
        when (resource) {
            is Resource.Success -> {
                logDebug("skuListObserver Success : " + resource.data.toString())
            }
            is Resource.Error -> {
                logError("skuListObserver Error : " + resource.exception.message.toString())
            }
            Resource.Loading -> logDebug("skuListObserver Loading")
        }
    }

    private fun subListener(resource: Resource<List<SkuDetails>>) {
        when (resource) {
            is Resource.Success -> {
                logDebug("subListObserver Success : " + resource.data.toString())
            }
            is Resource.Error -> {
                logError("subListObserver Error : " + resource.exception.message.toString())
            }
            Resource.Loading -> logDebug("skuListObserver Loading")
        }
    }

    fun endBillingConnection(){
        billingRepository.endDataSourceConnections()
    }

    companion object {

        private var INSTANCE: Bility? = null

        fun getInstance(context: Context, billingConfig: BillingConfig): Bility {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Bility(context, billingConfig)
                INSTANCE!!
            }
        }
    }
}
