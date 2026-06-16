package com.expiryreminder.app.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(iconName: String): ImageVector = when (iconName) {
    "restaurant" -> Icons.Default.Restaurant
    "local_laundry_service" -> Icons.Default.LocalLaundryService
    "favorite" -> Icons.Default.Favorite
    "badge" -> Icons.Default.Badge
    "account_balance" -> Icons.Default.AccountBalance
    "stars" -> Icons.Default.Stars
    "devices" -> Icons.Default.Devices
    "home" -> Icons.Default.Home
    "pets" -> Icons.Default.Pets
    "flight" -> Icons.Default.Flight
    "work" -> Icons.Default.Work
    else -> Icons.Default.Category
}
