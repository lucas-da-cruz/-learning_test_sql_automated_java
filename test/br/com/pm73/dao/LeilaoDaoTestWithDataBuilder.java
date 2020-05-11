package br.com.pm73.dao;
import br.com.pm73.dataBuilder.LeilaoBuilder;
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

public class LeilaoDaoTestWithDataBuilder {
    private Session session;
    private LeilaoDao leilaoDao;
    private UsuarioDao usuarioDao;

    @Before
    public void antes() {
        session = new CriadorDeSessao().getSession();
        leilaoDao = new LeilaoDao(session);
        usuarioDao = new UsuarioDao(session);

        // inicia transacao
        session.beginTransaction();
    }

    @After
    public void depois() {
        // faz o rollback
        session.getTransaction().rollback();
        session.close();
    }

    @Test
    public void deveContarLeiloesNaoEncerrados() {
        // criamos um usuario
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        // criamos os dois leiloes

        Leilao ativo = new LeilaoBuilder()
                .comDono(mauricio)
                .constroi();
        Leilao encerrado = new LeilaoBuilder()
                .comDono(mauricio)
                .encerrado()
                .constroi();

        // persistimos todos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        // pedimos o total para o DAO
        long total = leilaoDao.total();

        assertEquals(1L, total);
    }

    @Test
    public void deveRetornarZeroSeNaoHaLeiloesNovos() {
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        Leilao encerrado = new LeilaoBuilder()
                .comDono(mauricio)
                .encerrado()
                .constroi();
        Leilao tambemEncerrado = new LeilaoBuilder()
                .comDono(mauricio)
                .encerrado().constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(encerrado);
        leilaoDao.salvar(tambemEncerrado);

        long total = leilaoDao.total();

        assertEquals(0L, total);
    }

    @Test
    public void deveRetornarLeiloesDeProdutosNovos() {
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        Leilao produtoNovo =
                new LeilaoBuilder()
                        .comDono(mauricio)
                        .comNome("XBox")
                        .constroi();
        Leilao produtoUsado =
                new LeilaoBuilder().comNome("XBox")
                        .comDono(mauricio)
                        .usado()
                        .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(produtoNovo);
        leilaoDao.salvar(produtoUsado);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertEquals("XBox", novos.get(0).getNome());
    }

    @Test
    public void deveTrazerSomenteLeiloesAntigos() {
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        Leilao recente = new LeilaoBuilder()
                .comNome("XBox")
                .comDono(mauricio)
                .constroi();
        Leilao antigo = new LeilaoBuilder()
                .comDono(mauricio)
                .comNome("Geladeira")
                .diasAtras(10)
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(recente);
        leilaoDao.salvar(antigo);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
        assertEquals("Geladeira", antigos.get(0).getNome());
    }

    @Test
    public void deveTrazerSomenteLeiloesAntigosHaMaisDe7Dias() {
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        Leilao noLimite = new LeilaoBuilder()
                .diasAtras(7)
                .comDono(mauricio)
                .constroi();

        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -7);

        noLimite.setDataAbertura(dataAntiga);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(noLimite);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
    }

    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {

        // criando as datas
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        // criando os leiloes, cada um com uma data
        Leilao leilao1 = new LeilaoBuilder()
                .diasAtras(2)
                .comDono(mauricio)
                .comNome("XBox")
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .diasAtras(20)
                .comDono(mauricio)
                .comNome("XBox")
                .constroi();

        // persistindo os objetos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        // invocando o metodo para testar
        List<Leilao> leiloes =
                leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // garantindo que a query funcionou
        assertEquals(1, leiloes.size());
        assertEquals("XBox", leiloes.get(0).getNome());
    }

    @Test
    public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {

        // criando as datas
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();
        Calendar dataDoLeilao1 = Calendar.getInstance();
        dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);

        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        // criando os leiloes, cada um com uma data
        Leilao leilao1 = new LeilaoBuilder()
                .comDono(mauricio)
                .diasAtras(2)
                .comNome("XBox")
                .encerrado()
                .constroi();

        // persistindo os objetos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);

        // invocando o metodo para testar
        List<Leilao> leiloes =
                leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // garantindo que a query funcionou
        assertEquals(0, leiloes.size());
    }


}
