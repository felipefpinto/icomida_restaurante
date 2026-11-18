package com.example.icomidarestaurante.data.model

import java.math.BigDecimal

data class Produto(
    val id_produto: Int,
    val id_restaurante: Int,
    val nome: String,
    val descricao: String? = null,
    val preco_base: BigDecimal,
    val estoque: Int? = null,
    val serve_pessoas: Int? = null,
    val imagem_url: String? = null,
    val disponivel: Boolean = true,
    val ativo: Boolean = true
)
