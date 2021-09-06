package com.source.utils;

import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Source
 * @date 2021/07/29/14:10
 * @Description:
 */
public class Util {

    public static void printSystemUpBanner(Environment environment) {
        String banner = "-----------------------------------------\n" +
                "服务启动成功，时间：" + DateUtil.formatFullTime(LocalDateTime.now(), DateUtil.FULL_TIME_SPLIT_PATTERN) + "\n" +
                "服务名称：" + environment.getProperty("spring.application.name") + "\n" +
                "端口号：" + environment.getProperty("server.port") + "\n" +
                "-----------------------------------------";
        System.out.println(banner);
    }
}
