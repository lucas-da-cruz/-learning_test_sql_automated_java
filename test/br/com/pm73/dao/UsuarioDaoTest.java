package br.com.pm73.dao;

import br.com.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UsuarioDaoTest {

    private Session session;
    private UsuarioDao usuarioDao;

    @Before
    public void antes(){
        session = new CriadorDeSessao().getSession();
        usuarioDao = new UsuarioDao(session);

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
    public void deveEncontrarPeloNomeEEmail() {
        //Passado para o before
        /*Session session = new CriadorDeSessao().getSession();
        UsuarioDao usuarioDao = new UsuarioDao(session);*/

        // criando um usuario e salvando antes
        // de invocar o porNomeEEmail
        Usuario novoUsuario = new Usuario("João da Silva", "joao@dasilva.com.br");
        usuarioDao.salvar(novoUsuario);

        // agora buscamos no banco
        Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("João da Silva", "joao@dasilva.com.br");

        assertEquals("João da Silva", usuarioDoBanco.getNome());
        assertEquals("joao@dasilva.com.br", usuarioDoBanco.getEmail());
    }

    @Test
    public void porNomeEEmail() {
        //Passado para o before
        /*Session session = new CriadorDeSessao().getSession();
        UsuarioDao usuarioDao = new UsuarioDao(session);*/

        // agora buscamos no banco
        Usuario usuarioDoBanco = usuarioDao
                .porNomeEEmail("João da Silva", "joao@dasilva.com.br");

        assertNull(usuarioDoBanco);
    }

    //Maneira que fazendo mockando dados
    //E não simulando o Banco de dados
    /*@Test
    public void deveEncontrarPeloNomeEEmailMockado() {
        Session session = Mockito.mock(Session.class);
        Query query = Mockito.mock(Query.class);
        UsuarioDao usuarioDao = new UsuarioDao(session);

        Usuario usuario = new Usuario
                ("João da Silva", "joao@dasilva.com.br");
        String sql = "from Usuario u where u.nome = :nome and x.email = :email";

        Mockito.when(session.createQuery(sql)).thenReturn(query);
        Mockito.when(query.uniqueResult()).thenReturn(usuario);
        Mockito.when(query.setParameter("nome", "João da Silva")).thenReturn(query);
        Mockito.when(query.setParameter("email", "joao@dasilva.com.br")).thenReturn(query);

        Usuario usuarioDoBanco = usuarioDao
                .porNomeEEmail("João da Silva", "joao@dasilva.com.br");

        assertEquals(usuario.getNome(), usuarioDoBanco.getNome());
        assertEquals(usuario.getEmail(), usuarioDoBanco.getEmail());

    }*/
}
