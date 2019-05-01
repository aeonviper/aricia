package core.central;

import javax.persistence.EntityManagerFactory;

import com.google.inject.Injector;

public class Holder {

	private static Injector injector;
	private static EntityManagerFactory entityManagerFactory;
	
	public static EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public static void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		Holder.entityManagerFactory = entityManagerFactory;
	}

	public static Injector getInjector() {
		return injector;
	}

	public static void setInjector(Injector inj) {
		injector = inj;
	}
	
}
