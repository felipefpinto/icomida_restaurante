package com.example.icomidarestaurante.data.model

data class SecaoProduto(
    val id_produto: Int,
    val preco: java.math.BigDecimal? = null,
    val ordem: Int = 0
)