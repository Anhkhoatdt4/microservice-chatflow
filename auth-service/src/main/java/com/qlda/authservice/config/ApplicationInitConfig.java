package com.qlda.authservice.config;

import com.qlda.authservice.constant.PredefinedRole;
import com.qlda.authservice.entity.User;
import com.qlda.authservice.entity.Role;
import com.qlda.authservice.repository.RoleRepository;
import com.qlda.authservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driver-class-name",
            havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // Kiểm tra nếu user admin chưa tồn tại mới tạo
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {

                // Tạo role USER nếu chưa tồn tại
                if (roleRepository.findById(PredefinedRole.USER_ROLE).isEmpty()) {
                    roleRepository.save(Role.builder()
                            .name(PredefinedRole.USER_ROLE)
                            .description("User role")
                            .build());
                }

                Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                        .orElseGet(() -> roleRepository.save(Role.builder()
                                .name(PredefinedRole.ADMIN_ROLE)
                                .description("Admin role")
                                .build()));

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                // Tạo user admin
                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .email("admin123@gmail.com")
                        .enabled(true)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("Admin user created with default password: admin. Please change it!");
            } else {
                log.info("Admin user already exists");
            }

            log.info("Application initialization completed .....");
        };
    }

    private Role createRoleIfNotExists(RoleRepository roleRepository, String roleName, String description) {
        return roleRepository.findById(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(description)
                        .build()));
    }
}
