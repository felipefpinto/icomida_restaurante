package com.example.icomidarestaurante.data.model

data class Restaurante(
    val id_restaurante: Int,
    val razao_social: String,
    val nome_fantasia: String,
    val descricao: String?,
    val cnpj: String,
    val status: String,
    val responsavel_id: Int,
    val categorias: List<Categoria>,
    val canais_venda: List<CanalVenda>,
    val horarios_funcionamento: List<HorarioFuncionamento>,
    val logo_url: String?,
    val banner_url: String?
)

data class Categoria(
    val id: Int,
    val nome: String,
    val descricao: String?
)

data class CanalVenda(
    val id_canal_venda: Int,
    val id_restaurante: Int,
    val tipo: String,
    val ativo: Boolean
)

data class HorarioFuncionamento(
    val id_horario_funcionamento: Int,
    val id_restaurante: Int,
    val dia_semana: String,
    val hora_abertura: String,
    val hora_fechamento: String
)
