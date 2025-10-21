package com.mars.essalureservamedica.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.converter.DateConverter
import com.mars.essalureservamedica.data.dao.*
import com.mars.essalureservamedica.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Doctor::class, Cita::class, Calificacion::class, Notificacion::class],
    version = 3, // O increméntalo si has hecho otros cambios de esquema. Si no, déjalo.
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
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.doctorDao(), context)
                    }
                }
            }
        }

        private suspend fun populateDatabase(doctorDao: DoctorDao, context: Context) {
            // Construye las URIs a tus imágenes locales.
            // Asegúrate de tener al menos 10 imágenes (dr1.png, dr2.png, etc.)
            // Se repetirán para llenar todos los slots.
            val photos = (1..10).map {
                val drawableId = context.resources.getIdentifier("dr$it", "drawable", context.packageName)
                "android.resource://${context.packageName}/$drawableId"
            }

            // --- INICIO DE CAMBIOS: LISTA DE DOCTORES AMPLIADA ---
            val sampleDoctors = listOf(
                // Cardiología (3)
                Doctor(nombre = "Dr. Carlos Mendoza", especialidad = "Cardiología", experiencia = "15 años en cardiología intervencionista.", disponibilidad = "Lunes a Viernes, 8:00 AM - 4:00 PM", foto = photos[0]),
                Doctor(nombre = "Dra. Isabel Reyes", especialidad = "Cardiología", experiencia = "12 años en ecocardiografías y pruebas de esfuerzo.", disponibilidad = "Martes a Sábado, 9:00 AM - 5:00 PM", foto = photos[1]),
                Doctor(nombre = "Dr. Alejandro Soto", especialidad = "Cardiología", experiencia = "18 años especializándose en arritmias.", disponibilidad = "Lunes, Miércoles y Viernes, 7:00 AM - 3:00 PM", foto = photos[2]),

                // Pediatría (3)
                Doctor(nombre = "Dra. María González", especialidad = "Pediatría", experiencia = "10 años en cuidado infantil y neonatología.", disponibilidad = "Lunes a Sábado, 9:00 AM - 5:00 PM", foto = photos[3]),
                Doctor(nombre = "Dr. Fernando Navarro", especialidad = "Pediatría", experiencia = "8 años en desarrollo infantil y nutrición.", disponibilidad = "Lunes a Viernes, 10:00 AM - 6:00 PM", foto = photos[4]),
                Doctor(nombre = "Dra. Lucía Jiménez", especialidad = "Pediatría", experiencia = "14 años en emergencias pediátricas.", disponibilidad = "Lunes a Jueves, 8:00 AM - 4:00 PM", foto = photos[5]),

                // Traumatología (3)
                Doctor(nombre = "Dr. Luis Rodríguez", especialidad = "Traumatología", experiencia = "12 años en cirugía ortopédica y deportiva.", disponibilidad = "Martes a Sábado, 10:00 AM - 6:00 PM", foto = photos[6]),
                Doctor(nombre = "Dra. Patricia Herrera", especialidad = "Traumatología", experiencia = "9 años en lesiones de columna y rodilla.", disponibilidad = "Lunes a Viernes, 8:00 AM - 4:00 PM", foto = photos[7]),
                Doctor(nombre = "Dr. Jorge Campos", especialidad = "Traumatología", experiencia = "15 años en reemplazo de articulaciones.", disponibilidad = "Miércoles a Domingo, 9:00 AM - 5:00 PM", foto = photos[8]),

                // Ginecología (3)
                Doctor(nombre = "Dra. Ana Flores", especialidad = "Ginecología", experiencia = "8 años en medicina reproductiva y salud femenina.", disponibilidad = "Lunes a Viernes, 7:00 AM - 3:00 PM", foto = photos[9]),
                Doctor(nombre = "Dra. Gabriela Ponce", especialidad = "Ginecología", experiencia = "11 años en obstetricia y cuidado prenatal.", disponibilidad = "Martes a Sábado, 11:00 AM - 7:00 PM", foto = photos[0]),
                Doctor(nombre = "Dr. Mateo Díaz", especialidad = "Ginecología", experiencia = "10 años en oncología ginecológica.", disponibilidad = "Lunes, Martes y Jueves, 8:00 AM - 5:00 PM", foto = photos[1]),

                // Medicina General (3)
                Doctor(nombre = "Dr. Roberto Silva", especialidad = "Medicina General", experiencia = "20 años en medicina preventiva y atención primaria.", disponibilidad = "Lunes a Viernes, 6:00 AM - 2:00 PM", foto = photos[2]),
                Doctor(nombre = "Dra. Carmen Ortiz", especialidad = "Medicina General", experiencia = "15 años en manejo de enfermedades crónicas.", disponibilidad = "Lunes a Sábado, 7:00 AM - 3:00 PM", foto = photos[3]),
                Doctor(nombre = "Dr. Ricardo Rojas", especialidad = "Medicina General", experiencia = "10 años en atención a adultos mayores.", disponibilidad = "Lunes a Viernes, 9:00 AM - 6:00 PM", foto = photos[4]),

                // Dermatología (3)
                Doctor(nombre = "Dra. Sofía Torres", especialidad = "Dermatología", experiencia = "9 años tratando enfermedades de la piel y estética.", disponibilidad = "Lunes, Miércoles y Viernes, 9:00 AM - 6:00 PM", foto = photos[5]),
                Doctor(nombre = "Dr. David Benítez", especialidad = "Dermatología", experiencia = "12 años en dermatología pediátrica y alergias.", disponibilidad = "Martes y Jueves, 10:00 AM - 7:00 PM", foto = photos[6]),
                Doctor(nombre = "Dra. Mónica Peña", especialidad = "Dermatología", experiencia = "7 años en cirugía dermatológica y láser.", disponibilidad = "Lunes a Viernes, 8:00 AM - 4:00 PM", foto = photos[7]),

                // Neurología (3)
                Doctor(nombre = "Dr. Javier Ríos", especialidad = "Neurología", experiencia = "14 años en trastornos del sistema nervioso y migrañas.", disponibilidad = "Martes y Jueves, 8:00 AM - 5:00 PM", foto = photos[8]),
                Doctor(nombre = "Dra. Valeria Medina", especialidad = "Neurología", experiencia = "10 años en enfermedades neurodegenerativas.", disponibilidad = "Lunes, Miércoles y Viernes, 8:00 AM - 4:00 PM", foto = photos[9]),
                Doctor(nombre = "Dr. Samuel Castro", especialidad = "Neurología", experiencia = "16 años en epilepsia y trastornos del sueño.", disponibilidad = "Martes a Sábado, 9:00 AM - 5:00 PM", foto = photos[0])
            )
            // --- FIN DE CAMBIOS ---

            doctorDao.insertDoctors(sampleDoctors)
        }
    }
}
