package com.synpharm.service;

import java.util.Map;

/**
 * 通知服务接口
 *
 * <p>所有通知方式（邮件、短信、站内信等）的统一抽象。
 * 业务代码不关心是发邮件还是发短信，只依赖此接口。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
public interface NotifyService {

    /**
     * 发送通知
     *
     * @param target        接收目标（邮箱/手机号）
     * @param templateCode  模板编码
     * @param params        模板参数
     */
    void send(String target, String templateCode, Map<String, String> params);

    /**
     * 获取通知类型
     *
     * @return 通知类型，如 "email"、"sms"
     */
    String getNotifyType();
}
