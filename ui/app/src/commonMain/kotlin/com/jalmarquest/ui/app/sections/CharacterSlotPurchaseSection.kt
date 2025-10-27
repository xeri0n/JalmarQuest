package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.CharacterSlotEntitlement
import com.jalmarquest.core.model.IapProduct
import com.jalmarquest.core.model.ProductId
import com.jalmarquest.core.model.TransactionId
import com.jalmarquest.core.state.monetization.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Controller for character slot purchase UI.
 */
class CharacterSlotPurchaseController(
    private val scope: CoroutineScope,
    private val entitlementManager: EntitlementManager,
    private val glimmerWalletManager: GlimmerWalletManager,
    private val iapService: IapService
) {
    private val _purchaseResult = MutableStateFlow<PurchaseResultState?>(null)
    val purchaseResult: StateFlow<PurchaseResultState?> = _purchaseResult
    
    private val _restoreResult = MutableStateFlow<RestoreResultState?>(null)
    val restoreResult: StateFlow<RestoreResultState?> = _restoreResult
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    /**
     * Get all character slot statuses.
     */
    fun getAllSlots(): List<CharacterSlotStatus> {
        return entitlementManager.getAllCharacterSlots()
    }
    
    /**
     * Purchase a character slot.
     */
    fun purchaseSlot(product: IapProduct, slotNumber: Int) {
        scope.launch {
            _isProcessing.value = true
            _purchaseResult.value = null
            
            try {
                // Launch platform IAP flow
                val iapResult = iapService.launchPurchaseFlow(product)
                
                when (iapResult) {
                    is PurchaseResponse.Success -> {
                        // Process purchase through GlimmerWalletManager (which calls EntitlementManager)
                        val result = glimmerWalletManager.purchaseGlimmer(
                            product = product,
                            receiptData = iapResult.receiptData,
                            transactionId = iapResult.transactionId
                        )
                        
                        when (result) {
                            is PurchaseResult.Success -> {
                                // Acknowledge the purchase for non-consumables
                                iapService.acknowledgePurchase(iapResult.purchaseToken)
                                _purchaseResult.value = PurchaseResultState.Success(slotNumber)
                            }
                            is PurchaseResult.InvalidProduct -> {
                                _purchaseResult.value = PurchaseResultState.Error(result.reason)
                            }
                            is PurchaseResult.DuplicateTransaction -> {
                                _purchaseResult.value = PurchaseResultState.Error("Transaction already processed")
                            }
                        }
                    }
                    is PurchaseResponse.Cancelled -> {
                        _purchaseResult.value = PurchaseResultState.Error("Purchase cancelled")
                    }
                    is PurchaseResponse.Error -> {
                        _purchaseResult.value = PurchaseResultState.Error(iapResult.message)
                    }
                    is PurchaseResponse.AlreadyOwned -> {
                        _purchaseResult.value = PurchaseResultState.Error("Slot already owned")
                    }
                    is PurchaseResponse.NetworkError -> {
                        _purchaseResult.value = PurchaseResultState.Error("Network error. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _purchaseResult.value = PurchaseResultState.Error(e.message ?: "Unknown error")
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Restore previously purchased slots.
     */
    fun restorePurchases() {
        scope.launch {
            _isProcessing.value = true
            _restoreResult.value = null
            
            try {
                // Query platform IAP service for previous purchases
                val restoredPurchases = iapService.restorePurchases()
                
                // Convert to ReceiptData for EntitlementManager
                val receipts = restoredPurchases.map { purchase ->
                    ReceiptData(
                        productId = purchase.productId,
                        transactionId = TransactionId(purchase.transactionId),
                        purchaseTime = purchase.purchaseTimeMillis
                    )
                }
                
                // Restore entitlements
                val result = entitlementManager.restoreFromReceipts(receipts)
                when (result) {
                    is RestoreResult.Success -> {
                        _restoreResult.value = RestoreResultState.Success(result.restoredSlots.size)
                    }
                    is RestoreResult.Failure -> {
                        _restoreResult.value = RestoreResultState.Error(result.error)
                    }
                }
            } catch (e: Exception) {
                _restoreResult.value = RestoreResultState.Error(e.message ?: "Unknown error")
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Clear result notifications.
     */
    fun clearResults() {
        _purchaseResult.value = null
        _restoreResult.value = null
    }
}

sealed interface PurchaseResultState {
    data class Success(val slotNumber: Int) : PurchaseResultState
    data class Error(val message: String) : PurchaseResultState
}

sealed interface RestoreResultState {
    data class Success(val restoredCount: Int) : RestoreResultState
    data class Error(val message: String) : RestoreResultState
}

/**
 * Character slot purchase UI section.
 */
@Composable
fun CharacterSlotPurchaseSection(
    controller: CharacterSlotPurchaseController,
    modifier: Modifier = Modifier
) {
    val slots = remember { controller.getAllSlots() }
    val purchaseResult by controller.purchaseResult.collectAsState()
    val restoreResult by controller.restoreResult.collectAsState()
    val isProcessing by controller.isProcessing.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Character Slots",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Unlock additional character slots to manage multiple adventurers simultaneously.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Result notifications
        purchaseResult?.let { result ->
            PurchaseResultNotification(
                result = result,
                onDismiss = { controller.clearResults() }
            )
        }
        
        restoreResult?.let { result ->
            RestoreResultNotification(
                result = result,
                onDismiss = { controller.clearResults() }
            )
        }
        
        // Slot cards
        slots.forEach { slotStatus ->
            CharacterSlotCard(
                slotStatus = slotStatus,
                isProcessing = isProcessing,
                onPurchaseClick = { product ->
                    controller.purchaseSlot(product, slotStatus.slotNumber)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Restore purchases button
        OutlinedButton(
            onClick = { controller.restorePurchases() },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restore Purchases")
        }
        
        // Help text
        Text(
            text = "Purchased slots are permanent and will be restored when you sign in on a new device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun CharacterSlotCard(
    slotStatus: CharacterSlotStatus,
    isProcessing: Boolean,
    onPurchaseClick: (IapProduct) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (slotStatus.isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (slotStatus.isUnlocked) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slot info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Slot ${slotStatus.slotNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (slotStatus.isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (slotStatus.slotNumber == 1) {
                    Text(
                        text = "Free Slot",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (slotStatus.isUnlocked) {
                    Text(
                        text = "Purchased",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    slotStatus.product?.let { product ->
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Purchase button or status badge
            if (slotStatus.slotNumber == 1) {
                // Slot 1 is always free
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "FREE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            } else if (slotStatus.isUnlocked) {
                // Already purchased
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "OWNED",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Purchase button
                slotStatus.product?.let { product ->
                    Button(
                        onClick = { onPurchaseClick(product) },
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$${product.priceUsd}")
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseResultNotification(
    result: PurchaseResultState,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is PurchaseResultState.Success -> MaterialTheme.colorScheme.primaryContainer
                is PurchaseResultState.Error -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (result) {
                        is PurchaseResultState.Success -> Icons.Default.CheckCircle
                        is PurchaseResultState.Error -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (result) {
                        is PurchaseResultState.Success -> MaterialTheme.colorScheme.primary
                        is PurchaseResultState.Error -> MaterialTheme.colorScheme.error
                    }
                )
                
                Column {
                    Text(
                        text = when (result) {
                            is PurchaseResultState.Success -> "Purchase Successful"
                            is PurchaseResultState.Error -> "Purchase Failed"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (result) {
                            is PurchaseResultState.Success -> "Slot ${result.slotNumber} unlocked!"
                            is PurchaseResultState.Error -> result.message
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}

@Composable
private fun RestoreResultNotification(
    result: RestoreResultState,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is RestoreResultState.Success -> MaterialTheme.colorScheme.primaryContainer
                is RestoreResultState.Error -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (result) {
                        is RestoreResultState.Success -> Icons.Default.CheckCircle
                        is RestoreResultState.Error -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (result) {
                        is RestoreResultState.Success -> MaterialTheme.colorScheme.primary
                        is RestoreResultState.Error -> MaterialTheme.colorScheme.error
                    }
                )
                
                Column {
                    Text(
                        text = when (result) {
                            is RestoreResultState.Success -> "Restore Complete"
                            is RestoreResultState.Error -> "Restore Failed"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (result) {
                            is RestoreResultState.Success -> {
                                if (result.restoredCount == 0) {
                                    "No purchases found to restore"
                                } else {
                                    "${result.restoredCount} slot(s) restored"
                                }
                            }
                            is RestoreResultState.Error -> result.message
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}
