package br.com.bean;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

import br.com.dao.DaoGenerico;
import br.com.entidades.Cidades;
import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.jpa.JpaUtil;
import br.com.repository.IDaoPessoaImpl;

@ViewScoped
@ManagedBean(name = "pessoaBean")
public class PessoaBean {

	private Pessoa pessoa = new Pessoa();
	private DaoGenerico<Pessoa> daoGenerico = new DaoGenerico<Pessoa>();
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();

	private IDaoPessoaImpl daoPessoaImpl = new IDaoPessoaImpl();

	private List<SelectItem> estados;
	private List<SelectItem> cidades;
	
	private Part arquivoFoto;

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public DaoGenerico<Pessoa> getDaoGenerico() {
		return daoGenerico;
	}

	public void setDaoGenerico(DaoGenerico<Pessoa> daoGenerico) {
		this.daoGenerico = daoGenerico;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}

	public List<SelectItem> getEstados() {
		this.setEstados();
		return estados;
	}

	public void setEstados() {
		this.estados = daoPessoaImpl.listEstados();
	}

	public List<SelectItem> getCidades() {
		return cidades;
	}

	public void setCidades(List<SelectItem> cidades) {
		this.cidades = cidades;
	}
	
	public Part getArquivoFoto() {
		return arquivoFoto;
	}

	public void setArquivoFoto(Part arquivoFoto) {
		this.arquivoFoto = arquivoFoto;
	}

	public String salvarAtualizar() throws Exception{
		
		/* processar imagem - inicio */
		
		byte[] imagemByte = this.getByte(this.arquivoFoto.getInputStream());
		this.pessoa.setFotoIconBase64Original(imagemByte); /*Salva Imagem Original*/
		
		/* Trnasformar em bufferImage */
		BufferedImage bufferedImage =  ImageIO.read(new ByteArrayInputStream(imagemByte));
		
		/* Pega o tipo da imagem */
		int type = bufferedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType();
		
		int largura = 200;
		int altura = 200;
		
		/* Criar a Miniatura */
		BufferedImage resizedImage = new BufferedImage(largura, altura, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(bufferedImage, 0, 0, largura, altura, null);
		g.dispose();
		
		/* Escrever novamente a imagem em tamanho menor */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String extensao = this.arquivoFoto.getContentType().split("\\/")[1]; /* image/png */
		ImageIO.write(resizedImage, extensao, baos);
		
		String miniImagem = "data:" + this.arquivoFoto.getContentType() + ";base64," + 
								DatatypeConverter.printBase64Binary(baos.toByteArray());
		
		/* processar imagem - fim */
		
		pessoa.setFotoIconBase64(miniImagem);
		pessoa.setExtensao(extensao);
		
		pessoa = daoGenerico.atualizar(pessoa);
		this.carregarPessoas();
		this.mostrarMsg("Usuário Cadastrado com Sucesso!");
		return "";
	}

	private void mostrarMsg(String msg) {

		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage(msg);
		facesContext.addMessage(null, message);
	}

	public String novo() {
		pessoa = new Pessoa();
		return "";
	}

	public String remove() {
		daoGenerico.deletePorId(pessoa);
		pessoa = new Pessoa();
		this.carregarPessoas();
		this.mostrarMsg("Removido Com Sucesso!");
		return "";
	}

	public void editar() {

		if (this.pessoa.getCidade() != null) {
			Estados estado = this.pessoa.getCidade().getEstados();
			this.pessoa.setEstado(estado);

			List<Cidades> cidades = JpaUtil.getEntityManager()
					.createQuery("from Cidades where estados = " + estado.getId()).getResultList();

			List<SelectItem> selectItemsCidade = new ArrayList<SelectItem>();

			for (Cidades cidade : cidades) {
				selectItemsCidade.add(new SelectItem(cidade, cidade.getNome()));
			}

			this.setCidades(selectItemsCidade);
		}

	}

	@PostConstruct
	public void carregarPessoas() {
		pessoas = daoGenerico.getListEntity(Pessoa.class);
	}

	public String logar() {

		Pessoa pessoaPesquisaBanco = daoPessoaImpl.consultarUsuario(pessoa.getLogin(), pessoa.getSenha());

		if (pessoaPesquisaBanco != null) { // achou o usuário

			// adicionar o usuário na sessão
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();

			HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
			HttpSession session = req.getSession();

			session.setAttribute("usuariologado", pessoaPesquisaBanco);

			return "primeirapagina.xhtml";
		}

		return "index.jsf";
	}

	public String deslogar() {

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = req.getSession();

		session.invalidate();

		return "index.jsf";
	}

	public boolean permitirAcesso(String acesso) {

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = req.getSession();

		Pessoa pessoaUser = (Pessoa) session.getAttribute("usuariologado");

		return pessoaUser.getPerfilUser().equals(acesso);

	}

	public void pesquisaCep(AjaxBehaviorEvent event) {

		try {

			URL url = new URL("https://viacep.com.br/ws/" + this.pessoa.getCep() + "/json/");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			String cep = "";
			StringBuilder jsonCep = new StringBuilder();

			while ((cep = br.readLine()) != null) {
				jsonCep.append(cep);
			}

			Pessoa gsonAux = new Gson().fromJson(jsonCep.toString(), Pessoa.class);

			pessoa.setLogradouro(gsonAux.getLogradouro());
			pessoa.setComplemento(gsonAux.getComplemento());
			pessoa.setBairro(gsonAux.getBairro());
			pessoa.setLocalidade(gsonAux.getLocalidade());
			pessoa.setUf(gsonAux.getUf());
			pessoa.setDdd(gsonAux.getDdd());

			System.out.println(jsonCep);

		} catch (Exception e) {
			this.mostrarMsg("Erro ao consultar o CEP");
			e.printStackTrace();
		}

	}

	public void carregarCidades(AjaxBehaviorEvent event) {

		Estados estado = (Estados) ((HtmlSelectOneMenu) event.getSource()).getValue();

		if (estado != null) {
			this.pessoa.setEstado(estado);

			List<Cidades> cidades = JpaUtil.getEntityManager()
					.createQuery("from Cidades where estados = " + estado.getId()).getResultList();

			List<SelectItem> selectItemsCidade = new ArrayList<SelectItem>();

			for (Cidades cidade : cidades) {
				selectItemsCidade.add(new SelectItem(cidade, cidade.getNome()));
			}

			this.setCidades(selectItemsCidade);
		}
	}
	
	// método que converte inputStream para array de byte
	public byte[] getByte(InputStream is) throws IOException {
		
			
			int len;
			int size = 1024;
			byte[] buffer = null;
		
			if(is instanceof ByteArrayInputStream) {
				size = is.available();
				buffer = new byte[size];
				len = is.read(buffer, 0, size);
			}else {
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				buffer = new byte[size];
				
				while( (len = is.read(buffer, 0 , size)) != -1 ) {
					bos.write(buffer, 0, len);
				}
				
				buffer = bos.toByteArray();			
			}
		
		return buffer;
			
	}
	
	public void download() throws IOException {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String fileDownloadId = params.get("fileDownloadId");
		
		Pessoa pessoa = daoGenerico.consultar(Pessoa.class, fileDownloadId);
		
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		
		response.addHeader("Content-Disposition", "attachment; filename=download." + pessoa.getExtensao());
		response.setContentType("application/octet-stream");
		response.setContentLength(pessoa.getFotoIconBase64Original().length);
		
		response.getOutputStream().write(pessoa.getFotoIconBase64Original());
		response.getOutputStream().flush();
		
		FacesContext.getCurrentInstance().responseComplete();
	}

}








