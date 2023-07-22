package org.svenehrke.sakila.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svenehrke.demo.jooq.setupexisting.jooqlib.ActorWithFirstAndLastName;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.svenehrke.demo.jooq.setupexisting.jooqlib.Tables.ACTOR;

@RestController
public class ActorController {

	@Autowired
	private DataSource dataSource;

	// http://localhost:8080/actors
	@GetMapping(value = "/actors", produces = "application/json")
	public List<String> actors() throws SQLException {
		var connection = dataSource.getConnection();
		DSLContext jooq = DSL.using(connection, SQLDialect.POSTGRES);
		var lastName = "LOLLOBRIGIDA";
		List<ActorWithFirstAndLastName> result = jooq.
			select(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).from(ACTOR).where(ACTOR.LAST_NAME.eq(lastName))
			.fetchInto(ActorWithFirstAndLastName.class);
		return result.stream().map(ActorWithFirstAndLastName::lastName).toList();
	}
}
