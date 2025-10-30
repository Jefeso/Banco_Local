package br.edu.fatecpg.bancolocal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class Despesa(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val descricao: String,

    val valor: Double,

    val categoria: String,

    val data: Long
)