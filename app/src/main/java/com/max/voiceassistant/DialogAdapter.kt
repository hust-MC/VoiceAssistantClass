package com.max.voiceassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.max.voiceassistant.model.DialogMessage

/**
 * 对话列表适配器
 * 按照[DialogMessage]是用户还是助手来选择不同的item布局。
 */
class DialogAdapter : ListAdapter<DialogMessage, DialogAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogAdapter.ViewHolder {
        val layoutRes = when (viewType) {
            VIEW_TYPE_USER -> R.layout.item_dialog_user
            VIEW_TYPE_ASSISTANT -> R.layout.item_dialog_assistant
            else -> R.layout.item_dialog_assistant
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: DialogAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DialogMessage.User -> VIEW_TYPE_USER
            is DialogMessage.Assistant -> VIEW_TYPE_ASSISTANT
            else -> VIEW_TYPE_ASSISTANT
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        fun bind(message: DialogMessage) {
            tvContent.text = message.content
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DialogMessage>() {
        override fun areItemsTheSame(oldItem: DialogMessage, newItem: DialogMessage): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: DialogMessage, newItem: DialogMessage): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1
    }
}