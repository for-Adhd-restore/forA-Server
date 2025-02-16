package com.project.foradhd.domain.user.web.mapper;

import com.project.foradhd.domain.user.business.dto.in.*;
import com.project.foradhd.domain.user.business.dto.out.UserRoleData;
import com.project.foradhd.domain.user.business.dto.out.UserTokenData;
import com.project.foradhd.domain.user.business.dto.out.UserProfileDetailsData;
import com.project.foradhd.domain.user.persistence.entity.PushNotificationApproval;
import com.project.foradhd.domain.user.persistence.entity.Terms;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.entity.UserPrivacy;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import com.project.foradhd.domain.user.persistence.entity.UserPushNotificationApproval;
import com.project.foradhd.domain.user.persistence.entity.UserPushNotificationApproval.UserPushNotificationApprovalId;
import com.project.foradhd.domain.user.persistence.entity.UserTermsApproval;
import com.project.foradhd.domain.user.persistence.entity.UserTermsApproval.UserTermsApprovalId;
import com.project.foradhd.domain.user.persistence.enums.Role;
import com.project.foradhd.domain.user.web.dto.request.*;
import com.project.foradhd.domain.user.web.dto.response.EmailAuthValidationResponse;
import com.project.foradhd.domain.user.web.dto.response.SignUpResponse;
import com.project.foradhd.domain.user.web.dto.response.SnsSignUpResponse;
import com.project.foradhd.domain.user.web.dto.response.UserProfileDetailsResponse;
import com.project.foradhd.domain.user.web.dto.response.UserRoleResponse;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.Mappings;

