package com.project.foradhd.domain.user.business.service.impl;

import com.project.foradhd.domain.user.business.dto.in.*;
import com.project.foradhd.domain.user.business.dto.out.UserProfileDetailsData;
import com.project.foradhd.domain.user.business.service.UserAuthInfoService;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.*;
import com.project.foradhd.domain.user.persistence.enums.Provider;
import com.project.foradhd.domain.user.persistence.enums.Role;
import com.project.foradhd.domain.user.persistence.repository.*;
import com.project.foradhd.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.project.foradhd.global.exception.ErrorCode.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPrivacyRepository userPrivacyRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserBlockedRepository userBlockedRepository;
    private final TermsRepository termsRepository;
    private final UserTermsApprovalRepository userTermsApprovalRepository;
    private final PushNotificationApprovalRepository pushNotificationApprovalRepository;
    private final UserPushNotificationApprovalRepository userPushNotificationApprovalRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserAuthInfoService userAuthInfoService;

    @Override
    public boolean checkNickname(String nickname) {
        return userProfileRepository.findByNickname(nickname).isEmpty();
    }

    @Override
    public boolean checkEmail(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Override
    public UserProfileDetailsData getUserProfileDetails(String userId) {
        UserProfile userProfile = getUserProfileFetch(userId);
        return UserProfileDetailsData.builder()
                .userProfile(userProfile)
                .userRole(userProfile.getUser().getRole())
                .build();
    }

    @Override
    @Transactional
    public User signUp(SignUpData signUpData) {
        User user = signUpData.getUser();
        UserPrivacy userPrivacy = signUpData.getUserPrivacy();
        UserProfile userProfile = signUpData.getUserProfile();
        List<UserTermsApproval> userTermsApprovals = signUpData.getUserTermsApprovals();
        List<UserPushNotificationApproval> userPushNotificationApprovals = signUpData.getUserPushNotificationApprovals();
        validateDuplicatedEmail(user.getEmail());
        validateDuplicatedNickname(userProfile.getNickname());
        validateTermsApprovals(userTermsApprovals);
        validatePushNotificationApprovals(userPushNotificationApprovals);

        userRepository.save(user);
        userPrivacyRepository.save(userPrivacy);
        userProfileRepository.save(userProfile);
        userTermsApprovalRepository.saveAll(userTermsApprovals);
        userPushNotificationApprovalRepository.saveAll(userPushNotificationApprovals);
        userAuthInfoService.signUpByPassword(user, signUpData.getPassword());
        return user;
    }

    @Override
    @Transactional
    public User snsSignUp(String userId, SnsSignUpData snsSignUpData) {
        UserProfile userProfile = snsSignUpData.getUserProfile();
        List<UserTermsApproval> userTermsApprovals = snsSignUpData.getUserTermsApprovals();
        List<UserPushNotificationApproval> userPushNotificationApprovals = snsSignUpData.getUserPushNotificationApprovals();
        validateDuplicatedNickname(userProfile.getNickname());
        validateTermsApprovals(userTermsApprovals);
        validatePushNotificationApprovals(userPushNotificationApprovals);

        User user = getUser(userId);
        boolean hasProfile = true;
        user.updateAsUserRole(hasProfile);

        userProfileRepository.save(userProfile);
        userTermsApprovalRepository.saveAll(userTermsApprovals);
        userPushNotificationApprovalRepository.saveAll(userPushNotificationApprovals);
        return user;
    }

    @Override
    @Transactional
    public void blockUser(String userId, String blockedUserId, Boolean isBlocked) {
        User user = getUser(userId);
        User blockedUser = getUser(blockedUserId);
        userBlockedRepository.findByUserIdAndBlockedUserId(userId, blockedUserId)
                .ifPresentOrElse(
                        userBlocked -> userBlocked.updateIsBlocked(isBlocked),
                        () -> {
                            UserBlocked userBlocked = UserBlocked.builder()
                                    .user(user)
                                    .blockedUser(blockedUser)
                                    .deleted(!isBlocked)
                                    .build();
                            userBlockedRepository.save(userBlocked);
                        });
    }

    @Override
    public List<UserBlocked> getUserBlockedList(String userId) {
        return userBlockedRepository.findByUserId(userId);
    }

    @Override
    public List<String> getBlockedUserIdList(String userId) {
        return getUserBlockedList(userId).stream()
                .map(userBlocked -> userBlocked.getBlockedUser().getId())
                .toList();
    }

    @Override
    @Transactional
    public void updateProfile(String userId, ProfileUpdateData profileUpdateData) {
        UserProfile newUserProfile = profileUpdateData.getUserProfile();
        validateDuplicatedNickname(newUserProfile.getNickname(), userId);
        UserProfile originUserProfile = getUserProfile(userId);
        originUserProfile.updateProfile(newUserProfile);
    }

    @Override
    @Transactional
    public void updatePassword(String userId, PasswordUpdateData passwordUpdateData) {
        String prevPassword = passwordUpdateData.getPrevPassword();
        String newPassword = passwordUpdateData.getPassword();
        userAuthInfoService.validatePasswordMatches(userId, prevPassword);
        userAuthInfoService.updatePassword(userId, newPassword);
    }

    @Override
    @Transactional
    public User updateEmailAuth(String userId, String email) {
        User user = getUser(userId);
        boolean isVerifiedEmail = true;
        boolean hasProfile = hasUserProfile(user.getId());
        user.updateAsUserRole(isVerifiedEmail, hasProfile);
        user.updateEmail(email);
        return user;
    }

    @Override
    @Transactional
    public void updatePushNotificationApprovals(PushNotificationApprovalUpdateData pushNotificationApprovalUpdateData) {
        List<UserPushNotificationApproval> userPushNotificationApprovals = pushNotificationApprovalUpdateData.getUserPushNotificationApprovals();
        validatePushNotificationApprovals(userPushNotificationApprovals);
        userPushNotificationApprovalRepository.saveAll(userPushNotificationApprovals);
    }

    @Override
    @Transactional
    public void updateTermsApprovals(TermsApprovalsUpdateData termsApprovalsUpdateData) {
        List<UserTermsApproval> userTermsApprovals = termsApprovalsUpdateData.getUserTermsApprovals();
        validateTermsApprovals(userTermsApprovals);
        userTermsApprovalRepository.saveAll(userTermsApprovals); //update or insert
    }

    @Override
    @Transactional
    public void withdraw(String userId) {
        //유저, 인증 관련 데이터 삭제
        userTermsApprovalRepository.deleteByUserId(userId);
        userPushNotificationApprovalRepository.deleteByUserId(userId);
        userDeviceRepository.deleteByUserId(userId);
        userAuthInfoService.withdraw(userId);

        //유저 정보 기본값으로 초기화
        User user = getUser(userId);
        UserProfile userProfile = getUserProfile(userId);
        UserPrivacy userPrivacy = getUserPrivacy(userId);
        user.withdraw();
        userProfile.withdraw();
        userPrivacy.withdraw();
    }

    @Override
    @Transactional
    public void saveUserInfo(User user, UserPrivacy userPrivacy) {
        userRepository.save(user);
        userPrivacyRepository.save(userPrivacy);
    }

    @Override
    public User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER));
    }

    @Override
    public UserProfile getUserProfile(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER_PROFILE));
    }

    @Override
    public UserProfile getUserProfileFetch(String userId) {
        return userProfileRepository.findByUserIdFetch(userId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER_PROFILE));
    }

    @Override
    public UserPrivacy getUserPrivacy(String userId) {
        return userPrivacyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER_PRIVACY));
    }

    @Override
    public Optional<User> getSignedUpUser(String email, Provider provider, String externalUserId) {
        return userRepository.findByEmailOrProviderAndExternalUserId(email, provider, externalUserId);
    }

    @Override
    public Boolean hasVerifiedEmail(String userId) {
        return getUser(userId).getIsVerifiedEmail();
    }

    @Override
    public boolean hasUserProfile(String userId) {
        return userRepository.findByIdWithProfile(userId)
                .isPresent();
    }

    @Override
    public void validateDuplicatedEmail(String email) {
        boolean isExistingUser = userRepository.findByEmail(email).isPresent();
        if (isExistingUser) {
            throw new BusinessException(ALREADY_EXISTS_EMAIL);
        }
    }

    @Override
    public void validateDuplicatedEmail(String email, String userId) {
        boolean isExistingUser = userRepository.findByEmailAndUserIdNot(email, userId).isPresent();
        if (isExistingUser) {
            throw new BusinessException(ALREADY_EXISTS_EMAIL);
        }
    }

    @Override
    public void validateDuplicatedNickname(String nickname) {
        boolean isDuplicatedNickname = userProfileRepository.findByNickname(nickname).isPresent();
        if (isDuplicatedNickname) {
            throw new BusinessException(ALREADY_EXISTS_NICKNAME);
        }
    }

    @Override
    public void validateDuplicatedNickname(String nickname, String userId) {
        boolean isDuplicatedNickname = userProfileRepository.findByNicknameAndUserIdNot(nickname, userId)
                .isPresent();
        if (isDuplicatedNickname) {
            throw new BusinessException(ALREADY_EXISTS_NICKNAME);
        }
    }

    @Override
    public void validateTermsApprovals(List<UserTermsApproval> userTermsApprovals) {
        List<Terms> termsList = termsRepository.findAll();
        for (UserTermsApproval userTermsApproval : userTermsApprovals) {
            Long termsId = userTermsApproval.getId().getTerms().getId();
            validateTerms(termsId, userTermsApproval.getApproved(), termsList);
        }
    }

    private void validateTerms(Long termsId, Boolean approved, List<Terms> termsList) {
        for (Terms terms : termsList) {
            if (!Objects.equals(terms.getId(), termsId)) continue;
            if (!terms.getRequired() || approved) return;
            throw new BusinessException(REQUIRED_TERMS_APPROVAL);
        }
        throw new BusinessException(NOT_FOUND_TERMS);
    }

    @Override
    public void validatePushNotificationApprovals(
            List<UserPushNotificationApproval> userPushNotificationApprovals) {
        List<PushNotificationApproval> pushNotificationApprovalList = pushNotificationApprovalRepository.findAll();
        for (UserPushNotificationApproval userPushNotificationApproval : userPushNotificationApprovals) {
            Long pushNotificationApprovalId = userPushNotificationApproval.getId().getPushNotificationApproval().getId();
            validatePushNotificationApproval(pushNotificationApprovalId, pushNotificationApprovalList);
        }
    }

    private void validatePushNotificationApproval(Long pushNotificationApprovalId,
                                                List<PushNotificationApproval> pushNotificationApprovalList) {
        boolean isValidPushNotificationApproval = pushNotificationApprovalList.stream()
                .anyMatch(pushNotificationApproval ->
                        pushNotificationApproval.getId().equals(pushNotificationApprovalId));
        if (!isValidPushNotificationApproval) {
            throw new BusinessException(NOT_FOUND_PUSH_NOTIFICATION_APPROVAL);
        }
    }
}
