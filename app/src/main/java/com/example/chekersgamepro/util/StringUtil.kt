package com.example.chekersgamepro.util

class StringUtil {

    companion object {
        fun convertToAscii(playerName : String) : Int{
            var asciiSum = 0
            for (i in 0 until playerName.length) {
                asciiSum += playerName[i].toInt()
            }
            return asciiSum
        }
    }


}