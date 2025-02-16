package com.project.foradhd.domain.user.web.controller;

import com.project.foradhd.domain.user.business.dto.out.UserRoleData;
import com.project.foradhd.domain.user.web.dto.response.UserRoleResponse;
import com.project.foradhd.domain.user.business.dto.in.*;
import com.project.foradhd.domain.user.business.dto.out.UserTokenData;
import com.project.foradhd.domain.user.business.dto.out.UserProfileDetailsData;
import com.project.foradhd.domain.user.business.service.UserEmailAuthService;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.business.service.UserTokenService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.web.dto.request.*;
import com.project.foradhd.domain.user.web.dto.response.*;
import com.project.foradhd.domain.user.web.mapper.UserMapper;
import com.project.foradhd.global.AuthUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    private final UserService userService;
    private final UserTokenService userTokenService;
    private final UserEmailAuthService userEmailAuthService;
    private final UserMapper userMapper;

    @GetMapping("/nickname-check")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@ModelAttribute @Valid NicknameCheckRequest request) {
        boolean isValidNickname = userService.checkNickname(request.getNickname());
        return ResponseEntity.ok(new NicknameCheckResponse(isValidNickname));
    }

    @GetMapping("/email-check")
    public ResponseEntity<EmailCheckResponse> checkEmail(@ModelAttribute @Valid EmailCheckRequest request) {
        boolean isValidEmail = userService.checkEmail(request.getEmail());
        return ResponseEntity.ok(new EmailCheckResponse(isValidEmail));
    }

    @GetMapping
    public ResponseEntity<UserProfileDetailsResponse> getUserProfileDetails(@AuthUserId String userId) {
        UserProfileDetailsData userProfileDetailsData = userService.getUserProfileDetails(userId);
        UserProfileDetailsResponse response = userMapper.toUserProfileDetailsResponse(userProfileDetailsData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role")
    public ResponseEntity<UserRoleResponse> getUserRole(@AuthUserId String userId) {
        UserRoleData userRoleData = userService.getUserRole(userId);
        UserRoleResponse response = userMapper.toUserRoleResponse(userRoleData);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        SignUpData signUpData = userMapper.toSignUpData(request);
        User user = userService.signUp(signUpData);
        UserTokenData userTokenData = userTokenService.generateToken(user);
        return new ResponseEntity<>(userMapper.toSignUpResponse(userTokenData, user), HttpStatus.CREATED);
    }

    @PostMapping("/sns-sign-up")
    public ResponseEntity<SnsSignUpResponse> snsSignUp(@AuthUserId String userId,
        @RequestBody @Valid SnsSignUpRequest request) {
        SnsSignUpData snsSignUpData = userMapper.toSnsSignUpData(userId, request);
        User user = userService.snsSignUp(userId, snsSignUpData);
        UserTokenData userTokenData = userTokenService.generateToken(user);
        return new ResponseEntity<>(userMapper.toSnsSignUpResponse(userTokenData, user), HttpStatus.CREATED);
    }

    @PostMapping("/email-auth")
    public ResponseEntity<Void> authenticateEmail(@AuthUserId String userId,
                                                @RequestBody @Valid EmailAuthRequest request) {
        EmailAuthData emailAuthData = userMapper.toEmailAuthData(request);
        userEmailAuthService.authenticateEmail(userId, emailAuthData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/block")
    public ResponseEntity<Void> blockUser(@AuthUserId String userId, @RequestBody @Valid UserBlockRequest request) {
        userService.blockUser(userId, request.getBlockedUserId(), request.getIsBlocked());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email-auth")
    public ResponseEntity<EmailAuthValidationResponse> validateEmailAuth(@AuthUserId String userId,
                                                                @RequestBody @Valid EmailAuthValidationRequest request) {
        EmailAuthValidationData emailAuthValidationData = userMapper.toEmailAuthValidationData(request);
        UserTokenData userTokenData = userEmailAuthService.validateEmailAuth(userId, emailAuthValidationData)
                .map(userTokenService::generateToken)
                .orElse(new UserTokenData());
        return ResponseEntity.ok(userMapper.toEmailAuthValidationResponse(userTokenData));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthUserId String userId,
        @RequestBody @Valid ProfileUpdateRequest request) {
        ProfileUpdateData profileUpdateData = userMapper.toProfileUpdateData(request);
        userService.updateProfile(userId, profileUpdateData);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthUserId String userId,
        @RequestBody @Valid PasswordUpdateRequest request) {
        PasswordUpdateData passwordUpdateData = userMapper.toPasswordUpdateData(request);
        userService.updatePassword(userId, passwordUpdateData);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/push-notification-approvals")
    public ResponseEntity<Void> updatePushNotificationApprovals(@AuthUserId String userId,
        @RequestBody @Valid PushNotificationApprovalUpdateRequest request) {
        PushNotificationApprovalUpdateData pushNotificationApprovalUpdateData = userMapper
            .toPushNotificationApprovalUpdateData(userId, request);
        userService.updatePushNotificationApprovals(pushNotificationApprovalUpdateData);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/terms-approvals")
    public ResponseEntity<Void> updateTermsApprovals(@AuthUserId String userId,
        @RequestBody @Valid TermsApprovalsUpdateRequest request) {
        TermsApprovalsUpdateData termsApprovalsUpdateData = userMapper.toTermsApprovalsUpdateData(userId, request);
        userService.updateTermsApprovals(termsApprovalsUpdateData);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@AuthUserId String userId) {
        userService.withdraw(userId);
        return ResponseEntity.ok().build();
    }
}
