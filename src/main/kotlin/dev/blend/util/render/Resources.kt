package dev.blend.util.render

import dev.blend.util.misc.MiscUtil
import org.lwjgl.nanovg.NanoVG
import java.nio.ByteBuffer

class FontResource(
    fontName: String
) {
    private val resource: ByteBuffer
    val identifier: String = fontName
        .replace(' ', '_')
        .replace('-', '_')
    init {
        resource = MiscUtil.getResourceAsByteBuffer("fonts/$identifier.ttf")
        NanoVG.nvgCreateFontMem(DrawUtil.context, fontName, resource, false)
    }
}

class ImageResource(
    imageName: String,
) {
    val identifier = NanoVG.nvgCreateImageMem(DrawUtil.context, NanoVG.NVG_IMAGE_NEAREST, MiscUtil.getResourceAsByteBuffer("images/$imageName.png", 512))
}