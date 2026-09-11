package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.AuthorRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.AuthorResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Repository.AuthorRepository;
import org.example.librarymanagementsystem.entity.Author;
import org.example.librarymanagementsystem.exception.AuthorNotFoundException;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorService {
    @Autowired
    private AuthorRepository repository;
    @Autowired
    private ModelMapper modelMapper;

    private AuthorResponseDTO toDto(Author author){
        return modelMapper.map(author, AuthorResponseDTO.class);
    }
    private Author toEntity(AuthorRequestDTO dto ){
        return modelMapper.map(dto, Author.class);
    }
    private PageResponseDTO<AuthorResponseDTO> toPageresponse(Page<Author> page){
        List<AuthorResponseDTO> content=page.getContent()
                .stream().map(this::toDto).collect(Collectors.toList());
        return PageResponseDTO
                .<AuthorResponseDTO>builder()
                .content(content).currentPage(page.getNumber()).totalPages(page.getTotalPages()).totalElements(page.getTotalElements()).pageSize(page.getSize()).first(page.isFirst()).last(page.isLast()).empty(page.isEmpty()).build();
    }

    // ─── GET all paged ─────────────────────────────
    @Cacheable(
            value = "authors",
            key = "'all:' + #page + ':' + #size + ':'" +
                    "+ #sortBy + ':' + #direction"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<AuthorResponseDTO> getAllAuthors(int page,int size,String sortBy,String direction){
        log.info("Fetching all authors - page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Sort sort=direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
        Pageable pageable= PageRequest.of(page,size,sort);
        return toPageresponse(repository.findAll(pageable));
    }

    //Get by id
    @Cacheable(value = "author", key = "#id")
    @Transactional(readOnly = true)
    public AuthorResponseDTO getByIdWithBooks(Integer id){
        log.info("Fetching author by id={} with books", id);
        Author author=repository.findByIdWithBooks(id);
        if(author==null){
            log.warn("Author not found for id={}", id);
            throw new AuthorNotFoundException(id);
        }
        return toDto(author);
    }

    //get all with books
    @Cacheable(value = "authors", key = "'allWithBooks'")
    @Transactional(readOnly = true)
    public List<AuthorResponseDTO> getAllWithBook(){
        log.info("Fetching all authors with books");
        List<AuthorResponseDTO> authors = repository.findAllWithBooks().stream().map(this::toDto).collect(Collectors.toList());
        log.debug("Found {} authors", authors.size());
        return authors;
    }

    //Search by name
    @Cacheable(
            value = "authors",
            key = "'search:' + #keyword + ':' + #page + ':' + #size"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<AuthorResponseDTO> searchByName(String keyword, int page, int size) {
        log.info("Searching authors by name keyword='{}', page={}, size={}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstname").ascending());
        Page<Author> result = repository.findByFirstnameContainsOrLastnameContains(keyword, keyword, pageable);
        log.debug("Search for '{}' returned {} authors", keyword, result.getTotalElements());
        return toPageresponse(result);
    }

    //get by nationality
    @Cacheable(value = "authors", key = "'nationality:' + #nationality")
    @Transactional(readOnly = true)
    public List<AuthorResponseDTO> getByNationality(String nationality){
        log.info("Fetching authors by nationality='{}'", nationality);
        List<AuthorResponseDTO> authors = repository.findByNationality(nationality)
                .stream().map(this::toDto).collect(Collectors.toList());
        log.debug("Found {} authors with nationality='{}'", authors.size(), nationality);
        return authors;
    }

    //post save
    @Caching(evict = {
            @CacheEvict(value = "authors", allEntries = true),
            @CacheEvict(value = "author", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public AuthorResponseDTO save(AuthorRequestDTO dto){
        log.info("Saving new author email='{}'", dto.getEmail());
        if(repository.existsByEmail(dto.getEmail())){
            log.warn("Duplicate email on save attempt: '{}'", dto.getEmail());
            throw  new DuplicateEmailException(dto.getEmail());
        }
        Author author=toEntity(dto);
        Author saved = repository.save(author);
        log.info("Author saved successfully with id={}", saved.getId());
        return toDto(saved);
    }

    //put update
    @Caching(evict = {
            @CacheEvict(value = "authors", allEntries = true),
            @CacheEvict(value = "author", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public AuthorResponseDTO update(Integer id,AuthorRequestDTO dto){
        log.info("Updating author id={}", id);
        Author existing=repository.findById(id).orElseThrow(()->{
            log.warn("Author not found for update, id={}", id);
            return new AuthorNotFoundException(id);
        });
        if(!existing.getEmail().equals(dto.getEmail())&&
                repository.existsByEmail(dto.getEmail())) {
            log.warn("Duplicate email on update attempt: '{}'", dto.getEmail());
            throw new DuplicateEmailException(dto.getEmail());
        }
        existing.setFirstname(dto.getFirstname());
        existing.setLastname(dto.getLastname());
        existing.setEmail(dto.getEmail());
        existing.setNationality(dto.getNationality());
        Author updated = repository.save(existing);
        log.info("Author updated successfully, id={}", updated.getId());
        return toDto(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = "authors", allEntries = true),
            @CacheEvict(value = "author", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id){
        log.info("Deleting author id={}", id);
        Author author=repository.findById(id).orElseThrow(()->{
            log.warn("Author not found for delete, id={}", id);
            return new AuthorNotFoundException(id);
        });
        if(author.getBooks()!=null &&
                !author.getBooks().isEmpty()) {
            log.warn("Delete blocked for author id={} — has {} book(s)", id, author.getBooks().size());
            throw new BadRequestException(
                    "Cannot delete author with id "+ id +
                            "— has " + author.getBooks().size() +
                            " book(s). Remove books first.");
        }
        repository.deleteById(id);
        log.info("Author deleted successfully, id={}", id);
    }
}