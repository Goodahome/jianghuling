package com.jianghu.ling.auth.sms;

public interface SmsSender {
    void send(String phone, String scene, String code);
}
