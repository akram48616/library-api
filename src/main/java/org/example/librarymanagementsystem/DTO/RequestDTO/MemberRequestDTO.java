package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.librarymanagementsystem.entity.Member;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for registering or updating a member")
public class MemberRequestDTO {

    @Schema(description = "Member's first name", example = "Akram", minLength = 2, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Firstname is required")
    @Size(min = 2, max = 50, message = "Firstname must be 2-50 characters")
    private String firstname;

    @Schema(description = "Member's last name", example = "Shaik", minLength = 2, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Lastname is required")
    @Size(min = 2, max = 50, message = "Lastname must be 2-50 characters")
    private String lastname;

    @Schema(description = "Member's email address, must be unique", example = "akram@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Schema(description = "Member's phone number, exactly 10 digits", example = "9876543210", pattern = "^[0-9]{10}$", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
    private String phone;

    @Schema(description = "Membership start date; defaults to today if omitted", example = "2026-01-15")
    private LocalDate membershipDate;

    @Schema(description = "Membership status; defaults to ACTIVE if omitted", example = "ACTIVE")
    private Member.MembershipStatus membershipStatus;
}