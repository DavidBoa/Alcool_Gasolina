package com.example.exemplosimplesdecompose.data

data class Posto(
    val nome: String,
    val alcool: Double,
    val gasolina: Double,
//    val localizacao: String,
    val dataCadastro: String,
    val coordenadas: Coordenadas
)