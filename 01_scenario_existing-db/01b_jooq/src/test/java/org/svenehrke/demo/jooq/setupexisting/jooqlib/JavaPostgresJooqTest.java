package org.svenehrke.demo.jooq.setupexisting.jooqlib;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.JooqLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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

import static org.assertj.core.api.Assertions.assertThat;
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
	public void select_with_projection() {
		var lastName = "LOLLOBRIGIDA";
		List<ActorWithFirstAndLastName> result = jooq.
			select(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).from(ACTOR).where(ACTOR.LAST_NAME.eq(lastName))
			.fetchInto(ActorWithFirstAndLastName.class);
		assertThat(result).isNotNull();
		assertThat(result)
			.hasSize(1)
			.element(0)
			.satisfies(it -> {
				assertThat(it.lastName()).isEqualTo(lastName);
				assertThat(it.firstName()).isEqualTo("JOHNNY");
			})
		;
	}

	@Test
	public void insert_1() {
		Long id = jooq.insertInto(ACTOR)
			.columns(ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
			.values("Sven", "Ehrke")
			.returningResult(ACTOR.ACTOR_ID)
			.fetchOneInto(Long.class)
			;
		assertThat(id).isEqualTo(201);
	}
	@Test
	public void insert_2() {
		ActorRecord entity = jooq.newRecord(ACTOR);
		entity.setFirstName("Sven");
		entity.setLastName("Ehrke");
		entity.store();
		assertThat(entity.getActorId()).isEqualTo(202);
	}

	@Test
	@Disabled
	public void schema() {
		Queries ddl = jooq.ddl(Public.PUBLIC);
		for (Query q: ddl.queries()) {
			System.out.println(q);
		}
	}


}
