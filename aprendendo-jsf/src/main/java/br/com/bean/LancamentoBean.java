package br.com.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import br.com.dao.DaoGenerico;
import br.com.entidades.Lancamento;
import br.com.entidades.Pessoa;
import br.com.repository.IDaoLancamentoImpl;

@ViewScoped
@ManagedBean(name = "lancamentoBean")
public class LancamentoBean {
	
	private Lancamento lancamento = new Lancamento();
	private DaoGenerico<Lancamento> daoGenerico = new DaoGenerico<Lancamento>();
	private List<Lancamento> lancamentos = new ArrayList<Lancamento>();
	
	private IDaoLancamentoImpl daoLancamentoImpl = new IDaoLancamentoImpl();
	
	public Lancamento getLancamento() {
		return lancamento;
	}
	public void setLancamento(Lancamento lancamento) {
		this.lancamento = lancamento;
	}
	public DaoGenerico<Lancamento> getDaoGenerico() {
		return daoGenerico;
	}
	public void setDaoGenerico(DaoGenerico<Lancamento> daoGenerico) {
		this.daoGenerico = daoGenerico;
	}
	public List<Lancamento> getLancamentos() {
		return lancamentos;
	}
	public void setLancamentos(List<Lancamento> lancamentos) {
		this.lancamentos = lancamentos;
	}

	public String salvar() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		
		HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = req.getSession();
		
		Pessoa pessoaUser = (Pessoa) session.getAttribute("usuariologado");
		
		lancamento.setUsuario(pessoaUser);
		
		this.lancamento = daoGenerico.atualizar(lancamento);
		
		
		this.carregarLancamentos();
		
		return "";
	}
	
	@PostConstruct
	private void carregarLancamentos() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		
		HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = req.getSession();
		
		Pessoa pessoaUser = (Pessoa) session.getAttribute("usuariologado");
		
		this.lancamentos = daoLancamentoImpl.consultar(pessoaUser.getId());	
	}
	public String novo() {
		this.lancamento = new Lancamento();
		return "";
	}
	
	public String remove() {
		daoGenerico.deletePorId(this.lancamento);
		this.lancamento = new Lancamento();
		this.carregarLancamentos();
		return "";
	}
	
}
