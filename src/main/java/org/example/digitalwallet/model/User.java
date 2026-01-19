package org.example.digitalwallet.model;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private String email;

    private String username;

    private String password;

    private MembershipStatus membershipStatus;

    @Builder.Default
    private Roles role = Roles.USER;
}
