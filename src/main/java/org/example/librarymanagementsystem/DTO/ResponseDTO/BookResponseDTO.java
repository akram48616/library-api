package org.example.librarymanagementsystem.DTO.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Book response data")
public class BookResponseDTO implements Serializable {
    private Integer id;
    private String title;
    private String isbn;
    private double price;
    private int stock;
    private boolean available;       // stock > 0
    private LocalDate publishedDate;
    private PublisherResponseDTO publisher;
    private List<AuthorResponseDTO> authors;
    private List<CategoryResponseDTO> categories;
    // ✅ cover image info
    @Schema(description = "Cover image URL",
            example = "http://localhost:8080/api/files/covers/abc.jpg")
    private String coverImageUrl;

    @Schema(description = "Cover image file name",
            example = "effective-java-cover.jpg")
    private String coverImageName;

    @Schema(description = "Cover image type",
            example = "image/jpeg")
    private String coverImageType;

    @Schema(description = "Cover image size in bytes",
            example = "204800")
    private Long coverImageSize;



}
