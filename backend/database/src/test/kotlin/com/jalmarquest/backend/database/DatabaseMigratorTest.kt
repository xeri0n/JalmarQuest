package com.jalmarquest.backend.database

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager

class DatabaseMigratorTest {
    @Test
    fun migrationsCreateTablesAndSeedData() {
        val migrator = DatabaseMigrator(
            DatabaseConfig(
                jdbcUrl = postgres.jdbcUrl,
                username = postgres.username,
                password = postgres.password
            )
        )
        migrator.migrate()

        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM lore_snippets").use { rs ->
                    assertTrue(rs.next())
                    val count = rs.getInt(1)
                    assertTrue(count >= 3, "Expected at least 3 seeded lore snippets")
                }
            }
        }
    }

    companion object {
        private val postgres = PostgreSQLContainer("postgres:15-alpine")

        @JvmStatic
        @BeforeAll
        fun setup() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            postgres.close()
        }
    }
}
