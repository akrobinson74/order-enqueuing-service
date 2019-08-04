package com.phizzard.es.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.phizzard.es.ORDER_WITH_STRINGS_FOR_DOUBLES
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test

class InboundOrderfTest {
    @Test
    fun `verify an InboundOrder with strings for Doubles can be parsed`() {
        val inboundOrder = jacksonObjectMapper().readValue<InboundOrder>(ORDER_WITH_STRINGS_FOR_DOUBLES)
        assertThat(inboundOrder.orderItems[0].grossPrice is Double, equalTo(true))
    }
}