package dev.surovtsev.integration

import dev.surovtsev.integration.container.FtpContainer
import dev.surovtsev.integration.container.KafkaContainer
import dev.surovtsev.integration.container.PostgresContainer
import dev.surovtsev.integration.util.EventDataSource
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [BaseTest.ContextInitializer::class])
abstract class  BaseTest {

    class ContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.kafka.producer.bootstrap-servers=${KafkaContainer.getBootstrap()}",
                "spring.kafka.consumer.bootstrap-servers=${KafkaContainer.getBootstrap()}",
                "spring.datasource.url=${PostgresContainer.getJdbcUrl()}",
                "spring.datasource.username=${PostgresContainer.getJdbcUsername()}",
                "spring.datasource.password=${PostgresContainer.getJdbcPassword()}",
            )
        }
    }

    companion object {
        init {
            PostgresContainer.getInstance()
            KafkaContainer.getInstance()
            FtpContainer.getInstance()
            EventDataSource
        }
    }
}