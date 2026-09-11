package org.example.librarymanagementsystem.DTO.ResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Category response data")
public class CategoryResponseDTO implements Serializable {

    private Integer id;
    private String name;
    private String description;
    // books NOT included — avoids circular response
}
