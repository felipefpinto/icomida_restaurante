package com.example.icomidarestaurante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.icomidarestaurante.data.model.Restaurante
import com.example.icomidarestaurante.data.model.RelatorioVendas
import com.example.icomidarestaurante.data.model.calcularRelatorioVendas
import com.example.icomidarestaurante.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RelatorioVendasScreen(
    restaurante: Restaurante,
    onVoltar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val hoje = remember { LocalDate.now() }

    var periodo by remember { mutableStateOf("7") }
    var inicioPersonalizado by remember {
        mutableStateOf(hoje.minusDays(6).toString())
    }
    var fimPersonalizado by remember {
        mutableStateOf(hoje.toString())
    }

    var relatorio by remember {
        mutableStateOf<RelatorioVendas?>(null)
    }
    var carregando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }

    fun carregarRelatorio() {
        scope.launch {
            carregando = true
            erro = ""

            try {
                val inicio = when (periodo) {
                    "1" -> hoje
                    "7" -> hoje.minusDays(6)
                    "30" -> hoje.minusDays(29)
                    else -> LocalDate.parse(inicioPersonalizado)
                }

                val fim = if (periodo == "personalizado") {
                    LocalDate.parse(fimPersonalizado)
                } else {
                    hoje
                }

                require(!inicio.isAfter(fim)) {
                    "A data inicial não pode ser maior que a final."
                }
                require(!fim.isAfter(hoje)) {
                    "A data final não pode ser futura."
                }
                require(
                    java.time.temporal.ChronoUnit.DAYS
                        .between(inicio, fim) <= 366
                ) {
                    "Selecione um período de até 366 dias."
                }

                val pedidos = RetrofitClient.apiService
                    .listarPedidosRestaurante(
                        restaurante.id_restaurante
                    )

                relatorio = calcularRelatorioVendas(
                    pedidos = pedidos,
                    dataInicio = inicio,
                    dataFim = fim
                )

            } catch (e: Exception) {
                erro = e.message ?: "Erro ao carregar relatório."
            } finally {
                carregando = false
            }
        }
    }

    LaunchedEffect(restaurante.id_restaurante, periodo) {
        if (periodo != "personalizado") {
            carregarRelatorio()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    TextButton(onClick = onVoltar) {
                        Text("Voltar")
                    }
                    Text(
                        text = "Relatório de vendas",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    restaurante.nome_fantasia,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Text(
                    "Período",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "1" to "Hoje",
                        "7" to "7 dias",
                        "30" to "30 dias"
                    ).forEachIndexed { index, opcao ->
                        SegmentedButton(
                            selected = periodo == opcao.first,
                            onClick = { periodo = opcao.first },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = 3
                            )
                        ) {
                            Text(opcao.second)
                        }
                    }
                }

                TextButton(
                    onClick = { periodo = "personalizado" }
                ) {
                    Text("Selecionar período personalizado")
                }

                if (periodo == "personalizado") {
                    OutlinedTextField(
                        value = inicioPersonalizado,
                        onValueChange = { inicioPersonalizado = it },
                        label = { Text("Data inicial (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fimPersonalizado,
                        onValueChange = { fimPersonalizado = it },
                        label = { Text("Data final (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { carregarRelatorio() },
                        enabled = !carregando
                    ) {
                        Text("Aplicar período")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { carregarRelatorio() },
                    enabled = !carregando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Atualizar dados")
                }
            }

            if (carregando) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (erro.isNotEmpty()) {
                item {
                    Text(
                        erro,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            relatorio?.let { dados ->
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IndicadorVendas(
                            "Pedidos no período",
                            dados.quantidadePedidos.toString(),
                            "Todos os status"
                        )

                        IndicadorVendas(
                            "Vendas no período",
                            moedaRelatorio(dados.vendasTotal),
                            "Pedidos aceitos ou posteriores"
                        )

                        IndicadorVendas(
                            "Ticket médio",
                            moedaRelatorio(dados.ticketMedio),
                            "Média por venda"
                        )

                        IndicadorVendas(
                            "Cancelados / recusados",
                            dados.cancelados.toString(),
                            String.format(
                                Locale.forLanguageTag("pt-BR"),
                                "%.1f%% dos pedidos",
                                dados.taxaCancelamento
                            )
                        )
                    }
                }

                item {
                    Text(
                        "Vendas por dia",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(dados.vendasPorDia) { venda ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            venda.data.format(
                                DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy"
                                )
                            )
                        )
                        Text(moedaRelatorio(venda.total))
                    }
                }

                item {
                    HorizontalDivider()
                    Text(
                        "Pratos mais vendidos",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (dados.produtosMaisVendidos.isEmpty()) {
                    item {
                        Text("Nenhum produto vendido no período.")
                    }
                }

                items(
                    dados.produtosMaisVendidos
                ) { produto ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            produto.nome,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${produto.quantidade} un.")
                    }
                }

                item {
                    HorizontalDivider()
                    Text(
                        "Pedidos por status",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(dados.pedidosPorStatus) { status ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text(status.status)
                            Text(
                                "${status.quantidade} " +
                                        "(${status.percentual.toInt()}%)"
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = {
                                (status.percentual / 100.0)
                                    .toFloat()
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicadorVendas(
    titulo: String,
    valor: String,
    descricao: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                valor,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                descricao,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun moedaRelatorio(valor: BigDecimal): String {
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR")
    ).format(valor)
}
