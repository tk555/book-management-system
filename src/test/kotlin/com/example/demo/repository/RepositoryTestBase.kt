package com.example.demo.repository

import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.DriverManager

abstract class RepositoryTestBase {
    companion object {
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:18")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")

        init {
            postgres.start()
            Flyway
                .configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .load()
                .migrate()
        }
    }

    protected lateinit var connection: Connection
    protected lateinit var dsl: DSLContext

    @BeforeEach
    fun setUp() {
        connection =
            DriverManager.getConnection(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password,
            )
        dsl = DSL.using(connection, SQLDialect.POSTGRES)
        cleanupTables()
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun cleanupTables() {
        dsl.execute("TRUNCATE TABLE book_authors, books, authors RESTART IDENTITY CASCADE")
    }
}
