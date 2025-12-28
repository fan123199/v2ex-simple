package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Topic
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import im.fdx.v2ex.utils.extensions.findRownum
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import im.fdx.v2ex.R
import im.fdx.v2ex.data.model.Reply

// import im.fdx.v2ex.ui.helper.GoodTextView // Assuming we might use AndroidView for content for now

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReplyItem(
    reply: Reply,
    onMemberClick: (String?) -> Unit,
    onReplyClick: (Reply) -> Unit,
    onThankClick: (Reply) -> Unit,
    onLongClick: (Reply) -> Unit,
    onQuoteClick: (String, Int) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .combinedClickable(
                onClick = { onReplyClick(reply) },
                onLongClick = { onLongClick(reply) }
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            // Avatar using Coil AsyncImage
             AsyncImage(
                model = reply.member?.avatarNormalUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onMemberClick(reply.member?.username) }
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

                // Content - Using ClickableText to support link clicks
                val annotatedString = AnnotatedString.fromHtml(reply.content_rendered ?: "")
                
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = { offset ->
                        // Find if clicked position is on a link
                        annotatedString.getStringAnnotations(
                            tag = "URL",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let { annotation ->
                            val url = annotation.item
                            // Check if it's a V2EX topic URL (e.g., https://www.v2ex.com/t/12345#reply123)
                            if (url.contains("/t/")) {
                                // Try to find username and reply number from the content
                                val content = reply.content ?: ""
                                // Look for @username pattern in the content
                                val usernameRegex = """@([\w\-]+)\s*#(\d+)""".toRegex()
                                val match = usernameRegex.find(content)
                                match?.let {
                                    val username = it.groupValues[1]
                                    val replyNum = it.groupValues[2].toIntOrNull() ?: -1
                                    if (replyNum > 0) {
                                        onQuoteClick(username, replyNum)
                                    }
                                }
                            }
                        }
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
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}




