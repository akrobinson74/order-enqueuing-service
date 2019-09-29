package com.phizzard.es

import arrow.core.Either
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.mockk.every
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import kotlin.system.exitProcess

@ExtendWith(VertxExtension::class)
class VertxAppTest {
    init {
        registerJacksonModules()
    }

//    private var priorSecurityManager: SecurityManager? = null
    private val sqs = SQSContainer.instance

//    @BeforeEach
//    fun blockExit() {
//        priorSecurityManager = System.getSecurityManager()
//        System.setSecurityManager(NoExitSecurityManager)
//    }
//
//    @AfterEach
//    fun resetSecurityManager() {
//        System.setSecurityManager(priorSecurityManager)
//    }

    @Disabled
    @Test
    fun `main fails without args`(context: VertxTestContext) = runBlocking {
//        mockkStatic("kotlin.system.ProcessKt")
        every { exitProcess(1) } throws Exception("No so fast, bub!")

        val result = Either.catch { main(arrayOf()) }
        context.verify {
            result shouldBe instanceOf(Either.Left::class)
        }
        context.completeNow()
    }

    @Disabled
    @Test
    fun `main does not fail`(context: VertxTestContext) = runBlocking {
        val args = arrayOf("config-local.yml")
        val result = Either.catch { main(args) }
            .mapLeft {
                fail("main method call failed: ${it.localizedMessage}")
            }

        context.verify {
            result shouldBe instanceOf(Either.Right::class)
        }

        context.completeNow()
    }
}

object NoExitSecurityManager : SecurityManager() {
    override fun checkExit(status: Int) {
        throw SecurityException("No exit here, buddy!")
    }
}