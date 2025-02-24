package dev.surovtsev.integration.container

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

class KafkaContainer private constructor(imageName: String) : KafkaContainer(DockerImageName.parse(imageName)) {
    private constructor(network: Network?) : this("confluentinc/cp-kafka:7.2.2") {
        this
            .withNetwork(network)
            .withNetworkAliases(KafkaContainer::class.simpleName)
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:29092,BROKER://:9092")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,BROKER:PLAINTEXT")
            .start()
    }

    companion object {
        @Volatile
        private var INSTANCE: KafkaContainer? = null
        private var BROKER: String? = null
        private var BOOTSTRAP: String? = null

        @Synchronized
        fun getInstance(network: Network? = dev.surovtsev.integration.container.Network.getInstance()): KafkaContainer =
            INSTANCE ?: KafkaContainer(network).also { INSTANCE = it }

        fun getBroker(): String {
            if (BROKER == null) {
                BROKER = getInstance().networkAliases.first()
            }
            return BROKER ?: throw NoSuchElementException("No value present for kafka broker url")
        }

        fun getBootstrap(): String {
            if (BOOTSTRAP == null) {
                BOOTSTRAP = getInstance().bootstrapServers
            }
            return BOOTSTRAP ?: throw NoSuchElementException("No value present for kafka bootstrap server url")
        }
    }
}
