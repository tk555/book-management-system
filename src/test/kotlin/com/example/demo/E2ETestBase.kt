package com.example.demo

import org.flywaydb.core.Flyway
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager

abstract class E2ETestBase {
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

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @BeforeEach
    fun cleanUp() {
        DriverManager
            .getConnection(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password,
            ).use { connection ->
                val dsl = DSL.using(connection, SQLDialect.POSTGRES)
                dsl.execute("TRUNCATE TABLE book_authors, books, authors RESTART IDENTITY CASCADE")
            }
    }
}
