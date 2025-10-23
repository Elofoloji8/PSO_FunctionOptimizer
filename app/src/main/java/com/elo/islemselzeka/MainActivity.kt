package com.elo.islemselzeka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elo.islemselzeka.ui.theme.IslemselZekaTheme
import kotlin.random.Random
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IslemselZekaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PSOFunctionApp()
                }
            }
        }
    }
}

@Composable
fun PSOFunctionApp() {
    var functionInput by remember { mutableStateOf("") }
    var particleCount by remember { mutableStateOf("") }
    var iterationCount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Henüz çalıştırılmadı.") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "PSO Function Optimizer",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = functionInput,
            onValueChange = { functionInput = it },
            label = { Text("Fonksiyon (örnek: x^2 + 2x + 1)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = particleCount,
            onValueChange = { particleCount = it },
            label = { Text("Parçacık Sayısı") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = iterationCount,
            onValueChange = { iterationCount = it },
            label = { Text("İterasyon Sayısı") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val pCount = particleCount.toIntOrNull() ?: 20
                val iCount = iterationCount.toIntOrNull() ?: 50

                if (functionInput.isBlank()) {
                    resultText = "Lütfen bir fonksiyon giriniz!"
                } else {
                    try {
                        val steps = runPSOWithSteps(pCount, iCount, functionInput)
                        resultText = steps.joinToString("\n\n")
                    } catch (e: Exception) {
                        resultText = "Fonksiyon hatalı: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Başlat")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = resultText,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Parçacık veri sınıfı
data class Particle(
    var position: Double,
    var velocity: Double,
    var bestPosition: Double,
    var bestValue: Double
)

// Kullanıcının girdiği fonksiyonu hesaplayan fonksiyon
fun evalFunction(expr: String, x: Double): Double {
    val expression = ExpressionBuilder(expr)
        .variable("x")
        .build()
        .setVariable("x", x)
    return expression.evaluate()
}

// PSO algoritmasını buraya yazdım
fun runPSOWithSteps(particleCount: Int, maxIter: Int, funcExpr: String): List<String> {
    val w = 0.7
    val c1 = 1.5
    val c2 = 1.5
    val rand = Random(System.currentTimeMillis())

    val steps = mutableListOf<String>()
    val particles = ArrayList<Particle>()
    var globalBestPos = 0.0
    var globalBestVal = Double.MAX_VALUE

    // Başlangıç
    for (i in 0 until particleCount) {
        val pos = rand.nextDouble(-10.0, 10.0)
        val valFx = evalFunction(funcExpr, pos)
        val particle = Particle(pos, 0.0, pos, valFx)
        particles.add(particle)

        if (valFx < globalBestVal) {
            globalBestVal = valFx
            globalBestPos = pos
        }
    }

    steps.add("Başlangıç: En iyi x = %.4f, f(x) = %.4f".format(globalBestPos, globalBestVal))

    // İterasyon döngüsü
    for (iter in 1..maxIter) {
        for (p in particles) {
            val r1 = rand.nextDouble()
            val r2 = rand.nextDouble()

            // Hız ve konum güncelleme
            p.velocity = w * p.velocity +
                    c1 * r1 * (p.bestPosition - p.position) +
                    c2 * r2 * (globalBestPos - p.position)

            p.position += p.velocity
            val value = evalFunction(funcExpr, p.position)

            // Bireysel ve global en iyi güncellemeleri
            if (value < p.bestValue) {
                p.bestValue = value
                p.bestPosition = p.position
            }

            if (value < globalBestVal) {
                globalBestVal = value
                globalBestPos = p.position
            }
        }

        steps.add("İterasyon $iter → En iyi x = %.4f, f(x) = %.4f".format(globalBestPos, globalBestVal))
    }

    steps.add("Sonuç: x = %.5f, f(x) = %.5f".format(globalBestPos, globalBestVal))
    return steps
}