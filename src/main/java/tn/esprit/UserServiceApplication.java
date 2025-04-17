package tn.esprit;

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
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.repository.RoleRepository;

import java.util.Arrays;
import java.util.HashSet;

@EnableJpaAuditing
@EnableAsync
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "tn.esprit")

@Slf4j
public class UserServiceApplication {

    @Value("${app.admin.email}")
    private String adminEmail;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    @Transactional
    public CommandLineRunner commandLineRunner(
            RoleRepository roleRepository)
           {
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
