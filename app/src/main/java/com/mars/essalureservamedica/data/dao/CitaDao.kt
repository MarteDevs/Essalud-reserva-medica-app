package com.mars.essalureservamedica.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mars.essalureservamedica.data.entity.Cita
import java.util.*

@Dao
interface CitaDao {
    
    @Query("SELECT * FROM citas")
    fun getAllCitas(): LiveData<List<Cita>>
    
    @Query("SELECT * FROM citas WHERE id = :citaId")
    suspend fun getCitaById(citaId: Int): Cita?
    
    @Query("SELECT * FROM citas WHERE usuarioId = :userId ORDER BY fechaHora ASC")
    fun getCitasByUserId(userId: Int): LiveData<List<Cita>>
    
    @Query("SELECT * FROM citas WHERE doctorId = :doctorId ORDER BY fechaHora ASC")
    fun getCitasByDoctorId(doctorId: Int): LiveData<List<Cita>>
    
    @Query("SELECT * FROM citas WHERE usuarioId = :userId AND estado = :estado ORDER BY fechaHora ASC")
    fun getCitasByUserIdAndEstado(userId: Int, estado: String): LiveData<List<Cita>>
    
    @Query("""
        SELECT c.*, d.nombre as doctorNombre, d.especialidad as doctorEspecialidad 
        FROM citas c 
        INNER JOIN doctores d ON c.doctorId = d.id 
        WHERE c.usuarioId = :userId 
        ORDER BY c.fechaHora ASC
    """)
    fun getCitasWithDoctorInfoByUserId(userId: Int): LiveData<List<CitaWithDoctorInfo>>
    
    @Query("""
        SELECT * FROM citas 
        WHERE usuarioId = :userId 
        AND estado = 'Confirmada' 
        ORDER BY fechaHora ASC 
        LIMIT 1
    """)
    suspend fun getNextCitaByUserId(userId: Int): Cita?
    
    @Query("""
        SELECT * FROM citas 
        WHERE doctorId = :doctorId 
        AND DATE(fechaHora/1000, 'unixepoch') = DATE(:fecha/1000, 'unixepoch')
        AND estado = 'Confirmada'
        ORDER BY fechaHora ASC
    """)
    suspend fun getCitasPorDoctorYFecha(doctorId: Int, fecha: Long): List<Cita>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCita(cita: Cita): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cita: Cita): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitas(citas: List<Cita>)
    
    @Update
    suspend fun updateCita(cita: Cita)
    
    @Update
    suspend fun update(cita: Cita)
    
    @Delete
    suspend fun deleteCita(cita: Cita)
    
    @Delete
    suspend fun delete(cita: Cita)
    
    @Query("DELETE FROM citas WHERE id = :citaId")
    suspend fun deleteCitaById(citaId: Int)
    
    @Query("UPDATE citas SET estado = :estado WHERE id = :citaId")
    suspend fun updateCitaEstado(citaId: Int, estado: String)

    @Query("UPDATE citas SET fechaHora = :nuevaFechaHora, estado = 'Reprogramada' WHERE id = :citaId")
    suspend fun reprogramarCita(citaId: Int, nuevaFechaHora: Date)

    @Query("UPDATE citas SET estado = 'Cancelada' WHERE id = :citaId")
    suspend fun cancelarCita(citaId: Int)

    @Query("SELECT COUNT(*) FROM citas WHERE usuarioId = :userId")
    suspend fun getCitaCountByUserId(userId: Int): Int

    @Query("SELECT COUNT(*) FROM citas WHERE usuarioId = :userId AND estado = :estado")
    suspend fun getCitaCountByUserIdAndEstado(userId: Int, estado: String): Int

    @Query("""
        SELECT * FROM citas 
        WHERE usuarioId = :userId 
        AND fechaHora < :fechaActual 
        ORDER BY fechaHora DESC
    """)
    fun getHistorialCitas(userId: Int, fechaActual: Date): LiveData<List<Cita>>
}

data class CitaWithDoctorInfo(
    val id: Int,
    val usuarioId: Int,
    val doctorId: Int,
    val fechaHora: Date,
    val estado: String,
    val notas: String?,
    val doctorNombre: String,
    val doctorEspecialidad: String
) : java.io.Serializable