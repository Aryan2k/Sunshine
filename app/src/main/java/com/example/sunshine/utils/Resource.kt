package com.example.sunshine.utils

data class Resource<out T>(val status: RequestStatus, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(RequestStatus.SUCCESS, data, null)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(RequestStatus.LOADING, data, null)
        }

        fun <T> error(data: T?, msg: String): Resource<T> {
            return Resource(RequestStatus.EXCEPTION, data, msg)
        }
    }
}