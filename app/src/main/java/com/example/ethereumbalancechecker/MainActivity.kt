package com.example.ethereumbalancechecker
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.math.BigInteger
import com.example.ethereumbalancechecker.R

class MainActivity : Activity() {
    private lateinit var etAddress: EditText
    private lateinit var tvBalance: TextView
    private lateinit var tvNonce: TextView
    private lateinit var ethApi: EthereumApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etAddress = findViewById(R.id.etAddress)
        tvBalance = findViewById(R.id.tvBalance)
        tvNonce = findViewById(R.id.tvNonce)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.etherscan.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        ethApi = retrofit.create(EthereumApi::class.java)

        findViewById<Button>(R.id.btnScanQR).setOnClickListener {
            IntentIntegrator(this).initiateScan()
        }

        findViewById<Button>(R.id.btnCheckBalance).setOnClickListener {
            val address = etAddress.text.toString()
            if (address.isNotEmpty()) {
                checkBalance(address)
            } else {
                Toast.makeText(this, "Please enter an Ethereum address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
            } else {
                etAddress.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkBalance(address: String) {
        val apiKey = "ZUA44B7MEN97YPV65J33TKA4354X7T5TYY" // Replace with your actual API key

        tvBalance.text = "Fetching balance..."
        tvNonce.text = "Fetching nonce..."

        ethApi.getBalance(address = address, apikey = apiKey).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(call: Call<BalanceResponse>, response: Response<BalanceResponse>) {
                if (response.isSuccessful) {
                    val balance = response.body()?.result
                    tvBalance.text = "Balance: ${balance?.toBigInteger()?.toEther()} ETH"
                } else {
                    tvBalance.text = "Error fetching balance"
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                tvBalance.text = "Error: ${t.message}"
            }
        })

        ethApi.getNonce(address = address, apikey = apiKey).enqueue(object : Callback<NonceResponse> {
            override fun onResponse(call: Call<NonceResponse>, response: Response<NonceResponse>) {
                if (response.isSuccessful) {
                    val nonce = response.body()?.result
                    tvNonce.text = "Nonce: ${nonce?.removePrefix("0x")?.toInt(16)}"
                } else {
                    tvNonce.text = "Error fetching nonce"
                }
            }

            override fun onFailure(call: Call<NonceResponse>, t: Throwable) {
                tvNonce.text = "Error: ${t.message}"
            }
        })
    }

    private fun BigInteger.toEther(): String {
        val ether = this.toBigDecimal().divide(BigDecimal.TEN.pow(18))
        return String.format("%.6f", ether)
    }
}