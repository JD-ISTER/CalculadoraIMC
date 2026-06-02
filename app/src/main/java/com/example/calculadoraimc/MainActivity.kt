package com.example.calculadoraimc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "pantalla_ingreso") {
        composable("pantalla_ingreso") {
            InputScreen(navController = navController)
        }
        composable("resultado/{nombre}/{imc}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val imc = backStackEntry.arguments?.getString("imc") ?: "0.0"
            ResultScreen(nombre = nombre, imc = imc, navController = navController)
        }
    }
}

@Composable
fun InputScreen(navController: androidx.navigation.NavController) {
    var nombre by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Calculadora IMC", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = altura,
            onValueChange = { altura = it },
            label = { Text("Altura (m)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(text = mensajeError, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                mensajeError = ""

                val pesoDouble = peso.toDoubleOrNull()
                val alturaDouble = altura.toDoubleOrNull()

                if (pesoDouble == null || alturaDouble == null || pesoDouble <= 0 || alturaDouble <= 0) {
                    mensajeError = "Por favor, ingresa valores válidos"
                    return@Button
                }

                val imcCalculado = pesoDouble / (alturaDouble * alturaDouble)
                val imcFormateado = String.format("%.1f", imcCalculado)

                val nombreCodificado = nombre.replace(" ", "%20")
                navController.navigate("resultado/$nombreCodificado/$imcFormateado")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular")
        }
    }
}

@Composable
fun ResultScreen(nombre: String, imc: String, navController: androidx.navigation.NavController) {
    val imcValue = imc.toDoubleOrNull() ?: 0.0

    val nombreMostrar = nombre.replace("%20", " ")

    val (categoria, colorCategoria) = when {
        imcValue < 18.5 -> "Bajo peso" to Color.Red
        imcValue < 24.9 -> "Peso normal" to Color.Green
        imcValue < 29.9 -> "Sobrepeso" to Color(0xFFFFA500) // Naranja
        else -> "Obesidad" to Color.Red
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hola $nombreMostrar, tu resultado es:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Text("IMC: $imc", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = categoria,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorCategoria
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}
}