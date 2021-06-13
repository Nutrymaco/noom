# NOOM - Not Only Object Mapping
orm for Cassandra with query-driven modelling approach

main idea in abstracting from "creating table" and instead writing queries for whom library will create tables via whom queries will be executed effectively.
you need only create scheme via POJOs, then generate classes, whom will help you write queries

also library introduce "lazy migration" approach, which might be helpfull in certain situations

example of usage - https://github.com/Nutrymaco/orm-test-project

# USAGE TUTORIAL
(it won't be typical one-class-example) <br>

1. Start with defining configuration<br>
you need implement interface Configuration

```java
import com.nutrymaco.orm.config.Configuration;

public class OrmConfiguration implements Configuration {
    
    @Override
    public String packageName() {
        return "com.nutrymaco.project";
    }

    @Override
    public boolean enableSynchronisation() {
        return false;
    }

}
```
2. Define entities via POJO

```java
import com.nutrymaco.orm.generator.annotations.Entity;

@Entity
public class Movie {
    @Unique
    int id;
    String name;
    int year;
    List<Actor> actors;
}

@Entity
public class Actor {
    @Unique
    int id;
    String name;
    List<Movie> movies;
    Organisation organisation;
    City city;
}

@Entity
public class City {
    @Unique
    int id;
    int count;
    String name;
}

@Entity
public class Organisation {
    @Unique
    int id;
    String name;
    List<Actor> actors;
    City city;
}
```

2. Generate helper classes<br>
they will be generated in packages : fields, records and repository
```java
import com.nutrymaco.orm.generator.MainGenerator;

public class Generate {
    public static void main(String[] args) {
        MainGenerator.generate();
    }
}
```

3. Write queries<br>
you can just define method or explicitly write query via Query

```java
import com.nutrymaco.orm.generator.annotations.Repository;

@Repository
public interface MovieRepository {

    List<MovieRecord> getByYear(int year);

    default List<MovieRecord> getByActorName(String actorName) {
        return Query.select(MOVIE_ENTITY)
                .where(MOVIE.ACTOR.NAME.eq(actorName))
                .fetchInto(MovieRecord.class);
    }

}
```

4. Run queries
```java
import com.nutrymaco.orm.query.generation.RepositoryProvider;

public class Main {
    public static void main(String[] args) {
        MovieRepository repository = RepositoryProvider.from(MovieRepository.class);

        repository.getByYear(2018).stream()
                .map(movie -> "Название : " + movie.name())
                .forEach(System.out::println);

        repository.getByActorName("Christian Bale").stream()
                .map(movie -> "Название : " + movie.name())
                .forEach(System.out::println);
    }
}
```