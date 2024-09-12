package es.us.dad.dadVertx.verticles;

import com.google.gson.Gson;

import es.us.dad.dadVertx.entidades.Humedad;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class DadVerticleSenderP2P extends AbstractVerticle {
	String verticleID = "";

	public void start(Promise<Void> startFuture) {
		EventBus eventBus = getVertx().eventBus();
		getVertx().setPeriodic(4000, _id -> {
			eventBus.request("mensaje-punto-a-punto", "Soy Local,¿alguien me escucha?", reply -> {
				Message<Object> res = reply.result();
				verticleID = res.address();
				if (reply.succeeded()) {
					String replyMessage = (String) res.body();
					System.out.println("Respuesta recibida (" + res.address() + "): " + replyMessage + "\n\n\n");
				} else {
					System.out.println("No ha habido respuesta");
				}
			});
			Humedad h = new Humedad();
			Gson gson = new Gson();
			String json = gson.toJson(h);
			eventBus.request("mensaje-punto-a-punto", json, reply -> {
				String replyMsg = reply.result().body().toString();
				System.out.println(replyMsg);
			});
		});

		startFuture.complete();
	}
	public void stop(Promise<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}
}
