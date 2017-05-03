Это 2-е тестовое задание на должность Senior Java developer (оклад 250 тыс. рублей) в компании "Национальная система платёжных карт" (платежная система "Мир").

# Кэш картинок

Есть кэш в котором хранятся картинки (например, для социально сети). Нужно написать реализацию метода putToCache который сохраняет картинку в кэш и возвращает ее id, чтобы потом можно было по идентификатору запросить сохраненную картинку.
 
Также необходимо реализовать парный ему метод getFromCache, который возвращает сохраненную картинку по ее идентификатору. При отсутствии данных в кэше, возвращать null. Кэш должен при достижении лимита по памяти начинать использовать диск для хранения данных.

Методы loadFromFile, saveToFile уже реализованы, но они не являются thread-safe, и, что хуже, saveToFile работает 1000ms.

Лимит по памяти можно установить любой, допустим 100 мегабайт. Кеш используется в highly concurrent (многопоточной) среде.

```java

public class MyCache {

    public byte[] loadFromFile(String filename);
    public void saveToFile(String filename, byte[] data);

    public int putToCache(byte[] data);

    public byte[] getFromCache(int id);

}

```



# Implementation of in-memory cache

The idea for implementation is taken from the book "Java concurrency in practice", by Brian Goetz, article 5.6 (page 101). It took me about 8 hours to fulfill the job and good pile was to write unit-tests.  

### Prerequisites
- Java 8 is installed

### Run tests
`mvnw test`

The cache doesn't implement any kind of invalidation, so when all memory is allocated it
stops accepting new items.
