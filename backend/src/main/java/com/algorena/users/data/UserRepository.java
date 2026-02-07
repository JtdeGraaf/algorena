package com.algorena.users.data;

import com.algorena.users.domain.Provider;
import com.algorena.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByOauthIdentity_ProviderAndOauthIdentity_ProviderId(Provider provider, String providerId);

    boolean existsByUsernameAndIdNot(String username, Long userId);

}
