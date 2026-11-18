package com.example.icomidarestaurante.data.model

import java.math.BigDecimal

data class Pedido(
    val id_pedido: Int,
    val id_usuario: Int,
    val id_restaurante: Int,
    val id_endereco: Int?,

    val nome_restaurante: String?,
    val logo_restaurante: String?,

    val status: String,
    val tipo_entrega: String,

    val subtotal: BigDecimal,
    val taxa_entrega: BigDecimal,
    val desconto: BigDecimal,
    val valor_total: BigDecimal,

    val observacao: String?,

    val endereco_cep: String?,
    val endereco_logradouro: String?,
    val endereco_numero: String?,
    val endereco_complemento: String?,
    val endereco_bairro: String?,
    val endereco_cidade: String?,
    val endereco_uf: String?,
    val endereco_ponto_referencia: String?,

    val itens: List<ItemPedido>,
    val pagamentos: List<Pagamento>,

    val created_at: String,
    val updated_at: String
)

data class ItemPedido(
    val id_item_pedido: Int,
    val id_produto: Int?,
    val nome_produto: String,
    val quantidade: Int,
    val preco_unitario: BigDecimal,
    val subtotal: BigDecimal,
    val observacao: String?,
    val complementos: List<ItemPedidoComplemento>,
    val created_at: String
)

data class ItemPedidoComplemento(
    val id_item_pedido_complemento: Int,
    val id_complemento: Int?,
    val nome_complemento: String,
    val quantidade: Int,
    val preco_unitario: BigDecimal,
    val subtotal: BigDecimal
)

data class Pagamento(
    val id_pagamento: Int,
    val metodo: String,
    val status: String,
    val valor: BigDecimal,
    val mercado_pago_order_id: String?,
    val mercado_pago_payment_id: String?,
    val external_reference: String?,
    val created_at: String,
    val updated_at: String
)

data class PedidoStatusUpdate(
    val status: String
)
