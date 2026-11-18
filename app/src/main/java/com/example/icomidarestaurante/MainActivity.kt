
package com.example.icomidarestaurante

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.icomidarestaurante.ui.screens.RestauranteScreen
import com.example.icomidarestaurante.data.model.Responsavel
import com.example.icomidarestaurante.data.model.Restaurante
import com.example.icomidarestaurante.data.remote.RetrofitClient
import com.example.icomidarestaurante.ui.theme.IComidaRestauranteTheme
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IComidaRestauranteTheme {
                IdentificacaoScreen()
            }
        }
    }
}

@Composable
fun IdentificacaoScreen() {
    var email by rememberSaveable { mutableStateOf("") }
    var responsavel by remember { mutableStateOf<Responsavel?>(null) }
    var restaurantes by remember { mutableStateOf<List<Restaurante>>(emptyList()) }
    var restauranteSelecionado by remember {
        mutableStateOf<Restaurante?>(null)
    }
    var carregando by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    if (restauranteSelecionado != null) {
        RestauranteScreen(
            restaurante = restauranteSelecionado!!,
            onVoltar = {
                restauranteSelecionado = null
            }
        )
        return
    }

    val emailValido = Patterns.EMAIL_ADDRESS
        .matcher(email.trim())
        .matches()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "iComida Restaurante",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    mensagem = ""
                    responsavel = null
                    restaurantes = emptyList()
                },
                label = { Text("E-mail do responsável") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        carregando = true
                        mensagem = ""
                        responsavel = null
                        restaurantes = emptyList()

                        try {
                            val dadosResponsavel =
                                RetrofitClient.apiService
                                    .buscarResponsavelPorEmail(email.trim())

                            if (!dadosResponsavel.ativo) {
                                mensagem = "Responsável inativo."
                            } else {
                                val listaRestaurantes =
                                    RetrofitClient.apiService
                                        .listarRestaurantesDoResponsavel(
                                            dadosResponsavel.id_responsavel
                                        )

                                responsavel = dadosResponsavel
                                restaurantes = listaRestaurantes

                                if (listaRestaurantes.isEmpty()) {
                                    mensagem = "Nenhum restaurante encontrado."
                                }
                            }
                        } catch (e: HttpException) {
                            mensagem = when (e.code()) {
                                404 -> "Responsável não encontrado."
                                else -> "Erro HTTP: ${e.code()}"
                            }
                        } catch (e: IOException) {
                            mensagem = "Não foi possível conectar à API."
                        } catch (e: Exception) {
                            mensagem = "Erro ao consultar os restaurantes."
                        } finally {
                            carregando = false
                        }
                    }
                },
                enabled = emailValido && !carregando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar meus restaurantes")
            }

            if (carregando) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }

            if (mensagem.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(mensagem)
            }

            responsavel?.let { dados ->
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Olá, ${dados.nome}!",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Seus restaurantes",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = restaurantes,
                        key = { it.id_restaurante }
                    ) { restaurante ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    restauranteSelecionado = restaurante
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = restaurante.nome_fantasia,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Status: ${restaurante.status}"
                                )

                                Text(
                                    text = "ID: ${restaurante.id_restaurante}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
