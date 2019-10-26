# Redipper

[![Build Status](https://travis-ci.org/shahrivari/redipper.svg?branch=master)](https://travis-ci.org/shahrivari/redipper)

### Goals / Background

Redipper is a simple redis wrapper for Lettuce which makes it easier to use. The core features are:

* Definition of spaces over redis which gives a table-like feel over db.
* Generic collection API like Redisson.
* Automatic serialization of objects.
* Various encoders for compression and encryption.

### Add dependency

#####1.Add the JitPack repository to your project:
######pom.xml file
    <repositories>
	    <repository>
	        <id>jitpack.io</id>
	        <url>https://jitpack.io</url>
	    </repository>
	</repositories>
	
#####2. Add the dependency
######pom.xml file
	<dependency>
	    <groupId>com.github.shahrivari</groupId>
	    <artifactId>redipper</artifactId>
	    <version>1.0</version>
	</dependency>


### Get Started
Create object of any classes on your demand.

```kotlin
val redisCache = RedisMap.Builder(your_space, Person::class.java)
            .withTtl(10, TimeUnit.SECONDS)
            .build()

redisCache.set(person.id.toString(), person)

redisCache.get(person.id.toString())            
```

If you have specific serializer, you can pass it to redisWrapper.
```kotlin

```

Most of this code is written by [Mohammad Hossein Liaghat](https://github.com/MoHoLiaghat) and
 Mohammad Amin Badiezadegan.
