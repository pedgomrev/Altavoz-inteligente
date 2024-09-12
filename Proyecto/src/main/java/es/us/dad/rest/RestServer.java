package es.us.dad.rest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.us.dad.dadVertx.entidades.*;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class RestServer extends AbstractVerticle {

	MySQLPool mySqlClient;
	private Gson gson;
	MqttClient mqttClient;

	public void start(Promise<Void> startFuture) {

		// Instantiating a Gson serialize object using specific date format
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Instantiating a MqttClient object
		mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("daddatabase").setUser("root").setPassword("root123456789");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*

		router.route("/api/*").handler(BodyHandler.create());
		router.get("/api/luz/:idplaca").handler(this::getEncendido);
		router.get("/api/humedad/:idplaca/:umbral").handler(this::getByUmbral);
		router.post("/api/humedad").handler(this::addHumedad);
		router.post("/api/luz").handler(this::addLuz);
		router.post("/api/placa").handler(this::addPlaca);

		// CONEXION MQTT
		mqttClient.connect(1883, "localhost", s -> {
		});
	}

	@SuppressWarnings("unused")

	private void getEncendido(RoutingContext routingContext) {
		final Integer idPlaca = Integer.valueOf(routingContext.request().getParam("idplaca"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result()
						.preparedQuery("SELECT * FROM daddatabase.luz WHERE idPlaca = ? ORDER BY fecha DESC LIMIT 1;")
						.execute(Tuple.of(idPlaca), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								Luz result = null;
								for (Row elem : resultSet) {
									result = new Luz(elem.getInteger("id"), elem.getInteger("encendido"),
											elem.getInteger("idPlaca"), elem.getLong("fecha"));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(gson.toJson(result));
								System.out.println("Valores obtenidos:" + result.toString());
							} else {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(401).end();
								System.out.println("Error:" + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});

	}

	private void getByUmbral(RoutingContext routingContext) {
		final Integer idPlaca = Integer.valueOf(routingContext.request().getParam("idplaca"));
		final Double umbral = Double.valueOf(routingContext.request().getParam("umbral"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM daddatabase.humedad WHERE idPlaca = ? and valor >= ?")
						.execute(Tuple.of(idPlaca, umbral), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<Humedad> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Humedad(elem.getInteger("idHumedad"), elem.getDouble("valor"),
											elem.getInteger("idPlaca"), elem.getLong("fecha")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(gson.toJson(result));
								System.out.println("Valores obtenidos:" + result.toString());
							} else {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(401).end();
								System.out.println("Error:" + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});

	}

	private void addHumedad(RoutingContext routingContext) {
		final Humedad h = gson.fromJson(routingContext.getBodyAsString(), Humedad.class);
		h.setFecha(Calendar.getInstance().getTimeInMillis());
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result()
						.preparedQuery("INSERT INTO daddatabase.humedad (valor, idPlaca, fecha) VALUES(?,?,?);")
						.execute(Tuple.of(h.getValor(), h.getIdPlaca(), h.getFecha()), res -> {
							if (res.succeeded()) {

								routingContext.response().setStatusCode(201)
										.putHeader("content-type", "application/json; charset=utf-8")
										.end(gson.toJson(h));
								System.out.println("Datos recibidos correctamente\n" + h.toString());
								if (h.getValor() > 65.0) {
									// Mandar peticion MQTT para activar altavoz
									try {
										mqttClient.publish("altavoz_" + h.getIdPlaca(), Buffer.buffer("1"),
												MqttQoS.AT_LEAST_ONCE, false, false);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										mqttClient.connect(1883, "localhost", s -> {
										});
									}
								} else {
									// Desactivar altavoz
									try {
										mqttClient.publish("altavoz_" + h.getIdPlaca(), Buffer.buffer("0"),
												MqttQoS.AT_LEAST_ONCE, false, false);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										mqttClient.connect(1883, "localhost", s -> {
										});
									}

								}
							} else {
								routingContext.response().setStatusCode(401)
										.putHeader("content-type", "application/json; charset=utf-8").end();
								// Comprobar si el valor es umbral con un if y hacer el mqtt aqui

								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				routingContext.response().setStatusCode(401)
						.putHeader("content-type", "application/json; charset=utf-8").end();
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void addLuz(RoutingContext routingContext) {
		final Luz h = gson.fromJson(routingContext.getBodyAsString(), Luz.class);
		h.setFecha(Calendar.getInstance().getTimeInMillis());
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result()
						.preparedQuery("INSERT INTO daddatabase.luz (encendido, idPlaca, fecha) VALUES(?,?,?);")
						.execute(Tuple.of(h.getEncendida(), h.getIdPlaca(), h.getFecha()), res -> {
							if (res.succeeded()) {
								routingContext.response().setStatusCode(201)
										.putHeader("content-type", "application/json; charset=utf-8").end();
								// Comprobar si el valor es umbral con un if y hacer el mqtt aqui
								if (h.getEncendida() == 1) {
									// Mandar peticion MQTT para desactivar altavoz
									try {
										mqttClient.publish("altavoz_" + h.getIdPlaca(), Buffer.buffer("0"),
												MqttQoS.AT_LEAST_ONCE, false, false);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										mqttClient.connect(1883, "localhost", s -> {
										});
									}

									System.out.println("Datos recibidos correctamente\n" + h.toString());
								} else {
									routingContext.response().setStatusCode(401)
											.putHeader("content-type", "application/json; charset=utf-8").end();
									System.out.println("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							}
						});
			} else {
				routingContext.response().setStatusCode(401)
						.putHeader("content-type", "application/json; charset=utf-8").end();
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void addPlaca(RoutingContext routingContext) {
		final Placa h = gson.fromJson(routingContext.getBodyAsString(), Placa.class);
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("INSERT INTO daddatabase.placa (nombre) VALUES(?);")
						.execute(Tuple.of(h.getNombre()), res -> {
							if (res.succeeded()) {
								routingContext.response().setStatusCode(201)
										.putHeader("content-type", "application/json; charset=utf-8").end();
								System.out.println("Datos recibidos correctamente\n" + h.toString());
							} else {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(401).end();
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
