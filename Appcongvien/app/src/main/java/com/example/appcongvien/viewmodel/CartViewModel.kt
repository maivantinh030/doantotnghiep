package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appcongvien.data.model.UserVoucherDTO
import com.example.appcongvien.screen.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedVoucher = MutableStateFlow<String?>(null)
    val selectedVoucher: StateFlow<String?> = _selectedVoucher.asStateFlow()

    private val _selectedUserVoucher = MutableStateFlow<UserVoucherDTO?>(null)
    val selectedUserVoucher: StateFlow<UserVoucherDTO?> = _selectedUserVoucher.asStateFlow()

    // Computed values
    val itemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    val subtotal: Int
        get() = _cartItems.value.sumOf { it.totalPrice }

    val totalSaved: Int
        get() = _cartItems.value.sumOf { it.savedAmount }

    fun addToCart(gameId: String, gameName: String, pricePerTurn: Int, discount: Int = 0) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.gameId == gameId }

        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentItems.add(
                CartItem(
                    gameId = gameId,
                    gameName = gameName,
                    pricePerTurn = pricePerTurn,
                    discount = discount,
                    quantity = 1
                )
            )
        }

        _cartItems.value = currentItems
    }

    fun updateQuantity(gameId: String, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.gameId == gameId }

        if (index != -1) {
            if (quantity <= 0) {
                currentItems.removeAt(index)
            } else {
                currentItems[index] = currentItems[index].copy(quantity = quantity)
            }
            _cartItems.value = currentItems
        }
    }

    fun removeItem(gameId: String) {
        _cartItems.value = _cartItems.value.filter { it.gameId != gameId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedVoucher.value = null
        _selectedUserVoucher.value = null
    }

    fun selectVoucher(voucherCode: String?) {
        _selectedVoucher.value = voucherCode
    }

    fun selectUserVoucher(userVoucher: UserVoucherDTO?) {
        _selectedUserVoucher.value = userVoucher
        _selectedVoucher.value = userVoucher?.voucher?.code
    }

    fun isInCart(gameId: String): Boolean {
        return _cartItems.value.any { it.gameId == gameId }
    }

    fun getItemQuantity(gameId: String): Int {
        return _cartItems.value.find { it.gameId == gameId }?.quantity ?: 0
    }
}
