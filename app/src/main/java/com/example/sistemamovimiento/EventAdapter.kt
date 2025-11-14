package com.example.sistemamovimiento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val items: List<FakeEvent>,
    private val onClick: (FakeEvent) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_event_title)
        val location: TextView = view.findViewById(R.id.text_event_location)
        val timestamp: TextView = view.findViewById(R.id.text_event_timestamp)
        val thumb: ImageView = view.findViewById(R.id.image_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(v)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val e = items[position]
        holder.title.text = e.title
        holder.location.text = e.location
        holder.timestamp.text = e.timestamp
        // imagen placeholder
        holder.thumb.setImageResource(e.imageRes ?: R.drawable.ic_secure_watch_logo)

        holder.itemView.setOnClickListener { onClick(e) }
    }

    override fun getItemCount(): Int = items.size
}
