package org.svenehrke.sakila.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svenehrke.demo.jooq.setupexisting.jooqlib.ActorWithFirstAndLastName;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.svenehrke.demo.jooq.setupexisting.jooqlib.Tables.ACTOR;

@RestController
public class ActorController {

//	@Autowired
//	DataSource dataSource;

	@Autowired
	PostgreSQLContainer<?> db;

	// http://localhost:8080/actors
	@GetMapping(value = "/actors", produces = "application/json")
	public List<String> actors() throws SQLException {
		var connection = DriverManager.getConnection(
			db.getJdbcUrl(), db.getUsername(), db.getPassword()
		);
		DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
		var lastName = "LOLLOBRIGIDA";
		List<ActorWithFirstAndLastName> result = dsl.
			select(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).from(ACTOR).where(ACTOR.LAST_NAME.eq(lastName))
			.fetchInto(ActorWithFirstAndLastName.class);
//		return List.of("a", "b", "c");
		return result.stream().map(ActorWithFirstAndLastName::lastName).toList();
	}
}