@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {

    @Mappings({
        @Mapping(target = "email", source = "userProfile.user.email"),
        @Mapping(target = "nickname", source = "userProfile.nickname"),
        @Mapping(target = "profileImage", source = "userProfile.profileImage"),
        @Mapping(target = "forAdhdType", source = "userProfile.forAdhdType")
    })
    UserProfileDetailsResponse toUserProfileDetailsResponse(UserProfileDetailsData userProfileDetailsData);

    default SignUpData toSignUpData(SignUpRequest request) {
        User user = User.builder()
            .email(request.getEmail())
            .role(Role.USER) //일반 회원가입 시 이메일 인증이 선제조건
            .isVerifiedEmail(Boolean.TRUE)
            .build();
        UserPrivacy userPrivacy = UserPrivacy.builder()
            .user(user)
            .name(request.getName())
            .birth(request.getBirth())
            .gender(request.getGender())
            .build();
        UserProfile userProfile = UserProfile.builder()
            .user(user)
            .nickname(request.getNickname())
            .profileImage(request.getProfileImage())
            .forAdhdType(request.getForAdhdType())
            .build();
        List<UserTermsApproval> userTermsApprovals = request.getTermsApprovals().stream()
            .map(termsApproval ->
                mapToUserTermsApproval(user, termsApproval.getTermsId(), termsApproval.getApproved()))
            .toList();
        List<UserPushNotificationApproval> userPushNotificationApprovals = request.getPushNotificationApprovals().stream()
            .map(pushNotificationApproval ->
                mapToUserPushNotificationApproval(user, pushNotificationApproval.getPushNotificationApprovalId(),
                    pushNotificationApproval.getApproved()))
            .toList();

        return SignUpData.builder()
            .user(user)
            .userPrivacy(userPrivacy)
            .userProfile(userProfile)
            .password(request.getPassword().getPassword())
            .userTermsApprovals(userTermsApprovals)
            .userPushNotificationApprovals(userPushNotificationApprovals)
            .build();
    }

    default UserPushNotificationApproval mapToUserPushNotificationApproval(User user, Long pushNotificationApprovalId, Boolean approved) {
        return UserPushNotificationApproval.builder()
            .id(mapToUserPushNotificationApprovalId(user, pushNotificationApprovalId))
            .approved(approved)
            .build();
    }

    default UserPushNotificationApproval mapToUserPushNotificationApproval(String userId, Long pushNotificationApprovalId, Boolean approved) {
        return UserPushNotificationApproval.builder()
            .id(mapToUserPushNotificationApprovalId(userId, pushNotificationApprovalId))
            .approved(approved)
            .build();
    }

    default UserPushNotificationApprovalId mapToUserPushNotificationApprovalId(User user, Long pushNotificationApprovalId) {
        PushNotificationApproval pushNotificationApproval = PushNotificationApproval.builder()
            .id(pushNotificationApprovalId).build();
        return new UserPushNotificationApprovalId(user, pushNotificationApproval);
    }

    default UserPushNotificationApprovalId mapToUserPushNotificationApprovalId(String userId, Long pushNotificationApprovalId) {
        User user = User.builder().id(userId).build();
        PushNotificationApproval pushNotificationApproval = PushNotificationApproval.builder()
            .id(pushNotificationApprovalId).build();
        return new UserPushNotificationApprovalId(user, pushNotificationApproval);
    }

    default SnsSignUpData toSnsSignUpData(@Context String userId, SnsSignUpRequest request) {
        User user = User.builder().id(userId).build();
        UserProfile userProfile = UserProfile.builder()
            .user(user)
            .nickname(request.getNickname())
            .profileImage(request.getProfileImage())
            .forAdhdType(request.getForAdhdType())
            .build();
        List<UserTermsApproval> userTermsApprovals = request.getTermsApprovals().stream()
            .map(termsApproval ->
                mapToUserTermsApproval(userId, termsApproval.getTermsId(),
                    termsApproval.getApproved()))
            .toList();
        List<UserPushNotificationApproval> userPushNotificationApprovals = request.getPushNotificationApprovals().stream()
            .map(pushNotificationApproval ->
                mapToUserPushNotificationApproval(userId,
                    pushNotificationApproval.getPushNotificationApprovalId(),
                    pushNotificationApproval.getApproved()))
            .toList();
        return SnsSignUpData.builder()
            .userProfile(userProfile)
            .userTermsApprovals(userTermsApprovals)
            .userPushNotificationApprovals(userPushNotificationApprovals)
            .build();
    }

    default UserTermsApprovalId mapToUserTermsApprovalId(User user, Long termsId) {
        Terms terms = Terms.builder().id(termsId).build();
        return new UserTermsApprovalId(user, terms);
    }

    default UserTermsApprovalId mapToUserTermsApprovalId(String userId, Long termsId) {
        User user = User.builder().id(userId).build();
        Terms terms = Terms.builder().id(termsId).build();
        return new UserTermsApprovalId(user, terms);
    }

    default UserTermsApproval mapToUserTermsApproval(User user, Long termsId, Boolean approved) {
        return UserTermsApproval.builder()
            .id(mapToUserTermsApprovalId(user, termsId))
            .approved(approved)
            .build();
    }

    default UserTermsApproval mapToUserTermsApproval(String userId, Long termsId, Boolean approved) {
        return UserTermsApproval.builder()
            .id(mapToUserTermsApprovalId(userId, termsId))
            .approved(approved)
            .build();
    }

    @Mappings({
        @Mapping(target = "userProfile.nickname", source = "nickname"),
        @Mapping(target = "userProfile.profileImage", source = "profileImage"),
        @Mapping(target = "userProfile.forAdhdType", source = "forAdhdType")
    })
    ProfileUpdateData toProfileUpdateData(ProfileUpdateRequest request);

    @Mappings({
            @Mapping(target = "password", source = "password.password"),
            @Mapping(target = "passwordConfirm", source = "password.passwordConfirm"),
    })
    PasswordUpdateData toPasswordUpdateData(PasswordUpdateRequest request);

    default PushNotificationApprovalUpdateData toPushNotificationApprovalUpdateData(String userId,
        PushNotificationApprovalUpdateRequest request) {
        List<UserPushNotificationApproval> userPushNotificationApprovals = request.getPushNotificationApprovals()
            .stream()
            .map(pushNotificationApproval ->
                mapToUserPushNotificationApproval(userId,
                    pushNotificationApproval.getPushNotificationApprovalId(),
                    pushNotificationApproval.getApproved()))
            .toList();
        return new PushNotificationApprovalUpdateData(userPushNotificationApprovals);
    }

    default TermsApprovalsUpdateData toTermsApprovalsUpdateData(String userId, TermsApprovalsUpdateRequest request) {
        List<UserTermsApproval> userTermsApprovals = request.getTermsApprovals().stream()
            .map(termsApproval ->
                mapToUserTermsApproval(userId, termsApproval.getTermsId(), termsApproval.getApproved()))
            .toList();
        return new TermsApprovalsUpdateData(userTermsApprovals);
    }

    @Mapping(target = "hasVerifiedEmail", source = "user.isVerifiedEmail")
    SignUpResponse toSignUpResponse(UserTokenData userTokenData, User user);

    @Mapping(target = "hasVerifiedEmail", source = "user.isVerifiedEmail")
    @Mapping(target = "hasProfile", constant = "true")
    SnsSignUpResponse toSnsSignUpResponse(UserTokenData userTokenData, User user);

    EmailAuthData toEmailAuthData(EmailAuthRequest request);

    EmailAuthValidationData toEmailAuthValidationData(EmailAuthValidationRequest request);

    EmailAuthValidationResponse toEmailAuthValidationResponse(UserTokenData userTokenData);

    UserRoleResponse toUserRoleResponse(UserRoleData userRoleData);
}
