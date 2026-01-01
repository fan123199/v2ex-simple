package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Topic
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextLayoutResult
import im.fdx.v2ex.utils.extensions.findRownum
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import im.fdx.v2ex.R
import im.fdx.v2ex.data.model.Reply
import im.fdx.v2ex.utils.extensions.logd

// import im.fdx.v2ex.ui.helper.GoodTextView // Assuming we might use AndroidView for content for now

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReplyItem(
    reply: Reply,
    onMemberClick: (String?) -> Unit,
    onReplyClick: (Reply) -> Unit,
    onThankClick: (Reply) -> Unit,
    onLongClick: (Reply) -> Unit,
    onQuoteClick: (String, Int, Offset) -> Unit,
    onMentionClick: (String, Offset) -> Unit
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
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 0.dp)
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

                // Content - Using Text with modern LinkAnnotation support
                val contentRendered = reply.content_rendered ?: ""
                var lastTapOffset by remember { mutableStateOf(Offset.Zero) }
                var textCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
                
                val annotatedString =
                     AnnotatedString.fromHtml(htmlString = contentRendered,
                        linkStyles = TextLinkStyles(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary)
                        ),
                        linkInteractionListener = { annotation ->
                            if (annotation is androidx.compose.ui.text.LinkAnnotation.Url) {
                                val url = annotation.url
                                val globalOffset = textCoordinates?.let { it.localToWindow(lastTapOffset) } ?: lastTapOffset
                                logd("LinkInteractionListener Clicked: $url at $globalOffset")
                                if (url.contains("/member/")) {
                                    var username = url.substringAfterLast("/")
                                    if (username.isEmpty() || username.contains("?")) {
                                        username = url.removeSuffix("/").substringAfterLast("/")
                                    }
                                    logd("Extracted username: $username")
                                    
                                    val content = reply.content ?: ""
                                    val floorRegex = """@$username\s*#(\d+)""".toRegex()
                                    val floorMatch = floorRegex.find(content)
                                    if (floorMatch != null) {
                                        val replyNum = floorMatch.groupValues[1].toIntOrNull() ?: -1
                                        if (replyNum > 0) {
                                            onQuoteClick(username, replyNum, globalOffset)
                                            return@fromHtml
                                        }
                                    }
                                    onMentionClick(username, globalOffset)
                                }
                            }
                        }
                    )
                
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .onGloballyPositioned { textCoordinates = it }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                lastTapOffset = offset
                            }
                        }
                )
                
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.End,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Row(
                        modifier = Modifier
                            .clickable { onThankClick(reply) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                     ) {
                         Icon(
                             imageVector = if (reply.isThanked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                             contentDescription = "Thanks",
                             tint = if (reply.isThanked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                             modifier = Modifier.size(16.dp)
                         )
                         if (reply.thanks > 0) {
                             Text(
                                 text = reply.thanks.toString(),
                                 style = MaterialTheme.typography.labelSmall,
                                 color = if (reply.isThanked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                 modifier = Modifier.padding(start = 2.dp)
                             )
                         }
                     }
                     
                     Spacer(modifier = Modifier.width(16.dp))
                     
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




