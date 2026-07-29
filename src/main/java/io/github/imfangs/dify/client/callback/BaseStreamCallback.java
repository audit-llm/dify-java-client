package io.github.imfangs.dify.client.callback;

import io.github.imfangs.dify.client.event.ErrorEvent;
import io.github.imfangs.dify.client.event.PingEvent;

/**
 * 对话流式回调接口
 */
public interface BaseStreamCallback {

    /**
     * 错误事件
     *
     * @param event 事件
     */
    default void onError(ErrorEvent event) {
    }

    /**
     * 心跳
     *
     * @param event 事件
     */
    default void onPing(PingEvent event) {
    }

    /**
     * 异常处理
     * 用于处理非ErrorEvent类型的异常，如网络异常、解析异常等
     *
     * @param throwable 异常
     */
    default void onException(Throwable throwable) {
    }

    /**
     * 服务端正常结束流响应时触发。
     *
     * <p>部分 Chatflow 版本会在发送 {@code workflow_finished} 后直接关闭连接，
     * 而不再发送 {@code message_end}。调用方可借此完成本地流状态。</p>
     */
    default void onStreamComplete() {
    }

}
