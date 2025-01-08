package com.terfess.busetasyopal.actividades.shoping.recycler_shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.Purchase
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.shoping.ShopScreen
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ItemUserPurchaseBinding

class AdapterUserPurchases(
    var list: MutableList<UserPurchase>,
    functionsInstance: ShopScreen
) : RecyclerView.Adapter<AdapterUserPurchases.ViewHolderPurchases>() {

    private val shopScreenInstance = functionsInstance
    private val utilesFunctionsInst = UtilidadesMenores()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPurchases {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_user_purchase, parent, false)
        return ViewHolderPurchases(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolderPurchases, position: Int) {
        val itemPos = list[position]
        holder.showPurchase(itemPos)
    }

    inner class ViewHolderPurchases(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bindingItem = ItemUserPurchaseBinding.bind(itemView)

        private val titleDate = bindingItem.titleDate
        private val statePurchaseTv = bindingItem.statePurchase
        private val subtitlePurchase = bindingItem.subtitle
        private val descriptionPurchase = bindingItem.descriptionPurchase

        fun showPurchase(item: UserPurchase) {
            titleDate.text = item.titleDate
            statePurchaseTv.text = item.statePurchase
            subtitlePurchase.text = item.namePurchase
            descriptionPurchase.text = item.descriptionPurchase
        }
    }

    fun setNewData(newList: MutableList<UserPurchase>) {
        list = newList
        notifyDataSetChanged()
    }
}