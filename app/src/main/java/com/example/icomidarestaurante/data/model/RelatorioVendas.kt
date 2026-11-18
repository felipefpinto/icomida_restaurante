package com.example.icomidarestaurante.data.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val STATUS_VENDA = setOf(
    "ACEITO",
    "EM_PREPARO",
    "PRONTO",
    "SAIU_PARA_ENTREGA",
    "ENTREGUE"
)

private val STATUS_CANCELADOS = setOf(
    "CANCELADO",
    "RECUSADO"
)

private val FUSO_PAINEL = ZoneId.of("America/Sao_Paulo")

data class VendaPorDia(
    val data: LocalDate,
    val total: BigDecimal,
    val quantidadePedidos: Int
)

data class ProdutoMaisVendido(
    val chave: String,
    val nome: String,
    val quantidade: Int
)

data class StatusPedidoResumo(
    val status: String,
    val quantidade: Int,
    val percentual: Double
)

data class RelatorioVendas(
    val quantidadePedidos: Int,
    val vendasTotal: BigDecimal,
    val ticketMedio: BigDecimal,
    val cancelados: Int,
    val taxaCancelamento: Double,
    val vendasPorDia: List<VendaPorDia>,
    val produtosMaisVendidos: List<ProdutoMaisVendido>,
    val pedidosPorStatus: List<StatusPedidoResumo>
)

private fun dataDoPedido(valor: String): LocalDate {
    val texto = valor.trim().replace(" ", "T")

    return try {
        // Datas com fuso horário explícito
        OffsetDateTime.parse(texto)
            .atZoneSameInstant(FUSO_PAINEL)
            .toLocalDate()
    } catch (_: Exception) {
        // Datas sem fuso: mesma hipótese do painel web (-03:00)
        LocalDateTime.parse(texto)
            .toLocalDate()
    }
}

fun calcularRelatorioVendas(
    pedidos: List<Pedido>,
    dataInicio: LocalDate,
    dataFim: LocalDate
): RelatorioVendas {

    require(!dataInicio.isAfter(dataFim)) {
        "A data inicial deve ser anterior ou igual à final."
    }

    val pedidosPeriodo = pedidos.filter { pedido ->
        val data = dataDoPedido(pedido.created_at)
        !data.isBefore(dataInicio) && !data.isAfter(dataFim)
    }

    val pedidosVenda = pedidosPeriodo.filter { pedido ->
        pedido.status in STATUS_VENDA
    }

    val quantidadePedidos = pedidosPeriodo.size

    val cancelados = pedidosPeriodo.count { pedido ->
        pedido.status in STATUS_CANCELADOS
    }

    val vendasTotal = pedidosVenda.fold(BigDecimal.ZERO) {
            total, pedido -> total.add(pedido.valor_total)
    }

    val ticketMedio = if (pedidosVenda.isNotEmpty()) {
        vendasTotal.divide(
            BigDecimal(pedidosVenda.size),
            2,
            RoundingMode.HALF_UP
        )
    } else {
        BigDecimal.ZERO.setScale(2)
    }

    val taxaCancelamento = if (quantidadePedidos > 0) {
        cancelados.toDouble() * 100.0 / quantidadePedidos
    } else {
        0.0
    }

    // VENDAS POR DIA
    val vendasPorDia = mutableListOf<VendaPorDia>()
    var diaAtual = dataInicio

    while (!diaAtual.isAfter(dataFim)) {

        val vendasDia = pedidosVenda.filter { pedido ->
            dataDoPedido(pedido.created_at) == diaAtual
        }

        val totalDia = vendasDia.fold(BigDecimal.ZERO) {
                total, pedido -> total.add(pedido.valor_total)
        }

        vendasPorDia.add(
            VendaPorDia(
                data = diaAtual,
                total = totalDia,
                quantidadePedidos = vendasDia.size
            )
        )

        diaAtual = diaAtual.plusDays(1)
    }

    // PRODUTOS MAIS VENDIDOS
    val produtosMaisVendidos = pedidosVenda
        .flatMap { it.itens }
        .groupBy { item ->
            item.id_produto?.let { "id:$it" }
                ?: "nome:${item.nome_produto}"
        }
        .map { (chave, itens) ->
            ProdutoMaisVendido(
                chave = chave,
                nome = itens.first().nome_produto,
                quantidade = itens.sumOf { it.quantidade }
            )
        }
        .sortedWith(
            compareByDescending<ProdutoMaisVendido> {
                it.quantidade
            }.thenBy { it.nome }
        )
        .take(5)

    // PEDIDOS POR STATUS
    val pedidosPorStatus = pedidosPeriodo
        .groupingBy { it.status }
        .eachCount()
        .map { (status, quantidade) ->
            StatusPedidoResumo(
                status = status,
                quantidade = quantidade,
                percentual = if (quantidadePedidos > 0) {
                    quantidade.toDouble() * 100.0 /
                            quantidadePedidos
                } else {
                    0.0
                }
            )
        }
        .sortedByDescending { it.quantidade }

    return RelatorioVendas(
        quantidadePedidos = quantidadePedidos,
        vendasTotal = vendasTotal,
        ticketMedio = ticketMedio,
        cancelados = cancelados,
        taxaCancelamento = taxaCancelamento,
        vendasPorDia = vendasPorDia,
        produtosMaisVendidos = produtosMaisVendidos,
        pedidosPorStatus = pedidosPorStatus
    )
}
