package core.aspect;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.service.GenericService;

public class TransactionInterceptor implements MethodInterceptor {

    protected final Logger logger = LoggerFactory.getLogger("core");	

    private EntityManagerFactory entityManagerFactory;

    public TransactionInterceptor setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        return this;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        if (!(methodInvocation.getThis() instanceof GenericService)) {
            throw new RuntimeException("Only objects that extends " + GenericService.class.getName() + " can have methods intercepted");
        }

        Object result = null;
        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        Transactional transactional = methodInvocation.getMethod().getAnnotation(Transactional.class);

        if (transactional != null) {
            if (!GenericService.isInTransaction()) {            	            	
                entityManager = entityManagerFactory.createEntityManager();
                transaction = entityManager.getTransaction();
                try {
                    GenericService.startTransaction(entityManager);
                    
                    transaction.begin();
                    //System.out.println("transaction begin - " + methodInvocation.getMethod().getName());
                    result = methodInvocation.proceed();
                    //System.out.println("transaction commit - " + methodInvocation.getMethod().getName());
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
            } else {
            	//System.out.println("transaction already begin - start - " + methodInvocation.getMethod().getName());                
                result = methodInvocation.proceed();
                //System.out.println("transaction already begin - end   - " + methodInvocation.getMethod().getName());    
            }
        } else {
            result = methodInvocation.proceed();
        }

        return result;
    }
    
}
