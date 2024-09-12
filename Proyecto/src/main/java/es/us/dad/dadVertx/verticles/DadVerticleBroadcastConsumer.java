package es.us.dad.dadVertx.verticles;

import com.google.gson.Gson;

import es.us.dad.dadVertx.entidades.Humedad;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class DadVerticleBroadcastConsumer extends AbstractVerticle {
	public void start(Promise<Void> startFuture) {
		getVertx().eventBus().consumer("mensaje-broadcast", message -> {
			Gson gson = new Gson();
			Humedad h = gson.fromJson((String) message.body(), Humedad.class);
			System.out.println("Mensaje recibido: " + h.toString());
		});
	}
}
