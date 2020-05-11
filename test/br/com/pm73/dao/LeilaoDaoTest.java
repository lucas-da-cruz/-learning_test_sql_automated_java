package br.com.pm73.dao;

import br.com.pm73.dataBuilder.LeilaoBuilder;
import br.com.pm73.dominio.Lance;
import br.com.pm73.dominio.Leilao;
import br.com.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LeilaoDaoTest {
    private Session session;
    private UsuarioDao usuarioDao;
    private LeilaoDao leilaoDao;

    @Before
    public void antes(){
        session = new CriadorDeSessao().getSession();
        usuarioDao = new UsuarioDao(session);
        leilaoDao = new LeilaoDao(session);

        //Aqui defino uma nova transação no banco,
        //para depois realizar o roolback
        session.beginTransaction();
    }

    @After
    public void depois(){
        //Aqui realizo o roolback para zerar os dados que meu
        //teste inseriu e não impactar o próximo teste
        session.getTransaction().rollback();
        session.close();
    }

    @Test
    public void deveContarLeiloesNaoEncerrados(){

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Leilao ativo = new Leilao("Geladeira", 1500.0, mauricio, false);
        Leilao encerrado = new Leilao("Xbox", 1500.0, mauricio, false);

        encerrado.encerra();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        long total = leilaoDao.total();

        assertEquals(1, total);
    }

    @Test
    public void deveRetornarZeroSeNaoHaLeiloesNovos(){

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Leilao encerrado1 = new Leilao("Geladeira", 1500.0, mauricio, false);
        Leilao encerrado2 = new Leilao("Xbox", 1500.0, mauricio, false);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(encerrado1);
        leilaoDao.salvar(encerrado2);

        encerrado1.encerra();
        encerrado2.encerra();

        long total = leilaoDao.total();

        assertEquals(0, total);
    }

    @Test
    public void retornaQtdeLeiloesNovos(){

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Leilao leilaoUsado = new Leilao("Geladeira", 1500.0, mauricio, true);
        Leilao leilaoNovo = new Leilao("Xbox", 1500.0, mauricio, false);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilaoUsado);
        leilaoDao.salvar(leilaoNovo);

        List<Leilao> total = leilaoDao.novos();

        assertEquals(1, total.size());
        assertEquals("Xbox", total.get(0).getNome());
    }

    @Test
    public void deveTrazerSomenteLeiloesAntigos(){

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Leilao leilaoAntigo = new Leilao("Geladeira", 1500.0, mauricio, true);
        Leilao leilaoNovo = new Leilao("Xbox", 1500.0, mauricio, false);

        leilaoAntigo.setDataAbertura( new GregorianCalendar(2013,1,28));
        leilaoNovo.setDataAbertura( new GregorianCalendar(2020,5,8));

        //OU
        /*Calendar dataRecente = Calendar.getInstance();
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);*/

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilaoAntigo);
        leilaoDao.salvar(leilaoNovo);

        List<Leilao> total = leilaoDao.antigos();

        assertEquals(1, total.size());
        assertEquals("Geladeira", total.get(0).getNome());
    }

    @Test
    public void deveTrazerSomenteLeiloesAntigosHaMaisDe7Dias(){

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Leilao criadoA7DiasAtras = new Leilao("Geladeira", 1500.0, mauricio, true);

        Calendar seteDiasAtras = Calendar.getInstance();
        seteDiasAtras.add(Calendar.DAY_OF_MONTH, -7);

        criadoA7DiasAtras.setDataAbertura(seteDiasAtras);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(criadoA7DiasAtras);

        List<Leilao> total = leilaoDao.antigos();

        assertEquals(1, total.size());
        assertEquals("Geladeira", total.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodo(){
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, - 10);

        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Leilao leilao1 = new Leilao("Xbox", 700.0, mauricio, false);
        Calendar dataLeilao1 = Calendar.getInstance();
        dataLeilao1.add(Calendar.DAY_OF_MONTH, - 2);

        leilao1.setDataAbertura(dataLeilao1);

        Leilao leilao2 = new Leilao("Geladeira", 900.0, mauricio, false);
        Calendar dataLeilao2 = Calendar.getInstance();
        dataLeilao2.add(Calendar.DAY_OF_MONTH, - 20);

        leilao2.setDataAbertura(dataLeilao2);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        assertEquals(1, leiloes.size());
        assertEquals("Xbox", leiloes.get(0).getNome());
    }

    @Test
    public void naoDeveTrazerLeiloesEncerradoNoPeriodo(){
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, - 10);

        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");

        Calendar dataLeilao1 = Calendar.getInstance();
        dataLeilao1.add(Calendar.DAY_OF_MONTH, - 2);

        Leilao leilao1 = new Leilao("Xbox", 700.0, mauricio, false);
        leilao1.setDataAbertura(dataLeilao1);
        leilao1.encerra();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);

        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        assertEquals(0, leiloes.size());
    }

    @Test
    public void deveTrazerLeiloesEncerradosNoDisputadosEntre() {

        Calendar dataLance = Calendar.getInstance();

        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
        Usuario maria = new Usuario("Maria", "maria@mauricio.com.br");

        Calendar dataLeilao1 = Calendar.getInstance();

        Leilao leilao1 = new Leilao("Xbox", 500.0, mauricio, false);
        leilao1.setDataAbertura(dataLeilao1);
        leilao1.adicionaLance(new Lance(dataLance, maria, 510, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, mauricio, 520, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, maria, 530, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, mauricio, 540, leilao1));

        Leilao leilao2 = new Leilao("Geladeira", 700.0, mauricio, false);
        leilao2.setDataAbertura(dataLeilao1);
        leilao2.adicionaLance(new Lance(dataLance, maria, 710, leilao1));
        leilao2.adicionaLance(new Lance(dataLance, mauricio, 720, leilao1));
        leilao2.adicionaLance(new Lance(dataLance, maria, 730, leilao1));
        leilao2.adicionaLance(new Lance(dataLance, mauricio, 740, leilao1));

        usuarioDao.salvar(mauricio);
        usuarioDao.salvar(maria);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(400, 600);

        assertEquals(1, leiloes.size());
        assertEquals("Xbox", leiloes.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesNoDisputadosEntreQueEstejaEncerrado() {

        Calendar dataLance = Calendar.getInstance();

        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
        Usuario maria = new Usuario("Maria", "maria@mauricio.com.br");

        Calendar dataLeilao1 = Calendar.getInstance();

        Leilao leilao1 = new Leilao("Xbox", 500.0, mauricio, false);
        leilao1.setDataAbertura(dataLeilao1);
        leilao1.adicionaLance(new Lance(dataLance, maria, 510, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, mauricio, 520, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, maria, 530, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, mauricio, 540, leilao1));

        Leilao leilao2 = new Leilao("Geladeira", 700.0, mauricio, false);
        leilao2.setDataAbertura(dataLeilao1);
        leilao2.adicionaLance(new Lance(dataLance, maria, 710, leilao1));
        leilao2.adicionaLance(new Lance(dataLance, mauricio, 720, leilao1));
        leilao2.adicionaLance(new Lance(dataLance, maria, 730, leilao1));
        leilao2.adicionaLance(new Lance(dataLance, mauricio, 740, leilao1));
        leilao2.encerra();

        usuarioDao.salvar(mauricio);
        usuarioDao.salvar(maria);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(400, 800);

        assertEquals(1, leiloes.size());
        assertEquals("Xbox", leiloes.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesNoListaLeiloesDoUsuario() {

        Calendar dataLance = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
        Usuario maria = new Usuario("Maria", "maria@mauricio.com.br");

        Leilao leilao1 = new Leilao("Xbox", 500.0, mauricio, false);
        leilao1.adicionaLance(new Lance(dataLance, maria, 510, leilao1));
        leilao1.adicionaLance(new Lance(dataLance, mauricio, 520, leilao1));

        Leilao leilao2 = new Leilao("Geladeira", 700.0, maria, false);
        leilao2.adicionaLance(new Lance(dataLance, mauricio, 720, leilao1));

        Leilao leilao3 = new Leilao("Fogão", 800.0, mauricio, false);
        leilao3.adicionaLance(new Lance(dataLance, maria, 710, leilao1));

        usuarioDao.salvar(mauricio);
        usuarioDao.salvar(maria);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);
        leilaoDao.salvar(leilao3);

        List<Leilao> leiloesMauricio = leilaoDao.listaLeiloesDoUsuario(mauricio);

        assertEquals(2, leiloesMauricio.size());
        assertEquals("Xbox", leiloesMauricio.get(0).getNome());
        assertEquals("Geladeira", leiloesMauricio.get(1).getNome());

        List<Leilao> leiloesMaria = leilaoDao.listaLeiloesDoUsuario(maria);

        assertEquals(2, leiloesMaria.size());
        assertEquals("Xbox", leiloesMaria.get(0).getNome());
        assertEquals("Fogão", leiloesMaria.get(1).getNome());
    }

    @Test
    public void listaSomenteOsLeiloesDoUsuario() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Usuario comprador2 = new Usuario("Guilherme", "g@g.com");
        Leilao leilao = new LeilaoBuilder()
                .comDono(dono)
                .comValor(50.0)
                .comLance(Calendar.getInstance(), comprador, 100.0)
                .comLance(Calendar.getInstance(), comprador2, 200.0)
                .constroi();
        Leilao leilao2 = new LeilaoBuilder()
                .comDono(dono)
                .comValor(250.0)
                .comLance(Calendar.getInstance(), comprador2, 100.0)
                .constroi();
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        usuarioDao.salvar(comprador2);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }

    @Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Leilao leilao = new LeilaoBuilder()
                .comDono(dono)
                .comLance(Calendar.getInstance(), comprador, 100.0)
                .comLance(Calendar.getInstance(), comprador, 200.0)
                .constroi();
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }

}
