#In-memory cache

###Prerequisites
- Java 8 is installed

###Run tests
`mvnw test`

The cache doesn't implements any kind of invalidation, so when all memory is allocated it
stops to accept new items.
