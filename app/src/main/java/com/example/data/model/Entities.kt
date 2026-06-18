package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val status: String, // New, Contacted, Quoted, Won, Lost
    val source: String, // WhatsApp, Facebook, Telegram, LiveChat
    val requirements: String,
    val notes: String,
    val language: String, // English, Amharic, Afaan Oromo
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Sofa, Dining Table, Bed, Coffee Table, Office
    val price: Double,
    val material: String, // Wanza, Mahogany, Grar, MDF
    val dimensions: String,
    val warranty: String,
    val description: String,
    val stockStatus: String, // In Stock, Made to Order, Out of Stock
    val imageUrl: String = ""
) : Serializable

@Entity(tableName = "quotations")
data class Quotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int,
    val leadName: String,
    val productName: String,
    val dimensions: String,
    val material: String,
    val laborCost: Double,
    val transportCost: Double,
    val materialCost: Double,
    val subtotal: Double,
    val vat: Double, // 15% VAT
    val margin: Double, // Profit margin visualizer (e.g., 20%)
    val total: Double,
    val deliveryTimeEstimate: String,
    val terms: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int = 0,
    val channel: String, // WhatsApp, Facebook, Telegram, LiveChat
    val sender: String, // Customer, AI, Human Agent
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "language_configs")
data class LanguageConfig(
    @PrimaryKey val code: String, // "en", "am", "om"
    val name: String,             // "English", "Amharic", "Afaan Oromo"
    val isEnabled: Boolean = true,
    val systemPromptOverride: String = "", // Custom knowledge base instructions for this language
    val customGreeting: String = "",       // Custom greeting response
    val customFallback: String = ""        // Custom fallback when answer is unavailable
) : Serializable

@Entity(tableName = "album_categories")
data class AlbumCategory(
    @PrimaryKey val id: String, // e.g. "bedroom_sets"
    val name: String,
    val nameAm: String,
    val nameOm: String,
    val description: String
) : Serializable

@Entity(tableName = "product_albums")
data class ProductAlbum(
    @PrimaryKey val id: String, // e.g. "BS-001"
    val name: String,
    val category: String, // Bedroom Sets, Wardrobes, Kitchen Cabinets, TV Stands, Dining Sets, Office Furniture, Sofas, Coffee Tables, Reception Furniture, Custom Furniture Designs
    val designStyle: String, // e.g. "Modern", "Classic", "Contemporary", "Scandinavian"
    val descriptionAm: String,
    val descriptionOm: String,
    val descriptionEn: String,
    val dimensions: String,
    val materialOptions: String,
    val colorOptions: String,
    val estimatedProductionTime: String,
    val priceRangeLower: Double,
    val priceRangeUpper: Double,
    val popularityScore: Int,
    val tags: String,
    val imageUrls: String // Comma separated URLs
) : Serializable

@Entity(tableName = "customer_favorites")
data class CustomerFavorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerPhone: String,
    val albumId: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "customer_selections")
data class CustomerSelection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int,
    val albumId: String,
    val requirements: String = "",
    val budget: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "design_comparisons")
data class DesignComparison(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerPhone: String,
    val albumId1: String,
    val albumId2: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "album_analytics")
data class AlbumAnalytics(
    @PrimaryKey val albumId: String,
    val viewCount: Int = 0,
    val selectionCount: Int = 0,
    val favoriteCount: Int = 0
) : Serializable

