package com.ahr.stock.data.mapper

interface Mapper<in From, out To> {
    fun map(from: From): To
    fun mapList(from: List<From>): List<To> = from.map { map(it) }
}

