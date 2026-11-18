
package com.example.icomidarestaurante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

import com.example.icomidarestaurante.data.model.Pedido
import com.example.icomidarestaurante.data.model.PedidoStatusUpdate
import com.example.icomidarestaurante.data.model.Restaurante
import com.example.icomidarestaurante.data.remote.RetrofitClient

import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PedidosScreen(
    restaurante: Restaurante,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember {
        mutableIntStateOf(0)
    }

    var pedidos by remember(restaurante.id_restaurante) {
        mutableStateOf<List<Pedido>>(emptyList())
    }

    var carregando by remember {
        mutableStateOf(false)
    }

    var erro by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current

    var telaAtiva by remember {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            telaAtiva = lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // FILTRAR PEDIDOS POR ABA
    val pedidosFiltrados = pedidos.filter { pedido ->
        when (abaSelecionada) {
            0 -> pedido.status == "AGUARDANDO_ACEITE"

            1 -> pedido.status in listOf(
                "ACEITO",
                "EM_PREPARO",
                "PRONTO",
                "SAIU_PARA_ENTREGA"
            )

            2 -> pedido.status in listOf(
                "ENTREGUE",
                "RECUSADO",
                "AGUARDANDO_PAGAMENTO"
            )

            else -> false
        }
    }

    // CARREGAR PEDIDOS DA API

    var atualizando by remember {
        mutableStateOf(false)
    }

    suspend fun buscarPedidos(
        mostrarCarregamento: Boolean = false
    ) {
        if (atualizando) return

        atualizando = true

        if (mostrarCarregamento) {
            carregando = true
        }

        try {
            val novosPedidos = RetrofitClient.apiService
                .listarPedidosRestaurante(restaurante.id_restaurante)

            pedidos = novosPedidos
            erro = ""

        } catch (e: Exception) {
            if (mostrarCarregamento) {
                erro = "Erro ao carregar pedidos: ${e.message}"
            }
        } finally {
            carregando = false
            atualizando = false
        }
    }

    fun carregarPedidos() {
        scope.launch {
            buscarPedidos(mostrarCarregamento = false)
        }
    }


    // CARREGAMENTO INICIAL
    LaunchedEffect(restaurante.id_restaurante, telaAtiva) {
        if (telaAtiva) {
            buscarPedidos(mostrarCarregamento = pedidos.isEmpty())

            while (true) {
                delay(15_000L)
                buscarPedidos(mostrarCarregamento = false)
            }
        }
    }

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // VOLTAR
            TextButton(onClick = onVoltar) {
                Text("← Voltar")
            }

            // CABEÇALHO
            Text(
                text = "Pedidos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = restaurante.nome_fantasia,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ATUALIZAR PEDIDOS
            Button(
                onClick = { carregarPedidos() },
                enabled = !atualizando
            ) {
                Text("Atualizar pedidos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INDICADOR DE CARREGAMENTO
            if (carregando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ERRO DE CARREGAMENTO
            if (erro.isNotEmpty()) {
                Text(
                    text = erro,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ABAS DE PEDIDOS
            val abas = listOf(
                "Novos",
                "Em andamento",
                "Histórico"
            )

            ScrollableTabRow(
                selectedTabIndex = abaSelecionada,
                edgePadding = 0.dp
            ) {
                abas.forEachIndexed { index, titulo ->

                    Tab(
                        selected = abaSelecionada == index,
                        onClick = {
                            abaSelecionada = index
                        },
                        text = {
                            Text(titulo)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MENSAGEM DE LISTA VAZIA
            if (
                !carregando &&
                erro.isEmpty() &&
                pedidosFiltrados.isEmpty()
            ) {
                Text("Nenhum pedido nesta aba.")
            }

            // LISTAGEM DE PEDIDOS
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = pedidosFiltrados,
                    key = { it.id_pedido }
                ) { pedido ->

                    PedidoCard(
                        pedido = pedido,
                        onAtualizar = {
                            carregarPedidos()
                        }
                    )
                }
            }
        }
    }
}


// =====================================================
// CARD DE PEDIDO
// =====================================================

@Composable
fun PedidoCard(
    pedido: Pedido,
    onAtualizar: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var processando by remember {
        mutableStateOf(false)
    }

    var erroAcao by remember {
        mutableStateOf("")
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // IDENTIFICAÇÃO DO PEDIDO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Pedido #${pedido.id_pedido}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = pedido.status,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TIPO DE ENTREGA
            Text("Tipo: ${pedido.tipo_entrega}")

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // PRODUTOS DO PEDIDO
            pedido.itens.forEach { item ->

                Text(
                    text = "${item.quantidade}x ${item.nome_produto}"
                )

                // COMPLEMENTOS
                item.complementos.forEach { complemento ->

                    Text(
                        text = "  + ${complemento.quantidade}x ${complemento.nome_complemento}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // OBSERVAÇÃO DO ITEM
                if (!item.observacao.isNullOrBlank()) {

                    Text(
                        text = "Obs. item: ${item.observacao}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // OBSERVAÇÃO DO PEDIDO
            if (!pedido.observacao.isNullOrBlank()) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Observação: ${pedido.observacao}"
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // VALOR TOTAL
            Text(
                text = "Total: ${formatarMoeda(pedido.valor_total)}",
                fontWeight = FontWeight.Bold
            )

            // ERROS DAS AÇÕES
            if (erroAcao.isNotEmpty()) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = erroAcao,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // INDICADOR DE PROCESSAMENTO
            if (processando) {

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // =================================================
            // ACEITAR OU RECUSAR PEDIDO
            // =================================================

            if (pedido.status == "AGUARDANDO_ACEITE") {

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // RECUSAR
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                processando = true
                                erroAcao = ""

                                try {
                                    RetrofitClient.apiService
                                        .recusarPedido(
                                            pedido.id_restaurante,
                                            pedido.id_pedido
                                        )

                                    onAtualizar()

                                } catch (e: Exception) {
                                    erroAcao =
                                        "Erro ao recusar: ${e.message}"

                                } finally {
                                    processando = false
                                }
                            }
                        },
                        enabled = !processando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Recusar")
                    }

                    // ACEITAR
                    Button(
                        onClick = {
                            scope.launch {
                                processando = true
                                erroAcao = ""

                                try {
                                    RetrofitClient.apiService
                                        .aceitarPedido(
                                            pedido.id_restaurante,
                                            pedido.id_pedido
                                        )

                                    onAtualizar()

                                } catch (e: Exception) {
                                    erroAcao =
                                        "Erro ao aceitar: ${e.message}"

                                } finally {
                                    processando = false
                                }
                            }
                        },
                        enabled = !processando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceitar")
                    }
                }
            }

            // =================================================
            // DEFINIR PRÓXIMO STATUS
            // =================================================

            val proximoStatus = when (pedido.status) {

                "ACEITO" -> "EM_PREPARO"

                "EM_PREPARO" -> "PRONTO"

                "PRONTO" -> {
                    if (pedido.tipo_entrega == "RETIRADA") {
                        "ENTREGUE"
                    } else {
                        "SAIU_PARA_ENTREGA"
                    }
                }

                "SAIU_PARA_ENTREGA" -> "ENTREGUE"

                else -> null
            }

            val textoBotao = when (proximoStatus) {

                "EM_PREPARO" -> "Iniciar preparo"

                "PRONTO" -> "Marcar como pronto"

                "SAIU_PARA_ENTREGA" -> "Saiu para entrega"

                "ENTREGUE" -> "Marcar como entregue"

                else -> ""
            }

            // =================================================
            // BOTÃO PARA ATUALIZAR STATUS
            // =================================================

            if (proximoStatus != null) {

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            processando = true
                            erroAcao = ""

                            try {
                                RetrofitClient.apiService
                                    .atualizarStatusPedido(
                                        pedido.id_restaurante,
                                        pedido.id_pedido,
                                        PedidoStatusUpdate(
                                            status = proximoStatus
                                        )
                                    )

                                onAtualizar()

                            } catch (e: Exception) {
                                erroAcao =
                                    "Erro ao atualizar: ${e.message}"

                            } finally {
                                processando = false
                            }
                        }
                    },
                    enabled = !processando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(textoBotao)
                }
            }
        }
    }
}


// =====================================================
// FORMATAR VALORES EM REAIS
// =====================================================

private fun formatarMoeda(valor: BigDecimal): String {
    return NumberFormat
        .getCurrencyInstance(
            Locale.forLanguageTag("pt-BR")
        )
        .format(valor)
}
