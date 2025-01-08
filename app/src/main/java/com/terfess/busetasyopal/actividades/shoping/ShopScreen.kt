package com.terfess.busetasyopal.actividades.shoping

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.Purchase
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.shoping.recycler_shop.AdapterHolderShop
import com.terfess.busetasyopal.actividades.shoping.recycler_shop.AdapterUserPurchases
import com.terfess.busetasyopal.actividades.shoping.recycler_shop.Product
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityShopScreenBinding
import com.terfess.busetasyopal.services.BillingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShopScreen : AppCompatActivity() {
    private lateinit var binding: ActivityShopScreenBinding
    private lateinit var billingInst: BillingService

    val list = mutableListOf(
        Pair(
            "remove_ads_3months_subscription",
            "https://desarrollamos-apps.web.app/imagenes/utilsBusetasYopal/product_photo.webp"
        )
    )
    private var adaptProducts = AdapterHolderShop(mutableListOf(), this)
    private var adaptUserPurchases = AdapterUserPurchases(mutableListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityShopScreenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        billingInst = BillingService.getInstance(this)

        // Actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarShop)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Shop"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))

        // Recycler Products
        val recyclerProducts = binding.listProducts
        recyclerProducts.adapter = adaptProducts
        recyclerProducts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val recyclerPurchases = binding.listUserShops
        recyclerPurchases.adapter = adaptUserPurchases
        recyclerPurchases.layoutManager = LinearLayoutManager(this)

        goConnectAndGetData()
    }

    private fun goConnectAndGetData() {
        billingInst.startConnection {
            Log.d("ShopScreen", "Conexion lista, pidiendo products")
            getProductInfo(list[0].first, list[0].second)
            Log.d("ShopScreen", "Conexion lista, pidiendo purchases")
            getUserPurchases()
        }
    }

    private fun getProductInfo(id: String, img: String) {
        Log.d("ShopScreen", "En getProductInfo")

        billingInst.getProductDetails(id) { productDetails ->
            val price =
                productDetails?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice

            Log.d("ShopScreen", "Recibido de getProductInfo: $productDetails")

            val s = mutableListOf(
                Product(
                    id,
                    img,
                    productDetails?.title ?: "Sin título",
                    productDetails?.description ?: "Sin descripción",
                    price ?: "Sin precio"
                )
            )

            // Actualización del adaptador
            CoroutineScope(Dispatchers.Main).launch{
                adaptProducts.setNewData(s)
            }
        }
    }

    private fun getUserPurchases() {
        Log.i("ShopScreen", "En getUserPurchases")

        setVisibleTextNoPurchases(false)

        billingInst.validateExistingPurchases {
            Log.i("ShopScreen", "Purchaes obtenidas: $it")

            // Actualización del adaptador
            CoroutineScope(Dispatchers.Main).launch {
                adaptUserPurchases.setNewData(it)
                if (it.isNullOrEmpty()){
                    setVisibleTextNoPurchases(true)
                    UtilidadesMenores().saveAdsShowState(this@ShopScreen, false)
                }else{
                    UtilidadesMenores().saveAdsShowState(this@ShopScreen, true)
                }
            }
        }
    }

    private fun setVisibleTextNoPurchases(b: Boolean) {
        val s = binding.listUserShops
        val x = binding.textNoPurchases

        if (b){
            x.visibility = View.VISIBLE
            s.visibility = View.GONE
        }else{
            x.visibility = View.GONE
            s.visibility = View.VISIBLE
        }
    }

    fun startShop(info: Product) {
        billingInst.launchPurchaseFlowWithId(info.idProduct, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}