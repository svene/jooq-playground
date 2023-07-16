package org.svenehrke.demo.jooq.setupexisting.jooqlib;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.mcve.java.postgres.tables.records.ActorRecord;
import org.jooq.tools.JooqLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ResourceReaper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.jooq.mcve.java.postgres.Tables.ACTOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JavaPostgresJooqTest {

	static PostgreSQLContainer<?> db;
	static DSLContext ctx;
	static Connection connection;
	static JooqLogger log = JooqLogger.getLogger(JavaPostgresJooqTest.class);

	@BeforeClass
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

		ctx = DSL.using(connection, SQLDialect.POSTGRES);

		// Use JDBC directly instead of jOOQ to avoid DEBUG logging all of this
		try (Statement s = connection.createStatement()) {
			log.info("Finished setup");
		}
	}

	@AfterClass
	public static void end() {
		if (db != null) {
			ResourceReaper.instance().stopAndRemoveContainer(db.getContainerId(), db.getDockerImageName());
		}
	}

	// ====================================================================================

	@Test
	public void t1() {
		var lastName = "LOLLOBRIGIDA";
		ActorRecord record = ctx.fetchOne(ACTOR, ACTOR.LAST_NAME.eq(lastName));
		assertNotNull(record);
		assertNotNull(record.getLastName());
		assertEquals(lastName, record.getLastName());
	}

	@Test
	public void t2() {
		DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
		var lastName = "LOLLOBRIGIDA";
		List<ActorWithFirstAndLastName> result = dsl.
			select(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).from(ACTOR).where(ACTOR.LAST_NAME.eq(lastName))
			.fetchInto(ActorWithFirstAndLastName.class);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(lastName, result.get(0).lastName());
		assertEquals("JOHNNY", result.get(0).firstName());
	}
}
