package dev.surovtsev.integration.base

import dev.surovtsev.integration.container.PostgresContainer
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

interface DbBasedTest {

    /**
     *
     * @param query SQL-query with splitholders for parameters.
     * @param parameters List of parameters for the request.
     * @param mapper Function for converting a row from ResultSet to the desired entity.
     * @return Found entity or null if the result is empty.
     */
    fun <T : Any> queryForEntity(
        query: String,
        parameters: List<Any> = emptyList(),
        mapper: (ResultSet) -> T
    ): T? {
        val jdbcUrl = PostgresContainer.getInstance().jdbcUrl
        val username = PostgresContainer.getInstance().username
        val password = PostgresContainer.getInstance().password

        DriverManager.getConnection(jdbcUrl, username, password).use { connection ->
            connection.prepareStatement(query).use { statement ->
                parameters.forEachIndexed { index, param ->
                    statement.setObject(index + 1, param)
                }
                statement.executeQuery().use { resultSet ->
                    return if (resultSet.next()) {
                        mapper(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }



    /**
     *
     * @param query SQL-query with splitholders for parameters.
     * @param parameters List of parameters for the request.
     * @param mapper Function for converting a row from ResultSet to the desired entity.
     * @return The list of entities corresponding to the request.
     */
    fun <T : Any> queryForList(
        query: String,
        parameters: List<Any> = emptyList(),
        mapper: (ResultSet) -> T
    ): List<T> {
        val result = mutableListOf<T>()
        val jdbcUrl = PostgresContainer.getInstance().jdbcUrl
        val username = PostgresContainer.getInstance().username
        val password = PostgresContainer.getInstance().password

        DriverManager.getConnection(jdbcUrl, username, password).use { connection ->
            connection.prepareStatement(query).use { statement ->
                parameters.forEachIndexed { index, param ->
                    statement.setObject(index + 1, param)
                }
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        result.add(mapper(resultSet))
                    }
                }
            }
        }
        return result
    }

    /**
     * Universal ResultSet function is essentially T using reflection.
     *
     * @param rs ResultSet instance with data.
     * @param clazz KClass of the required object.
     * @return Object T, filled in ResultSet.
     * @throws IllegalArgumentException if the primary constructor is not found.
     */
    fun <T : Any> mapResultSetToEntity(rs: ResultSet, clazz: KClass<T>): T {
        // Get the primary entity constructorи
        val constructor = clazz.primaryConstructor ?: throw IllegalArgumentException("No primary constructor found for ${clazz.simpleName}")

        // Configure the parameters of the design -> values obtained from ResultSet.
        val args = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            // Convert parameter name (camelCase) to column name (snake_case)
            val columnName = camelToSnake(param.name!!)
            // Get the value from ResultSet. If necessary, you can extend the logic of type tapping.
            val value = rs.getObject(columnName)
            args[param] = value
        }

        return constructor.callBy(args)
    }

    /**
     * Convert camelCase string to snake_case.
     */
    fun camelToSnake(str: String): String {
        return str.split("(?=[A-Z])".toRegex())
            .joinToString("_") { it.lowercase() }
    }
}