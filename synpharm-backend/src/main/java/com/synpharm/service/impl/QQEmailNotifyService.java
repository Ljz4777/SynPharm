package com.synpharm.service.impl;

import com.synpharm.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * QQ邮箱通知服务实现
 *
 * <p>负责通过QQ邮箱发送邮件。实现 NotifyService 接口，和业务代码解耦。
 *
 * <p>扩展说明：未来如果换成阿里云邮件，只需新建一个 AliyunEmailNotifyService，
 * 业务代码不用改，Spring会自动注入新的实现（配合 @Primary 或配置）。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QQEmailNotifyService implements NotifyService {

    /** 邮件发送器（Spring Boot自动配置） */
    private final JavaMailSender mailSender;

    /** 发件人邮箱（从配置文件读取） */
    @Value("${spring.mail.username:noreply@synpharm.com}")
    private String fromEmail;

    /** 系统名称 */
    @Value("${app.name:SynPharm}")
    private String appName;

    /**
     * 发送邮件通知
     *
     * @param target        目标邮箱
     * @param templateCode  模板编码
     * @param params        模板参数
     */
    @Override
    public void send(String target, String templateCode, Map<String, String> params) {
        try {
            // 根据模板编码获取邮件内容
            EmailTemplate template = getTemplate(templateCode, params);

            // 构建邮件消息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(target);
            message.setSubject(template.subject);
            message.setText(template.content);

            // 发送邮件
            mailSender.send(message);
            log.info("邮件发送成功, to: {}, template: {}", target, templateCode);
        } catch (Exception e) {
            // 邮件发送失败不应该影响主流程，只记录错误日志
            log.error("邮件发送失败, to: {}, template: {}, error: {}",
                    target, templateCode, e.getMessage());
        }
    }

    /**
     * 获取通知类型
     */
    @Override
    public String getNotifyType() {
        return "email";
    }

    // ==================== 私有方法 ====================

    /**
     * 根据模板编码获取邮件模板
     * <p>实际项目中可以从数据库或配置文件读取模板，这里简化为硬编码。
     */
    private EmailTemplate getTemplate(String templateCode, Map<String, String> params) {
        EmailTemplate template = new EmailTemplate();

        if ("captcha".equals(templateCode)) {
            // 验证码邮件模板
            String code = params.getOrDefault("code", "");
            String minutes = params.getOrDefault("minutes", "5");
            template.subject = "【" + appName + "】验证码：" + code;
            template.content = String.format(
                    "您好！\n\n" +
                    "您的验证码是：%s\n\n" +
                    "验证码有效期 %s 分钟，请勿告诉他人。\n\n" +
                    "如非本人操作，请忽略此邮件。\n\n" +
                    "%s 团队",
                    code, minutes, appName
            );
        } else {
            // 默认模板
            template.subject = "【" + appName + "】系统通知";
            template.content = params.getOrDefault("content", "");
        }

        return template;
    }

    /**
     * 邮件模板内部类
     */
    private static class EmailTemplate {
        String subject;
        String content;
    }
}
