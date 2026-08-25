package com.renko.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Getter
@Setter
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreContactEntity
{
    @Email(message = "Invalid email")
    public String email;

    public String phone;
    public String address;
}
