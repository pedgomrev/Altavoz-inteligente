package es.us.dad.dadVertx.verticles;

import com.google.gson.Gson;

import es.us.dad.dadVertx.entidades.Humedad;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

public class DadVerticleBroadcastSender extends AbstractVerticle {
	
	public void start(Promise<Void> startFuture) {
		EventBus eventBus = getVertx().eventBus();
//		getVertx().setPeriodic(2000, _id -> {
//			Humedad h = new Humedad(1,20.0, "idplacaprueba", 18032022l);
//			Gson gson = new Gson();
//			String json = gson.toJson(h);
//			eventBus.publish("mensaje-broadcast", json);
//		});
	}
}
