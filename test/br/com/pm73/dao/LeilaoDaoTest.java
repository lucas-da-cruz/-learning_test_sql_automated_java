package br.com.pm73.dao;

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


}
