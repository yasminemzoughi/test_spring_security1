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


}
