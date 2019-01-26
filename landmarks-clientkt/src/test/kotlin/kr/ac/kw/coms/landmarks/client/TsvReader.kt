package kr.ac.kw.coms.landmarks.client

import java.io.Reader

/**
 * Portable tsv reader supporting multiline
 * Usage: File("a.tsv").bufferedReader().use { TsvReader(it).readAll() }
 */
class TsvReader(private val reader: Reader) {
  private val sb = StringBuilder()
  private val eof = (-1).toChar()

  private var character = reader.read().toChar()
  private fun peek(): Char = character
  private fun read(): Char {
    val ch = character
    character = reader.read().toChar()
    return ch
  }

  fun readAll(): List<List<String>> {
    val rows = mutableListOf<MutableList<String>>()
    while (true) {
      val row = mutableListOf<String>()
      cells@ while (true) {
        val ch = read()
        when (ch) {
          '\t' -> Unit
          '"' -> row.add(readEscapedWithInvalids())
          '\r', '\n' -> {
            if (ch == '\r' && peek() == '\n') {
              read()
            }
            rows.add(row)
            break@cells
          }
          eof -> {
            if (row.isNotEmpty()) {
              rows.add(row)
            }
            return rows
          }
          else -> {
            sb.append(ch)
            row.add(readUntilTab())
          }
        }
      }
    }
  }

  private fun readEscapedWithInvalids(): String {
    val valid = readEscaped()
    val ch = peek()
    return when (ch) {
      '\t', '\r', '\n', eof -> valid
      else -> valid + readUntilTab()
    }
  }

  private fun readEscaped(): String {
    while (true) {
      val ch = read()
      when (ch) {
        eof -> return resetBuilder()
        '"' -> when (peek()) {
          '"' -> sb.append('"')
          else -> return resetBuilder()
        }
        else -> sb.append(ch)
      }
    }
  }

  private fun readUntilTab(): String {
    while (true) {
      when (peek()) {
        '\t', '\r', '\n', eof -> return resetBuilder()
        else -> sb.append(read())
      }
    }
  }

  private fun resetBuilder(): String {
    val s = sb.toString()
    sb.setLength(0)
    return s
  }
}
