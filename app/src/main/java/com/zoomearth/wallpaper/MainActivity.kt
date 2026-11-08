package com.zoomearth.wallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EquationScreen()
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EquationScreen() {
    var equation by remember { mutableStateOf("2x + 3 = 11") }
    var outcome by remember { mutableStateOf<Solution?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Ecuaciones paso a paso") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Resuelve ecuaciones de una incógnita y descubre cómo llegar a la respuesta.",
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = equation,
                onValueChange = { equation = it; error = null },
                label = { Text("Escribe una ecuación") },
                placeholder = { Text("Por ejemplo: x^2 - 5x + 6 = 0") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 3,
                singleLine = false
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    try {
                        outcome = EquationSolver.solve(equation)
                        error = null
                    } catch (e: IllegalArgumentException) {
                        outcome = null
                        error = e.message ?: "No se pudo interpretar la ecuación."
                    }
                }) { Text("Resolver") }
                Button(onClick = {
                    equation = "x^2 - 5x + 6 = 0"
                    try {
                        outcome = EquationSolver.solve(equation)
                        error = null
                    } catch (e: IllegalArgumentException) {
                        outcome = null
                        error = e.message
                    }
                }) { Text("Probar ejemplo") }
            }
            Text(
                "Admite ecuaciones lineales y cuadráticas con x, paréntesis y operaciones +, −, ×, ÷ y potencias enteras hasta 2.",
                style = MaterialTheme.typography.bodySmall
            )
            error?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
            outcome?.let { solution ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Solución", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(solution.answer, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        solution.steps.forEachIndexed { index, step ->
                            Text("${index + 1}. $step", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

private data class Solution(val answer: String, val steps: List<String>)

/** A small symbolic solver for real polynomial equations of degree at most two. */
private object EquationSolver {
    fun solve(input: String): Solution {
        require(input.isNotBlank()) { "Escribe una ecuación para empezar." }
        val sides = input.split('=')
        require(sides.size == 2 && sides.all { it.isNotBlank() }) {
            "Escribe una sola igualdad, por ejemplo: 2x + 3 = 11."
        }
        val left = Parser(sides[0]).parse()
        val right = Parser(sides[1]).parse()
        val p = left - right
        val a = p.c[2]
        val b = p.c[1]
        val c = p.c[0]
        val steps = mutableListOf<String>()
        steps += "Desarrollamos ambos lados y pasamos todos los términos al lado izquierdo: ${p.pretty()} = 0."

        if (nearZero(a) && nearZero(b) && nearZero(c)) {
            steps += "La igualdad se cumple para cualquier valor de x."
            return Solution("Infinitas soluciones", steps)
        }
        if (nearZero(a) && nearZero(b)) {
            steps += "Obtenemos una igualdad falsa (${fmt(c)} = 0), así que no existe solución."
            return Solution("Sin solución", steps)
        }
        if (nearZero(a)) {
            steps += "Es una ecuación lineal. Despejamos x: ${fmt(b)}x = ${fmt(-c)}."
            val x = -c / b
            steps += "Dividimos entre ${fmt(b)}: x = ${fmt(x)}."
            return Solution("x = ${fmt(x)}", steps)
        }

        steps += "Identificamos a = ${fmt(a)}, b = ${fmt(b)} y c = ${fmt(c)} en ax² + bx + c = 0."
        val d = b * b - 4.0 * a * c
        steps += "Calculamos el discriminante: Δ = b² − 4ac = ${fmt(d)}."
        if (d < -1e-10) {
            steps += "Como Δ < 0, no hay soluciones reales."
            return Solution("Sin soluciones reales", steps)
        }
        if (nearZero(d)) {
            val x = -b / (2.0 * a)
            steps += "Como Δ = 0, hay una raíz doble: x = −b / (2a) = ${fmt(x)}."
            return Solution("x = ${fmt(x)} (raíz doble)", steps)
        }
        val root = kotlin.math.sqrt(d)
        val x1 = (-b + root) / (2.0 * a)
        val x2 = (-b - root) / (2.0 * a)
        steps += "Aplicamos la fórmula general x = (−b ± √Δ) / (2a)."
        steps += "Sustituimos los valores: x₁ = ${fmt(x1)} y x₂ = ${fmt(x2)}."
        return Solution("x₁ = ${fmt(x1)}; x₂ = ${fmt(x2)}", steps)
    }

    private fun nearZero(value: Double) = kotlin.math.abs(value) < 1e-10
    private fun fmt(value: Double): String {
        val clean = if (nearZero(value)) 0.0 else value
        return java.text.DecimalFormat("0.##########", java.text.DecimalFormatSymbols(java.util.Locale.US)).format(clean)
    }
}

/** Polynomial coefficients are stored as c[0] + c[1]x + c[2]x². */
private data class Polynomial(val c: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)) {
    operator fun plus(other: Polynomial) = Polynomial(DoubleArray(3) { c[it] + other.c[it] })
    operator fun minus(other: Polynomial) = this + other.scale(-1.0)
    fun scale(k: Double) = Polynomial(DoubleArray(3) { c[it] * k })

    operator fun times(other: Polynomial): Polynomial {
        val result = DoubleArray(5)
        for (i in 0..2) for (j in 0..2) result[i + j] += c[i] * other.c[j]
        require(kotlin.math.abs(result[3]) < 1e-10 && kotlin.math.abs(result[4]) < 1e-10) {
            "La ecuación supera el grado 2. Esta versión resuelve ecuaciones lineales y cuadráticas."
        }
        return Polynomial(doubleArrayOf(result[0], result[1], result[2]))
    }

    fun divide(other: Polynomial): Polynomial {
        require(kotlin.math.abs(other.c[1]) < 1e-10 && kotlin.math.abs(other.c[2]) < 1e-10) {
            "Solo se puede dividir entre un número, no entre una expresión con x."
        }
        require(kotlin.math.abs(other.c[0]) >= 1e-10) { "No se puede dividir entre cero." }
        return scale(1.0 / other.c[0])
    }

    fun pow(n: Int): Polynomial {
        require(n in 0..2) { "Solo se admiten potencias enteras de 0, 1 o 2." }
        var result = Polynomial(doubleArrayOf(1.0, 0.0, 0.0))
        repeat(n) { result *= this }
        return result
    }

    fun pretty(): String {
        val parts = mutableListOf<String>()
        for (degree in 2 downTo 0) {
            val value = c[degree]
            if (kotlin.math.abs(value) < 1e-10) continue
            val absValue = kotlin.math.abs(value)
            val term = when (degree) {
                2 -> "${if (kotlin.math.abs(absValue - 1.0) < 1e-10) "" else EquationSolver.run { fmtForPolynomial(absValue) }}x²"
                1 -> "${if (kotlin.math.abs(absValue - 1.0) < 1e-10) "" else EquationSolver.run { fmtForPolynomial(absValue) }}x"
                else -> EquationSolver.run { fmtForPolynomial(absValue) }
            }
            parts += (if (parts.isEmpty()) { if (value < 0) "−" else "" } else if (value < 0) " − " else " + ") + term
        }
        return parts.joinToString("").ifEmpty { "0" }
    }
}

private fun EquationSolver.run(block: EquationSolver.() -> String) = block()
private fun EquationSolver.fmtForPolynomial(value: Double): String = java.text.DecimalFormat(
    "0.##########", java.text.DecimalFormatSymbols(java.util.Locale.US)
).format(value)

/** Recursive-descent parser with implicit multiplication (2x, 3(x+1), (x+1)(x-1)). */
private class Parser(source: String) {
    private val tokens = tokenize(source)
    private var position = 0

    fun parse(): Polynomial {
        val result = expression()
        require(position == tokens.size) { "No se pudo interpretar '${tokens.getOrNull(position) ?: ""}'." }
        return result
    }

    private fun expression(): Polynomial {
        var value = term()
        while (peek() == "+" || peek() == "-") {
            value = if (take() == "+") value + term() else value - term()
        }
        return value
    }

    private fun term(): Polynomial {
        var value = unary()
        while (peek() == "*" || peek() == "/") {
            val op = take()
            val next = unary()
            value = if (op == "*") value * next else value.divide(next)
        }
        return value
    }

    private fun unary(): Polynomial = when (peek()) {
        "+" -> { take(); unary() }
        "-" -> { take(); unary().scale(-1.0) }
        else -> power()
    }

    private fun power(): Polynomial {
        val base = primary()
        if (peek() != "^") return base
        take()
        val exponent = primary()
        require(exponent.c[1] == 0.0 && exponent.c[2] == 0.0 && exponent.c[0] % 1.0 == 0.0) {
            "El exponente debe ser un entero entre 0 y 2."
        }
        return base.pow(exponent.c[0].toInt())
    }

    private fun primary(): Polynomial {
        val token = take()
        return when {
            token == "x" -> Polynomial(doubleArrayOf(0.0, 1.0, 0.0))
            token == "(" -> {
                val value = expression()
                require(take() == ")") { "Falta cerrar un paréntesis." }
                value
            }
            token == ")" -> throw IllegalArgumentException("Hay un paréntesis inesperado.")
            else -> token.toDoubleOrNull()?.let { Polynomial(doubleArrayOf(it, 0.0, 0.0)) }
                ?: throw IllegalArgumentException("No se reconoce '$token'. Usa números, x y operaciones básicas.")
        }
    }

    private fun peek() = tokens.getOrNull(position)
    private fun take(): String = tokens.getOrNull(position++)
        ?: throw IllegalArgumentException("La ecuación está incompleta.")

    private fun tokenize(text: String): List<String> {
        val raw = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (ch.isWhitespace()) { i++; continue }
            if (ch.isDigit() || ch == '.') {
                val start = i++
                while (i < text.length && (text[i].isDigit() || text[i] == '.')) i++
                val number = text.substring(start, i)
                require(number.toDoubleOrNull() != null) { "Número no válido: $number." }
                raw += number
            } else if (ch.lowercaseChar() == 'x') {
                raw += "x"; i++
            } else if (ch in "+-*/^()") {
                raw += ch.toString(); i++
            } else {
                throw IllegalArgumentException("Carácter no válido: '$ch'. Usa x como incógnita.")
            }
        }
        val result = mutableListOf<String>()
        for (token in raw) {
            val previous = result.lastOrNull()
            val previousEndsValue = previous == "x" || previous == ")" || previous?.toDoubleOrNull() != null
            val currentStartsValue = token == "x" || token == "(" || token.toDoubleOrNull() != null
            if (previousEndsValue && currentStartsValue) result += "*"
            result += token
        }
        require(result.isNotEmpty()) { "La ecuación está vacía." }
        return result
    }
}
