package dev.surovtsev.integration.util

import java.io.FileInputStream
import java.util.*

object EnvProperties {
    private val envProps = Properties().also { it.load(FileInputStream("./src/test/resources/env.properties")) }

    fun getKafkaBroker(): String {
        return envProps["kafka_broker"].toString()
    }

    fun getKafkaBootstrap(): String {
        return envProps["kafka_bootstrap"].toString()
    }

    fun getJdbcDatabaseName(): String {
        return envProps["jdbc_database_name"].toString()
    }

    fun getJdbcUsername(): String {
        return envProps["jdbc_username"].toString()
    }

    fun getJdbcPassword(): String {
        return envProps["jdbc_password"].toString()
    }

    fun getFtpPassword(): String {
        return envProps["ftp_password"].toString()
    }

    fun getUsername(): String {
        return envProps["ftp_username"].toString()
    }
}