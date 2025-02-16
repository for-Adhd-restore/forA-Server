package com.project.foradhd.domain.user.business.dto.out;

import com.project.foradhd.domain.user.persistence.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserRoleData {
    private Role role;
}
