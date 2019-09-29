package com.phizzard.es

import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version.Main
import de.flapdoodle.embed.process.runtime.Network

const val EMBEDDED_MONGO_HOST = "localhost"
const val EMBEDDED_MONGO_PORT = 12345

class EmbeddedMongoDBTestInstance {
    private val mongodStarter = MongodStarter.getDefaultInstance()
    private val mongodExecutable: MongodExecutable = mongodStarter.prepare(MongodConfigBuilder()
        .net(Net(EMBEDDED_MONGO_HOST, EMBEDDED_MONGO_PORT, Network.localhostIsIPv6()))
        .version(Main.PRODUCTION)
        .build())
    private var mongodProcess: MongodProcess? = null

    fun setUp() {
        mongodProcess = mongodExecutable.start()
        System.out.println("mongod process started: ${mongodProcess?.isProcessRunning}")
    }

    fun tearDown() {
        System.out.println("mongod process stopping...")
        mongodProcess?.stop()
        System.out.println("mongod process stopped: ${mongodProcess?.isProcessRunning}")
        mongodExecutable.stop()
    }
}