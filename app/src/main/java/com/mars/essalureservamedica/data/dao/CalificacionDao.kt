package com.mars.essalureservamedica.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mars.essalureservamedica.data.entity.Calificacion
import java.util.*

@Dao
interface CalificacionDao {
    
    @Query("SELECT * FROM calificaciones ORDER BY fechaCalificacion DESC")
    fun getAllCalificaciones(): LiveData<List<Calificacion>>
    
    @Query("SELECT * FROM calificaciones ORDER BY fechaCalificacion DESC")
    suspend fun getAllCalificacionesSync(): List<Calificacion>
    
    @Query("SELECT * FROM calificaciones WHERE doctorId = :doctorId ORDER BY fechaCalificacion DESC")
    fun getCalificacionesByDoctorId(doctorId: Int): LiveData<List<Calificacion>>
    
    @Query("SELECT * FROM calificaciones WHERE usuarioId = :usuarioId ORDER BY fechaCalificacion DESC")
    fun getCalificacionesByUsuarioId(usuarioId: Int): LiveData<List<Calificacion>>
    
    @Query("SELECT * FROM calificaciones WHERE citaId = :citaId")
    suspend fun getCalificacionByCitaId(citaId: Int): Calificacion?
    
    @Query("SELECT AVG(puntuacion) FROM calificaciones WHERE doctorId = :doctorId")
    suspend fun getPromedioPuntuacionDoctor(doctorId: Int): Float?
    
    @Query("SELECT COUNT(*) FROM calificaciones WHERE doctorId = :doctorId")
    suspend fun getCountCalificacionesDoctor(doctorId: Int): Int
    
    @Query("""
        SELECT c.*, u.nombreCompleto as usuarioNombre, d.nombre as doctorNombre 
        FROM calificaciones c 
        INNER JOIN users u ON c.usuarioId = u.id 
        INNER JOIN doctores d ON c.doctorId = d.id 
        WHERE c.doctorId = :doctorId 
        ORDER BY c.fechaCalificacion DESC
    """)
    fun getCalificacionesConDetallesByDoctorId(doctorId: Int): LiveData<List<CalificacionConDetalles>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calificacion: Calificacion): Long
    
    @Update
    suspend fun update(calificacion: Calificacion)
    
    @Delete
    suspend fun delete(calificacion: Calificacion)
    
    @Query("DELETE FROM calificaciones WHERE id = :calificacionId")
    suspend fun deleteById(calificacionId: Int)
}

data class CalificacionConDetalles(
    val id: Int,
    val usuarioId: Int,
    val doctorId: Int,
    val citaId: Int,
    val puntuacion: Float,
    val comentario: String?,
    val fechaCalificacion: Date,
    val usuarioNombre: String,
    val doctorNombre: String
) : java.io.Serializable