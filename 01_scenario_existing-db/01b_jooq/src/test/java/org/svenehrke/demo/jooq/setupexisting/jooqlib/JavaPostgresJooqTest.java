package org.svenehrke.demo.jooq.setupexisting.jooqlib;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.JooqLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.svenehrke.demo.jooq.setupexisting.jooqlib.tables.records.ActorRecord;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ResourceReaper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.svenehrke.demo.jooq.setupexisting.jooqlib.Tables.ACTOR;

public class JavaPostgresJooqTest {

	static PostgreSQLContainer<?> db;
	static DSLContext jooq;
	static Connection connection;
	static JooqLogger log = JooqLogger.getLogger(JavaPostgresJooqTest.class);

	@BeforeAll
	public static void init() throws SQLException {
		if (System.getProperty("db.url") == null) {
			db = new PostgreSQLContainer(
				DockerImageName.parse("simas/postgres-sakila").asCompatibleSubstituteFor("postgres")
			)
				.withUsername("postgres")
				.withDatabaseName("postgres")
				.withPassword("sakila")
			;

			db.start();
			System.setProperty("db.url", db.getJdbcUrl());
			System.setProperty("db.username", db.getUsername());
			System.setProperty("db.password", db.getPassword());
		}

		Properties properties = new Properties();
		properties.setProperty("username", "postgres");
		properties.setProperty("password", "sakila");

		log.info("Connecting");
		connection = DriverManager.getConnection(
			System.getProperty("db.url"),
			System.getProperty("db.username"),
			System.getProperty("db.password")
		);

		jooq = DSL.using(connection, SQLDialect.POSTGRES);

		// Use JDBC directly instead of jOOQ to avoid DEBUG logging all of this
		try (Statement s = connection.createStatement()) {
			log.info("Finished setup");
		}
	}

	@AfterAll
	public static void end() {
		if (db != null) {
			ResourceReaper.instance().stopAndRemoveContainer(db.getContainerId(), db.getDockerImageName());
		}
	}

	// ====================================================================================

	// See https://www.youtube.com/watch?v=ykoUBctblno , 23min00
	@Test
	public void selectFrom() {
		var lastName = "LOLLOBRIGIDA";
		Result<ActorRecord> records = jooq.selectFrom(ACTOR).fetch();
		assertThat(records).hasSize(200);
	}
	@Test
	public void fetchOne() {
		var lastName = "LOLLOBRIGIDA";
		ActorRecord record = jooq.fetchOne(ACTOR, ACTOR.LAST_NAME.eq(lastName));
		assertThat(record).isNotNull().extracting(ActorRecord::getLastName).isNotNull().isEqualTo(lastName);
	}

	@Test
	public void t2() {
		DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
		var lastName = "LOLLOBRIGIDA";
		List<ActorWithFirstAndLastName> result = dsl.
			select(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).from(ACTOR).where(ACTOR.LAST_NAME.eq(lastName))
			.fetchInto(ActorWithFirstAndLastName.class);
		assertThat(result).isNotNull();
		assertThat(result)
			.hasSize(1)
			.element(0)
			.extracting(it -> "%s,%s".formatted(it.lastName(), it.firstName())).isEqualTo("LOLLOBRIGIDA,JOHNNY")
		;
	}
}
