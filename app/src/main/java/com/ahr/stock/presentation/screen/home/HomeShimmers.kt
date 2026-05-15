package com.ahr.stock.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer


@Composable
fun StockListShimmer(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    Column(modifier = modifier) {
        repeat(5) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer(shimmer)
                            .background(color),
                    )
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(11.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer(shimmer)
                            .background(color),
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer(shimmer)
                            .background(color),
                    )
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer(shimmer)
                            .background(color),
                    )
                }
            }
            if (index < 4) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun SectorGridShimmer(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val columns = 3

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(columns) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shimmer(shimmer)
                            .background(color),
                    )
                }
            }
        }
    }
}

@Composable
fun NewsCardShimmer(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmer(shimmer)
                .background(color),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(shimmer)
                    .background(color),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(shimmer)
                    .background(color),
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(shimmer)
                    .background(color),
            )
        }
    }
}

