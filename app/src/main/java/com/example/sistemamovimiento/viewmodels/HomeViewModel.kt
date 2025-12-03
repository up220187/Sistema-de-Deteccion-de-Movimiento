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

class HomeViewModel(context: Context) : ViewModel() {

    private val repo: EventRepository
    private var instantJob: Job? = null

    private val _lastEvent = MutableLiveData<EventEntity?>()
    val lastEvent: LiveData<EventEntity?> = _lastEvent

    private val _events = MutableLiveData<List<EventEntity>>(emptyList())
    val events: LiveData<List<EventEntity>> = _events

    private val _stats = MutableLiveData<DashboardStats?>()
    val stats: LiveData<DashboardStats?> = _stats

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
        _lastEvent.value = event
        _events.value = repo.getLocalEvents()
        _stats.value = repo.getDashboardStats()
    }

    // instant refresh loop mientras la app esté en foreground
    fun startInstantRefresh() {
        if (instantJob != null) return
        instantJob = viewModelScope.launch {
            while (true) {
                try {
                    fetchAndSaveLastEvent()
                } catch (_: Exception) {}
                delay(15000) // 15s
            }
        }
    }

    fun stopInstantRefresh() {
        instantJob?.cancel()
        instantJob = null
    }

    // ayuda para gráfica
    fun countLast7Days(allEvents: List<EventEntity>): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        for (i in 0..6) counts[i] = 0
        val now = java.util.Calendar.getInstance()
        for (evt in allEvents) {
            val millis = if (evt.timestamp < 1_000_000_000_000L) evt.timestamp * 1000 else evt.timestamp
            val cal = java.util.Calendar.getInstance().apply { time = java.util.Date(millis) }
            val diff = daysBetween(cal, now)
            if (diff in 0..6) counts[diff] = (counts[diff] ?: 0) + 1
        }
        return counts
    }

    private fun daysBetween(day: java.util.Calendar, now: java.util.Calendar): Int {
        val start = java.util.Calendar.getInstance().apply {
            timeInMillis = day.timeInMillis
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        val end = java.util.Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        return ((end.timeInMillis - start.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    }
}
