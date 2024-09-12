package es.us.dad.dadVertx.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class DadVerticlePublisher extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startFuture) {
		String name1 = DadVerticleBroadcastConsumer.class.getName();
		getVertx().deployVerticle(name1, deployResult -> {
			if (deployResult.succeeded()) {
				System.out.println(name1 + " (" + deployResult.result() + ") ha sido desplegado correctamente");
			} else {
				deployResult.cause().printStackTrace();
			}
		});
		
		String name2 = DadVerticleBroadcastSender.class.getName();
		getVertx().deployVerticle(name2, deployResult -> {
			if (deployResult.succeeded()) {
				System.out.println(name2 + " (" + deployResult.result() + ") ha sido desplegado correctamente");
			} else {
				deployResult.cause().printStackTrace();
			}
		});
	}

	@Override
	public void stop(Promise<Void> stopFuture) throws Exception {
		getVertx().undeploy(DadVerticleBroadcastConsumer.class.getName());
		getVertx().undeploy(DadVerticleBroadcastSender.class.getName());
		super.stop(stopFuture);
	}
}


