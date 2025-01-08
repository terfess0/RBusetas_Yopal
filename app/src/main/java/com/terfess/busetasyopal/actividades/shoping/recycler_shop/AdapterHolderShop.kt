package com.terfess.busetasyopal.actividades.shoping.recycler_shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.shoping.ShopScreen
import com.terfess.busetasyopal.databinding.ItemProductShopBinding
import com.terfess.busetasyopal.services.BillingService

class AdapterHolderShop(
    var list : MutableList<Product>,
    functionsInstance: ShopScreen
): RecyclerView.Adapter<AdapterHolderShop.ViewHolderShop>() {

    private val shopScreenInstance = functionsInstance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderShop {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_product_shop, parent, false)
        return ViewHolderShop(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolderShop, position: Int) {
        val itemPos = list[position]
        holder.showProduct(itemPos)
    }

    inner class ViewHolderShop(itemView: View): RecyclerView.ViewHolder(itemView){
        private val bindingItem = ItemProductShopBinding.bind(itemView)

        private val title = bindingItem.titleProduct
        private val decription = bindingItem.productDescript
        private val price = bindingItem.productPrice

        private val imageProduct = bindingItem.productImage

        fun showProduct(product: Product){
            title.text = product.title
            decription.text = product.description
            price.text = product.price

            Glide.with(bindingItem.root.context)
                .load(product.imagenProduct)
                .placeholder(R.drawable.bord_cabecera)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageProduct)

            bindingItem.mainView.setOnClickListener {
                shopScreenInstance.startShop(product)
            }
        }
    }

    fun setNewData(newList:MutableList<Product>){
        list = newList
        notifyDataSetChanged()
    }
}