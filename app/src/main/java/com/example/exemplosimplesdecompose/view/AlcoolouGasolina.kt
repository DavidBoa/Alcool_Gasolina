package com.example.exemplosimplesdecompose.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.exemplosimplesdecompose.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.data.Posto
import com.example.exemplosimplesdecompose.data.Coordenadas
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AlcoolGasolinaPreco(
    navController: NavHostController,
    postoEditando: Posto? = null
) {

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val sharedPreferences = context.getSharedPreferences(
        "postos",
        Context.MODE_PRIVATE
    )

    val gson = Gson()

    var alcool by remember { mutableStateOf(postoEditando?.alcool?.toString() ?: "") }
    var gasolina by remember { mutableStateOf(postoEditando?.gasolina?.toString() ?: "") }
    var nomeDoPosto by remember { mutableStateOf(postoEditando?.nome ?: "") }

    // ✅ CORREÇÃO AQUI (única mudança relevante)
    var sliderState by remember {
        mutableStateOf(
            sharedPreferences.getBoolean("switch_state", true)
        )
    }

    var resultadoResId by remember { mutableStateOf(R.string.Aguardando_valores_para_o_cálculo) }
    var mensagem by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            mensagem = "Permissão de localização negada"
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.preencha_os_campos),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = alcool,
                onValueChange = { alcool = it },
                label = { Text(stringResource(R.string.preco_alcool)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
            )

            OutlinedTextField(
                value = gasolina,
                onValueChange = { gasolina = it },
                label = { Text(stringResource(R.string.preco_gasolina)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
            )

            OutlinedTextField(
                value = nomeDoPosto,
                onValueChange = { nomeDoPosto = it },
                label = { Text(stringResource(R.string.nome_posto)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("70%")

                Switch(
                    checked = sliderState,
                    onCheckedChange = {
                        sliderState = it
                        sharedPreferences.edit()
                            .putBoolean("switch_state", it)
                            .apply()
                    }
                )

                Text("75%")
            }

            Button(
                onClick = {
                    val a = alcool.toDoubleOrNull()
                    val g = gasolina.toDoubleOrNull()

                    val limite = if (sliderState) 0.75 else 0.70

                    resultadoResId = if (a != null && g != null && g != 0.0) {
                        if (a / g <= limite) {
                            R.string.compensa_alcool
                        } else {
                            R.string.compensa_gasolina
                        }
                    } else {
                        R.string.valores_invalidos
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calcular))
            }

            Text(
                text = stringResource(resultadoResId),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        mensagem = "Permissão de localização não concedida"
                        return@Button
                    }

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->

                        val coordenadas = Coordenadas(
                            latitude = location?.latitude ?: 0.0,
                            longitude = location?.longitude ?: 0.0
                        )

                        val listaJson = sharedPreferences.getString("lista_postos", null)

                        val lista = if (listaJson != null) {
                            gson.fromJson(listaJson, Array<Posto>::class.java).toMutableList()
                        } else {
                            mutableListOf()
                        }

                        postoEditando?.let {
                            lista.removeIf { it.nome == postoEditando.nome }
                        }

                        val dataCadastro = SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(Date())

                        val novoPosto = Posto(
                            nome = nomeDoPosto,
                            alcool = alcool.toDoubleOrNull() ?: 0.0,
                            gasolina = gasolina.toDoubleOrNull() ?: 0.0,
                            dataCadastro = dataCadastro,
                            coordenadas = coordenadas
                        )

                        lista.add(novoPosto)

                        sharedPreferences.edit()
                            .putString("lista_postos", gson.toJson(lista))
                            .apply()

                        mensagem = context.getString(R.string.posto_salvo)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (postoEditando != null)
                        stringResource(R.string.atualizar_posto)
                    else
                        stringResource(R.string.salvar_posto)
                )
            }

            if (mensagem.isNotEmpty()) {
                Text(text = mensagem)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController.navigate("lista")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.ver_lista))
            }
        }
    }
}
fun salvarPostoComGPS(
    coordenadas: Coordenadas,
    sharedPreferences: android.content.SharedPreferences,
    gson: Gson,
    postoEditando: Posto?,
    nomeDoPosto: String,
    alcool: String,
    gasolina: String
) {

    val listaJson = sharedPreferences.getString("lista_postos", null)

    val lista = if (listaJson != null) {
        gson.fromJson(listaJson, Array<Posto>::class.java).toMutableList()
    } else {
        mutableListOf()
    }

    postoEditando?.let {
        lista.removeIf { it.nome == postoEditando.nome }
    }

    val dataCadastro = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date())

    val novoPosto = Posto(
        nome = nomeDoPosto,
        alcool = alcool.toDoubleOrNull() ?: 0.0,
        gasolina = gasolina.toDoubleOrNull() ?: 0.0,
        dataCadastro = dataCadastro,
        coordenadas = coordenadas
    )

    lista.add(novoPosto)

    sharedPreferences.edit()
        .putString("lista_postos", gson.toJson(lista))
        .apply()
}