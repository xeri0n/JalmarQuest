package com.jalmarquest.backend.database

import org.flywaydb.core.Flyway
import org.postgresql.ds.PGSimpleDataSource

/**
 * Applies SQL migrations located under classpath:db/migration.
 */
class DatabaseMigrator(
    private val config: DatabaseConfig,
    private val migrationLocations: List<String> = listOf("classpath:db/migration")
) {
    fun migrate(): Int {
        val dataSource = PGSimpleDataSource().apply {
            setURL(config.jdbcUrl)
            user = config.username
            password = config.password
        }
        val flyway = Flyway.configure()
            .locations(*migrationLocations.toTypedArray())
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
        val result = flyway.migrate()
        return result.migrationsExecuted
    }
}
