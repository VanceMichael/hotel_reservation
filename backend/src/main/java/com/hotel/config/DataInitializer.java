package com.hotel.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.entity.User;
import com.hotel.mapper.UserMapper;
import com.hotel.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据初始化器
 * 仅在开发环境下自动修复预置测试账号密码
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /** schema.sql 中预置的测试用户名白名单 */
    private static final List<String> TEST_USERNAMES = List.of(
            "admin", "manager", "zhangsan", "lisi", "wangwu", "zhaoliu", "sunqi"
    );

    private static final String TEST_PASSWORD = "123456";

    @Override
    public void run(String... args) {
        if (!"prod".equals(activeProfile)) {
            fixTestUserPasswords();
        }
        log.info("应用启动完成，当前环境: {}", activeProfile);
    }

    /**
     * 逐个检查并修复预置测试账号的密码（仅开发环境）。
     * 只处理白名单内的用户名，不会影响运行期间新注册的账号。
     */
    private void fixTestUserPasswords() {
        try {
            int fixedCount = 0;
            for (String username : TEST_USERNAMES) {
                User user = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getUsername, username));
                if (user == null) {
                    continue;
                }
                if (!PasswordUtil.matches(TEST_PASSWORD, user.getPassword())) {
                    User update = new User();
                    update.setId(user.getId());
                    update.setPassword(PasswordUtil.encode(TEST_PASSWORD));
                    userMapper.updateById(update);
                    fixedCount++;
                }
            }
            if (fixedCount > 0) {
                log.info("已修复 {} 个测试账号的密码", fixedCount);
            }
        } catch (Exception e) {
            log.warn("测试账号密码修复失败: {}", e.getMessage());
        }
    }
}
