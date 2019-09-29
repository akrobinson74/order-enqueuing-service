package com.phizzard.es.models

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.phizzard.es.ORDER_WITH_STRINGS_FOR_DOUBLES
import com.phizzard.es.mapper
import com.phizzard.es.registerJacksonModules
import com.phizzard.es.serializationTestOrder
import com.phizzard.models.OrderStatus
import io.kotlintest.fail
import io.kotlintest.matchers.instanceOf
import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigInteger

class InboundOrderTests {
    init {
        registerJacksonModules()
    }

    private val jsonMonad =
        runBlocking { Either.catch { JsonObject(mapper.writeValueAsString(serializationTestOrder)) } }
    private val jsonObject = jsonMonad.getOrElse { JsonObject() }

    @Test
    fun `verify an InboundOrder with strings for Doubles can be parsed`() = runBlocking {
        val inboundOrder =
            Either.catch { jacksonObjectMapper().readValue<InboundOrder>(ORDER_WITH_STRINGS_FOR_DOUBLES) }
        inboundOrder shouldBe instanceOf(Either.Right::class)
    }

    @Test
    fun `object serializes as string`() {
        jsonMonad shouldBe instanceOf(Either.Right::class)
    }

    @Test
    fun `expected keys found in object as JsonObject`() {
        listOf(
            "id",
            "approvalCode",
            "buyerContact",
            "deliverToStore",
            "orderItems",
            "status",
            "store",
            "transactionDate"
        )
            .forEach { jsonObject.containsKey(it) shouldBe true }
        listOf("deliveryContact", "invoiceContact").forEach { jsonObject.getJsonObject(it) shouldBe null }
    }

    @Test
    fun `verify top-level properties`() {
        jsonObject.getString("approvalCode") shouldBeEqualIgnoringCase serializationTestOrder.approvalCode
        BigInteger.valueOf(jsonObject.getLong("id")) shouldBe serializationTestOrder.id
        OrderStatus.valueOf(jsonObject.getString("status")) shouldBe serializationTestOrder.status
    }

    @Test
    fun `check buyerContact`() {
        jsonObject.getJsonObject("buyerContact")?.let { contact ->
            val address = contact.getJsonObject("address")
            address shouldNotBe null
            contact.getString("email") shouldBe serializationTestOrder.buyerContact.email
            contact.getString("firstName") shouldBe serializationTestOrder.buyerContact.firstName
            contact.getString("lastName") shouldBe serializationTestOrder.buyerContact.lastName
            contact.getString("phoneNumber") shouldBe serializationTestOrder.buyerContact.phoneNumber
        }
            ?: fail("Missing required buyerContact Contact object")
    }

    @Suppress("UnsafeCast")
    @Test
    fun `check orderItems`() = runBlocking {
        val orderItemsArray = jsonObject.getJsonArray("orderItems")
        orderItemsArray shouldNotBe null
        assertTrue(orderItemsArray.size() >= 1)
        orderItemsArray.forEachIndexed { idx, obj ->
            val item = Either.catch { (obj as JsonObject).mapTo(OrderItem::class.java) }
                .getOrHandle { fail("Can't deserialize item as OrderItem: ${it.localizedMessage}") }
            item.articleDescription shouldBe serializationTestOrder.orderItems[idx].articleDescription
            item.currency shouldBe serializationTestOrder.orderItems[idx].currency
            item.grossPrice shouldBe serializationTestOrder.orderItems[idx].grossPrice
            item.gtin shouldBe serializationTestOrder.orderItems[idx].gtin
            item.quantity shouldBe serializationTestOrder.orderItems[idx].quantity
            item.supplierName shouldBe serializationTestOrder.orderItems[idx].supplierName
        }
    }

    @Test
    fun `check store`() {
        jsonObject.getJsonObject("store")?.let { store ->
            store.getValue("contactInfo") shouldBe serializationTestOrder.store.contactInfo
            store.getString("gln") shouldBe serializationTestOrder.store.gln
            store.getString("name") shouldBe serializationTestOrder.store.name
        }
            ?: fail("Missing required store StoreDetails object")
    }
}
