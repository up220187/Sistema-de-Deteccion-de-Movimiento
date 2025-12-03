package com.example.sistemamovimiento.network

import retrofit2.http.GET

interface EventApiService {
    @GET("last")
    suspend fun getLastEvent(): EventResponse
}
