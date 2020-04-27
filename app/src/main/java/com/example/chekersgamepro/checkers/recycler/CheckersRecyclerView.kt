package com.example.chekersgamepro.checkers.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Objects
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import kotlin.collections.ArrayList

class CheckersRecyclerView {

    companion object {

        abstract class Adapter<Model>(private val items: List<Model>) :
                RecyclerView.Adapter<ViewHolder<Model>>() {

            private val setVH = HashSet<ViewHolder<Model>>()

            /**
             * the onItemClickListener holder
             */
            private val onItemClickListener: OnItemClickListener? = null

            /**
             * listeners for click events
             */
            private val clickListeners: ArrayList<OnItemClickListener> = ArrayList()

            override fun onBindViewHolder(holder: ViewHolder<Model>, position: Int) {
                holder.setDataModel(getItem(position))

//                holder.setOnClickListener(View.OnClickListener { v: View? ->
//                    if (holder.adapterPosition == holder.layoutPosition
//                            && holder.adapterPosition < itemCount
//                            && holder.adapterPosition >= 0){
//                        notifyClickEvent(holder.itemView, holder.adapterPosition)
//
//                    }
//                })
            }

            open fun dispose(){
                Log.d("TEST_GAME", "CheckersRecyclerView dispose dispose dispose:  ")

                setVH.forEach { it.destroy() }
            }

            abstract fun getItem(position: Int): Model

            override fun getItemCount(): Int = items.size

            override fun onViewDetachedFromWindow(holder: ViewHolder<Model>) {
                super.onViewDetachedFromWindow(holder)
                Log.d("TEST_GAME", "CheckersRecyclerView onViewDetachedFromWindow MODEL:  ")
            }

            override fun onViewAttachedToWindow(holder: ViewHolder<Model>) {
                super.onViewAttachedToWindow(holder)
                Log.d("TEST_GAME", "CheckersRecyclerView onViewAttachedToWindow MODEL: ")
            }

            override fun onViewRecycled(holder: ViewHolder<Model>) {
                super.onViewRecycled(holder)
                Log.d("TEST_GAME", "CheckersRecyclerView onViewRecycled")
                holder.destroy()
            }

            @CallSuper
            override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                Log.d("TEST_GAME", "CheckersRecyclerView onAttachedToRecyclerView")

            }

            @CallSuper
            override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
                Log.d("TEST_GAME", "CheckersRecyclerView onDetachedFromRecyclerView")

            }

            open fun addOnItemClickListener(onItemClickListener: OnItemClickListener?) {
                this.clickListeners.add(onItemClickListener!!)
            }

            open fun removeOnItemClickListener(onItemClickListener: OnItemClickListener?) {
                this.clickListeners.remove(onItemClickListener)
            }

            open fun notifyClickEvent(view: View?, position: Int) {
                val listeners: Collection<OnItemClickListener> = HashSet<OnItemClickListener>(this.clickListeners)
                for (listener in listeners) {
                    listener.onItemClick(view, position)
                }
            }

            protected open fun addViewHolder(viewHolder: ViewHolder<Model>): ViewHolder<Model> {
                setVH.add(viewHolder)
                return viewHolder
            }

            interface OnItemClickListener {
                fun onItemClick(v: View?, itemPosition: Int)
            }

        }

        abstract class ViewHolder<Model>(itemView: View) :
                RecyclerView.ViewHolder(itemView) {

            constructor(parent: ViewGroup, layoutId: Int) : this(getLayout(parent, layoutId))

            protected val compositeDisposable = CompositeDisposable()

            private var currentModel: Model? = null

            open fun bindData(model: Model) {}

            open fun unBindData(model: Model) {}

            open fun bindDifferentData(model: Model) {}

            open fun unBindDifferentData(model: Model) {}

            fun setDataModel(model: Model) {
                val modelsIsEquals = Objects.equal(model, currentModel)

                if (currentModel != null) {
                    unBindData(currentModel!!)
                    if (!modelsIsEquals) {
                        unBindDifferentData(currentModel!!)
                    }
                }

                currentModel = model

                if (model != null) {
                    bindData(model) ///
                    if (!modelsIsEquals) {
                        bindDifferentData(model)
                    }
                }
            }

            open fun destroy() {

            }

            protected fun getDataModel() = this.currentModel

//            fun setOnClickListener(onClickListener: View.OnClickListener?) {
//                itemView.setOnClickListener(onClickListener)
//            }
        }

        fun getLayout(parent: ViewGroup, layoutId: Int): View =
                LayoutInflater.from(parent.context).inflate(layoutId, parent, false)


    }


}