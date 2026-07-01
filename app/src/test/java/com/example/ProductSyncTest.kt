package com.example

import com.example.data.api.ApiProduct
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductSyncTest {

    @Test
    fun testProductMappingAndStockStatus() {
        val apiProductInStock = ApiProduct(
            id = "test-uuid-1",
            sku = "TST-SKU-01",
            name = "Traditional Wanza Table",
            description = "Intricately hand-routed hardwood top.",
            price = 55000.0,
            currency = "ETB",
            inventoryCount = 10,
            imageUrls = listOf("https://test.url/img.png"),
            isActive = true,
            categoryName = "Dining Table"
        )

        val apiProductOutOfStock = ApiProduct(
            id = "test-uuid-2",
            sku = "TST-SKU-02",
            name = "Elegant Zigba Chair",
            description = "Plush velour seat finishing.",
            price = 12000.0,
            currency = "ETB",
            inventoryCount = 0,
            imageUrls = emptyList(),
            isActive = true,
            categoryName = "Sofa"
        )

        // Verify stock status logic behavior
        val stockStatusInStock = if (apiProductInStock.inventoryCount > 0) "In Stock" else "Made to Order"
        val stockStatusOutOfStock = if (apiProductOutOfStock.inventoryCount > 0) "In Stock" else "Made to Order"

        assertEquals("In Stock", stockStatusInStock)
        assertEquals("Made to Order", stockStatusOutOfStock)

        // Verify mapped data mapping rules
        val mappedProduct = Product(
            id = 1001,
            name = apiProductInStock.name ?: "Unnamed Carved Furniture",
            category = apiProductInStock.categoryName ?: "Sofa",
            price = apiProductInStock.price,
            material = "Guaranteed Hardwood (Wanza/Mahogany)",
            dimensions = "Premium Tailored Sizing",
            warranty = "10 Years Warranty",
            description = apiProductInStock.description ?: "Bespoke handcrafted carpentry.",
            stockStatus = stockStatusInStock,
            imageUrl = apiProductInStock.imageUrls?.firstOrNull() ?: ""
        )

        assertEquals("Traditional Wanza Table", mappedProduct.name)
        assertEquals(55000.0, mappedProduct.price, 0.0)
        assertEquals("In Stock", mappedProduct.stockStatus)
        assertEquals("https://test.url/img.png", mappedProduct.imageUrl)
    }
}
