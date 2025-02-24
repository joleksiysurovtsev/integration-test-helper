package dev.surovtsev.integration.container

import dev.surovtsev.integration.util.EnvProperties
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.Network

class PostgresContainer private constructor(imageName: String) :
    PostgreSQLContainer<PostgresContainer>(DockerImageName.parse(imageName)) {

    companion object {
        @Volatile
        private var INSTANCE: PostgresContainer? = null
        private var JDBC_URL: String? = null
        private var JDBC_USERNAME: String? = null
        private var JDBC_PASSWORD: String? = null

        @Synchronized
        fun getInstance(network: Network? = dev.surovtsev.integration.container.Network.getInstance()): PostgresContainer {
            return if (INSTANCE == null) {
                var retry = 0
                do {
                    INSTANCE = try {
                        PostgresContainer(network)
                    } catch (e: ContainerLaunchException) {
                        retry++
                        null
                    }
                } while (retry != 3 && INSTANCE == null)
                INSTANCE as PostgresContainer
            } else {
                INSTANCE as PostgresContainer
            }
        }

        fun getJdbcUrl(): String {
            if (JDBC_URL == null) {
                JDBC_URL = getInstance().jdbcUrl
            }
            return JDBC_URL ?: throw NoSuchElementException("No value present for jdbcUrl")
        }

        fun getJdbcUsername(): String {
            if (JDBC_USERNAME == null) {
                JDBC_USERNAME = if (System.getenv("isExternalEnv").toBoolean()) {
                    EnvProperties.getJdbcUsername()
                } else {
                    getInstance().username
                }
            }
            return JDBC_USERNAME ?: throw NoSuchElementException("No value present for jdbc username")
        }

        fun getJdbcPassword(): String {
            if (JDBC_PASSWORD == null) {
                JDBC_PASSWORD = if (System.getenv("isExternalEnv").toBoolean()) {
                    EnvProperties.getJdbcPassword()
                } else {
                    getInstance().password
                }
            }
            return JDBC_PASSWORD ?: throw NoSuchElementException("No value present for jdbc password")
        }
    }

    private constructor(network: Network?) : this("postgres:14") {
        this.withNetwork(network)
            .withNetworkAliases(PostgresContainer::class.simpleName)
            .withDatabaseName(EnvProperties.getJdbcDatabaseName())
            .withUsername(EnvProperties.getJdbcUsername())
            .withPassword(EnvProperties.getJdbcPassword())
            .withExposedPorts(5432)
            .start()
    }
}