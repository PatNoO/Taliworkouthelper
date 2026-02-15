package com.example.taliworkouthelper.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkScheduleScreen(shifts: List<WorkShift>, onAdd: () -> Unit, onRemove: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        shifts.forEach { s ->
            Text(text = "Shift: ${s.startHour}:00 - ${s.endHour}:00")
        }
        Button(onClick = onAdd, modifier = Modifier.padding(top = 8.dp)) {
            Text("Add sample shift")
        }
    }
}
