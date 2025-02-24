package dev.surovtsev.integration.container

import dev.surovtsev.integration.util.EnvProperties
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.Network

class FtpContainer private constructor(imageName: String) : GenericContainer<FtpContainer>(imageName) {

    companion object {
        @Volatile
        private var INSTANCE: FtpContainer? = null
        const val HOST: String = "localhost"
        private val FTP_USER_NAME: String = EnvProperties.getUsername()
        private val FTP_USER_PASS: String = EnvProperties.getFtpPassword()
        const val FTP_CONTAINER_PORT: Int = 21
        const val FTP_DATA_PORT: Int = 20
        const val FTP_USER_HOME: String = "/home/username"

        @Synchronized
        fun getInstance(network:  Network? = dev.surovtsev.integration.container.Network.getInstance()): FtpContainer =
            INSTANCE ?: FtpContainer(network).also { INSTANCE = it }
    }

    private constructor(network: Network?) : this(
        when (System.getProperty("os.arch")) {
            "aarch64" -> "zhabba/pure-ftpd-arm64:latest"
            else -> "stilliard/pure-ftpd:hardened"
        }

    ) {
        this.apply {
            (30000..30099).forEach { addFixedExposedPort(it, it) }
            addFixedExposedPort(FTP_CONTAINER_PORT, FTP_CONTAINER_PORT)
            addFixedExposedPort(FTP_DATA_PORT, FTP_DATA_PORT)
        }
            .withNetwork(network)
            .withNetworkAliases(this::class.simpleName)
            .withEnv("PUBLICHOST", HOST)
            .withEnv("FTP_USER_NAME", FTP_USER_NAME)
            .withEnv("FTP_USER_PASS", FTP_USER_PASS)
            .withEnv("FTP_USER_HOME", FTP_USER_HOME)
            .waitingFor(Wait.forListeningPort())
            .start()
    }
}
