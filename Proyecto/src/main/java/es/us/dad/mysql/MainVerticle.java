package es.us.dad.mysql;

import java.util.ArrayList;
import java.util.List;

import es.us.dad.dadVertx.entidades.Humedad;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends AbstractVerticle{

	MySQLPool mySqlClient;
	
	public void start(Promise<Void> startFuture) {
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dad_database").setUser("root").setPassword("rootdad");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		getAll();

//		getAllWithConnection();
//
//		for (int i = 0; i < 100; i++) {
//			getByName("lsoriamo");	
//		}

	}
//Estas clases hay que pasarlas al RestServer solo las que necesitemos ej: un getByIdPlaca
	private void getAll() {
		mySqlClient.query("SELECT * FROM dad_database.humedad;").execute(res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<Humedad> result = new ArrayList<Humedad>();
				for (Row elem : resultSet) {
					result.add(new Humedad(elem.getInteger("idHumedad"), elem.getDouble("valor"),
							elem.getInteger("idPlaca"), elem.getLong("fecha")));
				}
				System.out.println(result.toString());
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}
	private void getByUmbral(String umbral) {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM dad_database.humedad WHERE valor >= ?").execute(
						Tuple.of(umbral), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								JsonArray result = new JsonArray();
								for (Row elem : resultSet) {
									result.add(JsonObject.mapFrom(new Humedad(elem.getInteger("idHumedad"), elem.getDouble("valor"),
											elem.getInteger("idPlaca"), elem.getLong("fecha"))));
								}
								System.out.println(result.toString());
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}
}
