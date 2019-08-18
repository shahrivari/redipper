# Redipper

### Goals / Background

Redipper is a simple redis wrapper for Lettuce which makes it easier to use. The core features are:

* Definition of spaces over redis which gives a table-like feel over db.
* Generic collection API like Redisson.
* Automatic serialization of objects.
* Various encoders for compression and encryption.

### Add dependency

1.Add the JitPack repository to your project:
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the dependency pom.xml file
```
<dependency>
    <groupId>com.github.shahrivari</groupId>
    <artifactId>redipper</artifactId>
    <version>1.0</version>
</dependency>
```

### Get Started
Create object of any classes on your demand.

```kotlin
val redisCache = RedisMap.Builder(your_space, Person::class.java)
            .withTtl(10, TimeUnit.SECONDS)
            .build()

// set
redisCache.set(person.id.toString(), person)

// get
redisCache.get(person.id.toString())            
```

Note: If you have specific serializer, you can set it.
There are some serializer by default.
Note: You can have your own serializer.It should be implement ```Serializer``` interface.
```kotlin
val redisCache = RedisMap.Builder(your_space, Person::class.java)
            .withTtl(10, TimeUnit.SECONDS)
            .withSerializer(LongSerializer())
            .build()
```

Also you can set encryption.
```kotlin
val redisCache = RedisMap.Builder(your_space, Person::class.java)
            .withTtl(10, TimeUnit.SECONDS)
            .withSerializer(LongSerializer())
            .withEncoder(AesEmbeddedEncoder())
            .build()
```

There are two encrypt algorithm and two compression algorithm.
You can combine them and set with ```withEncoder``` method.
```kotlin
val redisCache = RedisMap.Builder(your_space, Person::class.java)
            .withTtl(10, TimeUnit.SECONDS)
            .withSerializer(LongSerializer())
            .withEncoder(GzipEncoder(),AesEmbeddedEncoder())
            .build()
```
Note: It's better to first pass compress and then encrypt for fewer final data size.

Most of this code is written by [Mohammad Hossein Liaghat](https://github.com/MoHoLiaghat) and
 Mohammad Amin Badiezadegan.
