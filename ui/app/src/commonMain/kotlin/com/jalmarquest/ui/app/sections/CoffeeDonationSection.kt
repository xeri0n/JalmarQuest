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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.IapProduct
import com.jalmarquest.core.model.IapProductCatalog
import com.jalmarquest.core.state.monetization.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Controller for Coffee Creator Donation UI.
 * Part of Alpha 2.2 Phase 5B: Coffee IAP Implementation
 */
class CoffeeDonationController(
    private val scope: CoroutineScope,
    private val glimmerWalletManager: GlimmerWalletManager,
    private val iapService: com.jalmarquest.core.state.monetization.IIapService
) {
    private val _donationResult = MutableStateFlow<DonationResultState?>(null)
    val donationResult: StateFlow<DonationResultState?> = _donationResult
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    /**
     * Check if coffee has already been purchased.
     */
    fun hasAlreadyDonated(): Boolean {
        return glimmerWalletManager.playerState.value.playerSettings.hasPurchasedCreatorCoffee
    }
    
    /**
     * Purchase coffee donation.
     */
    fun purchaseCoffee() {
        scope.launch {
            _isProcessing.value = true
            _donationResult.value = null
            
            try {
                val product = IapProductCatalog.CREATOR_COFFEE
                
                // Launch platform IAP flow
                val iapResult = iapService.launchPurchaseFlow(product)
                
                when (iapResult) {
                    is PurchaseResponse.Success -> {
                        // Process purchase through GlimmerWalletManager
                        val result = glimmerWalletManager.purchaseGlimmer(
                            product = product,
                            receiptData = iapResult.receiptData,
                            transactionId = iapResult.transactionId
                        )
                        
                        when (result) {
                            is PurchaseResult.Success -> {
                                // Acknowledge the purchase for non-consumables
                                iapService.acknowledgePurchase(iapResult.purchaseToken)
                                _donationResult.value = DonationResultState.Success
                            }
                            is PurchaseResult.InvalidProduct -> {
                                _donationResult.value = DonationResultState.Error(result.reason)
                            }
                            is PurchaseResult.DuplicateTransaction -> {
                                _donationResult.value = DonationResultState.Error("coffee_donation_error_duplicate")
                            }
                        }
                    }
                    is PurchaseResponse.Cancelled -> {
                        _donationResult.value = DonationResultState.Cancelled
                    }
                    is PurchaseResponse.Error -> {
                        _donationResult.value = DonationResultState.Error(iapResult.message)
                    }
                    is PurchaseResponse.AlreadyOwned -> {
                        _donationResult.value = DonationResultState.Error("coffee_donation_error_already_owned")
                    }
                    is PurchaseResponse.NetworkError -> {
                        _donationResult.value = DonationResultState.Error("coffee_donation_error_network")
                    }
                }
            } catch (e: Exception) {
                _donationResult.value = DonationResultState.Error("coffee_donation_error_unexpected:${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Clear donation result message.
     */
    fun clearDonationResult() {
        _donationResult.value = null
    }
}

/**
 * Donation result states.
 */
sealed class DonationResultState {
    data object Success : DonationResultState()
    data object Cancelled : DonationResultState()
    data class Error(val message: String) : DonationResultState()
}

/**
 * Main Coffee Donation Section UI.
 * 
 * This screen allows players to support the developer with a one-time $2.99 coffee donation,
 * which unlocks special thank-you rewards from the Exhausted Coder NPC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeDonationSection(
    controller: CoffeeDonationController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val donationResult by controller.donationResult.collectAsState()
    val isProcessing by controller.isProcessing.collectAsState()
    val alreadyDonated = controller.hasAlreadyDonated()
    
    // Show success dialog
    donationResult?.let { result ->
        when (result) {
            is DonationResultState.Success -> {
                AlertDialog(
                    onDismissRequest = { controller.clearDonationResult() },
                    title = { Text(stringResource(MR.strings.coffee_donation_success_title)) },
                    text = {
                        Text(stringResource(MR.strings.coffee_donation_success_message))
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            controller.clearDonationResult()
                            onBack()
                        }) {
                            Text(stringResource(MR.strings.coffee_donation_success_button))
                        }
                    }
                )
            }
            is DonationResultState.Error -> {
                AlertDialog(
                    onDismissRequest = { controller.clearDonationResult() },
                    title = { Text(stringResource(MR.strings.coffee_donation_error_title)) },
                    text = { 
                        // Handle error message localization
                        val message = when {
                            result.message == "coffee_donation_error_duplicate" -> 
                                stringResource(MR.strings.coffee_donation_error_duplicate)
                            result.message == "coffee_donation_error_already_owned" -> 
                                stringResource(MR.strings.coffee_donation_error_already_owned)
                            result.message == "coffee_donation_error_network" -> 
                                stringResource(MR.strings.coffee_donation_error_network)
                            result.message.startsWith("coffee_donation_error_unexpected:") -> {
                                val errorDetail = result.message.substringAfter(":")
                                stringResource(MR.strings.coffee_donation_error_unexpected, errorDetail)
                            }
                            else -> result.message
                        }
                        Text(message)
                    },
                    confirmButton = {
                        TextButton(onClick = { controller.clearDonationResult() }) {
                            Text(stringResource(MR.strings.coffee_donation_error_button))
                        }
                    }
                )
            }
            is DonationResultState.Cancelled -> {
                // Silent dismissal for cancellations
                LaunchedEffect(Unit) {
                    controller.clearDonationResult()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.coffee_donation_topbar_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = stringResource(MR.strings.coffee_donation_topbar_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coffee icon/header
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = stringResource(MR.strings.coffee_donation_icon_description),
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Title
            Text(
                text = stringResource(MR.strings.coffee_donation_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(MR.strings.coffee_donation_subtitle),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = stringResource(MR.strings.coffee_donation_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = stringResource(MR.strings.coffee_donation_rewards_header),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    RewardItem(
                        icon = Icons.Default.Star,
                        text = stringResource(MR.strings.coffee_donation_reward_shiny)
                    )
                    
                    RewardItem(
                        icon = Icons.Default.AccountCircle,
                        text = stringResource(MR.strings.coffee_donation_reward_cosmetic)
                    )
                    
                    RewardItem(
                        icon = Icons.Default.Chat,
                        text = stringResource(MR.strings.coffee_donation_reward_dialogue)
                    )
                    
                    RewardItem(
                        icon = Icons.Default.Favorite,
                        text = stringResource(MR.strings.coffee_donation_reward_feelgood)
                    )
                }
            }
            
            // Meta-humor quote from Exhausted Coder
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(MR.strings.coffee_donation_quote_attribution),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = stringResource(MR.strings.coffee_donation_quote_attribution),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    Text(
                        text = stringResource(MR.strings.coffee_donation_quote_text),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Purchase button
            if (alreadyDonated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(MR.strings.coffee_donation_already_donated_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(MR.strings.coffee_donation_already_donated_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(MR.strings.coffee_donation_already_donated_message),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = { controller.purchaseCoffee() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(MR.strings.coffee_donation_button_donate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Text(
                    text = stringResource(MR.strings.coffee_donation_button_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // Fine print
            Text(
                text = stringResource(MR.strings.coffee_donation_fine_print),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Reward item row component.
 */
@Composable
private fun RewardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
