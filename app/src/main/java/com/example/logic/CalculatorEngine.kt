package com.example.logic

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class CalculatorEngine {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat = DecimalFormat("#,##0.########", symbols).apply {
        isGroupingUsed = true
    }
    private val sciFormat = DecimalFormat("0.######E0", symbols)

    /**
     * Evaluates a mathematical expression string.
     * Returns a Pair: result String (or error message) and whether it succeeded.
     */
    fun evaluate(rawExpr: String): Pair<String, Boolean> {
        val sanitized = prepareExpression(rawExpr)
        if (sanitized.isBlank()) return "" to false

        return try {
            val tokens = tokenize(sanitized)
            val rpn = toRpn(tokens)
            val num = evalRpn(rpn)
            if (num.isNaN()) {
                "Error" to false
            } else if (num.isInfinite()) {
                "Cannot divide by 0" to false
            } else {
                formatNumber(num) to true
            }
        } catch (e: Exception) {
            "Error" to false
        }
    }

    fun formatNumber(num: Double): String {
        if (num == 0.0) return "0"
        val absVal = abs(num)
        return if (absVal >= 1e12 || (absVal < 1e-6 && absVal > 0.0)) {
            sciFormat.format(num)
        } else {
            // Remove trailing zero decimals if integer
            val rounded = (num * 1e9).roundToLong() / 1e9
            if (rounded == rounded.toLong().toDouble()) {
                DecimalFormat("#,##0", symbols).format(rounded.toLong())
            } else {
                decimalFormat.format(rounded)
            }
        }
    }

    private fun prepareExpression(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
            .trim()
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val op: Char, val precedence: Int, val rightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        object LeftParen : Token()
        object RightParen : Token()
    }

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < input.length) {
            val c = input[i]

            when {
                c.isWhitespace() -> {
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                        i++
                    }
                    val numStr = input.substring(start, i)
                    val value = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                    tokens.add(Token.Number(value))
                    expectUnary = false
                }
                c == '(' -> {
                    tokens.add(Token.LeftParen)
                    expectUnary = true
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.RightParen)
                    expectUnary = false
                    i++
                }
                c == '-' && expectUnary -> {
                    // Unary minus treated as 0 - x or negative factor
                    tokens.add(Token.Number(0.0))
                    tokens.add(Token.Operator('-', 2, false))
                    expectUnary = false
                    i++
                }
                c == '+' && expectUnary -> {
                    // Unary plus ignored
                    i++
                }
                c in "+-*/^%" -> {
                    val prec = when (c) {
                        '+', '-' -> 2
                        '*', '/', '%' -> 3
                        '^' -> 4
                        else -> 1
                    }
                    val rightAssoc = (c == '^')
                    tokens.add(Token.Operator(c, prec, rightAssoc))
                    expectUnary = true
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < input.length && input[i].isLetter()) {
                        i++
                    }
                    val funcName = input.substring(start, i).lowercase(Locale.ROOT)
                    tokens.add(Token.Function(funcName))
                    expectUnary = false
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private fun toRpn(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Function -> stack.addLast(token)
                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Operator &&
                            ((!token.rightAssociative && token.precedence <= top.precedence) ||
                                (token.rightAssociative && token.precedence < top.precedence))
                        ) {
                            output.add(stack.removeLast())
                        } else if (top is Token.Function) {
                            output.add(stack.removeLast())
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }
                is Token.LeftParen -> stack.addLast(token)
                is Token.RightParen -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LeftParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty() && stack.last() is Token.LeftParen) {
                        stack.removeLast()
                    }
                    if (stack.isNotEmpty() && stack.last() is Token.Function) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val t = stack.removeLast()
            if (t !is Token.LeftParen && t !is Token.RightParen) {
                output.add(t)
            }
        }

        return output
    }

    private fun evalRpn(tokens: List<Token>): Double {
        val stack = ArrayDeque<Double>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> stack.addLast(token.value)
                is Token.Operator -> {
                    if (stack.size < 2) throw IllegalStateException("Malformed expression")
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val res = when (token.op) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> if (b == 0.0) Double.POSITIVE_INFINITY else a / b
                        '%' -> a % b
                        '^' -> a.pow(b)
                        else -> 0.0
                    }
                    stack.addLast(res)
                }
                is Token.Function -> {
                    if (stack.isEmpty()) throw IllegalStateException("Malformed expression for function")
                    val arg = stack.removeLast()
                    val res = when (token.name) {
                        "sin" -> sin(Math.toRadians(arg))
                        "cos" -> cos(Math.toRadians(arg))
                        "tan" -> tan(Math.toRadians(arg))
                        "sqrt" -> if (arg < 0) Double.NaN else sqrt(arg)
                        "log" -> if (arg <= 0) Double.NaN else log10(arg)
                        "ln" -> if (arg <= 0) Double.NaN else ln(arg)
                        else -> arg
                    }
                    stack.addLast(res)
                }
                else -> Unit
            }
        }

        return if (stack.isNotEmpty()) stack.last() else 0.0
    }
}
