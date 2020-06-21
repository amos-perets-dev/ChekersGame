package com.example.chekersgamepro.checkers.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.chekersgamepro.checkers.CheckersApplication
import com.google.common.base.Objects
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import kotlin.collections.ArrayList


@SuppressLint("ViewConstructor")
class CheckersRecyclerView(context: Context, attributeSet: AttributeSet) : RecyclerView(context, attributeSet) {

    init {
        setHasFixedSize(true)
        setItemViewCacheSize(40)
        isDrawingCacheEnabled = true
        drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        isNestedScrollingEnabled = false
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    fun addPagerSnap(){
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(this)
    }

    override fun onAttachedToWindow() {
        Log.d("TEST_GAME", "CheckersRecyclerView onAttachedToWindow: ")

        super.onAttachedToWindow()
        // View is now attached
    }

    override fun onDetachedFromWindow() {
        Log.d("TEST_GAME", "CheckersRecyclerView onDetachedFromWindow: ")

        super.onDetachedFromWindow()
        // View is now detached, and about to be destroyed
    }



    companion object {

        abstract class Adapter<Model>(private var items: List<Model>) :
                RecyclerView.Adapter<ViewHolder<Model>>() {
            constructor() : this(Collections.emptyList())

            private val setVH = HashSet<ViewHolder<Model>>()

            private var screenWidth : Int = 0
            private var itemWidth : Int = 0
            private var itemOffset : Int = 0
            private val context = CheckersApplication.create().applicationContext

            init {
                screenWidth = context.resources.displayMetrics.widthPixels
                itemWidth = ((screenWidth / 1.4f).toInt())
                itemOffset = (screenWidth * 0.1).toInt()
            }

            /**
             * the onItemClickListener holder
             */
            private val onItemClickListener: OnItemClickListener? = null

            private lateinit var recyclerView: RecyclerView
            private lateinit var linearLayoutManager: LinearLayoutManager

            /**
             * listeners for click events
             */
            private val clickListeners: ArrayList<OnItemClickListener> = ArrayList()

//            init {
//                setHasStableIds(true)
//            }

            fun updateList(listPlayerEvents: List<Model>) {
                this.items = ArrayList(listPlayerEvents)
                notifyDataSetChanged()
//                notifyItemChanged(1)
//                notifyItemRangeChanged( listPlayerEvents.size - 1,1 )
//                notifyItemRangeInserted(this.items.size - 1, 1)
                Log.d("TEST_GAME", "CheckersRecyclerView updateList size: ${listPlayerEvents.size} ")
            }

            override fun onBindViewHolder(holder: ViewHolder<Model>, position: Int) {
                Log.d("TEST_GAME", "CheckersRecyclerView onBindViewHolder:  ")

                val model = getItemByPos(position) ?: getItem(position)

                holder.setDataModel(model)
                if(position == 0){
                    if (isFirstItemNeedChangeItemSize()){
                        changeItemSize(holder)
                    }
                } else if (isNeedChangeItemSize()){
                     changeItemSize(holder)
                }
            }

            protected open fun isNeedChangeItemSize() =  false

            protected open fun isFirstItemNeedChangeItemSize() =  true

            protected open fun getScreenWidth() = 0

            protected open fun getScreenHeight() = 0

            protected open fun getFactorHeight() =  1.8f

            private fun changeItemSize(holder: ViewHolder<Model>){
                val screenWidth = (holder.itemView.context).resources.displayMetrics.widthPixels
//                itemWidth = ((screenWidth / 1.4f).toInt())

                val lp = holder.itemView.layoutParams
//                lp.width = itemWidth
                val screenHeight = getScreenHeight()

                lp.height = ((screenHeight / getFactorHeight()).toInt())
                holder.itemView.layoutParams = lp
            }

            private fun getItemByPos(position: Int): Model{
                return items[position]
            }

            open fun dispose() {
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
                this.recyclerView = recyclerView

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
                Log.d("TEST_GAME", "CheckersRecyclerView lsetVH.add(viewHolder)")

                setVH.add(viewHolder)
                return viewHolder
            }

            fun scrollToPositionWithOffset() {
                val layoutManager = recyclerView.layoutManager
                Log.d("TEST_GAME", "CheckersRecyclerView scrollToPositionWithOffset layoutManager: ${layoutManager} itemcount: $itemCount")
                (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(1, itemOffset)
            }


            interface OnItemClickListener {
                fun onItemClick(v: View?, itemPosition: Int)
            }

        }

        abstract class ViewHolder<Model>(itemView: View) :
                RecyclerView.ViewHolder(itemView) {

            constructor(parent: ViewGroup, layoutId: Int) : this(getLayout(parent, layoutId))

            init {
                itemView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }

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
                    bindData(model)
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