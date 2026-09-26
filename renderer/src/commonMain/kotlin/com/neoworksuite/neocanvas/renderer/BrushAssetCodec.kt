package com.neoworksuite.neocanvas.renderer

/** Deterministic bounded coverage format used by built-in and version-3 pack assets. */
object BrushAssetCodec {
    private val magic = "NEOMASK1".encodeToByteArray()
    private const val HEADER_SIZE = 12
    private const val MAX_BYTES = 1024 * 1024

    fun encode(asset: BrushAsset): ByteArray {
        require(asset.coverage.any { (it.toInt() and 255) != 0 }) { "Brush mask coverage must not be empty." }
        val output = ArrayList<Byte>(HEADER_SIZE + asset.coverage.size / 2)
        magic.forEach(output::add)
        output += (asset.width ushr 8).toByte()
        output += asset.width.toByte()
        output += (asset.height ushr 8).toByte()
        output += asset.height.toByte()
        var index = 0
        while (index < asset.coverage.size) {
            val value = asset.coverage[index]
            var count = 1
            while (index + count < asset.coverage.size && asset.coverage[index + count] == value && count < 65_535) count++
            output += (count ushr 8).toByte()
            output += count.toByte()
            output += value
            index += count
        }
        require(output.size <= MAX_BYTES) { "Encoded brush mask exceeds 1 MiB." }
        return output.toByteArray()
    }

    fun decode(bytes: ByteArray, id: String): BrushAsset {
        require(bytes.size in (HEADER_SIZE + 3)..MAX_BYTES) { "Brush mask must be bounded and contain coverage runs." }
        require(bytes.copyOfRange(0, magic.size).contentEquals(magic)) { "Unsupported brush mask format." }
        val width = unsignedShort(bytes[8], bytes[9])
        val height = unsignedShort(bytes[10], bytes[11])
        require(width in 1..512 && height in 1..512) { "Brush mask dimensions must be between 1 and 512." }
        val expected = width * height
        val coverage = ByteArray(expected)
        var source = HEADER_SIZE
        var destination = 0
        var nonEmpty = false
        while (source < bytes.size) {
            require(source + 3 <= bytes.size) { "Truncated brush mask run." }
            val count = unsignedShort(bytes[source], bytes[source + 1])
            val value = bytes[source + 2]
            require(count > 0 && destination + count <= expected) { "Invalid brush mask run length." }
            coverage.fill(value, destination, destination + count)
            if ((value.toInt() and 255) != 0) nonEmpty = true
            destination += count
            source += 3
        }
        require(destination == expected) { "Brush mask coverage is incomplete." }
        require(nonEmpty) { "Brush mask coverage must not be empty." }
        return BrushAsset(id, width, height, coverage)
    }

    private fun unsignedShort(high: Byte, low: Byte): Int =
        ((high.toInt() and 255) shl 8) or (low.toInt() and 255)
}
