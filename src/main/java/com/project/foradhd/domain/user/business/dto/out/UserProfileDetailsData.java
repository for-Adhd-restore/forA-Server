package com.project.foradhd.domain.user.business.dto.out;

import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import com.project.foradhd.domain.user.persistence.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileDetailsData {

    private UserProfile userProfile;

    private Role userRole;
}
