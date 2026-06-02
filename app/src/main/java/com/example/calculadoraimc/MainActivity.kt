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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Envuelvo toda la app en el tema de Material3 para que se vea decente
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Aquí arranca el sistema de navegación
                    AppNavigation()
                }
            }
        }
    }
}

// Composable que controla por qué pantalla debe ir el usuario
@Composable
fun AppNavigation() {
    // rememberNavController crea el "GPS" de la app para moverme entre pantallas
    val navController = rememberNavController()

    // NavHost es el contenedor donde se dibujan las pantallas
    NavHost(navController = navController, startDestination = "pantalla_ingreso") {

        // Ruta de la primera pantalla (formulario)
        composable("pantalla_ingreso") {
            InputScreen(navController = navController)
        }

        // Ruta de la segunda pantalla con dos parámetros: nombre e IMC
        composable("resultado/{nombre}/{imc}") { backStackEntry ->
            // Saco los datos que vienen en la ruta
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val imc = backStackEntry.arguments?.getString("imc") ?: "0.0"
            ResultScreen(nombre = nombre, imc = imc, navController = navController)
        }
    }
}

// PANTALLA 1: Donde el usuario escribe sus datos
@Composable
fun InputScreen(navController: androidx.navigation.NavController) {
    // Declaro las variables que van a cambiar en la pantalla
    // remember guarda el valor para que no se pierda mientras escribo
    var nombre by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    // Uso Column para poner todo uno debajo del otro de forma ordenada
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Uso Row para alinear el título con un pequeño emoji que hace de ícono
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "⚖️ ", fontSize = 28.sp)
            Text(text = "Calculadora IMC", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Campo de texto para el nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it }, // Cada vez que escribe, actualizo la variable
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Campo para peso (solo permite teclado numérico)
        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Campo para altura (solo permite teclado numérico)
        OutlinedTextField(
            value = altura,
            onValueChange = { altura = it },
            label = { Text("Altura (m)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Si hay error, muestro el mensaje en rojo
        if (mensajeError.isNotEmpty()) {
            Text(text = mensajeError, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Botón para calcular
        Button(
            onClick = {
                mensajeError = "" // Limpio el error anterior antes de validar

                // Intento convertir los textos a números decimales (puede fallar si hay letras)
                val pesoDouble = peso.toDoubleOrNull()
                val alturaDouble = altura.toDoubleOrNull()

                // Validación: si es null o menor/igual a 0, muestro error y no avanzo
                if (pesoDouble == null || alturaDouble == null || pesoDouble <= 0 || alturaDouble <= 0) {
                    mensajeError = "Por favor, ingresa valores válidos"
                    return@Button // Salgo de la función para no navegar
                }

                // Calculo el IMC: peso dividido por altura al cuadrado
                val imcCalculado = pesoDouble / (alturaDouble * alturaDouble)
                // Lo dejo con 1 decimal para que se vea limpio
                val imcFormateado = String.format(Locale.US, "%.1f", imcCalculado)

                // Reemplazo espacios por %20 para que la ruta no se rompa
                val nombreCodificado = nombre.replace(" ", "%20")

                // Navego a la pantalla de resultado enviando los dos parámetros
                navController.navigate("resultado/$nombreCodificado/$imcFormateado")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular")
        }
    }
}

// PANTALLA 2: Donde se muestra el resultado
@Composable
fun ResultScreen(nombre: String, imc: String, navController: androidx.navigation.NavController) {
    // Convierto el IMC que llega como texto a número para poder compararlo
    val imcValue = imc.toDoubleOrNull() ?: 0.0

    // Devuelvo los espacios al nombre que antes cambié por %20
    val nombreMostrar = nombre.replace("%20", " ")

    // Uso when para ver en qué rango cae el IMC
    // Devuelvo una tupla con el texto de la categoría y el color que le toca
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

        // Uso Row para poner el IMC y la unidad lado a lado
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("IMC: $imc", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(" kg/m²", fontSize = 16.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Muestro la categoría con el color dinámico que calculé antes
        Text(
            text = categoria,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorCategoria
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para volver. popBackStack() me regresa a la pantalla anterior
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}