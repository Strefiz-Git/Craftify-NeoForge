package tech.thatgravyboat.craftify.utils

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

object ImageCaches {

    private val HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    private val TEXTURE_ID = AtomicInteger()

    val MISSING_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("missingno")
    val ALBUM = MemoryImageCache("album")
    val ASSET = MemoryImageCache("asset")

    fun fetch(url: URI): CompletableFuture<NativeImage> {
        return HTTP.sendAsync(HttpRequest.newBuilder(url).build(), HttpResponse.BodyHandlers.ofInputStream()).thenApply { response ->
            if (response.statusCode() in 200..299) {
                try {
                    ImageIO.read(response.body()).toNative()
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw RuntimeException("Failed to read image from URL: $url", e)
                }
            } else {
                throw RuntimeException("Failed to fetch image from URL: $url, Status Code: ${response.statusCode()}")
            }
        }
    }

    fun register(id: String, native: NativeImage): ResourceLocation {
        val location = ResourceLocation.fromNamespaceAndPath("craftify", "$id/${TEXTURE_ID.incrementAndGet()}")
        Minecraft.getInstance().textureManager.register(location, DynamicTexture(native))
        return location
    }
}

class MemoryImageCache(private val id: String) {

    private val textures = ConcurrentHashMap<URI, ResourceLocation>()
    private val loading = ConcurrentHashMap<URI, CompletableFuture<ResourceLocation?>>()

    fun get(url: URI?): ResourceLocation? {
        if (url == null) return null
        return textures[url] ?: run {
            load(url)
            null
        }
    }

    private fun load(url: URI) {
        loading.computeIfAbsent(url) {
            ImageCaches.fetch(url).handle { native, error ->
                if (error != null) {
                    error.printStackTrace()
                    null
                } else {
                    Minecraft.getInstance().submit {
                        textures[url] = ImageCaches.register(id, native)
                    }
                    null
                }
            }
        }
    }
}
