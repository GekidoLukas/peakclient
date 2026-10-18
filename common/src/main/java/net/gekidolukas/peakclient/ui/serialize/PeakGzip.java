package net.gekidolukas.peakclient.ui.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class PeakGzip {
    private PeakGzip() {}

    public static byte[] compress(String text) {
        try (var byteStream = new ByteArrayOutputStream();
             var gzip = new GZIPOutputStream(byteStream)) {
            gzip.write(text.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String decompress(byte[] data) {
        try (var gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
