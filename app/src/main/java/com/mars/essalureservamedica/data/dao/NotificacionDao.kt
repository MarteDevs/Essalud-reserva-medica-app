package com.mars.essalureservamedica.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mars.essalureservamedica.data.entity.Notificacion

@Dao
interface NotificacionDao {
    
    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun getNotificacionesByUserId(usuarioId: Int): LiveData<List<Notificacion>>
    
    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId AND leida = 0 ORDER BY fechaCreacion DESC")
    fun getNotificacionesNoLeidasByUserId(usuarioId: Int): LiveData<List<Notificacion>>
    
    @Query("SELECT COUNT(*) FROM notificaciones WHERE usuarioId = :usuarioId AND leida = 0")
    fun getCountNotificacionesNoLeidas(usuarioId: Int): LiveData<Int>
    
    @Query("SELECT * FROM notificaciones WHERE id = :notificacionId")
    suspend fun getNotificacionById(notificacionId: Int): Notificacion?
    
    @Query("SELECT * FROM notificaciones WHERE citaId = :citaId")
    suspend fun getNotificacionesByCitaId(citaId: Int): List<Notificacion>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacion(notificacion: Notificacion): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificaciones(notificaciones: List<Notificacion>)
    
    @Update
    suspend fun updateNotificacion(notificacion: Notificacion)
    
    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :notificacionId")
    suspend fun marcarComoLeida(notificacionId: Int)
    
    @Query("UPDATE notificaciones SET leida = 1 WHERE usuarioId = :usuarioId")
    suspend fun marcarTodasComoLeidas(usuarioId: Int)
    
    @Delete
    suspend fun deleteNotificacion(notificacion: Notificacion)
    
    @Query("DELETE FROM notificaciones WHERE id = :notificacionId")
    suspend fun deleteNotificacionById(notificacionId: Int)
    
    @Query("DELETE FROM notificaciones WHERE usuarioId = :usuarioId AND leida = 1")
    suspend fun deleteNotificacionesLeidas(usuarioId: Int)
    
    @Query("DELETE FROM notificaciones WHERE fechaCreacion < :fecha")
    suspend fun deleteNotificacionesAntiguas(fecha: Long)

    @Query("SELECT * FROM notificaciones ORDER BY fechaCreacion DESC")
    fun getAllNotificaciones(): LiveData<List<Notificacion>>

    @Query("SELECT * FROM notificaciones ORDER BY fechaCreacion DESC")
    suspend fun getAllNotificacionesSync(): List<Notificacion>
}