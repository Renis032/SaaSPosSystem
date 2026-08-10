package com.renko.model;

import com.renko.domain.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

@Entity // Represents a table in the database
@Getter // Lombok creates get method
@Setter // Lombok creates set method
@NoArgsConstructor // Generates an empty constructor
@AllArgsConstructor // Generates a constructor with all properties the class has
@EqualsAndHashCode // Generates equals and hashcode functions
public class User
{
    @Id // Key
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false) // Custom field in database, cannot be null
    private String fullName;

    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid") // must be a valid email
    private String email;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Column(nullable = false)
    private UserRole role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
