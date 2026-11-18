package com.example.icomidarestaurante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.icomidarestaurante.data.model.Restaurante

@Composable
fun RestauranteScreen(
    restaurante: Restaurante,
    onVoltar: () -> Unit
) {
    var mostrarPedidos by remember {
        mutableStateOf(false)
    }

    var mostrarRelatorio by remember {
        mutableStateOf(false)
    }

    var mostrarCardapio by remember {
        mutableStateOf(false)
    }


    if (mostrarPedidos) {
        PedidosScreen(
            restaurante = restaurante,
            onVoltar = { mostrarPedidos = false }
        )
        return
    }

    if (mostrarRelatorio) {
        RelatorioVendasScreen(
            restaurante = restaurante,
            onVoltar = { mostrarRelatorio = false }
        )
        return
    }


    if (mostrarCardapio) {
        CardapioScreen(
            restaurante = restaurante,
            onVoltar = { mostrarCardapio = false }
        )
        return
    }



    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TextButton(onClick = onVoltar) {
                        Text("Voltar")
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "iComida Restaurante",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Text(
                text = restaurante.nome_fantasia,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text("Status: ${restaurante.status}")

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Painel do restaurante",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            Card(
                onClick = { mostrarPedidos = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pedidos",
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(

                onClick = { mostrarCardapio = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cardápio e produtos",
                    modifier = Modifier.padding(20.dp)

                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                onClick = { mostrarRelatorio = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Relatórios de vendas",
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}
