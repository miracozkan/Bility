package com.mihanitylabs.bilitylib.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.mihanitylabs.bilitylib.data.model.BillingConfig
import com.mihanitylabs.bilitylib.util.*
import com.mihanitylabs.bilitylib.util.Security.verifyPurchase
import java.util.*


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:30 PM         │
//└─────────────────────────────┘


class BillingRepository(
    private val context: Context,
    private val billingConfig: BillingConfig
) : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient

    private val isClientReady
        get() = billingClient.isReady

    private var skuDetailListener: ((Resource<List<SkuDetails>>) -> Unit)? = null
    private var subDetailListener: ((Resource<List<SkuDetails>>) -> Unit)? = null
    private var purchaseListener: ((PurchaseResponse) -> Unit)? = null
    private var billingClientListener: ((BillingClientResponse) -> Unit)? = null

    fun setSkuDetailListener(skuDetailListener: (Resource<List<SkuDetails>>) -> Unit) {
        this.skuDetailListener = skuDetailListener
    }

    fun setSubDetailListener(subDetailListener: (Resource<List<SkuDetails>>) -> Unit) {
        this.subDetailListener = subDetailListener
    }

    fun setPurchaseListener(purchaseListener: (PurchaseResponse) -> Unit) {
        this.purchaseListener = purchaseListener
    }

    fun setBillingClientListener(billingClientListener: (BillingClientResponse) -> Unit) {
        this.billingClientListener = billingClientListener
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.apply { processPurchases(this.toSet()) }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseListener?.invoke(PurchaseResponse.UserCancelled)
            }
            BillingClient.BillingResponseCode.ERROR -> {
                purchaseListener?.invoke(PurchaseResponse.Error(Exception(billingResult.debugMessage)))
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                purchaseListener?.invoke(PurchaseResponse.FeatureNotSupported)
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                purchaseListener?.invoke(PurchaseResponse.BillingNotAvailable)
            }
            else -> purchaseListener?.invoke(PurchaseResponse.Error(Exception(billingResult.debugMessage)))
        }
    }

    fun startDataSourceConnections() {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService() {
        if (!isClientReady) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            billingClientListener?.invoke(BillingClientResponse.Connected)
                        }
                        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                            billingClientListener?.invoke(BillingClientResponse.ServerDisconnected)
                        }
                        BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {
                            billingClientListener?.invoke(BillingClientResponse.ServiceTimeOut)
                        }
                        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                            billingClientListener?.invoke(BillingClientResponse.ServiceUnavailable)
                        }
                        else -> billingClientListener?.invoke(BillingClientResponse.Error())
                    }
                }

                override fun onBillingServiceDisconnected() {
                    connectToPlayBillingService()
                }
            })
        }
    }

    fun getSkuDetail() {
        if (isClientReady) {
            skuDetailListener?.invoke(Resource.Loading)
            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(billingConfig.inAppSkuList)
                .build()
            billingClient.querySkuDetailsAsync(skuDetailsParams) { billingResult, skuDetailList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    skuDetailListener?.invoke(Resource.Success(skuDetailList.orEmpty()))
                } else {
                    billingClientListener?.invoke(BillingClientResponse.ServiceNotReady)
                    skuDetailListener?.invoke(Resource.Error(Exception("Service Not Ready")))
                }
            }
        } else {
            billingClientListener?.invoke(BillingClientResponse.ServiceNotReady)
            skuDetailListener?.invoke(Resource.Error(Exception("Service Not Ready")))
        }
    }

    fun getSubscriptionDetail() {
        if (isClientReady) {
            subDetailListener?.invoke(Resource.Loading)
            val subDetailsParams = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.SUBS)
                .setSkusList(billingConfig.subsSkuList)
                .build()
            billingClient.querySkuDetailsAsync(subDetailsParams) { billingResult, subDetailList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    subDetailListener?.invoke(Resource.Success(subDetailList.orEmpty()))
                } else {
                    billingClientListener?.invoke(BillingClientResponse.ServiceNotReady)
                    subDetailListener?.invoke(Resource.Error(Exception("Service Not Ready")))
                }
            }
        } else {
            billingClientListener?.invoke(BillingClientResponse.ServiceNotReady)
            subDetailListener?.invoke(Resource.Error(Exception("Service Not Ready")))
        }
    }

    fun queryPurchasesAsync() {
        val purchasesResult = HashSet<Purchase>()
        var result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        result.purchasesList?.apply { purchasesResult.addAll(this) }
        if (isSubscriptionSupported()) {
            result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
            result.purchasesList?.apply { purchasesResult.addAll(this) }
            logDebug("queryPurchasesAsync SUBS results: ${result.purchasesList?.size}")
        }
        processPurchases(purchasesResult)
    }

    private fun isSubscriptionSupported(): Boolean {
        val billingResult =
            billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        var succeeded = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> connectToPlayBillingService()
            BillingClient.BillingResponseCode.OK -> succeeded = true
        }
        return succeeded
    }

    private fun processPurchases(purchasesResult: Set<Purchase>) {
        val validPurchases = HashSet<Purchase>(purchasesResult.size)
        purchasesResult.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (isSignatureValid(purchase)) {
                    validPurchases.add(purchase)
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                purchaseListener?.invoke(PurchaseResponse.Pending(purchase))
            }
        }

        val (consumables, nonConsumables) = validPurchases.partition {
            billingConfig.consumableSkuList.contains(it.sku)
        }

        handleConsumablePurchasesAsync(consumables)
        acknowledgeNonConsumablePurchasesAsync(nonConsumables)
    }

    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        consumables.forEach {
            val params = ConsumeParams.newBuilder()
                .setPurchaseToken(it.purchaseToken)
                .build()

            billingClient.consumeAsync(params) { billingResult, _ ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        purchaseListener?.invoke(PurchaseResponse.Success(consumables))
                    }
                    else -> {
                        purchaseListener?.invoke(PurchaseResponse.Error(Exception(billingResult.debugMessage)))
                    }
                }
            }
        }
    }

    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        nonConsumables.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        purchaseListener?.invoke(PurchaseResponse.Success(nonConsumables))
                    }
                    else -> logError("acknowledgeNonConsumablePurchasesAsync response is ${billingResult.debugMessage}")
                }
            }

        }
    }

    fun startBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        if (isClientReady) {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        } else {
            billingClientListener?.invoke(BillingClientResponse.ServiceNotReady)
        }
    }

    fun getPurchaseHistory(
        inAppListener: (List<PurchaseHistoryRecord>?) -> Unit,
        subListener: (List<PurchaseHistoryRecord>?) -> Unit
    ) {
        if (isClientReady) {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { _, purchaseHistoryRecordList ->
                inAppListener.invoke(purchaseHistoryRecordList)
            }
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) { _, purchaseHistoryRecordList ->
                subListener.invoke(purchaseHistoryRecordList)
            }
        } else {
            inAppListener.invoke(emptyList())
            subListener.invoke(emptyList())
        }
    }

    private fun isSignatureValid(purchase: Purchase) =
        verifyPurchase(purchase.originalJson, purchase.signature, billingConfig.base64PublicKey)

    fun endDataSourceConnections() {
        billingClientListener?.invoke(BillingClientResponse.ServerDisconnected)
        if (isClientReady) billingClient.endConnection()
    }
}
