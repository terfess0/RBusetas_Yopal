package com.terfess.busetasyopal.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.terfess.busetasyopal.actividades.shoping.recycler_shop.UserPurchase
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class BillingService(private val contexto: Context) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingService", "Compra cancelada.")
        } else {
            Log.e("BillingService", "Error en la compra: ${billingResult.debugMessage}")
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(contexto)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()


    fun startConnection(onConnected: (() -> Unit)? = null) {

        if (billingClient.isReady) {
            Log.d("BillingService", "El cliente ya está conectado.")
            onConnected?.invoke()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingService", "Conexión establecida con éxito.")
                    onConnected?.invoke() // Llama al callback cuando la conexión sea exitosa
                } else {
                    Log.e("BillingService", "Error al conectar: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingService", "Servicio desconectado. Intentando reconectar...")
                startConnection(onConnected)
            }
        })
    }


    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                        UtilidadesMenores().saveAdsShowState(contexto, true)
                        UtilidadesMenores().crearAlertaSencilla(contexto, "Gracias por su compra")
                    } else {
                        // Manejo del fallo al reconocer la compra
                        val errorMessage = "Error al reconocer la compra: ${billingResult.debugMessage}"
                        Log.e("BillingService", errorMessage)
                        UtilidadesMenores().crearAlertaSencilla(contexto, "No se pudo reconocer la compra. Intentelo de nuevo.")
                    }
                }
            } else {
                // Si la compra ya está reconocida
                Log.w("BillingService", "La compra ya fue reconocida previamente.")
            }
        } else {
            // Si el estado de la compra no es PURCHASED
            Log.e("BillingService", "Estado de compra inválido: ${purchase.purchaseState}")
            UtilidadesMenores().crearAlertaSencilla(contexto, "La compra no se pudo realizar. Por favor, verifica e inténtalo de nuevo.")
        }
    }


    fun getProductDetails(productId: String, callback: (ProductDetails?) -> Unit) {

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsList.firstOrNull { it.productId == productId }
                callback(productDetails)
            } else {
                Log.e(
                    "BillingService",
                    "Error al obtener detalles del producto: ${billingResult.debugMessage}"
                )
                callback(null)
            }
        }
    }

    fun launchPurchaseFlowWithId(idProduct: String, activity: Activity) {

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(idProduct)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        // Consulta los detalles del producto de manera asincrónica
        try {
            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.e("BillingService", "Error al obtener detalles del producto: ${billingResult.debugMessage}")
                    return@queryProductDetailsAsync
                }

                val productDetails = productDetailsList.firstOrNull { it.productId == idProduct }
                if (productDetails == null) {
                    Log.e("BillingService", "No se encontró el producto con ID: $idProduct")
                    return@queryProductDetailsAsync
                }

                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (offerToken == null) {
                    Log.e("BillingService", "No hay oferta disponible para este producto.")
                    return@queryProductDetailsAsync
                }

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                val billingFlowResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                if (billingFlowResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.e("BillingService", "Error al iniciar el flujo de facturación: ${billingFlowResult.debugMessage}")
                }
            }
        } catch (e: Exception) {
            Log.e("BillingService", "Error inesperado durante la compra: ${e.message}")
            UtilidadesMenores().crearAlertaSencilla(activity, "Algo salió mal. Por favor, inténtalo de nuevo.")
        }

    }


    fun validateExistingPurchases(callback: (MutableList<UserPurchase>) -> Unit) {
        billingClient.queryPurchasesAsync(BillingClient.ProductType.SUBS) { billingResult, purchases ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                purchases.forEach { purchase ->
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PENDING -> {
                            Log.d("BillingService", "Compra pendiente. No se otorga el producto.")
                            UtilidadesMenores().saveAdsShowState(contexto, false)
                        }

                        Purchase.PurchaseState.PURCHASED -> {
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                    }
                }

                CoroutineScope(Dispatchers.Default).launch {
                    val list = processListPurchases(purchases)

                    callback(list)
                }
            } else {
                Log.e(
                    "BillingService",
                    "Error al validar compras existentes: ${billingResult.debugMessage}"
                )
                callback(mutableListOf())
            }
        }
    }

    private suspend fun processListPurchases(purchases: MutableList<Purchase>): MutableList<UserPurchase> {
        val newList = mutableListOf<UserPurchase>()
        val utilidadesMenores = UtilidadesMenores()

        purchases.forEach { iterator ->
            val state = utilidadesMenores.handlePurchaseState(iterator.purchaseState)
            val date = utilidadesMenores.convertTimeNumToDate(iterator.purchaseTime)
            val nameList = mutableListOf<String>()
            val descriptionList = mutableListOf<String>()

            iterator.products.forEach { productId ->
                // Usa una función suspend para obtener detalles
                val details = getProductDetailsSuspend(productId)
                nameList.add(details?.title ?: "Compra")
                descriptionList.add(details?.description ?: "Descripción")
            }

            newList.add(
                UserPurchase(
                    date,
                    state,
                    nameList.joinToString("\n"),
                    descriptionList.joinToString("\n")
                )
            )
        }
        return newList
    }

    suspend fun getProductDetailsSuspend(productId: String): ProductDetails? {
        return suspendCancellableCoroutine { continuation ->
            getProductDetails(productId) { list ->
                if (list != null) {
                    continuation.resume(list, onCancellation = null)
                } else {
                    continuation.resume(null, onCancellation = null)
                }
            }
        }
    }


    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("BillingService", "Compra reconocida correctamente.")
            } else {
                Log.e("BillingService", "Error al reconocer compra: ${billingResult.debugMessage}")
            }
        }
    }


    companion object {
        @Volatile
        private var instance: BillingService? = null

        fun getInstance(context: Context): BillingService {
            return instance ?: synchronized(this) {
                instance ?: BillingService(context).also { instance = it }
            }
        }
    }

}
