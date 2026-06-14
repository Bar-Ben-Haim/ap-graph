package project_biu.server;

import java.util.UUID;

/**
 * Utility class for parsing multipart/form-data bodies.
 */
public final class MultipartParser {
    private MultipartParser() {
    }

    public record MultipartResult(String filename, String content) {
    }

    /**
     * Parses a multipart/form-data body, returning the filename and content of the first file part.
     *
     * @param raw            the raw multipart/form-data body.
     * @param fileNameSuffix the file name suffix to append if the filename is not specified in the Content-Disposition header.
     * @return the filename and content of the first file part, or null if the body is invalid.
     */
    public static MultipartResult parseMultipart(String raw, String fileNameSuffix) {
        final String[] lines = raw.split("\n");
        if (lines.length < 1) return null;

        final String boundary = lines[0].trim();
        final StringBuilder content = new StringBuilder();
        final State state = new State();

        for (int i = 1; i < lines.length; i++) {
            if (!processLine(lines[i], boundary, fileNameSuffix, state, content))
                break;
        }

        return new MultipartResult(state.filename, content.toString().trim());
    }

    /**
     * Processes a single line of a multipart/form-data body to parse relevant file part data.
     *
     * @param line           the current line to process.
     * @param boundary       the boundary string separating parts of the multipart body.
     * @param fileNameSuffix the suffix to append to the file name if it is not specified in the Content-Disposition header.
     * @param state          the state object tracking parsing progress and file part metadata.
     * @param content        the StringBuilder used to accumulate the file part of the content.
     * @return true if parsing should continue, false if the closing boundary is reached and parsing should stop.
     */
    private static boolean processLine(String line,
                                       String boundary,
                                       String fileNameSuffix,
                                       State state,
                                       StringBuilder content) {
        final String trimmed = line.trim();

        if (trimmed.startsWith(boundary)) {
            return !state.inFilePart;
        }

        if (!state.inFilePart) {
            if (trimmed.toLowerCase().startsWith("content-disposition:")) {
                state.filename = extractFilename(trimmed, fileNameSuffix);
                state.inFilePart = true;
            }
        } else if (!state.pastPartHeaders) {
            state.pastPartHeaders = trimmed.isEmpty();
        } else {
            content.append(line).append("\n");
        }

        return true;
    }

    private static String extractFilename(String contentDisposition, String fileNameSuffix) {
        final int idx = contentDisposition.indexOf("filename=\"");
        if (idx == -1) return UUID.randomUUID() + fileNameSuffix;
        final int start = idx + 10;
        final int end = contentDisposition.indexOf("\"", start);
        return end != -1 ? contentDisposition.substring(start, end) : UUID.randomUUID() + fileNameSuffix;
    }

    private static class State {
        String filename;
        boolean inFilePart;
        boolean pastPartHeaders;
    }
}