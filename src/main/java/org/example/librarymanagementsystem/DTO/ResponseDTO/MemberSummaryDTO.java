package org.example.librarymanagementsystem.DTO.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Member summary data")
public class MemberSummaryDTO implements Serializable {
    private Integer id;
    private String firstname;
    private String lastname;
}