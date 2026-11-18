package com.example.icomidarestaurante.data.remote

import com.example.icomidarestaurante.data.model.Responsavel
import com.example.icomidarestaurante.data.model.Restaurante
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.icomidarestaurante.data.model.Pedido
import com.example.icomidarestaurante.data.model.PedidoStatusUpdate
import retrofit2.http.PATCH
import retrofit2.http.Body
import com.example.icomidarestaurante.data.model.Produto
import com.example.icomidarestaurante.data.model.Cardapio
import com.example.icomidarestaurante.data.model.SecaoCardapio
import com.example.icomidarestaurante.data.model.SecaoProduto
import com.example.icomidarestaurante.data.model.NovoProdutoRequest
import com.example.icomidarestaurante.data.model.VincularProdutoSecaoRequest
import retrofit2.http.POST
import com.example.icomidarestaurante.data.model.AtualizarProdutoRequest
import retrofit2.http.DELETE

interface ApiService {

    @GET("responsavel-restaurante/email/{email}")
    suspend fun buscarResponsavelPorEmail(
        @Path("email") email: String
    ): Responsavel

    @GET("responsavel-restaurante/{id_responsavel}/restaurantes")
    suspend fun listarRestaurantesDoResponsavel(
        @Path("id_responsavel") idResponsavel: Int
    ): List<Restaurante>


    @GET("restaurantes/{id_restaurante}/pedidos")
    suspend fun listarPedidosRestaurante(
        @Path("id_restaurante") idRestaurante: Int
    ): List<Pedido>

    @GET("restaurantes/{id_restaurante}/pedidos/{id_pedido}")
    suspend fun buscarPedidoRestaurante(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_pedido") idPedido: Int
    ): Pedido

    @PATCH("restaurantes/{id_restaurante}/pedidos/{id_pedido}/aceitar")
    suspend fun aceitarPedido(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_pedido") idPedido: Int
    ): Pedido

    @PATCH("restaurantes/{id_restaurante}/pedidos/{id_pedido}/recusar")
    suspend fun recusarPedido(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_pedido") idPedido: Int
    ): Pedido

    @PATCH("restaurantes/{id_restaurante}/pedidos/{id_pedido}/status")
    suspend fun atualizarStatusPedido(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_pedido") idPedido: Int,
        @Body dados: PedidoStatusUpdate
    ): Pedido


    // CARDÁPIOS
    @GET("restaurantes/{id_restaurante}/cardapios/")
    suspend fun listarCardapios(
        @Path("id_restaurante") idRestaurante: Int
    ): List<Cardapio>

    // SEÇÕES DO CARDÁPIO
    @GET("restaurantes/{id_restaurante}/cardapios/{id_cardapio}/secoes/")
    suspend fun listarSecoesCardapio(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_cardapio") idCardapio: Int
    ): List<SecaoCardapio>

    // PRODUTOS DO RESTAURANTE
    @GET("restaurantes/{id_restaurante}/produtos/")
    suspend fun listarProdutosRestaurante(
        @Path("id_restaurante") idRestaurante: Int
    ): List<Produto>


    @GET("secoes/{id_secao}/produtos/")
    suspend fun listarProdutosSecao(
        @Path("id_secao") idSecao: Int
    ): List<SecaoProduto>


    @POST("restaurantes/{id_restaurante}/produtos/")
    suspend fun criarProduto(
        @Path("id_restaurante") idRestaurante: Int,
        @Body dados: NovoProdutoRequest
    ): Produto

    @POST("secoes/{id_secao}/produtos/")
    suspend fun vincularProdutoSecao(
        @Path("id_secao") idSecao: Int,
        @Body dados: VincularProdutoSecaoRequest
    ): retrofit2.Response<okhttp3.ResponseBody>


    @GET("restaurantes/{id_restaurante}/produtos/{id_produto}")
    suspend fun buscarProduto(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_produto") idProduto: Int
    ): Produto

    @PATCH("restaurantes/{id_restaurante}/produtos/{id_produto}")
    suspend fun atualizarProduto(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_produto") idProduto: Int,
        @Body dados: AtualizarProdutoRequest
    ): Produto

    @DELETE("restaurantes/{id_restaurante}/produtos/{id_produto}")
    suspend fun excluirProduto(
        @Path("id_restaurante") idRestaurante: Int,
        @Path("id_produto") idProduto: Int
    ): retrofit2.Response<okhttp3.ResponseBody>





}
