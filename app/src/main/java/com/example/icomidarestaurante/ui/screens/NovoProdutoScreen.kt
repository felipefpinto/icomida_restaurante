package com.example.icomidarestaurante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.icomidarestaurante.data.model.*
import com.example.icomidarestaurante.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoProdutoScreen(
    restaurante: Restaurante,
    onVoltar: () -> Unit,
    onProdutoCriado: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var estoque by remember { mutableStateOf("") }
    var servePessoas by remember { mutableStateOf("") }

    var secoes by remember {
        mutableStateOf<List<Pair<Int, String>>>(emptyList())
    }
    var secaoSelecionada by remember { mutableStateOf<Int?>(null) }
    var menuAberto by remember { mutableStateOf(false) }

    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(restaurante.id_restaurante) {
        try {
            carregando = true
            erro = null

            val api = RetrofitClient.apiService
            val id = restaurante.id_restaurante
            val cardapios = api.listarCardapios(id)

            val lista = mutableListOf<Pair<Int, String>>()

            cardapios.forEach { cardapio ->
                val secoesCardapio = api.listarSecoesCardapio(
                    id, cardapio.id_cardapio
                )
                secoesCardapio.forEach { secao ->
                    lista.add(
                        secao.id_secao to
                                "${secao.nome} — ${cardapio.nome}"
                    )
                }
            }

            secoes = lista
            secaoSelecionada = lista.firstOrNull()?.first
        } catch (e: Exception) {
            erro = "Erro ao carregar seções: ${e.message}"
        } finally {
            carregando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo produto") },
                navigationIcon = {
                    TextButton(onClick = onVoltar) {
                        Text("Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it.take(120) },
                label = { Text("Nome do produto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it.take(500) },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = preco,
                onValueChange = { preco = it },
                label = { Text("Preço (R$)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = estoque,
                onValueChange = { estoque = it },
                label = { Text("Estoque (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = servePessoas,
                onValueChange = { servePessoas = it },
                label = { Text("Serve quantas pessoas?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                "Organização no cardápio",
                style = MaterialTheme.typography.titleMedium
            )

            if (carregando) {
                CircularProgressIndicator()
            } else if (secoes.isEmpty()) {
                Text("Nenhuma seção cadastrada.")
            } else {
                ExposedDropdownMenuBox(
                    expanded = menuAberto,
                    onExpandedChange = { menuAberto = it }
                ) {
                    OutlinedTextField(
                        value = secoes.firstOrNull {
                            it.first == secaoSelecionada
                        }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seção") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuAberto
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryNotEditable
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = menuAberto,
                        onDismissRequest = { menuAberto = false }
                    ) {
                        secoes.forEach { secao ->
                            DropdownMenuItem(
                                text = { Text(secao.second) },
                                onClick = {
                                    secaoSelecionada = secao.first
                                    menuAberto = false
                                }
                            )
                        }
                    }
                }
            }

            erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val valor = preco.replace(",", ".")
                        .toBigDecimalOrNull()
                    val estoqueNumero = if (estoque.isBlank()) {
                        null
                    } else {
                        estoque.toIntOrNull()
                    }
                    val pessoasNumero = if (servePessoas.isBlank()) {
                        null
                    } else {
                        servePessoas.toIntOrNull()
                    }

                    when {
                        nome.isBlank() ->
                            erro = "Informe o nome do produto."

                        valor == null || valor <= BigDecimal.ZERO ->
                            erro = "Informe um preço válido."

                        estoque.isNotBlank() &&
                                (estoqueNumero == null || estoqueNumero < 0) ->
                            erro = "Informe um estoque válido."

                        servePessoas.isNotBlank() &&
                                (pessoasNumero == null || pessoasNumero <= 0) ->
                            erro = "Informe uma quantidade de pessoas válida."

                        secaoSelecionada == null ->
                            erro = "Selecione uma seção."

                        else -> {
                            scope.launch {
                                salvando = true
                                erro = null

                                try {
                                    val api = RetrofitClient.apiService
                                    val criado = api.criarProduto(
                                        restaurante.id_restaurante,
                                        NovoProdutoRequest(
                                            nome = nome.trim(),
                                            descricao = descricao.trim()
                                                .ifBlank { null },
                                            preco_base = valor,
                                            estoque = estoqueNumero,
                                            serve_pessoas = pessoasNumero
                                        )
                                    )

                                    val resposta = api.vincularProdutoSecao(
                                        secaoSelecionada!!,
                                        VincularProdutoSecaoRequest(
                                            id_produto = criado.id_produto
                                        )
                                    )

                                    if (!resposta.isSuccessful) {
                                        erro = "Produto criado, mas não foi " +
                                                "possível vinculá-lo à seção."
                                    } else {
                                        onProdutoCriado()
                                    }
                                } catch (e: Exception) {
                                    erro = "Erro ao salvar: ${e.message}"
                                } finally {
                                    salvando = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !salvando && !carregando && secoes.isNotEmpty()
            ) {
                Text(if (salvando) "Salvando..." else "Salvar produto")
            }
        }
    }
}
