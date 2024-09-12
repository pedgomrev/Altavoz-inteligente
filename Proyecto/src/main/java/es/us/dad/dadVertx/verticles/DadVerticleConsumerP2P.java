package es.us.dad.dadVertx.verticles;

import com.google.gson.Gson;

import es.us.dad.dadVertx.entidades.Humedad;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class DadVerticleConsumerP2P extends AbstractVerticle {
	String verticleID = "";

	public void start(Promise<Void> startFuture) {
		getVertx().eventBus().consumer("mensaje-punto-a-punto", message -> {
			Gson gson = new Gson();
			Humedad h = gson.fromJson((String) message.body(), Humedad.class);
//			String customMessage = (String) message.body();
//			System.out.println("Mensaje recibido (" + message.address() + "): " + customMessage);
			String replyMessage = "Sí, yo te he escuchado al mensaje \"" + message.body().toString() + "\"";
			message.reply(gson.toJson(h));
		});
		startFuture.complete();
	}
	public void stop(Promise<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}
}
