package com.mars.essalureservamedica.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.database.AppDatabase
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.data.dao.CalificacionConDetalles
import com.mars.essalureservamedica.data.entity.Calificacion
import com.mars.essalureservamedica.data.entity.Cita
import com.mars.essalureservamedica.data.entity.Doctor
import com.mars.essalureservamedica.data.entity.Notificacion
import com.mars.essalureservamedica.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AppRepository(private val database: AppDatabase) {

    // User operations
    suspend fun insertUser(user: User): Long {
        return database.userDao().insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return database.userDao().getUserByEmail(email)
    }

    suspend fun getUserById(id: Int): User? {
        return database.userDao().getUserById(id)
    }

    suspend fun updateUser(user: User) {
        database.userDao().updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        database.userDao().deleteUser(user)
    }

    suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        return database.userDao().getUserByEmailAndPassword(email, password)
    }

    // Doctor operations
    fun getAllDoctors(): LiveData<List<Doctor>> {
        return database.doctorDao().getAllDoctors()
    }

    suspend fun getDoctorById(id: Int): Doctor? {
        return database.doctorDao().getDoctorById(id)
    }

    suspend fun getDoctorCount(): Int {
        return database.doctorDao().getDoctorCount()
    }

    suspend fun insertDoctor(doctor: Doctor): Long {
        return database.doctorDao().insert(doctor)
    }

    suspend fun updateDoctor(doctor: Doctor) {
        database.doctorDao().update(doctor)
    }

    suspend fun deleteDoctor(doctor: Doctor) {
        database.doctorDao().delete(doctor)
    }

    // Cita operations
    fun getCitasWithDoctorByUserId(userId: Int): LiveData<List<CitaWithDoctorInfo>> {
        return database.citaDao().getCitasWithDoctorInfoByUserId(userId)
    }

    fun getCitasByUserId(userId: Int): LiveData<List<Cita>> {
        return database.citaDao().getCitasByUserId(userId)
    }

    suspend fun getCitasPorDoctorYFecha(doctorId: Int, fecha: Long): List<Cita> {
        return database.citaDao().getCitasPorDoctorYFecha(doctorId, fecha)
    }

    suspend fun cancelarCita(citaId: Int) {
        database.citaDao().cancelarCita(citaId)
    }

    suspend fun reprogramarCita(citaId: Int, nuevaFechaHora: Date) {
        database.citaDao().reprogramarCita(citaId, nuevaFechaHora)
    }

    suspend fun updateCitaEstado(citaId: Int, estado: String) {
        database.citaDao().updateCitaEstado(citaId, estado)
    }

    fun getHistorialCitas(userId: Int, fechaActual: Date): LiveData<List<Cita>> {
        return database.citaDao().getHistorialCitas(userId, fechaActual)
    }

    suspend fun getCitaCountByUserId(userId: Int): Int {
        return database.citaDao().getCitaCountByUserId(userId)
    }

    suspend fun getCitaCountByUserIdAndEstado(userId: Int, estado: String): Int {
        return database.citaDao().getCitaCountByUserIdAndEstado(userId, estado)
    }

    suspend fun insertCita(cita: Cita): Long {
        return database.citaDao().insert(cita)
    }

    suspend fun updateCita(cita: Cita) {
        database.citaDao().update(cita)
    }

    suspend fun deleteCita(cita: Cita) {
        database.citaDao().delete(cita)
    }

    // Calificacion operations
    suspend fun insertCalificacion(calificacion: Calificacion): Long {
        return database.calificacionDao().insert(calificacion)
    }

    suspend fun updateCalificacion(calificacion: Calificacion) {
        database.calificacionDao().update(calificacion)
    }

    suspend fun deleteCalificacion(calificacion: Calificacion) {
        database.calificacionDao().delete(calificacion)
    }

    fun getCalificacionesByDoctorId(doctorId: Int): LiveData<List<Calificacion>> {
        return database.calificacionDao().getCalificacionesByDoctorId(doctorId)
    }

    fun getCalificacionesConDetallesByDoctorId(doctorId: Int): LiveData<List<CalificacionConDetalles>> {
        return database.calificacionDao().getCalificacionesConDetallesByDoctorId(doctorId)
    }

    suspend fun getCalificacionByCitaId(citaId: Int): Calificacion? {
        return database.calificacionDao().getCalificacionByCitaId(citaId)
    }

    suspend fun getPromedioPuntuacionDoctor(doctorId: Int): Float? {
        return database.calificacionDao().getPromedioPuntuacionDoctor(doctorId)
    }

    suspend fun getCountCalificacionesDoctor(doctorId: Int): Int {
        return database.calificacionDao().getCountCalificacionesDoctor(doctorId)
    }

    // Notificacion operations
    fun getNotificacionesByUserId(userId: Int): LiveData<List<Notificacion>> {
        return database.notificacionDao().getNotificacionesByUserId(userId)
    }

    fun getNotificacionesNoLeidasByUserId(userId: Int): LiveData<List<Notificacion>> {
        return database.notificacionDao().getNotificacionesNoLeidasByUserId(userId)
    }

    fun getCountNotificacionesNoLeidas(userId: Int): LiveData<Int> {
        return database.notificacionDao().getCountNotificacionesNoLeidas(userId)
    }

    suspend fun insertNotificacion(notificacion: Notificacion): Long {
        return database.notificacionDao().insertNotificacion(notificacion)
    }

    suspend fun marcarNotificacionComoLeida(notificacionId: Int) {
        database.notificacionDao().marcarComoLeida(notificacionId)
    }

    suspend fun marcarTodasNotificacionesComoLeidas(userId: Int) {
        database.notificacionDao().marcarTodasComoLeidas(userId)
    }

    suspend fun deleteNotificacionesLeidas(userId: Int) {
        database.notificacionDao().deleteNotificacionesLeidas(userId)
    }

    // Método para crear notificaciones automáticas
    suspend fun crearNotificacionCita(usuarioId: Int, citaId: Int, tipo: String, titulo: String, mensaje: String) {
        val notificacion = Notificacion(
            usuarioId = usuarioId,
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipo,
            citaId = citaId
        )
        insertNotificacion(notificacion)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context)
                val repository = AppRepository(database)
                
                INSTANCE = repository
                repository
            }
        }
    }
}