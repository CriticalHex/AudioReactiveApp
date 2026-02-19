package com.csci448.abhattarai.reactortest.points

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class lattice(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int,
    private val frequencyBand: Int = 0
) {

    private val octads: Array<Int> = arrayOf(
        255,      13107,    21845,    26265,    39321,    43605,    52275,
        197525,   208985,   329017,   348307,   394835,   417845,   592211,
        626741,   657977,   696467,   789653,   835673,   1121809,  1171729,
        1198289,  1213469,  1320987,  1344177,  1582193,  1589783,  1970449,
        2181149,  2196689,  2239761,  2282001,  2365553,  2392343,  2626587,
        2638257,  2957841,  3146585,  3158165,  3342387,  4270257,  4293147,
        4331543,  4358513,  4475665,  4502545,  4723409,  4727069,  4932625,
        5244307,  5263417,  5570645,  6293045,  6316115,  6684825,  7899153,
        8463383,  8471153,  8524977,  8536347,  8655389,  8659409,  8882193,
        8943633,  8947473,  9439541,  9474131,  10027161, 10488467, 10526777,
        11141205, 11813905, 12586073, 12632213, 13369395, 13771281, 14749969
    )
    private val points: Array<IntArray> = arrayOf(
        intArrayOf(4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -3),
        intArrayOf(2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 0, 0, 2, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 0, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 2, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 2, 0, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 2, 2, 0, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0),
        intArrayOf(2, 0, 2, 2, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0),
        intArrayOf(2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 2, 2, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0),
        intArrayOf(2, 2, 2, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0),
        intArrayOf(2, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0),
        intArrayOf(2, 2, 2, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0),
        intArrayOf(2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0),
        intArrayOf(2, 0, 0, 2, 2, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0),
        intArrayOf(2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0),
        intArrayOf(2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0),
        intArrayOf(2, 0, 0, 0, 2, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0),
        intArrayOf(2, 2, 0, 2, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 2, 0),
        intArrayOf(2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0),
        intArrayOf(2, 0, 0, 0, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 2, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0),
        intArrayOf(2, 0, 2, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0, 2, 0),
        intArrayOf(2, 2, 0, 0, 2, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0),
        intArrayOf(2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0),
        intArrayOf(2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0),
        intArrayOf(2, 0, 2, 0, 2, 2, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0),
        intArrayOf(2, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0),
        intArrayOf(2, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 2, 2, 2, 0),
        intArrayOf(2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 2, 2, 0, 0, 2, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 2, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2),
        intArrayOf(2, 2, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2),
        intArrayOf(2, 0, 2, 2, 2, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 2, 2, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 2, 0, 0, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2),
        intArrayOf(2, 0, 2, 0, 2, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2),
        intArrayOf(2, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 2),
        intArrayOf(2, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 2, 0, 0, 2),
        intArrayOf(2, 2, 0, 0, 2, 0, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2),
        intArrayOf(2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2),
        intArrayOf(2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 2, 2, 0, 2),
        intArrayOf(2, 0, 0, 2, 2, 0, 2, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2),
        intArrayOf(2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 2, 2),
        intArrayOf(2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 2, 0, 2, 2),
        intArrayOf(2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 2, 2, 2)
    )
    val edges: Array<Pair<Int, Int>> =  arrayOf(
        Pair(0, 1),   Pair(0, 2),   Pair(0, 3),   Pair(0, 4),   Pair(0, 5),   Pair(0, 6),   Pair(0, 7),
        Pair(0, 8),   Pair(0, 9),   Pair(0, 10),  Pair(0, 11),  Pair(0, 12),  Pair(0, 13),  Pair(0, 14),
        Pair(0, 15),  Pair(0, 16),  Pair(0, 17),  Pair(0, 18),  Pair(0, 19),  Pair(0, 20),  Pair(0, 21),
        Pair(0, 22),  Pair(1, 23),  Pair(2, 23),  Pair(3, 23),  Pair(4, 23),  Pair(5, 23),  Pair(6, 23),
        Pair(1, 24),  Pair(4, 24),  Pair(7, 24),  Pair(8, 24),  Pair(11, 24), Pair(12, 24), Pair(2, 25),
        Pair(5, 25),  Pair(7, 25),  Pair(9, 25),  Pair(11, 25), Pair(13, 25), Pair(3, 26),  Pair(6, 26),
        Pair(8, 26),  Pair(9, 26),  Pair(12, 26), Pair(13, 26), Pair(3, 27),  Pair(6, 27),  Pair(7, 27),
        Pair(10, 27), Pair(11, 27), Pair(14, 27), Pair(2, 28),  Pair(5, 28),  Pair(8, 28),  Pair(10, 28),
        Pair(12, 28), Pair(14, 28), Pair(1, 29),  Pair(4, 29),  Pair(9, 29),  Pair(10, 29), Pair(13, 29),
        Pair(14, 29), Pair(2, 30),  Pair(6, 30),  Pair(7, 30),  Pair(8, 30),  Pair(15, 30), Pair(16, 30),
        Pair(29, 30), Pair(3, 31),  Pair(5, 31),  Pair(11, 31), Pair(12, 31), Pair(15, 31), Pair(16, 31),
        Pair(29, 31), Pair(3, 32),  Pair(4, 32),  Pair(7, 32),  Pair(9, 32),  Pair(15, 32), Pair(17, 32),
        Pair(28, 32), Pair(1, 33),  Pair(6, 33),  Pair(11, 33), Pair(13, 33), Pair(15, 33), Pair(17, 33),
        Pair(28, 33), Pair(1, 34),  Pair(5, 34),  Pair(8, 34),  Pair(9, 34),  Pair(16, 34), Pair(17, 34),
        Pair(27, 34), Pair(2, 35),  Pair(4, 35),  Pair(12, 35), Pair(13, 35), Pair(16, 35), Pair(17, 35),
        Pair(27, 35), Pair(1, 36),  Pair(5, 36),  Pair(7, 36),  Pair(10, 36), Pair(15, 36), Pair(18, 36),
        Pair(26, 36), Pair(35, 36), Pair(2, 37),  Pair(4, 37),  Pair(11, 37), Pair(14, 37), Pair(15, 37),
        Pair(18, 37), Pair(26, 37), Pair(34, 37), Pair(3, 38),  Pair(4, 38),  Pair(8, 38),  Pair(10, 38),
        Pair(16, 38), Pair(18, 38), Pair(25, 38), Pair(33, 38), Pair(1, 39),  Pair(6, 39),  Pair(12, 39),
        Pair(14, 39), Pair(16, 39), Pair(18, 39), Pair(25, 39), Pair(32, 39), Pair(2, 40),  Pair(6, 40),
        Pair(9, 40),  Pair(10, 40), Pair(17, 40), Pair(18, 40), Pair(24, 40), Pair(31, 40), Pair(3, 41),
        Pair(5, 41),  Pair(13, 41), Pair(14, 41), Pair(17, 41), Pair(18, 41), Pair(24, 41), Pair(30, 41),
        Pair(8, 42),  Pair(9, 42),  Pair(10, 42), Pair(11, 42), Pair(15, 42), Pair(19, 42), Pair(23, 42),
        Pair(35, 42), Pair(39, 42), Pair(41, 42), Pair(7, 43),  Pair(12, 43), Pair(13, 43), Pair(14, 43),
        Pair(15, 43), Pair(19, 43), Pair(23, 43), Pair(34, 43), Pair(38, 43), Pair(40, 43), Pair(5, 44),
        Pair(6, 44),  Pair(10, 44), Pair(13, 44), Pair(16, 44), Pair(19, 44), Pair(24, 44), Pair(32, 44),
        Pair(37, 44), Pair(2, 45),  Pair(3, 45),  Pair(9, 45),  Pair(14, 45), Pair(16, 45), Pair(19, 45),
        Pair(24, 45), Pair(33, 45), Pair(36, 45), Pair(1, 46),  Pair(3, 46),  Pair(10, 46), Pair(12, 46),
        Pair(17, 46), Pair(19, 46), Pair(25, 46), Pair(30, 46), Pair(37, 46), Pair(4, 47),  Pair(6, 47),
        Pair(8, 47),  Pair(14, 47), Pair(17, 47), Pair(19, 47), Pair(25, 47), Pair(31, 47), Pair(36, 47),
        Pair(4, 48),  Pair(5, 48),  Pair(9, 48),  Pair(12, 48), Pair(18, 48), Pair(19, 48), Pair(27, 48),
        Pair(30, 48), Pair(33, 48), Pair(1, 49),  Pair(2, 49),  Pair(8, 49),  Pair(13, 49), Pair(18, 49),
        Pair(19, 49), Pair(27, 49), Pair(31, 49), Pair(32, 49), Pair(7, 50),  Pair(11, 50), Pair(16, 50),
        Pair(17, 50), Pair(18, 50), Pair(19, 50), Pair(23, 50), Pair(26, 50), Pair(28, 50), Pair(29, 50),
        Pair(2, 51),  Pair(3, 51),  Pair(10, 51), Pair(13, 51), Pair(15, 51), Pair(20, 51), Pair(24, 51),
        Pair(34, 51), Pair(39, 51), Pair(47, 51), Pair(48, 51), Pair(50, 51), Pair(5, 52),  Pair(6, 52),
        Pair(9, 52),  Pair(14, 52), Pair(15, 52), Pair(20, 52), Pair(24, 52), Pair(35, 52), Pair(38, 52),
        Pair(46, 52), Pair(49, 52), Pair(50, 52), Pair(7, 53),  Pair(9, 53),  Pair(10, 53), Pair(12, 53),
        Pair(16, 53), Pair(20, 53), Pair(23, 53), Pair(33, 53), Pair(37, 53), Pair(41, 53), Pair(47, 53),
        Pair(49, 53), Pair(8, 54),  Pair(11, 54), Pair(13, 54), Pair(14, 54), Pair(16, 54), Pair(20, 54),
        Pair(23, 54), Pair(32, 54), Pair(36, 54), Pair(40, 54), Pair(46, 54), Pair(48, 54), Pair(4, 55),
        Pair(5, 55),  Pair(10, 55), Pair(11, 55), Pair(17, 55), Pair(20, 55), Pair(26, 55), Pair(30, 55),
        Pair(39, 55), Pair(43, 55), Pair(45, 55), Pair(49, 55), Pair(1, 56),  Pair(2, 56),  Pair(7, 56),
        Pair(14, 56), Pair(17, 56), Pair(20, 56), Pair(26, 56), Pair(31, 56), Pair(38, 56), Pair(42, 56),
        Pair(44, 56), Pair(48, 56), Pair(1, 57),  Pair(3, 57),  Pair(9, 57),  Pair(11, 57), Pair(18, 57),
        Pair(20, 57), Pair(28, 57), Pair(30, 57), Pair(35, 57), Pair(43, 57), Pair(44, 57), Pair(47, 57),
        Pair(4, 58),  Pair(6, 58),  Pair(7, 58),  Pair(13, 58), Pair(18, 58), Pair(20, 58), Pair(28, 58),
        Pair(31, 58), Pair(34, 58), Pair(42, 58), Pair(45, 58), Pair(46, 58), Pair(8, 59),  Pair(12, 59),
        Pair(15, 59), Pair(17, 59), Pair(18, 59), Pair(20, 59), Pair(23, 59), Pair(25, 59), Pair(27, 59),
        Pair(29, 59), Pair(44, 59), Pair(45, 59), Pair(3, 60),  Pair(5, 60),  Pair(7, 60),  Pair(8, 60),
        Pair(19, 60), Pair(20, 60), Pair(29, 60), Pair(33, 60), Pair(35, 60), Pair(37, 60), Pair(39, 60),
        Pair(40, 60), Pair(2, 61),  Pair(6, 61),  Pair(11, 61), Pair(12, 61), Pair(19, 61), Pair(20, 61),
        Pair(29, 61), Pair(32, 61), Pair(34, 61), Pair(36, 61), Pair(38, 61), Pair(41, 61), Pair(1, 62),
        Pair(4, 62),  Pair(15, 62), Pair(16, 62), Pair(19, 62), Pair(20, 62), Pair(25, 62), Pair(26, 62),
        Pair(27, 62), Pair(28, 62), Pair(40, 62), Pair(41, 62), Pair(4, 63),  Pair(6, 63),  Pair(10, 63),
        Pair(12, 63), Pair(15, 63), Pair(21, 63), Pair(25, 63), Pair(34, 63), Pair(41, 63), Pair(45, 63),
        Pair(49, 63), Pair(50, 63), Pair(54, 63), Pair(56, 63), Pair(57, 63), Pair(60, 63), Pair(1, 64),
        Pair(3, 64),  Pair(8, 64),  Pair(14, 64), Pair(15, 64), Pair(21, 64), Pair(25, 64), Pair(35, 64),
        Pair(40, 64), Pair(44, 64), Pair(48, 64), Pair(50, 64), Pair(53, 64), Pair(55, 64), Pair(58, 64),
        Pair(61, 64), Pair(1, 65),  Pair(2, 65),  Pair(10, 65), Pair(11, 65), Pair(16, 65), Pair(21, 65),
        Pair(26, 65), Pair(32, 65), Pair(41, 65), Pair(43, 65), Pair(47, 65), Pair(48, 65), Pair(52, 65),
        Pair(58, 65), Pair(59, 65), Pair(60, 65), Pair(4, 66),  Pair(5, 66),  Pair(7, 66),  Pair(14, 66),
        Pair(16, 66), Pair(21, 66), Pair(26, 66), Pair(33, 66), Pair(40, 66), Pair(42, 66), Pair(46, 66),
        Pair(49, 66), Pair(51, 66), Pair(57, 66), Pair(59, 66), Pair(61, 66), Pair(7, 67),  Pair(8, 67),
        Pair(10, 67), Pair(13, 67), Pair(17, 67), Pair(21, 67), Pair(23, 67), Pair(31, 67), Pair(37, 67),
        Pair(39, 67), Pair(45, 67), Pair(48, 67), Pair(52, 67), Pair(57, 67), Pair(61, 67), Pair(62, 67),
        Pair(9, 68),  Pair(11, 68), Pair(12, 68), Pair(14, 68), Pair(17, 68), Pair(21, 68), Pair(23, 68),
        Pair(30, 68), Pair(36, 68), Pair(38, 68), Pair(44, 68), Pair(49, 68), Pair(51, 68), Pair(58, 68),
        Pair(60, 68), Pair(62, 68), Pair(5, 69),  Pair(6, 69),  Pair(8, 69),  Pair(11, 69), Pair(18, 69),
        Pair(21, 69), Pair(29, 69), Pair(32, 69), Pair(35, 69), Pair(43, 69), Pair(45, 69), Pair(46, 69),
        Pair(51, 69), Pair(53, 69), Pair(56, 69), Pair(62, 69), Pair(2, 70),  Pair(3, 70),  Pair(7, 70),
        Pair(12, 70), Pair(18, 70), Pair(21, 70), Pair(29, 70), Pair(33, 70), Pair(34, 70), Pair(42, 70),
        Pair(44, 70), Pair(47, 70), Pair(52, 70), Pair(54, 70), Pair(55, 70), Pair(62, 70), Pair(9, 71),
        Pair(13, 71), Pair(15, 71), Pair(16, 71), Pair(18, 71), Pair(21, 71), Pair(23, 71), Pair(24, 71),
        Pair(27, 71), Pair(28, 71), Pair(46, 71), Pair(47, 71), Pair(55, 71), Pair(56, 71), Pair(60, 71),
        Pair(61, 71), Pair(1, 72),  Pair(6, 72),  Pair(7, 72),  Pair(9, 72),  Pair(19, 72), Pair(21, 72),
        Pair(28, 72), Pair(31, 72), Pair(35, 72), Pair(37, 72), Pair(38, 72), Pair(41, 72), Pair(51, 72),
        Pair(54, 72), Pair(55, 72), Pair(59, 72), Pair(3, 73),  Pair(4, 73),  Pair(11, 73), Pair(13, 73),
        Pair(19, 73), Pair(21, 73), Pair(28, 73), Pair(30, 73), Pair(34, 73), Pair(36, 73), Pair(39, 73),
        Pair(40, 73), Pair(52, 73), Pair(53, 73), Pair(56, 73), Pair(59, 73), Pair(2, 74),  Pair(5, 74),
        Pair(15, 74), Pair(17, 74), Pair(19, 74), Pair(21, 74), Pair(24, 74), Pair(26, 74), Pair(27, 74),
        Pair(29, 74), Pair(38, 74), Pair(39, 74), Pair(53, 74), Pair(54, 74), Pair(57, 74), Pair(58, 74),
        Pair(2, 75),  Pair(4, 75),  Pair(8, 75),  Pair(9, 75),  Pair(20, 75), Pair(21, 75), Pair(27, 75),
        Pair(31, 75), Pair(33, 75), Pair(36, 75), Pair(39, 75), Pair(41, 75), Pair(43, 75), Pair(44, 75),
        Pair(46, 75), Pair(50, 75), Pair(1, 76),  Pair(5, 76),  Pair(12, 76), Pair(13, 76), Pair(20, 76),
        Pair(21, 76), Pair(27, 76), Pair(30, 76), Pair(32, 76), Pair(37, 76), Pair(38, 76), Pair(40, 76),
        Pair(42, 76), Pair(45, 76), Pair(47, 76), Pair(50, 76), Pair(3, 77),  Pair(6, 77),  Pair(16, 77),
        Pair(17, 77), Pair(20, 77), Pair(21, 77), Pair(24, 77), Pair(25, 77), Pair(28, 77), Pair(29, 77),
        Pair(36, 77), Pair(37, 77), Pair(42, 77), Pair(43, 77), Pair(48, 77), Pair(49, 77), Pair(10, 78),
        Pair(14, 78), Pair(18, 78), Pair(19, 78), Pair(20, 78), Pair(21, 78), Pair(23, 78), Pair(24, 78),
        Pair(25, 78), Pair(26, 78), Pair(30, 78), Pair(31, 78), Pair(32, 78), Pair(33, 78), Pair(34, 78),
        Pair(35, 78), Pair(1, 79),  Pair(2, 79),  Pair(9, 79),  Pair(12, 79), Pair(15, 79), Pair(22, 79),
        Pair(27, 79), Pair(38, 79), Pair(41, 79), Pair(44, 79), Pair(47, 79), Pair(50, 79), Pair(54, 79),
        Pair(55, 79), Pair(58, 79), Pair(60, 79), Pair(66, 79), Pair(67, 79), Pair(69, 79), Pair(73, 79),
        Pair(77, 79), Pair(78, 79), Pair(4, 80),  Pair(5, 80),  Pair(8, 80),  Pair(13, 80), Pair(15, 80),
        Pair(22, 80), Pair(27, 80), Pair(39, 80), Pair(40, 80), Pair(45, 80), Pair(46, 80), Pair(50, 80),
        Pair(53, 80), Pair(56, 80), Pair(57, 80), Pair(61, 80), Pair(65, 80), Pair(68, 80), Pair(70, 80),
        Pair(72, 80), Pair(77, 80), Pair(78, 80), Pair(4, 81),  Pair(6, 81),  Pair(9, 81),  Pair(11, 81),
        Pair(16, 81), Pair(22, 81), Pair(28, 81), Pair(36, 81), Pair(41, 81), Pair(43, 81), Pair(46, 81),
        Pair(49, 81), Pair(51, 81), Pair(56, 81), Pair(59, 81), Pair(60, 81), Pair(64, 81), Pair(67, 81),
        Pair(70, 81), Pair(74, 81), Pair(76, 81), Pair(78, 81), Pair(1, 82),  Pair(3, 82),  Pair(7, 82),
        Pair(13, 82), Pair(16, 82), Pair(22, 82), Pair(28, 82), Pair(37, 82), Pair(40, 82), Pair(42, 82),
        Pair(47, 82), Pair(48, 82), Pair(52, 82), Pair(55, 82), Pair(59, 82), Pair(61, 82), Pair(63, 82),
        Pair(68, 82), Pair(69, 82), Pair(74, 82), Pair(75, 82), Pair(78, 82), Pair(2, 83),  Pair(3, 83),
        Pair(8, 83),  Pair(11, 83), Pair(17, 83), Pair(22, 83), Pair(29, 83), Pair(36, 83), Pair(39, 83),
        Pair(43, 83), Pair(44, 83), Pair(48, 83), Pair(52, 83), Pair(53, 83), Pair(58, 83), Pair(62, 83),
        Pair(63, 83), Pair(66, 83), Pair(71, 83), Pair(72, 83), Pair(76, 83), Pair(78, 83), Pair(5, 84),
        Pair(6, 84),  Pair(7, 84),  Pair(12, 84), Pair(17, 84), Pair(22, 84), Pair(29, 84), Pair(37, 84),
        Pair(38, 84), Pair(42, 84), Pair(45, 84), Pair(49, 84), Pair(51, 84), Pair(54, 84), Pair(57, 84),
        Pair(62, 84), Pair(64, 84), Pair(65, 84), Pair(71, 84), Pair(73, 84), Pair(75, 84), Pair(78, 84),
        Pair(10, 85), Pair(14, 85), Pair(15, 85), Pair(16, 85), Pair(17, 85), Pair(22, 85), Pair(23, 85),
        Pair(24, 85), Pair(25, 85), Pair(26, 85), Pair(48, 85), Pair(49, 85), Pair(57, 85), Pair(58, 85),
        Pair(60, 85), Pair(61, 85), Pair(69, 85), Pair(70, 85), Pair(72, 85), Pair(73, 85), Pair(75, 85),
        Pair(76, 85), Pair(10, 86), Pair(11, 86), Pair(12, 86), Pair(13, 86), Pair(18, 86), Pair(22, 86),
        Pair(23, 86), Pair(30, 86), Pair(32, 86), Pair(34, 86), Pair(45, 86), Pair(47, 86), Pair(52, 86),
        Pair(56, 86), Pair(60, 86), Pair(62, 86), Pair(64, 86), Pair(66, 86), Pair(72, 86), Pair(74, 86),
        Pair(75, 86), Pair(77, 86), Pair(7, 87),  Pair(8, 87),  Pair(9, 87),  Pair(14, 87), Pair(18, 87),
        Pair(22, 87), Pair(23, 87), Pair(31, 87), Pair(33, 87), Pair(35, 87), Pair(44, 87), Pair(46, 87),
        Pair(51, 87), Pair(55, 87), Pair(61, 87), Pair(62, 87), Pair(63, 87), Pair(65, 87), Pair(73, 87),
        Pair(74, 87), Pair(76, 87), Pair(77, 87), Pair(2, 88),  Pair(4, 88),  Pair(7, 88),  Pair(10, 88),
        Pair(19, 88), Pair(22, 88), Pair(26, 88), Pair(31, 88), Pair(33, 88), Pair(34, 88), Pair(39, 88),
        Pair(41, 88), Pair(52, 88), Pair(54, 88), Pair(57, 88), Pair(59, 88), Pair(64, 88), Pair(68, 88),
        Pair(69, 88), Pair(71, 88), Pair(76, 88), Pair(77, 88), Pair(1, 89),  Pair(5, 89),  Pair(11, 89),
        Pair(14, 89), Pair(19, 89), Pair(22, 89), Pair(26, 89), Pair(30, 89), Pair(32, 89), Pair(35, 89),
        Pair(38, 89), Pair(40, 89), Pair(51, 89), Pair(53, 89), Pair(58, 89), Pair(59, 89), Pair(63, 89),
        Pair(67, 89), Pair(70, 89), Pair(71, 89), Pair(75, 89), Pair(77, 89), Pair(3, 90),  Pair(6, 90),
        Pair(15, 90), Pair(18, 90), Pair(19, 90), Pair(22, 90), Pair(24, 90), Pair(25, 90), Pair(28, 90),
        Pair(29, 90), Pair(34, 90), Pair(35, 90), Pair(53, 90), Pair(54, 90), Pair(55, 90), Pair(56, 90),
        Pair(65, 90), Pair(66, 90), Pair(67, 90), Pair(68, 90), Pair(75, 90), Pair(76, 90), Pair(1, 91),
        Pair(6, 91),  Pair(8, 91),  Pair(10, 91), Pair(20, 91), Pair(22, 91), Pair(25, 91), Pair(31, 91),
        Pair(32, 91), Pair(35, 91), Pair(37, 91), Pair(41, 91), Pair(43, 91), Pair(45, 91), Pair(48, 91),
        Pair(50, 91), Pair(66, 91), Pair(68, 91), Pair(70, 91), Pair(71, 91), Pair(73, 91), Pair(74, 91),
        Pair(3, 92),  Pair(4, 92),  Pair(12, 92), Pair(14, 92), Pair(20, 92), Pair(22, 92), Pair(25, 92),
        Pair(30, 92), Pair(33, 92), Pair(34, 92), Pair(36, 92), Pair(40, 92), Pair(42, 92), Pair(44, 92),
        Pair(49, 92), Pair(50, 92), Pair(65, 92), Pair(67, 92), Pair(69, 92), Pair(71, 92), Pair(72, 92),
        Pair(74, 92), Pair(2, 93),  Pair(5, 93),  Pair(16, 93), Pair(18, 93), Pair(20, 93), Pair(22, 93),
        Pair(24, 93), Pair(26, 93), Pair(27, 93), Pair(29, 93), Pair(32, 93), Pair(33, 93), Pair(42, 93),
        Pair(43, 93), Pair(46, 93), Pair(47, 93), Pair(63, 93), Pair(64, 93), Pair(67, 93), Pair(68, 93),
        Pair(72, 93), Pair(73, 93), Pair(9, 94),  Pair(13, 94), Pair(17, 94), Pair(19, 94), Pair(20, 94),
        Pair(22, 94), Pair(23, 94), Pair(24, 94), Pair(27, 94), Pair(28, 94), Pair(30, 94), Pair(31, 94),
        Pair(36, 94), Pair(37, 94), Pair(38, 94), Pair(39, 94), Pair(63, 94), Pair(64, 94), Pair(65, 94),
        Pair(66, 94), Pair(69, 94), Pair(70, 94), Pair(3, 95),  Pair(5, 95),  Pair(9, 95),  Pair(10, 95),
        Pair(21, 95), Pair(22, 95), Pair(24, 95), Pair(30, 95), Pair(33, 95), Pair(35, 95), Pair(37, 95),
        Pair(39, 95), Pair(43, 95), Pair(47, 95), Pair(49, 95), Pair(50, 95), Pair(54, 95), Pair(56, 95),
        Pair(58, 95), Pair(59, 95), Pair(61, 95), Pair(62, 95), Pair(2, 96),  Pair(6, 96),  Pair(13, 96),
        Pair(14, 96), Pair(21, 96), Pair(22, 96), Pair(24, 96), Pair(31, 96), Pair(32, 96), Pair(34, 96),
        Pair(36, 96), Pair(38, 96), Pair(42, 96), Pair(46, 96), Pair(48, 96), Pair(50, 96), Pair(53, 96),
        Pair(55, 96), Pair(57, 96), Pair(59, 96), Pair(60, 96), Pair(62, 96), Pair(1, 97),  Pair(4, 97),
        Pair(17, 97), Pair(18, 97), Pair(21, 97), Pair(22, 97), Pair(25, 97), Pair(26, 97), Pair(27, 97),
        Pair(28, 97), Pair(30, 97), Pair(31, 97), Pair(42, 97), Pair(43, 97), Pair(44, 97), Pair(45, 97),
        Pair(51, 97), Pair(52, 97), Pair(53, 97), Pair(54, 97), Pair(60, 97), Pair(61, 97), Pair(8, 98),
        Pair(12, 98), Pair(16, 98), Pair(19, 98), Pair(21, 98), Pair(22, 98), Pair(23, 98), Pair(25, 98),
        Pair(27, 98), Pair(29, 98), Pair(32, 98), Pair(33, 98), Pair(36, 98), Pair(37, 98), Pair(40, 98),
        Pair(41, 98), Pair(51, 98), Pair(52, 98), Pair(55, 98), Pair(56, 98), Pair(57, 98), Pair(58, 98),
        Pair(7, 99),  Pair(11, 99), Pair(15, 99), Pair(20, 99), Pair(21, 99), Pair(22, 99), Pair(23, 99),
        Pair(26, 99), Pair(28, 99), Pair(29, 99), Pair(34, 99), Pair(35, 99), Pair(38, 99), Pair(39, 99),
        Pair(40, 99), Pair(41, 99), Pair(44, 99), Pair(45, 99), Pair(46, 99), Pair(47, 99), Pair(48, 99),
        Pair(49, 99))

    private var color: Color = Color(220 / 255f, 208 / 255f, 255 / 255f, 100 / 255f)



    private val elevenCycle: Array<IntArray> = arrayOf(
        intArrayOf(0, 0),  intArrayOf(1, 0),  intArrayOf(1, 1),  intArrayOf(1, 8),
        intArrayOf(0, 0),  intArrayOf(2, 0),  intArrayOf(2, 3),  intArrayOf(2, 10),
        intArrayOf(2, 8),  intArrayOf(1, 2),  intArrayOf(1, 9),  intArrayOf(1, 6),
        intArrayOf(1, 3),  intArrayOf(2, 9),  intArrayOf(2, 5),  intArrayOf(2, 2),
        intArrayOf(2, 7),  intArrayOf(1, 10), intArrayOf(1, 5),  intArrayOf(1, 7),
        intArrayOf(1, 4),  intArrayOf(2, 1),  intArrayOf(2, 4),  intArrayOf(2, 6)
    )


    private val _normalizer = sqrt(11.0)

    private val _projectedVectors: Array<DoubleArray> = Array(2) {DoubleArray(24)}

    private val _projectedPoints: Array<Offset> = Array(100) {Offset.Zero}

    private var position: Offset = Offset(x.toFloat(), y.toFloat())

    fun getColor(): Color = color
    fun getProjectedPoints(): Array<Offset> = _projectedPoints

    private fun computeProjectedVectors(time: Double) {
        for (i in 0 until 24) {
            when (elevenCycle[i][0]) {
                0 -> {
                    _projectedVectors[0][i] = 0.0
                    _projectedVectors[1][i] = 0.0
                }
                1 -> {
                    val phase = elevenCycle[i][1].toDouble() / 11.0 + time
                    val angle = 2.0 * PI * phase
                    _projectedVectors[0][i] = cos(angle) / _normalizer
                    _projectedVectors[1][i] = -sin(angle) / _normalizer
                }
                2 -> {
                    val phase = elevenCycle[i][1].toDouble() / 11.0 - time
                    val angle = 2.0 * PI * phase
                    _projectedVectors[0][i] = cos(angle) / _normalizer
                    _projectedVectors[1][i] = -sin(angle) / _normalizer
                }
                else -> {
                    _projectedVectors[0][i] = 0.0
                    _projectedVectors[1][i] = 0.0
                }
            }
        }
    }

    private fun computeProjectedPoints(volume: Double) {
        val base = height.toFloat() / 5f
        val scale = base * (1f + volume.toFloat().coerceIn(0f, 2f))

        for (i in 0 until 100) {
            var u = 0.0
            var v = 0.0

            for (j in 0 until 24) {
                val p = points[i][j].toDouble()
                u += p * _projectedVectors[0][j]
                v += p * _projectedVectors[1][j]
            }

            _projectedPoints[i] = Offset(
                x = position.x + scale * v.toFloat(),
                y = position.y + scale * u.toFloat()
            )
        }
    }

    fun DrawScope.drawLatticeLines(l: lattice, maxLines: Int = 1100, strokeWidth: Float = 1f) {
        val pts = l.getProjectedPoints()
        val c = l.getColor()
        val n = minOf(l.edges.size, maxLines)

        for (k in 0 until n) {
            val (i, j) = l.edges[k]
            drawLine(
                color = c,
                start = pts[i],
                end = pts[j],
                strokeWidth = strokeWidth
            )
        }
    }




//    fun update(time: Double) {
//        computeProjectedVectors(time)
//        computeProjectedPoints()
//        color = computeColor(time)
//    }

    fun update(time: Double, volume: Double = 0.0) {
        computeProjectedVectors(time)
        computeProjectedPoints(volume)
        color = computeColor(time)
    }

    fun setPosition(p: Offset) { position = p }




    fun update(time: Double, volumeBands: List<Float>) {
        computeProjectedVectors(time)
        val v = volumeBands.getOrNull(frequencyBand) ?: 0f
        computeProjectedPoints(smooth(v))
        color = computeColor(time)
    }


}


