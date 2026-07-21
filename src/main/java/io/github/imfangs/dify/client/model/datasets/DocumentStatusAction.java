package io.github.imfangs.dify.client.model.datasets;

/**
 * 批量更新文档状态动作。
 *
 * <p>对应 Dify 知识库 API {@code PATCH /datasets/{dataset_id}/documents/status/{action}}。
 * 实测在 Dify 1.7.2 上可用（enable/disable 生效，archive/un_archive 受文档当前状态业务校验）。</p>
 */
public enum DocumentStatusAction {
    ENABLE("enable"),
    DISABLE("disable"),
    ARCHIVE("archive"),
    UN_ARCHIVE("un_archive");

    private final String apiValue;

    DocumentStatusAction(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }
}
