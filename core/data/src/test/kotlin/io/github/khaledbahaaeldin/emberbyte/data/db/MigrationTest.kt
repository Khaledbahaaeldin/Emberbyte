package io.github.khaledbahaaeldin.emberbyte.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    /**
     * The version-1 schema. COPY the `createSql` strings of the five tables from
     * core/data/schemas/.../1.json (replace `${TABLE_NAME}` by the table name) and the `identityHash` of 1.json.
     */
    private val v1Statements: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `total_minute` (`minuteStart` INTEGER NOT NULL, `network` INTEGER NOT NULL, `subscriptionId` INTEGER NOT NULL, `rxBytes` INTEGER NOT NULL, `txBytes` INTEGER NOT NULL, PRIMARY KEY(`minuteStart`, `network`, `subscriptionId`))",
        "CREATE TABLE IF NOT EXISTS `usage_hourly` (`hourStart` INTEGER NOT NULL, `uid` INTEGER NOT NULL, `network` INTEGER NOT NULL, `subscriptionId` INTEGER NOT NULL, `rxBytes` INTEGER NOT NULL, `txBytes` INTEGER NOT NULL, PRIMARY KEY(`hourStart`, `uid`, `network`, `subscriptionId`))",
        "CREATE TABLE IF NOT EXISTS `app_meta` (`uid` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `label` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`uid`))",
        "CREATE TABLE IF NOT EXISTS `coverage_gap` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fromAt` INTEGER NOT NULL, `toAt` INTEGER NOT NULL, `reason` TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS `counter_checkpoint` (`source` TEXT NOT NULL, `at` INTEGER NOT NULL, `rxBytes` INTEGER NOT NULL, `txBytes` INTEGER NOT NULL, `bootId` TEXT NOT NULL, PRIMARY KEY(`source`))",
        "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)",
        "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7c308347c508fc357ffa36c5ecfc51a3')",
    )

    private fun createVersion1(name: String) {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) { v1Statements.forEach(db::execSQL) }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                },
            ).build(),
        )
        helper.writableDatabase.use { db ->
            db.execSQL("INSERT INTO total_minute (minuteStart, network, subscriptionId, rxBytes, txBytes) VALUES (1000, 0, 3, 11, 22)")
        }
        helper.close()
    }

    @Test fun migrating_from_1_to_2_keeps_the_usage_rows_and_creates_the_plan_tables(): Unit = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        createVersion1(name)
        val room = Room.databaseBuilder(context, EmberbyteDatabase::class.java, name).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
        assertEquals(1, room.usageDao().minuteRows(0L, Long.MAX_VALUE).size)
        val id = room.planDao().putPlan(PlanEntity(name = "Main", subscriptionId = null, capBytes = 1, cycleJson = "{}", rolloverJson = "{}", archived = false))
        assertEquals(1L, id)
        room.planDao().putFired(AlertFiredEntity(planId = id, windowStart = 5L, percent = 80))
        assertEquals(80, room.planDao().firedPercent(id, 5L))
        room.close()
        context.deleteDatabase(name)
    }

    @Test fun migrating_from_1_all_the_way_to_3_works(): Unit = runBlocking {
        val name = "migration-1-3-test.db"
        context.deleteDatabase(name)
        createVersion1(name)
        val room = Room.databaseBuilder(context, EmberbyteDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        assertEquals(1, room.usageDao().minuteRows(0L, Long.MAX_VALUE).size)
        val planId = room.planDao().putPlan(PlanEntity(name = "Main", subscriptionId = null, capBytes = 1, cycleJson = "{}", rolloverJson = "{}", archived = false))
        assertEquals(1L, planId)
        val budgetId = room.insightDao().putBudget(AppBudgetEntity(packageName = "com.video", limitBytes = 5, period = "DAILY", planId = null, warnAtPercent = 80, enabled = true))
        assertEquals(1L, budgetId)
        room.close()
        context.deleteDatabase(name)
    }

    private fun createVersion3(name: String) {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(
                object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) { SchemaFile.statements(3).forEach(db::execSQL) }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                },
            ).build(),
        )
        helper.writableDatabase.use { db ->
            db.execSQL("INSERT INTO plan (name, subscriptionId, capBytes, cycleJson, rolloverJson, archived) VALUES ('Main', NULL, 5000, '{}', '{}', 0)")
            db.execSQL("INSERT INTO plan (name, subscriptionId, capBytes, cycleJson, rolloverJson, archived) VALUES ('Work', 3, 10000, '{}', '{}', 0)")
            db.execSQL("INSERT INTO plan (name, subscriptionId, capBytes, cycleJson, rolloverJson, archived) VALUES ('Archived', 1, 2000, '{}', '{}', 1)")

            db.execSQL("INSERT INTO total_minute (minuteStart, network, subscriptionId, rxBytes, txBytes) VALUES (1000, 0, 3, 11, 22)")
            db.execSQL("INSERT INTO usage_hourly (hourStart, uid, network, subscriptionId, rxBytes, txBytes) VALUES (1000, 1001, 0, 3, 11, 22)")
            db.execSQL("INSERT INTO app_meta (uid, packageName, label, updatedAt) VALUES (1001, 'com.example', 'Example', 500)")
            db.execSQL("INSERT INTO coverage_gap (fromAt, toAt, reason) VALUES (100, 200, 'reboot')")
            db.execSQL("INSERT INTO counter_checkpoint (source, at, rxBytes, txBytes, bootId) VALUES ('wlan0', 100, 10, 20, 'boot1')")
            db.execSQL("INSERT INTO add_on (planId, label, bytes, validFrom, validUntil) VALUES (1, 'Extra', 100, 0, 1000)")
            db.execSQL("INSERT INTO free_rule (planId, label, daysMask, startMinute, endMinute, packageName) VALUES (1, 'FreeNight', 127, 0, 360, NULL)")
            db.execSQL("INSERT INTO alert_rule (planId, thresholdPercent, enabled) VALUES (1, 80, 1)")
            db.execSQL("INSERT INTO alert_fired (planId, windowStart, percent) VALUES (1, 1000, 80)")
            db.execSQL("INSERT INTO app_budget (packageName, limitBytes, period, planId, warnAtPercent, enabled) VALUES ('com.video', 5, 'DAILY', 1, 80, 1)")
            db.execSQL("INSERT INTO spike_event (packageName, windowStart, detectedAt, windowMinutes, bytes, baselineBytes, multiple, dismissed) VALUES ('com.video', 1, 2, 120, 3, 4, 5.0, 0)")
            db.execSQL("INSERT INTO budget_state (budgetId, periodStart, state) VALUES (1, 1000, 'OK')")
            db.execSQL("INSERT INTO spike_notice (packageName, notifiedAt) VALUES ('com.video', 1000)")
        }
        helper.close()
    }

    @Test fun migration_3_to_4_keeps_every_plan_row_and_defaults_to_phone_and_unpinned(): Unit = runBlocking {
        val name = "migration-3-4-plans.db"
        context.deleteDatabase(name)
        createVersion3(name)
        val room = Room.databaseBuilder(context, EmberbyteDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        val plan1 = room.planDao().plan(1)!!
        assertEquals("Main", plan1.name)
        assertEquals(null, plan1.subscriptionId)
        assertEquals(5000L, plan1.capBytes)
        assertEquals(false, plan1.archived)
        assertEquals("phone", plan1.firstCycleMode)
        assertEquals(null, plan1.firstCycleBytes)
        assertEquals(null, plan1.firstCycleAt)
        assertEquals(null, plan1.firstCycleStart)
        assertEquals(false, plan1.pinned)

        val plan2 = room.planDao().plan(2)!!
        assertEquals("Work", plan2.name)
        assertEquals(3, plan2.subscriptionId)
        assertEquals(10000L, plan2.capBytes)
        assertEquals(false, plan2.archived)
        assertEquals("phone", plan2.firstCycleMode)
        assertEquals(false, plan2.pinned)

        val plan3 = room.planDao().plan(3)!!
        assertEquals("Archived", plan3.name)
        assertEquals(1, plan3.subscriptionId)
        assertEquals(2000L, plan3.capBytes)
        assertEquals(true, plan3.archived)
        assertEquals("phone", plan3.firstCycleMode)
        assertEquals(false, plan3.pinned)

        room.close()
        context.deleteDatabase(name)
    }

    @Test fun migration_3_to_4_keeps_all_other_tables_row_counts(): Unit = runBlocking {
        val name = "migration-3-4-tables.db"
        context.deleteDatabase(name)
        createVersion3(name)
        val room = Room.databaseBuilder(context, EmberbyteDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        assertEquals(1, room.usageDao().minuteRows(0L, Long.MAX_VALUE).size)
        assertEquals(1, room.usageDao().hourlyRows(0L, Long.MAX_VALUE).size)
        assertEquals(80, room.planDao().firedPercent(1, 1000L))
        room.close()
        context.deleteDatabase(name)
    }

    @Test fun schema_4_validates_after_migration(): Unit = runBlocking {
        val name = "migration-3-4-schema.db"
        context.deleteDatabase(name)
        createVersion3(name)
        val room = Room.databaseBuilder(context, EmberbyteDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        assertEquals(4, room.openHelper.writableDatabase.version)
        room.close()
        context.deleteDatabase(name)
    }

    @Test fun a_fresh_install_creates_version_2_directly() = runBlocking {
        val room = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
        val id = room.planDao().putPlan(PlanEntity(name = "Main", subscriptionId = 3, capBytes = 5, cycleJson = "{}", rolloverJson = "{}", archived = false))
        val back = room.planDao().plan(id)!!
        assertEquals(3, back.subscriptionId)
        assertEquals(5L, back.capBytes)
        room.close()
    }

    @Test fun a_fresh_install_creates_version_3_directly() = runBlocking {
        val room = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
        val id = room.planDao().putPlan(PlanEntity(name = "Main", subscriptionId = 3, capBytes = 5, cycleJson = "{}", rolloverJson = "{}", archived = false))
        val back = room.planDao().plan(id)!!
        assertEquals(3, back.subscriptionId)
        assertEquals(5L, back.capBytes)
        val budgetId = room.insightDao().putBudget(AppBudgetEntity(packageName = "com.video", limitBytes = 5, period = "DAILY", planId = null, warnAtPercent = 80, enabled = true))
        assertEquals(1L, budgetId)
        room.close()
    }
}

