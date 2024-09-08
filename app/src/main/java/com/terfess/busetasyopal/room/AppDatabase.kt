package com.terfess.busetasyopal.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.modelos_dato.DatoFrecuencia
import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta
import com.terfess.busetasyopal.room.model.Coordinate
import com.terfess.busetasyopal.room.model.Route
import com.terfess.busetasyopal.room.model.Schedule
import com.terfess.busetasyopal.room.model.Version

@Dao
interface RouteDao {
    @Query("SELECT * FROM route WHERE enabled = 1")
    fun getRoutesActives(): List<Route>

    @Query("SELECT id_route, frec_mon_fri, frec_sat, frec_sun_holi FROM route WHERE id_route = :idRuta")
    fun getFrequencieRoute(idRuta: Int): DatoFrecuencia

    @Query("SELECT id_route FROM route WHERE enabled = 1")
    fun getAllIdsRoute(): List<Int>

    @Query("SELECT sites FROM route WHERE id_route = :idRoute")
    fun getSimpleSitesRoute(idRoute: Int): String

    @Query("SELECT sites_extended FROM route WHERE id_route = :idRoute")
    fun getSiteExtendedRoute(idRoute: Int): String

    @Query("SELECT id_route, sites_extended FROM route WHERE enabled = 1")
    fun getAllSitesExtended(): List<DatosPrimariosRuta>

    @Insert
    fun inserNewRoute(route: Route)

    //at update (delete and re write)
    @Query("DELETE FROM route")
    fun deleteRoutesTable()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule WHERE id_route = :idRuta AND period = :period")
    fun getSchedule(idRuta: Int, period: String): Schedule

    @Transaction
    suspend fun insertSchedules(
        schedules: List<Schedule>
    ) {
        schedules.forEach { schedule ->
            insertSchedule(schedule)
        }
    }

    @Insert
    fun insertSchedule(schedule: Schedule)

}

@Dao
interface CoordinateDao {
    @Query("SELECT * FROM coordinate WHERE type_path = :path")
    fun getAllCoordinates(path: String): List<Coordinate>

    @Query("SELECT * FROM coordinate WHERE id_route = :idRoute AND type_path = :path")
    fun getCoordRoutePath(idRoute: Int, path: String): List<Coordinate>

    @Transaction
    fun insertCoordinate(
        type_path: String,
        id_route: Int,
        listCoord: List<LatLng>
    ) {

        val coordinates = listCoord.map { iterator ->
            Coordinate(
                type_path = type_path,
                id_route = id_route,
                latitude = iterator.latitude,
                longitude = iterator.longitude
            )
        }
        insertCoordinatesPrivate(coordinates)
    }

    @Insert
    fun insertCoordinatesPrivate(coordinate: List<Coordinate>)
}

@Dao
interface VersionDao {

    @Query("SELECT num_version FROM version WHERE id_version = 1")
    fun getVersion(): Int

    @Update
    fun updateVersion(version: Version)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVersion(version: Version)
}

@Database(
    entities = [
        Route::class,
        Coordinate::class,
        Schedule::class,
        Version::class
    ], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun coordinateDao(): CoordinateDao
    abstract fun versionDao(): VersionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database_busetas" // Nombre de la base de datos
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}