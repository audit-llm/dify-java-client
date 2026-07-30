package io.github.imfangs.dify.client.model.datasets;

import io.github.imfangs.dify.client.util.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证与 Dify 文档列表接口的字段命名契约保持一致。
 */
class DocumentListResponseSerializationTest {

    @Test
    void shouldDeserializeDifyDocumentFieldsInSnakeCase() {
        String json = "{\"data\":[{\"id\":\"doc-1\",\"name\":\"审计条例.pdf\","
                + "\"created_at\":1785346038,\"word_count\":128,"
                + "\"indexing_status\":\"completed\",\"display_status\":\"available\"}],"
                + "\"has_more\":false,\"limit\":20,\"total\":1,\"page\":1}";

        DocumentListResponse response = JsonUtils.fromJson(json, DocumentListResponse.class);

        assertNotNull(response);
        assertEquals(Boolean.FALSE, response.getHasMore());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        DocumentListResponse.DocumentInfo document = response.getData().get(0);
        assertEquals(1785346038L, document.getCreatedAt());
        assertEquals(128, document.getWordCount());
        assertEquals("completed", document.getIndexingStatus());
        assertEquals("available", document.getDisplayStatus());
    }

    @Test
    void shouldSerializeDifyDocumentFieldsInSnakeCase() {
        DocumentListResponse response = DocumentListResponse.builder()
                .hasMore(false)
                .data(java.util.Collections.singletonList(DocumentListResponse.DocumentInfo.builder()
                        .id("doc-1")
                        .createdAt(1785346038L)
                        .wordCount(128)
                        .displayStatus("available")
                        .build()))
                .build();

        String json = JsonUtils.toJson(response);

        assertTrue(json.contains("\"has_more\":false"));
        assertTrue(json.contains("\"created_at\":1785346038"));
        assertTrue(json.contains("\"word_count\":128"));
        assertTrue(json.contains("\"display_status\":\"available\""));
        assertFalse(json.contains("hasMore"));
        assertFalse(json.contains("createdAt"));
        assertFalse(json.contains("wordCount"));
        assertFalse(json.contains("displayStatus"));
    }
}
