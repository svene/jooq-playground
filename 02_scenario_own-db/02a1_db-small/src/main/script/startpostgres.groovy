import org.testcontainers.containers.PostgreSQLContainer

db = new PostgreSQLContainer("postgres:latest")
        .withDatabaseName("postgres")
        .withUsername(properties['db.username'])
        .withPassword(properties['db.password'])
;

db.start();
project.properties.setProperty('db.url', db.getJdbcUrl());
project.properties.setProperty('testcontainer.containerid', db.getContainerId());
project.properties.setProperty('testcontainer.imageName', db.getDockerImageName());
