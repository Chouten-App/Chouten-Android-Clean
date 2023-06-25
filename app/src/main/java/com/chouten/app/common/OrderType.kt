package com.chouten.app.common

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}