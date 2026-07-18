package com.riverflow.ai.knowledge.chunk;

import lombok.Data;

/**
 * 文档分块选项
 */
@Data
public class ChunkOptions {

    /**
     * 分块大小（字符数）
     */
    private int size = 512;

    /**
     * 重叠字符数
     */
    private int overlap = 64;

    /**
     * 最大分块数
     */
    private int maxChunks = 100;

    /**
     * 是否保留段落边界（按 \n\n 分割）
     */
    private boolean keepParagraphBoundary = false;
}
