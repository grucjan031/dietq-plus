package com.example.dietq_plus

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Dish(
    val id: Int,
    val nazwa: String,
    val opis: String,
    val sposob_przygotowania: String,
    val ma_zdjecie: Boolean,
    val skladniki: List<Ingredient>,
    val wartosci_odzywcze: NutritionalValues
)

data class Ingredient(
    val nazwa: String,
    val ilosc: Int,
    val jednostka: String,
    val wartosci_na_100g: NutritionalValues
)

data class NutritionalValues(
    val kcal: Double,
    val bialko: Double,
    val weglowodany: Double,
    val tluszcze: Double
)

interface DishApiService {
    @GET("api/getalldishes")
    suspend fun getAllDishes(): List<Dish>
}

object RetrofitInstance {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.55.107:5000/") // Adres dla emulatora (localhost)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: DishApiService = retrofit.create(DishApiService::class.java)
}
