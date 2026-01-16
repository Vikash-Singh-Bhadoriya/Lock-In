import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.domain.ExecutionState

@Composable
fun TaskExecutionOverlay(
    state: ExecutionState,
    onStop: () -> Unit
) {
    if (state !is ExecutionState.Running) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "LOCKED IN",
                    fontSize = 12.sp,
                    color = Color(0xFFAAAAAA)
                )
                Text(
                    text = state.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            TextButton(onClick = onStop) {
                Text(
                    "STOP",
                    color = Color.Red
                )
            }
        }
    }
}