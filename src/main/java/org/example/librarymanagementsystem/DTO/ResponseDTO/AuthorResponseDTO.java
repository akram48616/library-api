package org.example.librarymanagementsystem.DTO.ResponseDTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Author response data")
public class AuthorResponseDTO implements Serializable {

    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String nationality;
    // books NOT included — avoids circular response
}
