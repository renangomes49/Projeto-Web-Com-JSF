package br.com.repository;

import java.util.List;

import br.com.entidades.Lancamento;

public interface IDaoLancamento {

	public List<Lancamento> consultar(Long idUser);
	
}
