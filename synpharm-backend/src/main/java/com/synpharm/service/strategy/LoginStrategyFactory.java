package com.synpharm.service.strategy;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.service.LoginStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录策略工厂
 *
 * <p>负责管理所有登录策略，根据登录类型选择对应的策略执行。
 *
 * <p>设计亮点：
 * <ul>
 *   <li>自动发现：Spring自动注入所有 LoginStrategy 实现类</li>
 *   <li>快速查找：启动时构建Map缓存，O(1)时间找到策略</li>
 *   <li>无侵入扩展：新增登录方式只需加一个 @Component 实现类，不用改这里</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

    /** 所有登录策略列表（Spring自动注入所有 LoginStrategy 实现类） */
    private final List<LoginStrategy> strategies;

    /** 策略缓存Map：key=loginType，value=策略实现类 */
    private final Map<String, LoginStrategy> strategyMap = new HashMap<>();

    /**
     * 初始化策略映射
     * <p>在Bean创建完成后自动执行，遍历所有策略按 getLoginType() 存入Map。
     */
    @PostConstruct
    public void init() {
        for (LoginStrategy strategy : strategies) {
            String loginType = strategy.getLoginType();
            strategyMap.put(loginType, strategy);
            log.info("注册登录策略: {} -> {}", loginType, strategy.getClass().getSimpleName());
        }
        log.info("登录策略初始化完成，共注册 {} 种登录方式", strategyMap.size());
    }

    /**
     * 执行登录
     * <p>根据登录类型找到对应的策略，执行登录逻辑。
     *
     * @param loginType   登录类型
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录响应
     * @throws BusinessException 不支持的登录类型时抛出
     */
    public LoginResponse login(String loginType, LoginRequest request, HttpServletRequest httpRequest) {
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            log.warn("不支持的登录类型: {}", loginType);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的登录方式: " + loginType);
        }
        log.debug("使用登录策略: {} 处理登录", strategy.getClass().getSimpleName());
        return strategy.login(request, httpRequest);
    }
}
