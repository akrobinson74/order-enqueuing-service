package com.phizzard.es

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.phizzard.es.models.InboundOrder

const val SAMPLE_ADDRESS = """
{
  "id": 123467890,
  "city": "Berlin",
  "country": "Germany",
  "federalState": "Berlin",
  "latitude": 52.431181,
  "longitude": 13.538744,
  "street": "Am Studio",
  "streetNumber": "2a",
  "zipCode": "12489"
}
"""
const val SAMPLE_ORDER = """
{
  "id": 123467890,
  "approvalCode": "666",
  "buyerContact": {
    "address": {
      "id": 123467890,
      "city": "Berlin",
      "country": "Germany",
      "federalState": "Berlin",
      "latitude": 52.431181,
      "longitude": 13.538744,
      "street": "Am Studio",
      "streetNumber": "2a",
      "zipCode": "12489"
    },
    "email": "michael.mente@phizzard.com",
    "firstName": "Michael",
    "lastName": "Mente",
    "phoneNumber": "49123456789910"
  },
  "orderItems": [
    {
      "articleDescription": "Ladies Kjus T-shirt",
      "currency": "CHF",
      "gtin": "07612997688944",
      "grossPrice": 64.07,
      "netPrice": 69.0,
      "quantity": 1,
      "supplierName": "kjus"
    }
  ],
  "status": "PROCESSING",
  "store": {
    "name": "Pais",
    "storeId": "200742"
  }
}
"""

val mapper = jacksonObjectMapper()
val sampleOrderJson = jacksonObjectMapper().readValue<InboundOrder>(SAMPLE_ORDER)