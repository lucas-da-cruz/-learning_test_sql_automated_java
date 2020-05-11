package br.com.pm73.dataBuilder;

import br.com.pm73.dominio.Lance;
import br.com.pm73.dominio.Leilao;
import br.com.pm73.dominio.Usuario;

import java.util.Calendar;
import java.util.List;

public class LeilaoBuilder {

    private Usuario dono;
    private double valor;
    private String nome;
    private boolean usado;
    private Calendar dataAbertura;
    private boolean encerrado;
    private List<Lance> lances;


    public LeilaoBuilder() {
        this.dono = new Usuario("Joao da Silva", "joao@silva.com.br");
        this.valor = 1500.0;
        this.nome = "XBox";
        this.usado = false;
        this.dataAbertura = Calendar.getInstance();
    }

    public LeilaoBuilder comDono(Usuario dono) {
        this.dono = dono;
        return this;
    }

    public LeilaoBuilder comValor(double valor) {
        this.valor = valor;
        return this;
    }

    public LeilaoBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public LeilaoBuilder usado() {
        this.usado = true;
        return this;
    }

    public LeilaoBuilder encerrado() {
        this.encerrado = true;
        return this;
    }

    public LeilaoBuilder diasAtras(int dias) {
        Calendar data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_MONTH, -dias);

        this.dataAbertura = data;

        return this;
    }

    public LeilaoBuilder comLance(Calendar data, Usuario usuario, double valor) {
        Leilao leilao = new Leilao(nome, valor, dono, usado);
        Lance lance = new Lance(data, usuario, valor, leilao);
        lances.add(lance);
        return this;
    }

    public Leilao constroi() {
        Leilao leilao = new Leilao(nome, valor, dono, usado);
        leilao.setDataAbertura(dataAbertura);
        for(int i = 0; i < lances.size(); i++){
            leilao.adicionaLance(lances.get(i));
        }

        if(encerrado) leilao.encerra();

        return leilao;
    }

}