package com.example.icomidarestaurante.data.model

import java.math.BigDecimal

data class NovoProdutoRequest(
    val nome: String,
    val descricao: String?,
    val preco_base: BigDecimal,
    val estoque: Int?,
    val serve_pessoas: Int?
)

data class VincularProdutoSecaoRequest(
    val id_produto: Int,
    val preco: BigDecimal? = null,
    val ordem: Int = 0
)
