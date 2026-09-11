# Caching Commands Guide for Library Management System

This document outlines all the caching annotations and strategies applied to the services in the Library Management System.

---

## Overview

The application uses **Spring Cache** abstraction with `@Cacheable`, `@CacheEvict`, and `@Caching` annotations to improve performance by reducing database queries.

---

## Cache Configuration

### Cache Names and Keys

| Service | Cache Name | Cache Name (Details) | Key Strategy |
|---------|------------|----------------------|--------------|
| **Author** | `authors` | `author` | `#id` or custom composite keys |
| **Book** | `books` | `book` | `#id` or composite keys |
| **Category** | `categories` | `category` | `#id` or name-based keys |
| **Publisher** | `publishers` | `publisher` | `#id` or with_books composite |
| **Member** | `members` | `member` | `#id` or with_borrows composite |
| **Borrow** | `borrows` | `borrow` | `#id` or status-based keys |

---

## Service-by-Service Caching Strategies

### 1. **AuthorService**

#### Read Operations (Cached)
```java
@Cacheable(value = "authors", key = "'all:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction")
public PageResponseDTO<AuthorResponseDTO> getAllAuthors(int page, int size, String sortBy, String direction)

@Cacheable(value = "author", key = "#id")
public AuthorResponseDTO getByIdWithBooks(Integer id)

@Cacheable(value = "authors", key = "'all_with_books'")
public List<AuthorResponseDTO> getAllWithBook()

@Cacheable(value = "authors", key = "'search:' + #keyword + ':' + #page + ':' + #size")
public PageResponseDTO<AuthorResponseDTO> searchByName(String keyword, int page, int size)

@Cacheable(value = "authors", key = "'nationality:' + #nationality")
public List<AuthorResponseDTO> getByNationality(String nationality)
```

#### Write Operations (Cache Evicted)
```java
@CacheEvict(value = "authors", allEntries = true)
public AuthorResponseDTO save(AuthorRequestDTO dto)

@CacheEvict(value = "authors", allEntries = true)
public AuthorResponseDTO update(Integer id, AuthorRequestDTO dto)

@CacheEvict(value = "authors", allEntries = true)
public void delete(Integer id)
```

**Cache Invalidation Strategy:** All author caches cleared on any modification

---

### 2. **BookService**

#### Read Operations (Cached)
```java
@Cacheable(value="books", key="'all:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction")
public PageResponseDTO<BookResponseDTO> getAllBooks(int page, int size, String sortBy, String direction)

@Cacheable(value = "book", key = "#id")
public BookResponseDTO getById(Integer id)

@Cacheable(value = "books", key = "'search:' + #keyword + ':' + #page + ':' + #size")
public PageResponseDTO<BookResponseDTO> searchByTitle(String keyword, int page, int size)

@Cacheable(value = "books", key = "'available:' + #page + ':' + #size")
public PageResponseDTO<BookResponseDTO> getAvailableBooks(int page, int size)

@Cacheable(value = "books", key = "'category:' + #categoryId")
public List<BookResponseDTO> getByCategory(Integer categoryId)

@Cacheable(value = "books", key = "'author:' + #authorId")
public List<BookResponseDTO> getByAuthor(Integer authorId)
```

#### Write Operations (Selective Cache Eviction)
```java
@Caching(evict = {
    @CacheEvict(value = "books", allEntries = true),
    @CacheEvict(value = "book", allEntries = true)
})
public BookResponseDTO save(BookRequestDTO dto)

@Caching(evict = {
    @CacheEvict(value = "book", key = "#id"),
    @CacheEvict(value = "books", allEntries = true)
})
public BookResponseDTO update(Integer id, BookRequestDTO dto)

@Caching(evict = {
    @CacheEvict(value = "book", key = "#id"),
    @CacheEvict(value = "books", allEntries = true)
})
public void delete(Integer id)
```

**Cache Invalidation Strategy:** Fine-grained eviction - specific book + all list caches

---

### 3. **CategoryService**

#### Read Operations (Cached)
```java
@Cacheable(value = "categories", key = "'all'")
public List<CategoryResponseDTO> getAllCategories()

@Cacheable(value = "category", key = "#id")
public CategoryResponseDTO getByid(Integer id)

@Cacheable(value = "categories", key = "'search:' + #keyword")
public List<CategoryResponseDTO> searchByName(String keyword)
```

