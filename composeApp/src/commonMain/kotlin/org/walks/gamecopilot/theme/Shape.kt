package org.walks.gamecopilot.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * 从 AppCornerRadius 创建 Material3 Shapes
 */
fun AppCornerRadius.toMaterialShapes(): Shapes {
    return Shapes(
        extraSmall = RoundedCornerShape(xs),
        small = RoundedCornerShape(sm),
        medium = RoundedCornerShape(md),
        large = RoundedCornerShape(lg),
        extraLarge = RoundedCornerShape(xl)
    )
}
