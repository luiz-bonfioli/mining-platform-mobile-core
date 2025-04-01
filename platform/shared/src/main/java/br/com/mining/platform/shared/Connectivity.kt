package br.com.mining.platform.shared

data class Connectivity(
        var event: ConnectivityState,
        var isOnline: Boolean
)