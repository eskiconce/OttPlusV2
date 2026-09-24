package com.ott.tv.data.repository

import com.ott.tv.data.api.OttApiService
import com.ott.tv.data.model.ActivationStatus
import com.ott.tv.data.model.ActivationStatusRequest
import com.ott.tv.data.model.ActivationStatusResponse
import com.ott.tv.data.model.RegisterDeviceRequest
import com.ott.tv.data.model.RegisterDeviceResponse
import com.ott.tv.data.storage.AuthTokenStore
import com.ott.tv.data.storage.DeviceIdProvider
import kotlinx.coroutines.delay

class ActivationRepository(
    private val apiService: OttApiService,
    private val deviceIdProvider: DeviceIdProvider,
    private val tokenStore: AuthTokenStore
) {

    private val deviceId: String
        get() = deviceIdProvider.deviceId

    suspend fun registerDevice(deviceName: String): Result<RegisterDeviceResponse> {
        return try {
            val request = RegisterDeviceRequest(
                deviceId = deviceId,
                deviceName = deviceName
            )
            val response = apiService.registerDevice(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiException("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun waitForActivation(
        activationCode: String,
        onStatusChecked: (ActivationStatus) -> Unit = {}
    ): Result<ActivationStatusResponse> {
        var attempts = 0

        while (attempts < MAX_POLLING_ATTEMPTS) {
            val result = checkActivationStatus(activationCode)

            if (result.isFailure) {
                return result
            }

            val body = result.getOrNull() ?: return Result.failure(ApiException("Empty response"))
            val status = parseStatus(body.status)
            onStatusChecked(status)

            when (status) {
                ActivationStatus.ACTIVATED -> {
                    tokenStore.saveTokens(body)
                    return Result.success(body)
                }
                ActivationStatus.EXPIRED -> {
                    return Result.failure(ActivationExpiredException())
                }
                ActivationStatus.PENDING, ActivationStatus.UNKNOWN -> {
                    delay(POLLING_INTERVAL_MS)
                    attempts++
                }
            }
        }

        return Result.failure(ActivationExpiredException())
    }

    private suspend fun checkActivationStatus(activationCode: String): Result<ActivationStatusResponse> {
        return try {
            val request = ActivationStatusRequest(deviceId, activationCode)
            val response = apiService.checkActivationStatus(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiException("Status check failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStatus(status: String?): ActivationStatus {
        return when (status?.lowercase()) {
            "pending", "waiting" -> ActivationStatus.PENDING
            "activated", "active" -> ActivationStatus.ACTIVATED
            "expired", "timeout" -> ActivationStatus.EXPIRED
            else -> ActivationStatus.UNKNOWN
        }
    }

    companion object {
        private const val POLLING_INTERVAL_MS = 5_000L
        private const val MAX_POLLING_ATTEMPTS = 120
    }
}

class ApiException(message: String) : Exception(message)
class ActivationExpiredException : Exception("Activation code expired")
