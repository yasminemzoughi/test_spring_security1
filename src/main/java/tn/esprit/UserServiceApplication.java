package tn.esprit;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.auth.AuthenticationService;
import tn.esprit.dto.RegistrationRequest;
import tn.esprit.entity.Permission;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.security.JwtService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@EnableJpaAuditing
@EnableAsync
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "tn.esprit")

@Slf4j
public class UserServiceApplication {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    @Transactional
    public CommandLineRunner commandLineRunner(
            AuthenticationService service,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        return args -> {
            initializeRoles(roleRepository);
        };
    }

    private void initializeRoles(RoleRepository roleRepository) {
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = Role.builder()
                        .name(roleEnum)
                        .permissions(new HashSet<>(roleEnum.getPermissions()))
                        .build();

                roleRepository.save(role);
                log.info("Created role: {} with permissions: {}",
                        roleEnum.name(), roleEnum.getPermissions());
            }
        });
    }

/*    private void initializeAdminUser(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     RoleRepository roleRepository,
                                     JwtService jwtService,
                                     AuthenticationService authService) {

        if (!userRepository.existsByEmail(adminEmail)) {
            try {
                User admin = authService.createAdminUser(
                        "Admin",
                        "Admin",
                        adminEmail,
                        adminPassword
                );

                // Verify the admin has the ADMIN role with permissions
                admin = userRepository.findByEmail(adminEmail).orElseThrow();
                log.info("Admin roles: {}", admin.getRoles());
                admin.getRoles().forEach(role ->
                        log.info("Role {} permissions: {}", role.getName(), role.getPermissions()));

                String token = jwtService.generateToken(admin);
                log.info("Admin user created successfully with token: {}", token);

                // Use the new public method instead
                Claims claims = jwtService.getAllClaims(token);
                log.info("Token claims: {}", claims);
            } catch (Exception e) {
                log.error("Failed to create admin user", e);
                throw new RuntimeException("Admin user creation failed", e);
            }
        } else {
            log.info("Admin user already exists");
        }
    }

 */
}
