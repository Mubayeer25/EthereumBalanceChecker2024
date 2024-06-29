package com.example.ethereumbalancechecker
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EthereumApi {
    @GET("api")
    fun getBalance(
        @Query("module") module: String = "account",
        @Query("action") action: String = "balance",
        @Query("address") address: String,
        @Query("tag") tag: String = "latest",
        @Query("apikey") apikey: String
    ): Call<BalanceResponse>

    @GET("api")
    fun getNonce(
        @Query("module") module: String = "proxy",
        @Query("action") action: String = "eth_getTransactionCount",
        @Query("address") address: String,
        @Query("tag") tag: String = "latest",
        @Query("apikey") apikey: String
    ): Call<NonceResponse>
}

data class BalanceResponse(
    val status: String,
    val message: String,
    val result: String
)

data class NonceResponse(
    val jsonrpc: String,
    val id: Int,
    val result: String
)