package com.qlda.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(columnDefinition = "VARCHAR(36)")
    String id;

    @Column(nullable = false, unique = true)
    String username;
    String password;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    boolean enabled;

    String firstName;
    String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Role>roles;
}
