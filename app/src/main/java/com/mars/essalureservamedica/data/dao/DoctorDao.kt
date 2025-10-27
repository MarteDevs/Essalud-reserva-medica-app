package com.mars.essalureservamedica.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mars.essalureservamedica.data.entity.Doctor

@Dao
interface DoctorDao {
    
    @Query("SELECT * FROM doctores")
    fun getAllDoctors(): LiveData<List<Doctor>>
    
    @Query("SELECT * FROM doctores")
    suspend fun getAllDoctorsSync(): List<Doctor>
    
    @Query("SELECT * FROM doctores WHERE id = :doctorId")
    suspend fun getDoctorById(doctorId: Int): Doctor?
    
    @Query("SELECT * FROM doctores WHERE especialidad = :especialidad")
    fun getDoctorsByEspecialidad(especialidad: String): LiveData<List<Doctor>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doctor: Doctor): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<Doctor>)
    
    @Update
    suspend fun update(doctor: Doctor)
    
    @Delete
    suspend fun delete(doctor: Doctor)
    
    @Query("DELETE FROM doctores WHERE id = :doctorId")
    suspend fun deleteDoctorById(doctorId: Int)
    
    @Query("SELECT COUNT(*) FROM doctores")
    suspend fun getDoctorCount(): Int
}