package com.example.sistemamovimiento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sistemamovimiento.data.local.EventEntity
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private val items: List<EventEntity>,
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
        val event = items[position]

        // 1. Título basado en severidad
        holder.title.text = "Prioridad ${event.severity.uppercase()}"

        // 2. Subtítulo: Contar sensores activos
        val activeCount = listOf(event.ir, event.pir, event.sound).count { it == 1 }
        holder.location.text = "Sensores activos: $activeCount"

        // 3. Formatear fecha
        // Ajuste por si viene en segundos o milisegundos
        val tsMillis = if (event.timestamp < 1000000000000L) event.timestamp * 1000 else event.timestamp
        val date = Date(tsMillis)
        val format = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        holder.timestamp.text = format.format(date)

        // 4. Imagen con Glide usando blobUrl directo de la entidad
        if (!event.blobUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(event.blobUrl)
                .placeholder(R.drawable.securewatch_logo)
                .error(R.drawable.securewatch_logo)
                .centerCrop()
                .into(holder.thumb)
        } else {
            holder.thumb.setImageResource(R.drawable.securewatch_logo)
        }

        holder.itemView.setOnClickListener { onClick(event) }
    }

    override fun getItemCount(): Int = items.size
}
