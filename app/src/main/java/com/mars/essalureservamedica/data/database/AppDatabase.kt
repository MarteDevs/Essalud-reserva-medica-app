package com.mars.essalureservamedica.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mars.essalureservamedica.data.converter.DateConverter
import com.mars.essalureservamedica.data.dao.CalificacionDao
import com.mars.essalureservamedica.data.dao.CitaDao
import com.mars.essalureservamedica.data.dao.DoctorDao
import com.mars.essalureservamedica.data.dao.NotificacionDao
import com.mars.essalureservamedica.data.dao.UserDao
import com.mars.essalureservamedica.data.entity.Calificacion
import com.mars.essalureservamedica.data.entity.Cita
import com.mars.essalureservamedica.data.entity.Doctor
import com.mars.essalureservamedica.data.entity.Notificacion
import com.mars.essalureservamedica.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Doctor::class, Cita::class, Calificacion::class, Notificacion::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun doctorDao(): DoctorDao
    abstract fun citaDao(): CitaDao
    abstract fun calificacionDao(): CalificacionDao
    abstract fun notificacionDao(): NotificacionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "essalud_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.doctorDao())
                    }
                }
            }
        }
        
        private suspend fun populateDatabase(doctorDao: DoctorDao) {
            // Poblar con doctores de ejemplo
            val sampleDoctors = listOf(
                Doctor(
                    nombre = "Dr. Carlos Mendoza",
                    especialidad = "Cardiología",
                    experiencia = "15 años de experiencia en cardiología intervencionista",
                    disponibilidad = "Lunes a Viernes, 8:00 AM - 4:00 PM",
                    foto = "https://via.placeholder.com/300x300/4CAF50/FFFFFF?text=CM"
                ),
                Doctor(
                    nombre = "Dra. María González",
                    especialidad = "Pediatría",
                    experiencia = "10 años en cuidado infantil y vacunación",
                    disponibilidad = "Lunes a Sábado, 9:00 AM - 5:00 PM",
                    foto = "https://via.placeholder.com/300x300/2196F3/FFFFFF?text=MG"
                ),
                Doctor(
                    nombre = "Dr. Luis Rodríguez",
                    especialidad = "Traumatología",
                    experiencia = "12 años en cirugía ortopédica y medicina deportiva",
                    disponibilidad = "Martes a Sábado, 10:00 AM - 6:00 PM",
                    foto = "https://via.placeholder.com/300x300/FF9800/FFFFFF?text=LR"
                ),
                Doctor(
                    nombre = "Dra. Ana Flores",
                    especialidad = "Ginecología",
                    experiencia = "8 años en medicina reproductiva y salud femenina",
                    disponibilidad = "Lunes a Viernes, 7:00 AM - 3:00 PM",
                    foto = "https://via.placeholder.com/300x300/E91E63/FFFFFF?text=AF"
                ),
                Doctor(
                    nombre = "Dr. Roberto Silva",
                    especialidad = "Medicina General",
                    experiencia = "20 años en medicina preventiva y atención primaria",
                    disponibilidad = "Lunes a Viernes, 6:00 AM - 2:00 PM",
                    foto = "https://via.placeholder.com/300x300/9C27B0/FFFFFF?text=RS"
                )
            )
            
            doctorDao.insertDoctors(sampleDoctors)
        }
    }
}