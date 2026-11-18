package com.example.icomidarestaurante.data.model

import java.math.BigDecimal

data class AtualizarProdutoRequest(
    val nome: String,
    val descricao: String?,
    val preco_base: BigDecimal,
    val estoque: Int?,
    val serve_pessoas: Int?,
    val disponivel: Boolean
)
