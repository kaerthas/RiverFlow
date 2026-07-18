package com.riverflow.ai.knowledge.chunk;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认文档分块器
 *
 * <p>按字符长度滑动窗口切分，支持重叠。</p>
 */
@Component
public class DefaultDocumentChunker implements DocumentChunker {

    @Override
    public List<String> chunk(String text, ChunkOptions options) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = normalize(text);
        int size = Math.max(options.getSize(), 1);
        int overlap = Math.max(options.getOverlap(), 0);
        if (overlap >= size) {
            overlap = size / 2;
        }
        int maxChunks = Math.max(options.getMaxChunks(), 1);

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int len = normalized.length();
        while (start < len) {
            int end = Math.min(start + size, len);
            chunks.add(normalized.substring(start, end));
            if (chunks.size() >= maxChunks) {
                break;
            }
            int nextStart = start + size - overlap;
            if (nextStart <= start) {
                nextStart = start + 1;
            }
            start = nextStart;
            if (start >= len) {
                break;
            }
        }
        return chunks;
    }

    /**
     * 标准化文本：去除多余空白
     */
    private String normalize(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }
}
