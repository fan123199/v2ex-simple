package im.fdx.v2ex.ui.common

import im.fdx.v2ex.data.model.Data
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.ImageUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    photos: List<String>,
    initialPage: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { photos.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${pagerState.currentPage + 1}/${photos.size}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                val imageUrl = photos[page]
                ZoomableImage(
                    imageUrl = imageUrl,
                    modifier = Modifier.fillMaxSize()
                )

                // Buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { ImageUtil.downloadImage(context, imageUrl) },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_downward_24),
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { ImageUtil.shareImage(context, imageUrl) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)
                    val extraWidth = (size.width * scale - size.width).coerceAtLeast(0f)
                    val extraHeight = (size.height * scale - size.height).coerceAtLeast(0f)
                    
                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2
                    
                    offset = Offset(
                        (offset.x + pan.x * scale).coerceIn(-maxX, maxX),
                        (offset.y + pan.y * scale).coerceIn(-maxY, maxY)
                    )
                }
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}



