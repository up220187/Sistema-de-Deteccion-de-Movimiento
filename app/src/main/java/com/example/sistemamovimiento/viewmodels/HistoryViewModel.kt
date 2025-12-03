package com.example.sistemamovimiento.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository
import kotlinx.coroutines.launch

class HistoryViewModel(context: Context) : ViewModel() {

    private val repo: EventRepository

    private val _events = MutableLiveData<List<EventEntity>>(emptyList())
    val events: LiveData<List<EventEntity>> = _events

    init {
        val db = AppDatabase.getDatabase(context)
        repo = EventRepository(RetrofitClient.instance, db.eventDao())
    }

    fun loadLocalEvents() {
        viewModelScope.launch {
            _events.value = repo.getLocalEvents()
        }
    }
}
