package com.example.wellme.presentation.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wellme.presentation.common.BottomSheetContent
import com.example.wellme.presentation.common.ScannerOverlay
import com.example.wellme.theme.WellMeTheme

@Composable
fun PaymentConfirmationScreen(
    state: SheetState,
    onClose: () -> Unit,
    onScan: (String, Long) -> Unit,
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
                    onDismiss = onClose
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
            state = SheetState.Processing,
            onClose = {},
            onScan = { _, _ -> }
        )
    }
}
