package com.example.pruebaandroid.auth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String,
    val nombre: String,
    val email: String,
    val periodoValidacion: Int
)
