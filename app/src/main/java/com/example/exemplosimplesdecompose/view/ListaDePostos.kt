package com.example.exemplosimplesdecompose.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.data.Posto
import com.google.gson.Gson
import androidx.compose.ui.res.stringResource
import com.example.exemplosimplesdecompose.R
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ListaDePostos(navController: NavHostController) {

    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences(
        "postos",
        Context.MODE_PRIVATE
    )

    val gson = remember { Gson() }

    var postos by remember { mutableStateOf(listOf<Posto>()) }

    fun carregarPostos() {
        val listaJson = sharedPreferences.getString("lista_postos", null)

        postos = if (listaJson != null) {
            gson.fromJson(listaJson, Array<Posto>::class.java).toList()
        } else {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        carregarPostos()
    }

    Column(modifier = Modifier.padding(40.dp)) {

        Text(
            text = stringResource(R.string.lista_postos),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(postos) { posto ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(text = "${stringResource(R.string.nome_posto)}: ${posto.nome}")
                        Text(text = "${stringResource(R.string.preco_alcool)}: ${posto.alcool}")
                        Text(text = "${stringResource(R.string.preco_gasolina)}: ${posto.gasolina}")
//                        Text(text = "${stringResource(R.string.localizacao)}: ${posto.localizacao}")
                        Text(text = "Latitude: ${posto.coordenadas.latitude}")
                        Text(text = "Longitude: ${posto.coordenadas.longitude}")
                        Text(text = "${stringResource(R.string.data_cadastro)}: ${posto.dataCadastro}")

                        Spacer(modifier = Modifier.height(8.dp))

                        // 🗺 BOTÃO MAPA (NOVO)
                        Button(onClick = {

                            val uri = Uri.parse(
                                "geo:${posto.coordenadas.latitude},${posto.coordenadas.longitude}")

                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)

                        }) {
                            Text("Ver no mapa")
                        }

                        Button(onClick = {

                            val novaLista = postos.toMutableList()
                            novaLista.remove(posto)

                            sharedPreferences.edit()
                                .putString("lista_postos", gson.toJson(novaLista))
                                .apply()

                            carregarPostos()

                        }) {
                            Text(text = stringResource(R.string.excluir))
                        }

                        Button(onClick = {

                            val json = gson.toJson(posto)
                            val jsonEncoded = URLEncoder.encode(
                                json,
                                StandardCharsets.UTF_8.toString()
                            )

                            navController.navigate("editar/$jsonEncoded")

                        }) {
                            Text(text = stringResource(R.string.editar))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("mainalcgas") {
                    popUpTo("mainalcgas") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.voltar_calculo))
        }
    }
}