package com.example.taliworkouthelper.partner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PartnerListScreen(partners: List<Partner>, onConnect: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        partners.forEach { p ->
            Text(
                text = p.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConnect(p.id) }
                    .padding(8.dp)
            )
        }
        Button(onClick = { /* no-op */ }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Refresh")
        }
    }
}
