package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.CategoryRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.CategoryResponseDTO;
import org.example.librarymanagementsystem.Repository.CategoryRepository;
import org.example.librarymanagementsystem.entity.Category;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.CategoryNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryService {
    @Autowired
    private CategoryRepository repository;
    @Autowired
    private ModelMapper modelMapper;

    public CategoryResponseDTO toDto(Category category){
        return modelMapper.map(category,CategoryResponseDTO.class);
    }

    @Cacheable(value = "categories", key = "'all'")
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(){
        log.info("Fetching all categories");
        List<CategoryResponseDTO> categories = repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
        log.debug("Found {} categories", categories.size());
        return categories;
    }

    @Cacheable(value = "category", key = "#id")
    @Transactional(readOnly = true)
    public CategoryResponseDTO getByid(Integer id){
        log.info("Fetching category by id={}", id);
        Category category = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found for id={}", id);
                    return new CategoryNotFoundException(id);
                });
        return toDto(category);
    }

    // ─── SEARCH by name ────────────────────────────
    @Cacheable(
            value = "categories",
            key = "'search:' + #keyword"
    )
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> searchByName(String keyword) {
        log.info("Searching categories by name keyword='{}'", keyword);
        List<CategoryResponseDTO> results = repository.findByNameContaining(keyword)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
        log.debug("Search for '{}' returned {} categories", keyword, results.size());
        return results;
    }

    // ─── POST save ─────────────────────────────────
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponseDTO save(CategoryRequestDTO dto) {
        log.info("Saving new category name='{}'", dto.getName());

        if (repository.existsByName(dto.getName())) {
            log.warn("Duplicate category name on save attempt: '{}'", dto.getName());
            throw new BadRequestException(
                    "Category '" + dto.getName() + "' already exists");
        }

        Category category = modelMapper.map(dto, Category.class);
        Category saved = repository.save(category);
        log.info("Category saved successfully with id={}", saved.getId());
        return toDto(saved);
    }

    // ─── PUT update ────────────────────────────────
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponseDTO update(Integer id,
                                      CategoryRequestDTO dto) {
        log.info("Updating category id={}", id);

        Category existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found for update, id={}", id);
                    return new CategoryNotFoundException(id);
                });

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());

        Category updated = repository.save(existing);
        log.info("Category updated successfully, id={}", updated.getId());
        return toDto(updated);
    }

    // ─── DELETE ────────────────────────────────────
    @CacheEvict(value = "categories", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        log.info("Deleting category id={}", id);

        Category category = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found for delete, id={}", id);
                    return new CategoryNotFoundException(id);
                });

        if (category.getBooks() != null
                && !category.getBooks().isEmpty()) {
            log.warn("Delete blocked for category id={} — has {} book(s)", id, category.getBooks().size());
            throw new BadRequestException(
                    "Cannot delete category with id " + id +
                            " — has " + category.getBooks().size() +
                            " book(s). Remove books first.");
        }

        repository.deleteById(id);
        log.info("Category deleted successfully, id={}", id);
    }
}