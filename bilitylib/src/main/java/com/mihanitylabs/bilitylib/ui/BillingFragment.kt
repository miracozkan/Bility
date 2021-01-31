package com.mihanitylabs.bilitylib.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.SkuDetails
import com.mihanitylabs.bilitylib.R
import com.mihanitylabs.bilitylib.data.model.BillingConfig
import com.mihanitylabs.bilitylib.databinding.FragmentBillingBinding
import com.mihanitylabs.bilitylib.util.*

class BillingFragment : Fragment(R.layout.fragment_billing) {

    private val binding by viewBinding(FragmentBillingBinding::bind)

    private val billingRepository by lazy {
        DependencyUtil.provideBillingRepository(requireContext(), billingConfig ?: throw Exception("Billing Config is null"))
    }
    private val billingViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(billingRepository)
        ).get(BillingViewModel::class.java)
    }

    private val billingConfig: BillingConfig? by lazy { arguments?.getParcelable(BILLING_CONFIG) }

    private val billingClientObserver = EventObserver<BillingClientResponse> { clientResponse ->
        when (clientResponse) {
            BillingClientResponse.Connected -> {
                billingViewModel.setPurchaseListener()
                billingViewModel.setSkuListener() // if in app purchase is enabled
                billingViewModel.setSubListener() // if subscription is enabled
                logDebug("BillingClientResponse.Connected")
            }
            BillingClientResponse.Loading -> logDebug("BillingClientResponse.Loading")
            BillingClientResponse.ServerDisconnected -> {
                logError("BillingClientResponse.ServerDisconnected")
            }
            BillingClientResponse.ServiceTimeOut -> {
                logError("BillingClientResponse.ServiceTimeOut")
            }
            BillingClientResponse.ServiceUnavailable -> {
                logError("BillingClientResponse.ServiceUnavailable")
            }
            is BillingClientResponse.Error -> {
                logError(clientResponse.exception?.localizedMessage.toString())
            }
            BillingClientResponse.ServiceNotReady -> {
                logError("BillingClientResponse.ServiceNotReady")
            }
        }
    }
    private val skuListObserver by lazy {
        Observer<Resource<List<SkuDetails>>> { skuResource ->
            when (skuResource) {
                is Resource.Success -> {
                    logDebug("skuListObserver Success : " + skuResource.data.toString())
                }
                is Resource.Error -> {
                    logError("skuListObserver Error : " + skuResource.exception.message.toString())
                }
                Resource.Loading -> logDebug("skuListObserver Loading")
            }
        }
    }
    private val subListObserver by lazy {
        Observer<Resource<List<SkuDetails>>> { skuResource ->
            when (skuResource) {
                is Resource.Success -> {
                    logDebug("subListObserver Success : " + skuResource.data.toString())
                }
                is Resource.Error -> {
                    logError("subListObserver Error : " + skuResource.exception.message.toString())
                }
                Resource.Loading -> logDebug("skuListObserver Loading")
            }
        }
    }
    private val purchaseObserver by lazy {
        Observer<PurchaseResponse> { purchaseResponse ->
            when (purchaseResponse) {
                is PurchaseResponse.Success -> logDebug(purchaseResponse.purchaseList.toString())
                is PurchaseResponse.Pending -> logError(purchaseResponse.pendingItem.toString())
                PurchaseResponse.FeatureNotSupported -> logError("PurchaseResponse.FeatureNotSupported")
                PurchaseResponse.UserCancelled -> logError("PurchaseResponse.UserCancelled")
                is PurchaseResponse.Error -> logError(purchaseResponse.exception?.message.toString())
                PurchaseResponse.BillingNotAvailable -> logError("PurchaseResponse.BillingNotAvailable")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        billingViewModel.billingClientResult.observe(viewLifecycleOwner, billingClientObserver)
        billingViewModel.skuDetailListener.observe(viewLifecycleOwner, skuListObserver)
        billingViewModel.purchaseListener.observe(viewLifecycleOwner, purchaseObserver)
        billingViewModel.subDetailListener.observe(viewLifecycleOwner, subListObserver)
    }

    private fun onMakePurchase(sku: SkuDetails) {
        billingViewModel.makePurchase(requireActivity(), sku)
    }

    private fun navBack(){
        parentFragmentManager.popBackStack()
    }

    companion object {

        private const val BILLING_CONFIG = "billingConfig"

        @JvmStatic
        fun newInstance(billingConfig: BillingConfig) = BillingFragment().apply {
            arguments = Bundle().apply { putParcelable(BILLING_CONFIG, billingConfig) }
        }
    }
}