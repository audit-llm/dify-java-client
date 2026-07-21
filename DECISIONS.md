# dify-java-client 决策记录

> 本文档记录本仓库（`com.whucs.audit:dify-java-client`，基于上游 `imfangs/dify-java-client` 的私有 fork）的关键决策，供后续接手者快速了解"为什么是这个状态"。

## 决策 1：本 fork 不做 Dify 接口二开，所有兼容补丁写在 backend

### 背景

我们曾基于 Dify **新版（1.13.1+）** 的知识库文档下载接口，在本 fork 上做过二开，新增了：

- `DifyDatasetsClient.getDocumentDownloadUrl()` → 调用 `GET /v1/datasets/{ds}/documents/{doc}/download`
- `DifyDatasetsClient.downloadDocumentsAsZip()` → 调用 `POST /v1/datasets/{ds}/documents/batch-download`
- 配套的 `BatchDownloadDocumentsRequest` / `DocumentDownloadResponse` / `DocumentStatusAction` 等

### 问题

**二开完成后才发现：生产环境 Dify 版本是 1.7.2，根本没有上述下载端点**（这些端点在 Dify 1.13.1 才由 PR #33100 引入）。调用即返回 404（HTML 错误页），功能完全不可用。

定位过程：通过 GitHub commit search 找到引入端点的 commit `0ab4e163`（2026-03-10），对比 release tag 日期，确认 1.13.1（2026-03-17）是首个包含该功能的版本，而生产 1.7.2 远低于此。

### 决策

1. **本 fork 退回与上游原版一致**（代码层面），**仅保留 pom 的私有坐标改动**（见决策 2）。
2. **所有针对旧版 Dify 的兼容补丁，一律写在 `audit-backend`，不污染本库**。例如知识库文档下载的 workaround（走 Dify 控制台内部 API），实现位置：`audit-backend` 的 `DifyConsoleClient` + 知识库下载 service。

### 历史存档

二开的完整代码历史保留在 `feature/dataset-document-download` 分支（未删除）。如果想看"曾经怎么做的二开"，或未来升级 Dify 后想参考，可以查这个分支：

```bash
git log --oneline feature/dataset-document-download
# 1f5804c feat(datasets): add document download APIs
# 2a39e1f test(datasets): align download tests with integration style
```

main 分支上的代码与上游 `imfangs/dify-java-client` v1.2.7 一致，**不含**任何下载相关方法。

### 何时能改回来

当生产 Dify 升级到 **1.13.1 或更高**后：
- 知识库下载可改用本库（重新 cherry-pick `feature/dataset-document-download` 的下载方法，或等上游 imfangs 合入）。
- `audit-backend` 里的 console API workaround 应**删除**（详见 backend 的 `docs/knowledge-download.md`）。

---

## 决策 2：保留私有 Maven 坐标 `com.whucs.audit`

pom 的 `groupId` 从上游的 `io.github.imfangs` 改为 `com.whucs.audit`，`version` 改为 `1.2.7-whucs-SNAPSHOT`。

### 原因

让本 fork 与 Maven Central 上的官方发布物**坐标错开**，避免：
- 某台机器没装本地版时，Maven 静默 fallback 到 Central 官方原版（编译过但行为不同）。
- 我们对 djc 的任何小修改无法独立发版。

私有坐标保证：拿不到本地版就**编译失败**（失败比静默错误好）。

### GPG 签名

`maven-gpg-plugin` 设了 `<skip>true</skip>`，因为本 fork 不发布到 Maven Central，本机构建不需要 GPG 私钥。

---

## 相关文档

- 知识库下载功能的现实实现与全景图：`audit-backend/docs/knowledge-download.md`
- 数据分析多 SQL 改造方案（另一项进行中的工作）：`audit-backend` 根目录 plan 文件
