package com.talsk.amadz.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talsk.amadz.domain.entity.SimInfo
import com.talsk.amadz.ui.theme.AmadzTheme

@Composable
fun SimSelectionDialog(
    sims: List<SimInfo>,
    onSimSelected: (SimInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose SIM",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sims.forEachIndexed { index, sim ->

                    val label = sim.displayName?.takeIf { it.isNotBlank() }
                        ?: "SIM ${sim.simSlotIndex + 1}"

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSimSelected(sim) }
                            .padding(vertical = 4.dp),

                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },

                        headlineContent = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },

                        supportingContent = {
                            Text(
                                text = "Slot ${sim.simSlotIndex + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    if (index != sims.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(start = 56.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SimSelectionDialogPreview() {
    AmadzTheme {
        SimSelectionDialog(
            sims = listOf(
                SimInfo("1", 0, "Personal"),
                SimInfo("2", 1, "Work"),
            ),
            onSimSelected = {},
            onDismiss = {}
        )
    }
}