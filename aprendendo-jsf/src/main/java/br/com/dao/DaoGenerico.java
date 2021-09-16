package br.com.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.jpa.JpaUtil;


public class DaoGenerico<E> {

public void salvar(E entidade) {
		
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		entityManager.persist(entidade);
		
		entityTransaction.commit();
		entityManager.close();
		
	}
	
	public E atualizar(E entidade) {
		
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		E objeto = entityManager.merge(entidade);
		
		entityTransaction.commit();
		entityManager.close();
		
		return objeto;
		
	}
	
	public void delete(E entidade) {
		
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		entityManager.remove(entidade);
		
		entityTransaction.commit();
		entityManager.close();
		
		
	}
	
	public void deletePorId(E entidade) {
		
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		Object id = JpaUtil.getPrimaryKey(entidade);
		entityManager.createNativeQuery("delete from " + entidade.getClass().getSimpleName().toLowerCase() + " where id =" + id ).executeUpdate();		
		entityTransaction.commit();
		entityManager.close();
	}
	
	public List<E> getListEntity(Class<E> entidade){
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		List<E> listEntidade = entityManager.createQuery("from " + entidade.getName() + " order by nome").getResultList();
		
		entityTransaction.commit();
		entityManager.close();
		return listEntidade;
	}
	
	public E consultar(Class<E> entidade, String codigo) {
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		E objeto = (E) entityManager.find(entidade, Long.parseLong(codigo));
	
		return objeto;
	}
	
}
