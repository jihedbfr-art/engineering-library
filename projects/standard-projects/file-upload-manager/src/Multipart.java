import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-rolled multipart/form-data parser. No external libraries.
 * Parses a raw request body given the boundary from the Content-Type header.
 */
public final class Multipart {

    public static class Part {
        public final Map<String, String> headers = new LinkedHashMap<>();
        public byte[] content;

        public String fieldName() {
            return dispositionParam("name");
        }

        public String fileName() {
            return dispositionParam("filename");
        }

        public boolean isFile() {
            return fileName() != null;
        }

        private String dispositionParam(String key) {
            String disposition = headers.get("content-disposition");
            if (disposition == null) return null;
            String marker = key + "=\"";
            int idx = disposition.indexOf(marker);
            if (idx == -1) return null;
            int start = idx + marker.length();
            int end = disposition.indexOf('"', start);
            if (end == -1) return null;
            return disposition.substring(start, end);
        }
    }

    /** Extracts the boundary string from a Content-Type header value. */
    public static String extractBoundary(String contentType) {
        if (contentType == null) return null;
        String marker = "boundary=";
        int idx = contentType.indexOf(marker);
        if (idx == -1) return null;
        String boundary = contentType.substring(idx + marker.length());
        int semi = boundary.indexOf(';');
        if (semi != -1) boundary = boundary.substring(0, semi);
        boundary = boundary.trim();
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }
        return boundary;
    }

    public static List<Part> parse(byte[] body, String boundary) {
        List<Part> parts = new ArrayList<>();
        byte[] delimiter = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);
        byte[] crlf = {13, 10};

        List<Integer> boundaryPositions = new ArrayList<>();
        int idx = 0;
        while (true) {
            int found = indexOf(body, delimiter, idx);
            if (found == -1) break;
            boundaryPositions.add(found);
            idx = found + delimiter.length;
        }

        for (int i = 0; i < boundaryPositions.size() - 1; i++) {
            int segmentStart = boundaryPositions.get(i) + delimiter.length;
            int segmentEnd = boundaryPositions.get(i + 1);
            // segment starts right after boundary marker; skip trailing "--" (final boundary) case
            if (segmentStart >= body.length) continue;
            // strip leading CRLF after boundary
            if (body[segmentStart] == '-' && segmentStart + 1 < body.length && body[segmentStart + 1] == '-') {
                continue; // this was the closing boundary "--boundary--"
            }
            if (matchesAt(body, segmentStart, crlf)) segmentStart += 2;
            // segment ends right before the next boundary's CRLF
            int contentEnd = segmentEnd;
            if (contentEnd >= 2 && matchesAt(body, contentEnd - 2, crlf)) contentEnd -= 2;
            if (segmentStart > contentEnd) continue;

            Part part = new Part();
            int headerEnd = indexOf(body, new byte[]{13, 10, 13, 10}, segmentStart);
            if (headerEnd == -1 || headerEnd > contentEnd) continue;
            String headerBlock = new String(body, segmentStart, headerEnd - segmentStart, StandardCharsets.UTF_8);
            for (String line : headerBlock.split("\r\n")) {
                int colon = line.indexOf(':');
                if (colon == -1) continue;
                String key = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                part.headers.put(key, value);
            }
            int contentStart = headerEnd + 4;
            int len = Math.max(0, contentEnd - contentStart);
            byte[] content = new byte[len];
            System.arraycopy(body, contentStart, content, 0, len);
            part.content = content;
            parts.add(part);
        }
        return parts;
    }

    private static boolean matchesAt(byte[] haystack, int pos, byte[] needle) {
        if (pos < 0 || pos + needle.length > haystack.length) return false;
        for (int i = 0; i < needle.length; i++) {
            if (haystack[pos + i] != needle[i]) return false;
        }
        return true;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int fromIndex) {
        outer:
        for (int i = fromIndex; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
