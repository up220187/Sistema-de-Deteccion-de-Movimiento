package com.example.sistemamovimiento.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.local.EventEntity
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private var items: List<EventEntity>,
    private val onClick: (EventEntity) -> Unit
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

        // --- TÍTULO ---
        holder.title.text = "Prioridad ${e.severity}"

        // --- DETALLE DE SENSORES ---
        val active = mutableListOf<String>()
        if (e.ir == 1) active.add("IR")
        if (e.pir == 1) active.add("PIR")
        if (e.sound == 1) active.add("Sonido")
        holder.location.text = "Sensores: ${
            if (active.isEmpty()) "Ninguno" else active.joinToString(", ")
        }"

        // --- TIMESTAMP ---
        val ts = if (e.timestamp < 1_000_000_000_000L) e.timestamp * 1000 else e.timestamp
        val date = Date(ts)
        holder.timestamp.text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date)

        // --- MINI FOTO ---
        Glide.with(holder.itemView.context)
            .load(e.blobUrl ?: R.drawable.securewatch_logo)
            .placeholder(R.drawable.securewatch_logo)
            .into(holder.thumb)

        holder.itemView.setOnClickListener { onClick(e) }
    }



    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<EventEntity>) {
        items = newItems
        notifyDataSetChanged()
    }






}
