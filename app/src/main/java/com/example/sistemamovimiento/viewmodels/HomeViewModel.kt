package com.example.sistemamovimiento.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.DashboardStats
import com.example.sistemamovimiento.repository.EventRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(context: Context) : ViewModel() {

    private val repo: EventRepository

    private var instantJob: Job? = null   // Refresco rápido
    private var backgroundJob: Job? = null // Para pruebas internas (opcional)

    private val _lastEvent = MutableLiveData<EventEntity?>()
    val lastEvent: LiveData<EventEntity?> = _lastEvent

    private val _events = MutableLiveData<List<EventEntity>>()
    val events: LiveData<List<EventEntity>> = _events

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    init {
        val db = AppDatabase.getDatabase(context)
        repo = EventRepository(RetrofitClient.instance, db.eventDao())
    }

    fun loadLocalData() {
        viewModelScope.launch {
            _lastEvent.value = repo.getLastLocalEvent()
            _events.value = repo.getLocalEvents()
            _stats.value = repo.getDashboardStats()
        }
    }

    suspend fun fetchAndSaveLastEvent() {
        val event = repo.fetchAndSaveLastEvent()
        _lastEvent.postValue(event)
        _events.postValue(repo.getLocalEvents())
        _stats.postValue(repo.getDashboardStats())
    }

    // ---------------------------------------------------------
    // 🔥 REFRESCO INSTANTÁNEO CADA 2 SEGUNDOS (solo en Home)
    // ---------------------------------------------------------
    fun startInstantRefresh() {
        if (instantJob != null) return

        instantJob = viewModelScope.launch {
            while (true) {
                try {
                    fetchAndSaveLastEvent()
                } catch (_: Exception) {}
                delay(2000)   // ⚡ SUPER RÁPIDO dentro de la app
            }
        }
    }

    fun stopInstantRefresh() {
        instantJob?.cancel()
        instantJob = null
    }

    // ---------------------------------------------------------
    // FUNCIONES PARA LA GRÁFICA
    // ---------------------------------------------------------
    fun countLast7Days(events: List<EventEntity>): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        for (i in 0..6) counts[i] = 0

        val now = Calendar.getInstance()

        for (evt in events) {
            val millis =
                if (evt.timestamp < 1_000_000_000_000L) evt.timestamp * 1000 else evt.timestamp
            val cal = Calendar.getInstance().apply { time = Date(millis) }

            val diff =
                ((now.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            if (diff in 0..6) {
                counts[diff] = (counts[diff] ?: 0) + 1
            }
        }

        return counts
    }
}
