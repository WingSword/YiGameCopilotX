package org.walks.rooms;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/** Fixed-size durable command receipts for virtual ledger and drawing operations. */
final class CommandFingerprint {
    private CommandFingerprint() {}
    static String of(Map<String,Object> command) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Json.write(command).getBytes(StandardCharsets.UTF_8))); }
        catch(NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
}
