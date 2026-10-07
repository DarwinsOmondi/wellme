package com.example.wellme.presentation.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.presentation.common.BottomSheetContent
import com.example.wellme.presentation.common.ScannerOverlay
import com.example.wellme.theme.WellMeTheme

@Composable
fun PaymentConfirmationScreen(
    state: SheetState,
    onClose: () -> Unit,
    onScan: (String, Long) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        ScannerOverlay(
            onClose = onClose,
            onScan = onScan,
            showSimulateButton = state is SheetState.Idle
        )
        
        if (state !is SheetState.Idle) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomSheetContent(
                    state = state,
                    onDismiss = onClose,
                    onScan = onScan,
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentConfirmationScreenPreview() {
    WellMeTheme {
        PaymentConfirmationScreen(
            state = SheetState.Scanned(
                merchant = MerchantProfile(
                    merchantId = "campus_cafe",
                    businessName = "Campus Cafe",
                    discountTier = 0.15,
                    poolTargetInCents = 1000000L,
                    poolRaisedInCents = 500000L,
                    isVerified = true
                ),
                originalAmountInCents = 15000L,
                discountAppliedInCents = 2250L,
                netAmountInCents = 12750L
            ),
            onClose = {},
            onScan = { _, _ -> },
            onConfirm = {}
        )
    }
}
