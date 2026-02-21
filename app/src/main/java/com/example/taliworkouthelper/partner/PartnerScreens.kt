package com.example.taliworkouthelper.partner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ScreenTextColor = Color(0xFFF4F1FF)
private val ScreenMetaColor = Color(0xFFB9AADA)
private val ScreenBackgroundTop = Color(0xFF080810)
private val ScreenBackgroundBottom = Color(0xFF161334)
private val PartnerCardBackground = Color(0xDB15122E)
private val PartnerCardBorder = Color(0x3DB48CFF)
private val ConnectStart = Color(0xFF7C3CFF)
private val ConnectEnd = Color(0xFFC04DFF)
private val StatusBackground = Color(0x427C3CFF)
private val StatusTextColor = Color(0xFFE9DEFF)

@Composable
fun PartnerListScreen(partners: List<Partner>, onConnect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBackgroundTop, ScreenBackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Partners", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
            Text(
                "Social fitness list with quick connect actions",
                color = ScreenMetaColor
            )

            if (partners.isEmpty()) {
                EmptyStateCard()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    partners.forEach { partner ->
                        PartnerItem(partner = partner, onConnect = onConnect)
                    }
                }
            }

            ErrorDesignNoteCard()
        }
    }
}

@Composable
private fun PartnerItem(partner: Partner, onConnect: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PartnerCardBackground),
        border = BorderStroke(1.dp, PartnerCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarGlow()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(partner.name, color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
                Text("Ready for partner workout planning", color = ScreenMetaColor)
                StatusBadge("Available")
            }
            ConnectButton(onClick = { onConnect(partner.id) })
        }
    }
}

@Composable
private fun AvatarGlow() {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFD54FFF), Color(0xFF7C3CFF))
                )
            )
            .padding(14.dp)
    ) {
        Text(" ")
    }
}

@Composable
private fun StatusBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(StatusBackground)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text, color = StatusTextColor)
    }
}

@Composable
private fun ConnectButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.horizontalGradient(listOf(ConnectStart, ConnectEnd))),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text("Connect")
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = PartnerCardBackground),
        border = BorderStroke(1.dp, PartnerCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("No partners found", color = ScreenTextColor, fontWeight = FontWeight.Medium)
            Text(
                "Design note: surface an inviting placeholder with one clear connect action.",
                color = ScreenMetaColor
            )
        }
    }
}

@Composable
private fun ErrorDesignNoteCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = PartnerCardBackground),
        border = BorderStroke(1.dp, PartnerCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Error state design note", color = ScreenTextColor, fontWeight = FontWeight.Medium)
            Text(
                "If partner load/connect fails, keep list visible and show inline retry action per row.",
                color = ScreenMetaColor
            )
        }
    }
}
