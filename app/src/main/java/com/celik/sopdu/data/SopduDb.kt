package com.celik.sopdu.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PeerEntity::class, MessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SopduDb : RoomDatabase() {

    abstract fun dao(): SopduDao

    companion object {
        @Volatile private var INSTANCE: SopduDb? = null

        fun get(context: Context): SopduDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SopduDb::class.java,
                    "sopdu.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN deliveryStatus TEXT NOT NULL DEFAULT 'RECEIVED'")
            }
        }
    }
}
