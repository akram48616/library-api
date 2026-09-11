package org.example.librarymanagementsystem.DTO.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.librarymanagementsystem.entity.Member;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Member response data")
public class MemberResponseDTO implements Serializable {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private LocalDate memberShipDate;
    private Member.MembershipStatus membershipStatus;

}
