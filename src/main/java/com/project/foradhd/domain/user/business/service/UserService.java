package com.project.foradhd.domain.user.business.service;

import com.project.foradhd.domain.user.business.dto.in.PasswordUpdateData;
import com.project.foradhd.domain.user.business.dto.in.ProfileUpdateData;
import com.project.foradhd.domain.user.business.dto.in.PushNotificationApprovalUpdateData;
import com.project.foradhd.domain.user.business.dto.in.SignUpData;
import com.project.foradhd.domain.user.business.dto.in.SnsSignUpData;
import com.project.foradhd.domain.user.business.dto.in.TermsApprovalsUpdateData;
import com.project.foradhd.domain.user.business.dto.out.UserProfileDetailsData;
import com.project.foradhd.domain.user.persistence.entity.*;
import com.project.foradhd.domain.user.persistence.enums.Provider;

import com.project.foradhd.domain.user.persistence.enums.Role;
import java.util.List;
import java.util.Optional;

public interface UserService {

    boolean checkNickname(String nickname);

    boolean checkEmail(String email);

    UserProfileDetailsData getUserProfileDetails(String userId);

    User signUp(SignUpData signUpData);

    User snsSignUp(String userId, SnsSignUpData snsSignUpData);

    void blockUser(String userId, String blockedUserId, Boolean isBlocked);

    List<UserBlocked> getUserBlockedList(String userId);

    List<String> getBlockedUserIdList(String userId);

    void updateProfile(String userId, ProfileUpdateData profileUpdateData);

    void updatePassword(String userId, PasswordUpdateData passwordUpdateData);

    User updateEmailAuth(String userId, String email);

    void updatePushNotificationApprovals(PushNotificationApprovalUpdateData pushNotificationApprovalUpdateData);

    void updateTermsApprovals(TermsApprovalsUpdateData termsApprovalsUpdateData);

    void withdraw(String userId);

    void saveUserInfo(User user, UserPrivacy userPrivacy);

    User getUser(String userId);

    UserProfile getUserProfile(String userId);

    UserProfile getUserProfileFetch(String userId);

    UserPrivacy getUserPrivacy(String userId);

    Optional<User> getSignedUpUser(String email, Provider provider, String externalUserId);

    Boolean hasVerifiedEmail(String userId);

    boolean hasUserProfile(String userId);

    void validateDuplicatedEmail(String email);

    void validateDuplicatedEmail(String email, String userId);

    void validateDuplicatedNickname(String nickname);

    void validateDuplicatedNickname(String nickname, String userId);

    void validateTermsApprovals(List<UserTermsApproval> userTermsApprovals);

    void validatePushNotificationApprovals(List<UserPushNotificationApproval> userPushNotificationApprovals);
}
