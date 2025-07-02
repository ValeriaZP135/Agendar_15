package com.tecsup.agendar_15.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.tecsup.agendar_15.data.database.dao.*
import com.tecsup.agendar_15.data.database.entities.*

@Database(
    entities = [Usuario::class, Curso::class, Evento::class, Tarea::class],
    version = 1,
    exportSchema = false
)
abstract class AgendarDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun cursoDao(): CursoDao
    abstract fun eventoDao(): EventoDao
    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var INSTANCE: AgendarDatabase? = null

        fun getDatabase(context: Context): AgendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgendarDatabase::class.java,
                    "agendar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}