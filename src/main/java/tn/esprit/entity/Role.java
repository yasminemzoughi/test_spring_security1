package tn.esprit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.FetchMode;
import org.hibernate.annotations.Fetch;
import tn.esprit.entity.Permission;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    private RoleEnum name;


    @ElementCollection(fetch = FetchType.EAGER, targetClass = Permission.class)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();

}
