package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushAssetRef
import com.neoworksuite.neocanvas.brushes.Sha256

object BuiltInBrushAssets {
    private val records by lazy { BuiltInBrushAssetData.records.associateBy { it.ref } }
    private val assets = mutableMapOf<BrushAssetRef, BrushAsset>()

    val resolver: BrushAssetResolver = BrushAssetResolver(::resolve)

    fun encoded(ref: BrushAssetRef): ByteArray? = records[ref]?.let { BrushAssetCodec.encode(expand(it)) }

    private fun resolve(ref: BrushAssetRef): BrushAsset? {
        assets[ref]?.let { return it }
        val record = records[ref] ?: return null
        val asset = expand(record)
        val encoded = BrushAssetCodec.encode(asset)
        require(Sha256.hex(encoded) == ref.sha256) { "Built-in brush asset hash mismatch for '${ref.id}'." }
        assets[ref] = asset
        return asset
    }

    private fun expand(record: BuiltInBrushAssetRecord): BrushAsset {
        val coverage = ByteArray(record.width * record.height)
        var destination = 0
        var index = 0
        while (index < record.runs.size) {
            val count = record.runs[index++]
            val value = record.runs[index++].toByte()
            coverage.fill(value, destination, destination + count)
            destination += count
        }
        require(destination == coverage.size) { "Generated brush asset coverage is incomplete." }
        return BrushAsset(record.ref.id, record.width, record.height, coverage)
    }
}

internal data class BuiltInBrushAssetRecord(
    val ref: BrushAssetRef,
    val width: Int,
    val height: Int,
    val runs: IntArray,
)
