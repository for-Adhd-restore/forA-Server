package com.project.foradhd.domain.user.web.dto.response;

import com.project.foradhd.domain.user.persistence.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRoleResponse {

    private Role role;
}
