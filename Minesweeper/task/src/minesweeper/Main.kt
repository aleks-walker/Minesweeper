package minesweeper

import kotlin.random.Random

fun main() {
    val minesweeper = Minesweeper()
    minesweeper.play()
}

class Minesweeper {

    fun play() {
        val board = Board()
        println("How many mines do you want on the field?")
        val numberOfMines = readLine()!!.toInt()
        board.fillRandom(numberOfMines)
//        board.calcMines()
        board.printBoard()
        do {
            board.handleMarkCell()
            board.printBoard()
            val isWin = board.isWin()
            if (isWin) println("Congratulations! You found all the mines!")
        } while (!isWin && !board.isDead())

    }

}

class Board {
    companion object {
        const val MINE = 'X'
        const val EMPTY_CELL = '.'
        const val MARK = '*'
        const val EXPLORED = '/'
        const val ACTION_FREE = "free"
        const val ACTION_MINE = "mine"
    }

    object Field {
        const val columns: Int = 9
        const val rows: Int = 9
        val field: Array<CharArray> = Array(columns) { CharArray(rows) { EMPTY_CELL } }

        fun getCell(c: Coordinates) = field[c.x][c.y]
        fun setCell(c: Coordinates, value: Char) {
            field[c.x][c.y] = value
        }
    }

    private var isDead = false
    private val minesCoordinates = mutableSetOf<Coordinates>()
    private val marksCoordinates = mutableSetOf<Coordinates>()

    fun fillRandom(numberOfMines: Int) {
        repeat(numberOfMines) { plantMineRandomly() }
    }

    private fun plantMineRandomly() {
        var coordinates = Coordinates(Random.nextInt(9), Random.nextInt(9))
        while (true) {
            if (isMined(coordinates)) {
                coordinates = Coordinates(Random.nextInt(9), Random.nextInt(9))
            } else {
                Field.setCell(coordinates, MINE)
                minesCoordinates.add(coordinates)
                break
            }
        }
        println(marksCoordinates.joinToString())
    }

    private fun isMined(coordinates: Coordinates): Boolean {
        return minesCoordinates.contains(coordinates)
    }

    fun printBoard(hideMines: Boolean = true) {
        println(" │${('1'..'9').joinToString(separator = "")}│")
        println("—│—————————│")
        for ((i, element) in Field.field.withIndex()) {
            var line = "${i + 1}│" + element.joinToString(separator = "") + "│"
            if (hideMines) line = line.replace(MINE, EMPTY_CELL)
            println(line)
        }
        println("—│—————————│")
    }

    fun calcMines() {
        for (mineCoordinate in minesCoordinates) {
            markAround(mineCoordinate)
        }
    }

    private fun markAround(c: Coordinates) {
        val neighbors = getNeighbors(c)
        for (neighbor in neighbors) {
            increaseCounter(neighbor)
        }
    }

    private fun minesAround(c: Coordinates): Int {
        return getNeighbors(c).filter { minesCoordinates.contains(it) }.count()
    }

    private fun increaseCounter(c: Coordinates) {
        val cellValue = Field.getCell(c)
        if (cellValue != MINE) {
            if (cellValue == EMPTY_CELL) {
                Field.setCell(c, '1')
            } else {
                setMinesCount(c, cellValue.digitValue().plus(1))
            }
        }
    }

    private fun isSafe(c: Coordinates): Boolean {
        return (c.x >= 0 && c.x < Field.field.size) && (c.y >= 0 && c.y < Field.field.size)
    }

    fun handleMarkCell() {
        println("Set/unset mine marks or claim a cell as free:")
        val (y, x, action) = readLine()!!.split(" ")
        val c = Coordinates(x.toInt() - 1, y.toInt() - 1)
        if (isMined(c) && action == ACTION_FREE) {
            println("You stepped on a mine and failed!")
            isDead = true
        } else {
            resolveAction(action, c)
        }
    }

    private fun resolveAction(action: String, c: Coordinates) {
        if (action == ACTION_FREE) {
            exploreCells(c)
        } else if (action == ACTION_MINE) {
            markCell(c)
        }
    }

    private fun exploreCells(cell: Coordinates) {
        val minesAround = minesAround(cell)
        if (!isMined(cell) && !isExplored(cell)) {
            if (minesAround > 0) {
                setMinesCount(cell, minesAround)
                return
            }
            Field.setCell(cell, EXPLORED)
        } else return

        val neighbors = getNeighbors(cell)
        for (neighbor in neighbors) {
            exploreCells(neighbor)
        }

    }

    private fun isExplored(coordinates: Coordinates): Boolean {
        return Field.getCell(coordinates) == EXPLORED
    }

    private fun setMinesCount(cell: Coordinates, minesAround: Int) {
        Field.setCell(cell, minesAround.toString()[0])
    }

    private fun getNeighbors(c: Coordinates): Set<Coordinates> {
        val neighbors = mutableSetOf<Coordinates>()
        for (x in -1..1) {
            for (y in -1..1) {
                val coordinates = Coordinates(c.x + x, c.y + y)
                if (isSafe(coordinates) && coordinates != c) {
                    neighbors.add(coordinates)
                }
            }
        }
        return neighbors
    }

    private fun isNumber(c: Coordinates): Boolean {
        return Field.getCell(c).isDigit()
    }

    private fun markCell(c: Coordinates) {
        if (isMarked(c)) {
            removeMark(c)
        } else setMark(c)
    }

    private fun isMarked(c: Coordinates): Boolean = Field.getCell(c) == MARK
    private fun removeMark(c: Coordinates) {
        Field.setCell(c, EMPTY_CELL)
        marksCoordinates.remove(c)
    }

    private fun setMark(c: Coordinates) {
        Field.setCell(c, MARK)
        marksCoordinates.add(c)
    }

    fun isDead(): Boolean = isDead

    fun isWin(): Boolean = !isDead && onlyMinesLeft() || allMinesMarked()

    private fun allMinesMarked(): Boolean = minesCoordinates == marksCoordinates

    private fun onlyMinesLeft(): Boolean = getUnexploredCells().isEmpty()

    private fun getUnexploredCells(): List<Coordinates> {
        val cells = mutableListOf<Coordinates>()
        Field.field.forEachIndexed { x, row ->
            row.forEachIndexed { y, column ->
                if (column == EMPTY_CELL) cells.add(Coordinates(x, y))
            }
        }
        return cells
    }
}

data class Coordinates(val x: Int, val y: Int)


fun Char.digitValue(): Int = Character.getNumericValue(this)
