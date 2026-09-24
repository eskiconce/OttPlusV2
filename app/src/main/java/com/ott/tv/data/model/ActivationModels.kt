package com.ott.tv.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request body used when registering this device with the backend.
 */
data class RegisterDeviceRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("device_type") val deviceType: String = "android_tv"
)

/**
 * Response returned by the backend when a device registers successfully.
 * Contains the activation code that must be shown on screen.
 */
data class RegisterDeviceResponse(
    @SerializedName("activation_code") val activationCode: String,
    @SerializedName("expires_in") val expiresInSeconds: Int
)

/**
 * Request body used to poll the activation status.
 */
data class ActivationStatusRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("activation_code") val activationCode: String
)

/**
 * Response returned by the activation status endpoint.
 * When [status] is "activated" the tokens are included.
 */
data class ActivationStatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresInSeconds: Int?
)

enum class ActivationStatus {
    PENDING,
    ACTIVATED,
    EXPIRED,
    UNKNOWN
}
