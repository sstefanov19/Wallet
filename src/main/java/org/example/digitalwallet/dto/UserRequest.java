package org.example.digitalwallet.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.digitalwallet.model.MembershipStatus;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    private String email;
    private String username;
    private String password;

    private MembershipStatus status = MembershipStatus.FREE;

}
