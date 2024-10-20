/**
 * Implementation and examples According to https://en.wikipedia.org/wiki/Brainfuck
 * @author Nils Osswald<contact@nilsosswald.de>
 */

import java.nio.file.Files
import java.nio.file.Path

/** Increment the data pointer by one */
const val INC_POINTER = '>'
/** Decrement the data pointer by one */
const val DEC_POINTER = '<'
/** Increment the byte at the data pointer by one */
const val INC_MEMORY = '+'
/** Decrement the byte at the data pointer by one */
const val DEC_MEMORY = '-'
/** Output the byte at the data pointer */
const val PRINT = '.'
/** Accept one byte of input, storing its value in the byte at the data pointer */
const val READ = ','
/** If the byte at the data pointer is zero, then instead of moving the instruction pointer
 * forward to the next command, jump it forward to the command after the matching ] command */
const val JUMP_IF_ZERO = '['
/** If the byte at the data pointer is nonzero, then instead of moving the instruction pointer
 * forward to the next command, jump it back to the command after the matching [ command. */
const val JUMP_IF_NOT_ZERO = ']'

val instructionChars = arrayOf(
  INC_POINTER,
  DEC_POINTER,
  INC_MEMORY,
  DEC_MEMORY,
  PRINT,
  READ,
  JUMP_IF_ZERO,
  JUMP_IF_NOT_ZERO
)

const val MEM_SIZE = 100 // bytes

fun main(args: Array<String>) {
  val instructions = Path.of(args.first())
    .let(Files::readAllLines)
    .joinToString(separator = "")
    .filter(instructionChars::contains)
  val memory = ByteArray(MEM_SIZE) { 0 }
  // Stores each closing bracket associated to its open bracket
  val bracketsCache = mutableMapOf<Int, Int>()

  var instructionPointer = 0
  var dataPointer = 0

  while (instructionPointer < instructions.length) {
    when (val instruction = instructions[instructionPointer]) {
      INC_POINTER -> dataPointer++
      DEC_POINTER -> dataPointer--
      INC_MEMORY -> memory[dataPointer]++
      DEC_MEMORY -> memory[dataPointer]--
      PRINT -> print(memory[dataPointer].toInt().toChar())
      READ -> memory[dataPointer] = System.`in`.read().toByte()
      JUMP_IF_ZERO -> {
        val closingBracketIndex = bracketsCache.getOrPut(instructionPointer) {
          findClosingBracket(instructionPointer, instructions)
        }
        if (memory[dataPointer].toInt() == 0) {
          instructionPointer = closingBracketIndex
        }
      }
      JUMP_IF_NOT_ZERO -> {
        if (memory[dataPointer].toInt() != 0) {
          instructionPointer = bracketsCache.keyOf(instructionPointer)
            ?: throw IllegalStateException(
              "Encountered closing bracket of loop at $instructionPointer before opening bracket")
        }
      }
      else -> throw IllegalStateException("Invalid instruction char: $instruction")
    }
    instructionPointer++
  }
}

private fun findClosingBracket(openingBracketIndex: Int, instructions: String): Int {
  var delta = 1
  var index = openingBracketIndex

  while (delta != 0) {
    when (instructions.getOrNull(++index)) {
      JUMP_IF_ZERO -> delta++
      JUMP_IF_NOT_ZERO -> delta--
      null -> throw IllegalStateException(
        "Could not find a closing bracket for opening bracket at index $openingBracketIndex")
    }
  }

  return index
}

private fun <K, V> Map<K, V>.keyOf(value: V): K? =
  this.entries.firstOrNull { it.value == value }?.key