package com.ott.tv.data.api

import com.ott.tv.data.model.ActivationStatusRequest
import com.ott.tv.data.model.ActivationStatusResponse
import com.ott.tv.data.model.RegisterDeviceRequest
import com.ott.tv.data.model.RegisterDeviceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OttApiService {

    @POST("api/v1/devices/register")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<RegisterDeviceResponse>

    @POST("api/v1/devices/status")
    suspend fun checkActivationStatus(@Body request: ActivationStatusRequest): Response<ActivationStatusResponse>
}
