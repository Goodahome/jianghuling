package com.jianghu.ling.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.auth.dto.LoginRequest;
import com.jianghu.ling.auth.dto.RegisterRequest;
import com.jianghu.ling.auth.sms.SmsSender;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.config.AppProperties;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.JwtService;
import com.jianghu.ling.security.PrincipalType;
import com.jianghu.ling.user.domain.*;
import com.jianghu.ling.user.mapper.*;
import com.jianghu.ling.user.service.UserAssetService;
import com.jianghu.ling.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SmsSender smsSender;
    private final AppProperties appProperties;
    private final StringRedisTemplate redisTemplate;
    private final InviteCodeMapper inviteCodeMapper;
    private final InviteRelationMapper inviteRelationMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserOfficeMapper userOfficeMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;
    private final UserAssetService userAssetService;
    private final ConfigService configService;

    public Map<String, Object> sendSms(String phone, String scene) {
        if (!"REGISTER".equals(scene) && !"LOGIN".equals(scene)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "scene无效");
        }
        String code = appProperties.getMockSmsCode();
        if (!StringUtils.hasText(code)) {
            code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        }
        redisTemplate.opsForValue().set(smsKey(scene, phone), code, Duration.ofMinutes(5));
        smsSender.send(phone, scene, code);
        return Map.of("expireIn", 300);
    }

    public Map<String, Object> validateInvite(String inviteCode) {
        InviteCode code = findValidInvite(inviteCode);
        String nickname = "平台";
        if (code.getOwnerUserId() != null) {
            UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                    .eq(UserProfile::getUserId, code.getOwnerUserId())
                    .last("LIMIT 1"));
            if (profile != null) {
                nickname = profile.getNickname();
            }
        }
        return Map.of("valid", true, "inviterNickname", nickname);
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req, HttpServletRequest request) {
        InviteCode invite = findValidInvite(req.getInviteCode());
        boolean useSms = StringUtils.hasText(req.getSmsCode());
        boolean usePwd = StringUtils.hasText(req.getPassword());
        if (!useSms && !usePwd) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请提供验证码或密码");
        }
        if (useSms) {
            verifySms("REGISTER", req.getPhone(), req.getSmsCode());
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "手机号已注册");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());
        user.setPasswordHash(usePwd ? passwordEncoder.encode(req.getPassword()) : null);
        user.setStatus("ACTIVE");
        user.setCity("遵义");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname(req.getNickname());
        profile.setAvatarUrl("");
        profile.setBio("");
        profile.setRealNameStatus("NONE");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.insert(profile);

        walletService.getOrCreate(user.getId());
        userAssetService.getOrCreate(user.getId());

        invite.setUsedCount(invite.getUsedCount() + 1);
        inviteCodeMapper.updateById(invite);

        InviteRelation relation = new InviteRelation();
        relation.setInviterId(invite.getOwnerUserId() == null ? 0L : invite.getOwnerUserId());
        relation.setInviteeId(user.getId());
        relation.setInviteCodeId(invite.getId());
        relation.setCreatedAt(LocalDateTime.now());
        inviteRelationMapper.insert(relation);

        return issueLoginResponse(user, request);
    }

    @Transactional
    public Map<String, Object> login(LoginRequest req, HttpServletRequest request) {
        User user;
        if ("PASSWORD".equalsIgnoreCase(req.getLoginType())) {
            if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "用户名或密码不能为空");
            }
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, req.getUsername())
                    .last("LIMIT 1"));
            if (user == null || user.getPasswordHash() == null
                    || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
                writeLoginLog(null, null, request, "FAIL");
                throw new BizException(ErrorCode.PARAM_INVALID, "用户名或密码错误");
            }
        } else if ("SMS".equalsIgnoreCase(req.getLoginType())) {
            if (!StringUtils.hasText(req.getPhone()) || !StringUtils.hasText(req.getSmsCode())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "手机号或验证码不能为空");
            }
            verifySms("LOGIN", req.getPhone(), req.getSmsCode());
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, req.getPhone())
                    .last("LIMIT 1"));
            if (user == null) {
                writeLoginLog(null, null, request, "FAIL");
                throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
            }
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "loginType无效");
        }
        assertUserActive(user);
        return issueLoginResponse(user, request);
    }

    public void logout() {
        AuthPrincipal principal = AuthContext.get();
        if (principal != null) {
            jwtService.blacklist(principal.getJti(), appJwtRemain());
        }
    }

    public Map<String, Object> me() {
        Long userId = AuthContext.requireUserId();
        User user = userMapper.selectById(userId);
        assertUserActive(user);
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        UserAsset asset = userAssetService.getOrCreate(userId);
        Map<String, Object> wallet = walletService.accountView(userId);
        List<String> offices = activeOffices(userId);
        int dayLimit = configService.getInt("claim_day_limit", 10);
        int todayClaims = todayClaimCount(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("phone", user.getPhone());
        data.put("username", user.getUsername());
        data.put("nickname", profile.getNickname());
        data.put("avatarUrl", profile.getAvatarUrl());
        data.put("bio", profile.getBio());
        data.put("realNameStatus", profile.getRealNameStatus());
        data.put("city", user.getCity());
        data.put("status", user.getStatus());
        data.put("level", userAssetService.levelOf(asset.getChivalry()));
        data.put("levelTitle", userAssetService.levelTitle(asset.getChivalry()));
        data.put("chivalry", asset.getChivalry());
        data.put("stamina", asset.getStamina());
        data.put("completedOrders", asset.getCompletedOrders());
        data.put("goodRate", asset.getGoodRate());
        data.put("reputationScore", asset.getReputationScore());
        data.put("balance", wallet.get("balance"));
        data.put("frozen", wallet.get("frozen"));
        data.put("todayClaimCount", todayClaims);
        data.put("claimDayLimit", dayLimit);
        data.put("isLord", false);
        data.put("offices", offices);
        return data;
    }

    private Map<String, Object> issueLoginResponse(User user, HttpServletRequest request) {
        JwtService.TokenResult token = jwtService.issue(user.getId(), PrincipalType.USER);
        writeLoginLog(user.getId(), null, request, "SUCCESS");
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, user.getId()).last("LIMIT 1"));
        UserAsset asset = userAssetService.getOrCreate(user.getId());
        Map<String, Object> userView = new LinkedHashMap<>();
        userView.put("id", user.getId());
        userView.put("nickname", profile.getNickname());
        userView.put("avatarUrl", profile.getAvatarUrl() == null ? "" : profile.getAvatarUrl());
        userView.put("level", userAssetService.levelOf(asset.getChivalry()));
        userView.put("levelTitle", userAssetService.levelTitle(asset.getChivalry()));
        userView.put("offices", activeOffices(user.getId()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token.token());
        data.put("expiresIn", token.expiresIn());
        data.put("user", userView);
        return data;
    }

    private InviteCode findValidInvite(String code) {
        InviteCode invite = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCode, code)
                .last("LIMIT 1"));
        if (invite == null || !"ACTIVE".equals(invite.getStatus())
                || invite.getUsedCount() >= invite.getQuota()
                || (invite.getExpireAt() != null && invite.getExpireAt().isBefore(LocalDateTime.now()))) {
            throw new BizException(ErrorCode.INVITE_INVALID);
        }
        return invite;
    }

    private void verifySms(String scene, String phone, String smsCode) {
        String cached = redisTemplate.opsForValue().get(smsKey(scene, phone));
        if (cached == null || !cached.equals(smsCode)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "验证码错误或已过期");
        }
        redisTemplate.delete(smsKey(scene, phone));
    }

    private String smsKey(String scene, String phone) {
        return "sms:" + scene + ":" + phone;
    }

    public void assertUserActive(User user) {
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if ("BANNED".equals(user.getStatus()) || "DISABLED".equals(user.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_BANNED);
        }
    }

    public List<String> activeOffices(Long userId) {
        List<UserOffice> list = userOfficeMapper.selectList(new LambdaQueryWrapper<UserOffice>()
                .eq(UserOffice::getUserId, userId)
                .eq(UserOffice::getStatus, "ACTIVE"));
        LocalDateTime now = LocalDateTime.now();
        return list.stream()
                .filter(o -> o.getEndAt() == null || o.getEndAt().isAfter(now))
                .map(UserOffice::getOfficeCode)
                .collect(Collectors.toList());
    }

    private int todayClaimCount(Long userId) {
        String key = "claim:day:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ":" + userId;
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0 : Integer.parseInt(val);
    }

    private void writeLoginLog(Long userId, Long adminId, HttpServletRequest request, String result) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setAdminId(adminId);
        log.setIp(request == null ? null : request.getRemoteAddr());
        log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    private long appJwtRemain() {
        return 7200;
    }
}
