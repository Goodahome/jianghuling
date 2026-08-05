package com.jianghu.ling.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsSender implements SmsSender {

    @Override
    public void send(String phone, String scene, String code) {
        log.info("[MockSMS] phone={} scene={} code={}", phone, scene, code);
    }
}
