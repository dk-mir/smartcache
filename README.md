# In-memory cache

### Prerequisites
- Java 8 is installed

### Run tests
`mvnw test`

The cache doesn't implement any kind of invalidation, so when all memory is allocated it
stops accepting new items.
