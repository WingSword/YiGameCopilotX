# Netty's ReflectiveChannelFactory creates the LAN server channel via reflection.
-keep,allowoptimization,allowobfuscation class io.netty.channel.socket.nio.NioServerSocketChannel {
    public <init>();
}

# Netty checks this inherited runtime annotation before reusing a channel handler.
-keep @interface io.netty.channel.ChannelHandler$Sharable
-keep,allowshrinking,allowobfuscation,allowoptimization @io.netty.channel.ChannelHandler$Sharable class *

# TypeParameterMatcher reflects over codec type parameters during WebSocket upgrade.
-keepattributes Signature
-keep,allowshrinking,allowobfuscation class io.netty.handler.codec.MessageToByteEncoder
-keep,allowshrinking,allowobfuscation class * extends io.netty.handler.codec.MessageToByteEncoder

# Leak detection resolves these method names reflectively during class initialization.
-keepclassmembers class io.netty.buffer.AbstractByteBufAllocator {
    *** toLeakAwareBuffer(...);
}
-keepclassmembers class io.netty.buffer.AdvancedLeakAwareByteBuf {
    *** touch(...);
    *** recordLeakNonRefCountingOperation(...);
}
-keepclassmembers class io.netty.util.ReferenceCountUtil {
    *** touch(...);
}

# Netty's optional native OpenSSL provider is not shipped on Android.
# LAN hosting uses a plain TCP connector; cloud HTTPS uses the platform client.
-dontwarn io.netty.internal.tcnative.**

# Optional desktop logging backends. Netty falls back to the available logger.
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**

# JFR is a desktop diagnostic facility guarded by Netty's availability checks.
-dontwarn jdk.jfr.**

# Ktor's IntelliJ debugger probe catches missing JVM management APIs on Android.
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
