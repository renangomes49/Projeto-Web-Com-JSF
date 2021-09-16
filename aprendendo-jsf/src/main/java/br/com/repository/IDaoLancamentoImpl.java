package br.com.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.entidades.Lancamento;
import br.com.jpa.JpaUtil;

public class IDaoLancamentoImpl implements IDaoLancamento{

	@Override
	public List<Lancamento> consultar(Long idUser) {
		
		List<Lancamento> lancamentos = null;
		
		EntityManager entityManager = JpaUtil.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		lancamentos = entityManager.createQuery("from Lancamento where usuario.id = " + idUser).getResultList();
		entityTransaction.commit();
		
		entityManager.close();
		
		return lancamentos;
	}

}
