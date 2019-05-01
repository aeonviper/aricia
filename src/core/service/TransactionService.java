package core.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import core.central.Holder;

public class TransactionService {
	
	public void execute() {		
		EntityManager entityManager = Holder.getEntityManagerFactory().createEntityManager();		
		EntityTransaction transaction = entityManager.getTransaction();		
		try {
			GenericService.startTransaction(entityManager);
			transaction.begin();
			//System.out.println("transaction begin - execute");            
			wrap();
			//System.out.println("transaction commit - execute");            
			transaction.commit();
		} catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println(e);
            throw e;
        } finally {
        	GenericService.endTransaction(); 
            entityManager.close();                    
        }		
	}
	
	protected void wrap() {
		
	}
	
}
