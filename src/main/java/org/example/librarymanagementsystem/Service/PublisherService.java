package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.PublisherRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PublisherResponseDTO;
import org.example.librarymanagementsystem.Repository.PublisherRepository;
import org.example.librarymanagementsystem.entity.Publisher;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.PublisherNotFoundException;
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
public class PublisherService {

    @Autowired
    private PublisherRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    private PublisherResponseDTO toDTO(Publisher publisher) {
        return modelMapper.map(publisher, PublisherResponseDTO.class);
    }

    // ─── GET all ───────────────────────────────────
    @Cacheable(value = "publishers", key = "'all'")
    @Transactional(readOnly = true)
    public List<PublisherResponseDTO> getAllPublishers() {
        log.info("Fetching all publishers");
        List<PublisherResponseDTO> publishers = repository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} publishers", publishers.size());
        return publishers;
    }

    // ─── GET by id ─────────────────────────────────
    @Cacheable(value = "publisher", key = "#id")
    @Transactional(readOnly = true)
    public PublisherResponseDTO getById(Integer id) {
        log.info("Fetching publisher by id={}", id);
        Publisher publisher = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Publisher not found for id={}", id);
                    return new PublisherNotFoundException(id);
                });
        return toDTO(publisher);
    }

    // ─── GET by id with books ──────────────────────
    @Cacheable(value = "publisher", key = "'with_books:' + #id")
    @Transactional(readOnly = true)
    public PublisherResponseDTO getByIdWithBooks(Integer id) {
        log.info("Fetching publisher by id={} with books", id);
        Publisher publisher = repository.findByIdWithBooks(id);
        if (publisher == null) {
            log.warn("Publisher not found for id={}", id);
            throw new PublisherNotFoundException(id);
        }
        return toDTO(publisher);
    }

    // ─── SEARCH by name ────────────────────────────
    @Cacheable(
            value = "publishers",
            key = "'search:' + #keyword"
    )
    @Transactional(readOnly = true)
    public List<PublisherResponseDTO> searchByName(String keyword) {
        log.info("Searching publishers by name keyword='{}'", keyword);
        List<PublisherResponseDTO> results = repository.findByNameContaining(keyword)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Search for '{}' returned {} publishers", keyword, results.size());
        return results;
    }

    // ─── POST save ─────────────────────────────────
    @CacheEvict(value = "publishers", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public PublisherResponseDTO save(PublisherRequestDTO dto) {
        log.info("Saving new publisher name='{}'", dto.getName());

        if (repository.existsByName(dto.getName())) {
            log.warn("Duplicate publisher name on save attempt: '{}'", dto.getName());
            throw new BadRequestException(
                    "Publisher '" + dto.getName() + "' already exists");
        }

        Publisher publisher = modelMapper.map(dto, Publisher.class);
        Publisher saved = repository.save(publisher);
        log.info("Publisher saved successfully with id={}", saved.getId());
        return toDTO(saved);
    }

    // ─── PUT update ────────────────────────────────
    @CacheEvict(value = "publishers", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public PublisherResponseDTO update(Integer id,
                                       PublisherRequestDTO dto) {
        log.info("Updating publisher id={}", id);

        Publisher existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Publisher not found for update, id={}", id);
                    return new PublisherNotFoundException(id);
                });

        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        existing.setPhone(dto.getPhone());

        Publisher updated = repository.save(existing);
        log.info("Publisher updated successfully, id={}", updated.getId());
        return toDTO(updated);
    }

    // ─── DELETE ────────────────────────────────────
    @CacheEvict(value = "publishers", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        log.info("Deleting publisher id={}", id);

        Publisher publisher = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Publisher not found for delete, id={}", id);
                    return new PublisherNotFoundException(id);
                });

        if (publisher.getBooks() != null
                && !publisher.getBooks().isEmpty()) {
            log.warn("Delete blocked for publisher id={} — has {} book(s)", id, publisher.getBooks().size());
            throw new BadRequestException(
                    "Cannot delete publisher with id " + id +
                            " — has " + publisher.getBooks().size() +
                            " book(s). Remove books first.");
        }

        repository.deleteById(id);
        log.info("Publisher deleted successfully, id={}", id);
    }
}