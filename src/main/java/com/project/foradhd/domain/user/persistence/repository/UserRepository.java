package com.project.foradhd.domain.user.persistence.repository;

import com.project.foradhd.domain.board.persistence.enums.HandleReport;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.enums.Provider;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query("""
        select u from User u
        where u.email = :email and u.id <> :userId
        """)
    Optional<User> findByEmailAndUserIdNot(@Param("email") String email, @Param("userId") String userId);

    @Query("""
        select u from User u 
        left join AuthSocialLogin asl on asl.user.id = u.id 
        where u.email = :email or (asl.provider = :provider and asl.externalUserId = :externalUserId)
    """)
    Optional<User> findByEmailOrProviderAndExternalUserId(@Param("email") String email,
        @Param("provider") Provider provider, @Param("externalUserId") String externalUserId);

    @Query("select u from User u inner join UserProfile up on up.user.id = u.id where u.id = :userId")
    Optional<User> findByIdWithProfile(@Param("userId") String userId);

    @Query("select u from User u where u.status = :status" )
    List<User> findByStatus(@Param("status") HandleReport status);
}
