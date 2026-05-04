package com.example.exemplosimplesdecompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.exemplosimplesdecompose.ui.theme.ExemploSimplesDeComposeTheme
import com.example.exemplosimplesdecompose.view.AlcoolGasolinaPreco
import com.example.exemplosimplesdecompose.view.ListaDePostos
import com.example.exemplosimplesdecompose.view.Welcome
import com.example.exemplosimplesdecompose.data.Posto
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExemploSimplesDeComposeTheme {

                val navController: NavHostController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "welcome"
                ) {

                    composable("welcome") {
                        Welcome(navController)
                    }

                    composable("mainalcgas") {
                        AlcoolGasolinaPreco(navController)
                    }

                    composable("lista") {
                        ListaDePostos(navController)
                    }

                    composable("editar/{postoJson}") { backStackEntry ->

                        val jsonEncoded = backStackEntry.arguments?.getString("postoJson") ?: ""
                        val json = URLDecoder.decode(jsonEncoded, StandardCharsets.UTF_8.toString())

                        val posto = Gson().fromJson(json, Posto::class.java)

                        AlcoolGasolinaPreco(navController, posto)

                    }

                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExemploSimplesDeComposeTheme {
        Greeting("Android")
    }
}