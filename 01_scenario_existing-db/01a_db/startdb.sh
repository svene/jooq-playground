#!/bin/sh
docker run -d -e POSTGRES_PASSWORD=sakila -p 5432:5432 --name sakila simas/postgres-sakila
