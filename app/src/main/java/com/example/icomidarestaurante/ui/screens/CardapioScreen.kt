package com.example.icomidarestaurante.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.icomidarestaurante.data.model.Cardapio
import com.example.icomidarestaurante.data.model.Produto
import com.example.icomidarestaurante.data.model.Restaurante
import com.example.icomidarestaurante.data.model.SecaoCardapio
import com.example.icomidarestaurante.data.remote.RetrofitClient
import kotlinx.coroutines.async
import java.text.NumberFormat
import java.util.Locale

private data class SecaoComProdutos(
    val secao: SecaoCardapio,
    val produtos: List<Produto>
)

private data class CardapioComSecoes(
    val cardapio: Cardapio,
    val secoes: List<SecaoComProdutos>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardapioScreen(
    restaurante: Restaurante,
    onVoltar: () -> Unit,
    onNovoProduto: () -> Unit = {},
    onEditarProduto: (Produto) -> Unit = {}
) {
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var cardapios by remember {
        mutableStateOf<List<CardapioComSecoes>>(emptyList())
    }
    var produtos by remember {
        mutableStateOf<List<Produto>>(emptyList())
    }
    var atualizar by remember { mutableIntStateOf(0) }

    var mostrarNovoProduto by remember {
        mutableStateOf(false)
    }
    var produtoEmEdicao by remember {
        mutableStateOf<Produto?>(null)
    }

    LaunchedEffect(restaurante.id_restaurante, atualizar) {
        carregando = true
        erro = null

        try {
            val api = RetrofitClient.apiService
            val id = restaurante.id_restaurante

            val listaCardapios = async {
                api.listarCardapios(id)
            }
            val listaProdutos = async {
                api.listarProdutosRestaurante(id)
            }

            val cardapiosRecebidos = listaCardapios.await()
            produtos = listaProdutos.await()



            cardapios = cardapiosRecebidos.map { cardapio ->
                val secoes = api.listarSecoesCardapio(
                    id,
                    cardapio.id_cardapio
                )


                CardapioComSecoes(
                    cardapio = cardapio,
                    secoes = secoes.map { secao ->

                        val relacoes = api.listarProdutosSecao(
                            secao.id_secao
                        )

                        val produtosDaSecao = relacoes
                            .sortedBy { it.ordem }
                            .mapNotNull { relacao ->
                                produtos.find {
                                    it.id_produto == relacao.id_produto
                                }
                            }

                        SecaoComProdutos(
                            secao = secao,
                            produtos = produtosDaSecao
                        )
                    }
                )

            }
        } catch (e: Exception) {
            erro = e.message ?: "Erro ao carregar cardápio."
        } finally {
            carregando = false
        }


    }


    produtoEmEdicao?.let { produto ->
        EditarProdutoScreen(
            restaurante = restaurante,
            produto = produto,
            onVoltar = {
                produtoEmEdicao = null
            },
            onProdutoAtualizado = {
                produtoEmEdicao = null
                atualizar++
            }
        )
        return
    }


    if (mostrarNovoProduto) {
        NovoProdutoScreen(
            restaurante = restaurante,
            onVoltar = {
                mostrarNovoProduto = false
            },
            onProdutoCriado = {
                mostrarNovoProduto = false
                atualizar++
            }
        )
        return
    }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cardápio e produtos") },
                navigationIcon = {
                    TextButton(onClick = onVoltar) {
                        Text("Voltar")
                    }
                },
                actions = {
                    TextButton(onClick = { atualizar++ }) {
                        Text("Atualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarNovoProduto = true }
            ) {
                Text("+")
            }
        }
    ) { padding ->

        when {
            carregando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            erro != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(erro ?: "Erro desconhecido")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { atualizar++ }) {
                        Text("Tentar novamente")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            restaurante.nome_fantasia,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${cardapios.size} cardápio(s) • " +
                                    "${produtos.size} produto(s)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (cardapios.isEmpty()) {
                        item {
                            Text("Nenhum cardápio cadastrado.")
                        }
                    }

                    cardapios.forEach { cardapio ->
                        item {
                            Text(
                                cardapio.cardapio.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }


                        cardapio.secoes.forEach { secao ->
                            item {
                                Text(
                                    secao.secao.nome,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            items(
                                items = secao.produtos,
                                key = {
                                    "secao_${secao.secao.id_secao}_produto_${it.id_produto}"
                                }
                            ) { produto ->
                                ProdutoCard(
                                    produto = produto,
                                    onClick = { produtoEmEdicao = produto }
                                )
                            }

                            if (secao.produtos.isEmpty()) {
                                item {
                                    Text(
                                        "Nenhum produto nesta seção.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                    }

                }
            }
        }
    }
}

@Composable
private fun ProdutoCard(
    produto: Produto,
    onClick: () -> Unit
) {
    val moeda = remember {
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("pt-BR")
        )
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                produto.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            produto.descricao?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    moeda.format(produto.preco_base),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (produto.disponivel) {
                        "Disponível"
                    } else {
                        "Indisponível"
                    },
                    color = if (produto.disponivel) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}
