package com.example.sistemamovimiento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase de modelo de datos para representar una lectura del sensor en Firestore.
 */
data class SensorReading(
    val id: String, // ID del documento de Firestore
    val timestamp: Long, // Marca de tiempo guardada
    val raw_payload: String, // El string de datos del sensor
    val received_by_user: String
)

/**
 * Adaptador para el RecyclerView que muestra la lista de lecturas de sensores.
 */
class SensorReadingAdapter(private var readings: List<SensorReading>) :
    RecyclerView.Adapter<SensorReadingAdapter.ReadingViewHolder>() {

    // Formato de fecha para la UI
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    inner class ReadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val tvRawPayload: TextView = itemView.findViewById(R.id.tv_raw_payload)

        fun bind(reading: SensorReading) {
            // Convierte el timestamp (long) a una cadena de fecha legible
            val date = Date(reading.timestamp)
            tvTimestamp.text = "Fecha: ${dateFormat.format(date)} (ID Doc: ${reading.id})"

            // Muestra el payload (la cadena de datos del sensor)
            tvRawPayload.text = "Datos: ${reading.raw_payload}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_reading, parent, false)
        return ReadingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        holder.bind(readings[position])
    }

    override fun getItemCount(): Int = readings.size

    /**
     * Función para actualizar la lista de datos.
     * Ordena por fecha descendente (más reciente primero).
     */
    fun updateReadings(newReadings: List<SensorReading>) {
        readings = newReadings.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}