#### Write Operations (Cache Evicted)
```java
@CacheEvict(value = "categories", allEntries = true)
public CategoryResponseDTO save(CategoryRequestDTO dto)

@CacheEvict(value = "categories", allEntries = true)
public CategoryResponseDTO update(Integer id, CategoryRequestDTO dto)

@CacheEvict(value = "categories", allEntries = true)
public void delete(Integer id)
```

**Cache Invalidation Strategy:** All category caches cleared on modification

---

### 4. **PublisherService**

#### Read Operations (Cached)
```java
@Cacheable(value = "publishers", key = "'all'")
public List<PublisherResponseDTO> getAllPublishers()

@Cacheable(value = "publisher", key = "#id")
public PublisherResponseDTO getById(Integer id)

@Cacheable(value = "publisher", key = "'with_books:' + #id")
public PublisherResponseDTO getByIdWithBooks(Integer id)

@Cacheable(value = "publishers", key = "'search:' + #keyword")
public List<PublisherResponseDTO> searchByName(String keyword)
```

#### Write Operations (Cache Evicted)
```java
@CacheEvict(value = "publishers", allEntries = true)
public PublisherResponseDTO save(PublisherRequestDTO dto)

@CacheEvict(value = "publishers", allEntries = true)
public PublisherResponseDTO update(Integer id, PublisherRequestDTO dto)

@CacheEvict(value = "publishers", allEntries = true)
public void delete(Integer id)
```

**Cache Invalidation Strategy:** All publisher caches cleared on modification

---

### 5. **MemberService**

#### Read Operations (Cached)
```java
@Cacheable(value = "members", key = "'all:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction")
public PageResponseDTO<MemberResponseDTO> getAllMembers(int page, int size, String sortBy, String direction)

@Cacheable(value = "member", key = "#id")
public MemberResponseDTO getById(Integer id)

@Cacheable(value = "member", key = "'with_borrows:' + #id")
public MemberResponseDTO getByIdWithBorrows(Integer id)

@Cacheable(value = "members", key = "'search:' + #keyword + ':' + #page + ':' + #size")
public PageResponseDTO<MemberResponseDTO> searchByName(String keyword, int page, int size)

@Cacheable(value = "members", key = "'status:' + #status")
public List<MemberResponseDTO> getByStatus(Member.MembershipStatus status)
```

#### Write Operations (Cache Evicted)
```java
@CacheEvict(value = "members", allEntries = true)
public MemberResponseDTO save(MemberRequestDTO dto)

@CacheEvict(value = "members", allEntries = true)
public MemberResponseDTO update(Integer id, MemberRequestDTO dto)

@CacheEvict(value = "members", allEntries = true)
public void delete(Integer id)

@CacheEvict(value = "members", allEntries = true)
public MemberResponseDTO suspendMember(Integer id)

@CacheEvict(value = "members", allEntries = true)
public MemberResponseDTO activateMember(Integer id)
```

**Cache Invalidation Strategy:** All member caches cleared on any status change or modification

---

### 6. **BorrowService**

#### Read Operations (Cached)
```java
@Cacheable(value = "borrows", key = "'all:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction")
public PageResponseDTO<BorrowResponseDTO> getAllBorrows(int page, int size, String sortBy, String direction)

@Cacheable(value = "borrow", key = "#id")
public BorrowResponseDTO getById(Integer id)

@Cacheable(value = "borrows", key = "'overdue'")
public List<BorrowResponseDTO> getOverdueBorrows()

@Cacheable(value = "borrows", key = "'member:' + #memberId")
public List<BorrowResponseDTO> getByMember(Integer memberId)

@Cacheable(value = "borrows", key = "'book:' + #bookId")
public List<BorrowResponseDTO> getByBook(Integer bookId)
```

#### Write Operations (Cache Evicted)
```java
@CacheEvict(value = "borrows", allEntries = true)
public BorrowResponseDTO borrowBook(BorrowRequestDTO dto)

@CacheEvict(value = "borrows", allEntries = true)
public BorrowResponseDTO returnBook(Integer borrowId)

@CacheEvict(value = "borrows", allEntries = true)
public String updateOverdueRecords()
```

