package com.example.icomidarestaurante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.icomidarestaurante.data.model.*
import com.example.icomidarestaurante.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProdutoScreen(
    restaurante: Restaurante,
    produto: Produto,
    onVoltar: () -> Unit,
    onProdutoAtualizado: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var nome by remember(produto.id_produto) {
        mutableStateOf(produto.nome)
    }
    var descricao by remember(produto.id_produto) {
        mutableStateOf(produto.descricao ?: "")
    }
    var preco by remember(produto.id_produto) {
        mutableStateOf(produto.preco_base.toPlainString())
    }
    var estoque by remember(produto.id_produto) {
        mutableStateOf(produto.estoque?.toString() ?: "")
    }
    var servePessoas by remember(produto.id_produto) {
        mutableStateOf(produto.serve_pessoas?.toString() ?: "")
    }
    var disponivel by remember(produto.id_produto) {
        mutableStateOf(produto.disponivel)
    }

    var salvando by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf(false) }
    var confirmarExclusao by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar produto") },
                navigationIcon = {
                    TextButton(
                        onClick = onVoltar,
                        enabled = !salvando && !excluindo
                    ) {
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
                onValueChange = { nome = it },
                label = { Text("Nome do produto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = preco,
                onValueChange = { preco = it },
                label = { Text("Preço (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = estoque,
                onValueChange = { estoque = it },
                label = { Text("Estoque (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = servePessoas,
                onValueChange = { servePessoas = it },
                label = { Text("Serve quantas pessoas?") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Disponibilidade")
                    Text(
                        if (disponivel) "Produto disponível"
                        else "Produto indisponível",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = disponivel,
                    onCheckedChange = { disponivel = it }
                )
            }

            erro?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !salvando && !excluindo,
                onClick = {
                    val valor = preco.replace(",", ".")
                        .toBigDecimalOrNull()

                    val estoqueNumero =
                        estoque.takeIf { it.isNotBlank() }
                            ?.toIntOrNull()

                    val pessoasNumero =
                        servePessoas.takeIf { it.isNotBlank() }
                            ?.toIntOrNull()

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
                            erro = "Informe uma quantidade válida."

                        else -> {
                            scope.launch {
                                salvando = true
                                erro = null

                                try {
                                    RetrofitClient.apiService.atualizarProduto(
                                        restaurante.id_restaurante,
                                        produto.id_produto,
                                        AtualizarProdutoRequest(
                                            nome = nome.trim(),
                                            descricao = descricao.trim()
                                                .ifBlank { null },
                                            preco_base = valor,
                                            estoque = estoqueNumero,
                                            serve_pessoas = pessoasNumero,
                                            disponivel = disponivel
                                        )
                                    )
                                    onProdutoAtualizado()
                                } catch (e: Exception) {
                                    erro = "Erro ao atualizar: ${e.message}"
                                } finally {
                                    salvando = false
                                }
                            }
                        }
                    }
                }
            ) {
                Text(if (salvando) "Salvando..." else "Salvar alterações")
            }

            OutlinedButton(
                onClick = { confirmarExclusao = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !salvando && !excluindo,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Excluir produto")
            }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                if (!excluindo) confirmarExclusao = false
            },
            title = { Text("Excluir produto?") },
            text = {
                Text(
                    "Deseja excluir '${produto.nome}'? " +
                            "Essa ação não poderá ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !excluindo,
                    onClick = {
                        scope.launch {
                            excluindo = true
                            erro = null
                            try {
                                val resposta =
                                    RetrofitClient.apiService.excluirProduto(
                                        restaurante.id_restaurante,
                                        produto.id_produto
                                    )

                                if (resposta.isSuccessful) {
                                    confirmarExclusao = false
                                    onProdutoAtualizado()
                                } else {
                                    erro = "Erro ao excluir: HTTP " +
                                            resposta.code()
                                    confirmarExclusao = false
                                }
                            } catch (e: Exception) {
                                erro = "Erro ao excluir: ${e.message}"
                                confirmarExclusao = false
                            } finally {
                                excluindo = false
                            }
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !excluindo,
                    onClick = { confirmarExclusao = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
