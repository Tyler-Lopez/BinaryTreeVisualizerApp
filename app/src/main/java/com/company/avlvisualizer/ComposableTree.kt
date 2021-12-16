package com.company.avlvisualizer

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.times
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun ComposableTree(
    data: Pair<List<NodeComposableData>, Int>,
    modifier: Modifier = Modifier,
    style: ComposableTreeStyle = ComposableTreeStyle(),
    onNodeSelect: (String) -> Unit,
) {
    var selectedIndex: Int by remember {
        mutableStateOf(-1)
    }
    var scale by remember {
        mutableStateOf(1f)
    }
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }
    // Used to
    var selectionInfo: MutableList<Pair<Rect, NodeComposableData>> = mutableListOf()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val boxWithConstraintsScope = this

        Canvas(modifier = modifier
            .width(boxWithConstraintsScope.maxWidth)
            .height(boxWithConstraintsScope.maxHeight)
            .background(Color.DarkGray)
            .pointerInput(Unit) {
                // DRAG AND ZOOM
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale // Old Scale
                    scale *= zoom // New Scale
                    offset = (offset + centroid / oldScale) - (centroid / scale + pan / oldScale)
                }
            }
            .pointerInput(Unit) {
                // NODE SELECT
                detectTapGestures(
                    onTap = {
                        /*
                            // it = when you click returns an Offset
                            // (0, 0) is the top left of the screen
                            // So let's first correct it to be (0, 0) in the center of the screen
                            val tapAsCenter = Offset(
                                x = ((-this.size.width / 2f) + it.x
                                        ),
                                y = ((-this.size.height / 2f) + it.y)
                            )
                            // Next, let's divide that by the scale so the bigger the scale the smaller the offset
                            val scaledTap = Offset(tapAsCenter.x, tapAsCenter.y)
                            // Now that it's been scaled... we need to add the center of the canvas
                            // This does not yet take into account offset
                            val canvasCenteredTap = (scaledTap + it)
                            // Offsetted...
                            val offsettedTap = Offset(
                                canvasCenteredTap.x + (offset.x * 2f),
                                canvasCenteredTap.y + (offset.y * 2f)
                            )

                            var found = false

                            for (i in 0..selectionInfo.lastIndex) {
                                if (found) continue
                                println("i is $i")
                                val rect = selectionInfo[i].first
                                val rectOffset = rect.center
                                val offsetOfRectToCenter = Offset(
                                    x = ((-this.size.width / 2f + rectOffset.x) * 2f),
                                    y = ((-this.size.height / 2f + rectOffset.y) * 2f)
                                )
                                val newOffset = Offset(
                                    selectionInfo[0].first.center.x + offsetOfRectToCenter.x,
                                    selectionInfo[0].first.center.y + offsetOfRectToCenter.y
                                )
                                val newRect = Rect(center = newOffset, radius = style.nodeSize * 3f)
                                if (rect.contains(offsettedTap)) {
                                    println("i is $i")
                                    onNodeSelect(
                                        "NODE Value: ${selectionInfo[i].second.value}\nMATCHED AT $i\n" +
                                                "Found at $offsettedTap" + "\nActually $newOffset"
                                    )
                                    selectedIndex = i
                                    found = true
                                }
                            }
                            if (!found) {
                                onNodeSelect("")
                                selectedIndex = -1
                            }

                        }

                   */
                    }
                )
            }
            .graphicsLayer {
                // APPLY ZOOM
                translationX = -offset.x * scale
                translationY = -offset.y * scale
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }


        ) {
            selectionInfo.clear()
            // Iterate through data, drawing each node
            val dataList = data.first
            val height = data.second

            for (i in 0..dataList.lastIndex) {

                var xShift = 0f
                var yShift = 0f

                var parentPosition = Offset(0f, 0f)

                var nodeHeight = height

                val node = dataList[i]
                val nodeSize = style.nodeSize

                for (child in node.path) {
                    parentPosition = Offset(xShift, yShift)
                    when (child) {
                        BinaryNodeChild.LEFT -> xShift -= nodeSize * 0.05f * 2f.pow(
                            nodeHeight + 3
                        )
                        BinaryNodeChild.RIGHT -> xShift += nodeSize * 0.05f * 2f.pow(
                            nodeHeight + 3
                        )
                    }
                    nodeHeight -= 1
                    yShift += nodeSize * style.ySpacing
                }

                val centerPos = Offset(center.x + xShift, center.y + yShift)
                val parentPos = Offset(center.x + parentPosition.x, center.y + parentPosition.y)


                selectionInfo.add(
                    Pair(
                        Rect(
                            center = centerPos,
                            radius = nodeSize * 5f
                        ),
                        node
                    )
                )
                println("here")


                drawLine(
                    color = style.nodeColor,
                    start = parentPos,
                    end = centerPos,
                    strokeWidth = style.lineWidth
                )
            }

            for (i in 0..selectionInfo.lastIndex) {

                val node = selectionInfo[i].second
                val centerPos = selectionInfo[i].first.center

                val isSelected = i == selectedIndex

                drawCircle(
                    center = centerPos,
                    color = if (isSelected) style.selectedNodeColor else style.nodeColor,
                    radius = style.nodeSize
                )

                val paint = Paint()
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = style.nodeSize
                paint.color = 0xffffffff.toInt()

                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "${node.value}",
                        centerPos.x,
                        centerPos.y + (style.nodeSize / 3),
                        paint
                    )
                }
            }
        }
    }
}