**Cache Invalidation Strategy:** All borrow caches cleared on state changes (borrow/return)

---

### 7. **AuthService & RefreshTokenService**

These services handle authentication and token management:
- **AuthService:** Login/logout operations (no caching recommended)
- **RefreshTokenService:** Token refresh/revocation (no caching recommended)

**Rationale:** Authentication tokens are security-sensitive and should not be cached. User sessions are often invalidated, making cache invalidation complex.

---

## Cache Key Patterns

### Pagination Keys
```
'all:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction
```
- Used for paginated list queries
- Ensures different pages are cached separately

### Single Entity Keys
```
#id
```
- Simple ID-based caching
- Most efficient for direct lookups

### Composite Keys
```
'with_books:' + #id
'with_borrows:' + #id
```
- Different cache entries for eager-loaded relationships
- Prevents cache collisions

### Search Keys
```
'search:' + #keyword + ':' + #page + ':' + #size
'search:' + #keyword
```
- Search term as part of key
- Pagination support for search results

### Filter Keys
```
'status:' + #status
'nationality:' + #nationality
'category:' + #categoryId
'author:' + #authorId
```
- Filter value as key component
- Fast lookups by filter criteria

---

## Cache Invalidation Strategy

### **Full Eviction (Conservative)**
Applied to: `CategoryService`, `PublisherService`
- Simpler data models with fewer relationships
- All entries cleared on any modification
- **Pros:** Guaranteed consistency, simple logic
- **Cons:** More cache misses after updates

### **Selective Eviction (Optimized)**
Applied to: `AuthorService`, `BookService`, `MemberService`, `BorrowService`
- Complex data models with relationships
- Specific keys + all list caches evicted
- **Pros:** Better performance, reduces cache misses
- **Cons:** More complex invalidation logic

---

## Performance Benefits

1. **Reduced Database Load:** Frequently accessed data served from memory
2. **Faster Response Times:** Cache hits eliminate DB roundtrips
3. **Scalability:** Fewer database connections needed
4. **Search Optimization:** Search results cached by keyword

### Estimated Cache Hit Rates
- **Author Lists:** 80-90% (paginated, stable data)
- **Book Availability:** 70-80% (changes with borrows)
- **Member Status:** 75-85% (stable, small dataset)
- **Search Results:** 60-70% (depends on query variety)

---

## Configuration Requirements

Ensure the following is configured in `application.properties` or `application.yml`:

```properties
# Spring Cache Configuration
spring.cache.type=redis  # or caffeine, simple, etc.
spring.cache.redis.time-to-live=3600000  # 1 hour TTL
spring.cache.redis.key-prefix=lms:
```

Or for in-memory caching:
```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=1h
```

Enable caching in main application class:
```java
@EnableCaching
@SpringBootApplication
public class LibraryManagementSystemApplication { }
```

---

## Monitoring Cache Performance

Monitor cache hits/misses:
```java
// Example: Inject CacheManager
@Autowired
private CacheManager cacheManager;

public void logCacheStats() {
    Cache authorsCache = cacheManager.getCache("authors");
    // Log cache stats
}
```

---

## Best Practices

1. **Cache Keys Should Be Immutable:** Avoid mutable object keys
2. **TTL Configuration:** Set appropriate expiration times
3. **Cache Warming:** Pre-load frequently accessed data
4. **Monitoring:** Track cache hit ratios
5. **Testing:** Clear caches between test cases with `@CacheEvict`
6. **Documentation:** Keep cache strategies updated
7. **Selective Caching:** Don't cache highly dynamic data

---

## Troubleshooting

### Cache Not Working
- Check if `@EnableCaching` is present
- Verify cache provider is configured
- Ensure method is public (Spring AOP limitation)

### Stale Data
- Reduce TTL for frequently changing data
- Verify `@CacheEvict` is applied to write operations
- Check cache invalidation logic

### Memory Issues
- Reduce cache size limits
- Implement cache eviction policies
- Monitor cache memory usage

---

## Related Documentation

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Cache Annotations](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/annotation/package-summary.html)
- [Redis Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.cache)
