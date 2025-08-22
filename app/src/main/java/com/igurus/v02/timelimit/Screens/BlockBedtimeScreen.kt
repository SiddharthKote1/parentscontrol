package com.igurus.v02.timelimit.Screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.igurus.v02.ReelsBlockingService.BedtimeBlock
import com.igurus.v02.ReelsBlockingService.MainViewModel

@Composable
fun BlockBedtimeScreen(
    navController: NavController,
    packageName: String,
    appName: String,
    viewModel: MainViewModel
) {
    // Default start/end times
    var startHour by remember { mutableStateOf(22) } // 10 PM
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(6) } // 6 AM
    var endMinute by remember { mutableStateOf(0) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bedtime Blocking: $appName",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Select the time range when this app should be blocked",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Start Time Picker
        Button(
            onClick = {
                TimePickerDialog(
                    context,
                    { _: android.widget.TimePicker, hour: Int, minute: Int ->
                        startHour = hour
                        startMinute = minute
                    },
                    startHour,
                    startMinute,
                    true
                ).show()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Time: %02d:%02d".format(startHour, startMinute))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // End Time Picker
        Button(
            onClick = {
                TimePickerDialog(
                    context,
                    { _: android.widget.TimePicker, hour: Int, minute: Int ->
                        endHour = hour
                        endMinute = minute
                    },
                    endHour,
                    endMinute,
                    true
                ).show()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End Time: %02d:%02d".format(endHour, endMinute))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        // Save Button
        // Save Button
        Button(
            onClick = {
                // Create BedtimeBlock object with hour & minute
                val bedtime = BedtimeBlock(
                    packageName = packageName,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute
                )

                // Save to ViewModel
                viewModel.setBedtimeBlock(
                    packageName = packageName,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute
                )

                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Block", fontSize = 18.sp)
        }
    }
}
