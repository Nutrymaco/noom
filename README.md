# NOOM - Not Only Object Mapping
orm for Cassandra with support "table-on-query" approach

main idea in abstracting from "creating table" and instead writing queries for whom library will create tables via whom queries will be executed effectively.
you need only create scheme via POJO, then generate classes, whom will help you write queries

also library introduce "lazy migration" approach, which might be helpfull in certain situations

example of usage - https://github.com/Nutrymaco/orm-test-project
