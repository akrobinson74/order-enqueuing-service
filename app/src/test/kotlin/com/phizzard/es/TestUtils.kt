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
  "deliveryContact": {
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
  "invoiceContact": {
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
      "id": 123467890,
      "brandName": "string",
      "productDetails": [
        {
          "id": 123467890,
          "productAttributes": [
            {
              "group": "DIMENSIONS",
              "name": "COLOR",
              "value": "peuce"
            }
          ]
        }
      ],
      "productVariantDetails": [
        {
          "currency": "EUR",
          "priceType": "sale",
          "productVariantId": 123467890,
          "productVariantAttributes": [
            {
              "group": "DIMENSIONS",
              "name": "COLOR",
              "value": "peuce"
            }
          ],
          "storeDetails": {
            "contactInfo": {
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
            "id": 123467890,
            "name": "FootLocker",
            "type": "Sporting Goods"
          }
        }
      ],
      "quantity": 2,
      "supplierName": "kjus",
      "value": 19.95
    }
  ],
  "status": "PROCESSING",
  "storeDetails": {
    "contactInfo": {
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
    "id": 123467890,
    "name": "FootLocker",
    "type": "Sporting Goods"
  },
  "uuid": "AF504EC2-4005-11E9-B16F-2436AB45578C"
}
"""

val mapper = jacksonObjectMapper()
val sampleOrderJson = jacksonObjectMapper().readValue<InboundOrder>(SAMPLE_ORDER)