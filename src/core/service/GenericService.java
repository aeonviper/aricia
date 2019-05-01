package core.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import javax.persistence.*;
//import org.springframework.transaction.annotation.Transactional;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.ejb.*;

import core.aspect.Transactional;
import core.model.GenericEntity;

public abstract class GenericService <T extends GenericEntity<ID>,ID extends Serializable> {

	static final ThreadLocal<EntityManager> transactionallyScoped = new ThreadLocal<EntityManager>() {
		protected EntityManager initialValue() {
			return null;
		}
	};

	protected Class<T> type;   

	public GenericService() {
		//this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}    

	public static boolean isInTransaction() {
		return transactionallyScoped.get() != null;
	}

	public static EntityManager getTransactionEntityManager() {
		return transactionallyScoped.get();
	}

	public static void startTransaction(EntityManager em) {
		transactionallyScoped.set(em);
	}

	public static void endTransaction() {
		transactionallyScoped.set(null);
	}    

	public Class<T> getType() {
		return type;
	}

	protected void setType(Class<T> type) {
		this.type = type; 
	}

	private String getClassName(Class c) {
		/*
		String name = c.getName();
		int firstChar = name.lastIndexOf ('.') + 1;
		if ( firstChar > 0 ) {
			name = name.substring (firstChar);
		}
		return name;
		 */
		return c.getSimpleName();
	}

	@Transactional
	public T find(ID id) {
		if (id == null) { return null; }
		return getTransactionEntityManager().find(type, id);
	}

	@Transactional
	public List<T> list() {
		return getTransactionEntityManager().createQuery("from " + type.getName()).getResultList();
	}

	@Transactional
	public List<T> listAll() {
		return getTransactionEntityManager().createNamedQuery(getClassName(type) + ".listAll").getResultList();
	}

	@Transactional
	public List<T> rangeListAll(int startPosition, int max) {
		return getTransactionEntityManager().createNamedQuery(getClassName(type) + ".listAll").
		setFirstResult(startPosition).
		setMaxResults(max).
		getResultList();
	}

	@Transactional
	public List<T> listByQuery(String query, Object... parameters) {
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		return namedQuery.getResultList();
	}

	@Transactional
	public List<T> rangeListByQuery(String query, int startPosition, int max, Object... parameters) {
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		return namedQuery.setFirstResult(startPosition).setMaxResults(max).getResultList();
	}

	@Transactional
	public List listAnythingByQuery(String query, Object... parameters) {       
		List list;
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		try {
			list = namedQuery.getResultList();
		} catch (NoResultException e) {
			list = new ArrayList();
		}
		return list;
	}

	@Transactional
	public List rangeListAnythingByQuery(String query, int startPosition, int max, Object... parameters) {       
		List list;
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		try {
			list = namedQuery.setFirstResult(startPosition).setMaxResults(max).getResultList();
		} catch (NoResultException e) {
			list = new ArrayList();
		}
		return list;
	}

	/* native query */

	@Transactional
	public List listAnythingByNativeQuery(String query, String resultSetMapping, Object... parameters) {       
		List list;
		javax.persistence.Query nativeQuery = getTransactionEntityManager().createNativeQuery(query, resultSetMapping);
		for (int i=0;i<parameters.length;i=i+2) {
			nativeQuery = nativeQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		try {
			list = nativeQuery.getResultList();
		} catch (NoResultException e) {
			list = new ArrayList();
		}
		return list;
	}

	@Transactional
	public Object findAnythingByNativeQuery(String query, String resultSetMapping, Object... parameters) {       
		Object obj;
		javax.persistence.Query nativeQuery = getTransactionEntityManager().createNativeQuery(query, resultSetMapping);
		for (int i=0;i<parameters.length;i=i+2) {
			nativeQuery = nativeQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		try {
			obj = nativeQuery.getSingleResult();
		} catch (NoResultException e) {
			obj = null;
		}
		return obj;
	}


	@Transactional
	public T findByQuery(String query, Object... parameters) {       
		T entity;
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		try {
			entity = (T) namedQuery.getSingleResult();
		} catch (NoResultException e) {
			entity = null;
		}
		return entity;
	}

	@Transactional
	public Object findAnythingByQuery(String query, Object... parameters) {       
		Object entity;
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		try {
			entity = namedQuery.getSingleResult();
		} catch (NoResultException e) {
			entity = null;
		}
		return entity;
	}

	/* criteria */

	@Transactional
	public List<T> listByCriteria(Criterion... criterion) {
		Criteria criteria = ((HibernateEntityManager)getTransactionEntityManager()).getSession().createCriteria(type);
		for (Criterion c : criterion) {
			criteria.add(c);
		}       
		return criteria.list();
	}

	@Transactional
	public List<T> listByCriteriaWithOrder(Order[] orders, Criterion... criterion) {
		Criteria criteria = ((HibernateEntityManager)getTransactionEntityManager()).getSession().createCriteria(type);
		for (Criterion c : criterion) {
			criteria.add(c);
		}       
		for (int i = 0;i < orders.length; i++) {
			criteria.addOrder(orders[i]);
		}
		return criteria.list();
	}

	@Transactional
	public List<T> listByCriteria(int startPosition, int max, Criterion... criterion) {
		Criteria criteria = ((HibernateEntityManager)getTransactionEntityManager()).getSession().createCriteria(type);
		for (Criterion c : criterion) {
			criteria.add(c);
		}       
		return criteria.setFirstResult(startPosition).setMaxResults(max).list();
	}

	@Transactional
	public List<T> listByCriteriaWithOrder(int startPosition, int max, Order[] orders, Criterion... criterion) {
		Criteria criteria = ((HibernateEntityManager)getTransactionEntityManager()).getSession().createCriteria(type);
		for (Criterion c : criterion) {
			criteria.add(c);
		}       
		for (int i = 0;i < orders.length; i++) {
			criteria.addOrder(orders[i]);
		}
		return criteria.setFirstResult(startPosition).setMaxResults(max).list();
	}

	@Transactional
	public T findByCriteria(Criterion... criterion) {
		T entity;
		Criteria criteria = ((HibernateEntityManager)getTransactionEntityManager()).getSession().createCriteria(type);
		for (Criterion c : criterion) {
			criteria.add(c);
		}
		try {
			entity = (T) criteria.uniqueResult();
		} catch (HibernateException e) {
			entity = null;
		}
		return entity;
	}

	@Transactional
	public Integer countByCriteria(Criterion... criterion) {
		Integer count = 0;
		Criteria criteria = ((HibernateEntityManager)getTransactionEntityManager()).getSession().createCriteria(type);
		criteria.setProjection(Projections.rowCount());
		for (Criterion c : criterion) {
			criteria.add(c);
		}
		try {
			count = (Integer) criteria.uniqueResult();
		} catch (HibernateException e) {
			// zero or null ?
			count = 0;
		}
		return count;
	}

	/* execute */

	@Transactional
	public void executeQuery(String query, Object... parameters) {
		javax.persistence.Query namedQuery = getTransactionEntityManager().createNamedQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			namedQuery = namedQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		namedQuery.executeUpdate();
	}

	@Transactional
	public void executeNativeQuery(String query, Object... parameters) {
		javax.persistence.Query nativeQuery = getTransactionEntityManager().createNativeQuery(query);
		for (int i=0;i<parameters.length;i=i+2) {
			nativeQuery = nativeQuery.setParameter((String) parameters[i], parameters[i+1]);
		}
		nativeQuery.executeUpdate();
	}

	@Transactional
	public void save(T entity) {
		if (entity.getId() == null) {
			getTransactionEntityManager().persist(entity);
		} else {
			// jpa contract says the returned entity is the one bound to the persistence context
			getTransactionEntityManager().merge(entity);
		}
	}

	@Transactional
	public void delete(ID id) {
		T entity = getTransactionEntityManager().find(type, id);
		if (entity != null) {
			getTransactionEntityManager().remove(entity);
		}		
	}

	@Transactional
	public void delete(T entity) {
		if (entity != null) {
			getTransactionEntityManager().remove(entity);
		}
	}

	/* helper methods */

	public static <E> List<E> listBuilder(List l, EntryBuilder<E> entryBuilder) {
		if (l == null || l.size() == 0) {
			return new ArrayList<E>();
		}
		List<E> list = new ArrayList<E>(l.size());
		Object[] objs;
		for (Iterator it = l.iterator(); it.hasNext(); ) {
			objs = (Object[]) it.next();
			list.add(entryBuilder.build(objs));
		}
		return list;
	}

	public interface EntryBuilder<E> {
		public E build(Object[] objects);
	}

}