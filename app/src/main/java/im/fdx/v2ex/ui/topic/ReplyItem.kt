package im.fdx.v2ex.ui.topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import im.fdx.v2ex.R
// import im.fdx.v2ex.view.GoodTextView // Assuming we might use AndroidView for content for now

@Composable
fun ReplyItem(
    reply: Reply,
    onMemberClick: (String?) -> Unit,
    onReplyClick: (Reply) -> Unit,
    onThankClick: (Reply) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
             AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    Glide.with(imageView)
                        .load(reply.member?.avatarNormalUrl)
                        .into(imageView)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reply.member?.username ?: "",
                        style = MaterialTheme.typography.labelMedium,
                         color = MaterialTheme.colorScheme.onSurface,
                         fontWeight = FontWeight.Bold
                    )
                    
                    if (reply.isLouzu) {
                         Spacer(modifier = Modifier.width(4.dp))
                         Text(
                            text = "OP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = reply.createdOriginal, // Or calculate logic
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "#${reply.getRowNum()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Content - Using AndroidView for HTML support for now as a safe bet
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                           setTextIsSelectable(true)
                           textSize = 15f
                           setTextColor(android.graphics.Color.BLACK) // Theme adaptation needed
                        }
                    },
                    update = { textView ->
                         textView.text = HtmlCompat.fromHtml(reply.content_rendered, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                )
                
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.End,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     if (reply.thanks > 0) {
                          Icon(
                             painter = painterResource(id = R.drawable.ic_thank),
                             contentDescription = "Thanks",
                             tint = MaterialTheme.colorScheme.onSurfaceVariant,
                             modifier = Modifier.size(16.dp)
                         )
                         Text(
                             text = reply.thanks.toString(),
                             style = MaterialTheme.typography.labelSmall,
                             modifier = Modifier.padding(start = 2.dp, end = 12.dp)
                         )
                     }
                     
                     Icon(
                         painter = painterResource(id = R.drawable.ic_comment),
                         contentDescription = "Reply",
                         tint = MaterialTheme.colorScheme.onSurfaceVariant,
                         modifier = Modifier.size(16.dp)
                     )
                 }

            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}
