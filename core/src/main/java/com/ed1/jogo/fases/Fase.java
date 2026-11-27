package com.ed1.jogo.fases;
import com.ed1.jogo.Fila;
import com.ed1.jogo.ListaEncadeada;
import com.ed1.jogo.itens.Coletavel;

public class Fase {
    public final int numero;
    public final String mapa;
    public final float largura, altura;
    public final ListaEncadeada<Plataforma> plataformas = new ListaEncadeada<>();
    public final float inicioX, inicioY, chegadaX;
    public final ListaEncadeada<Coletavel> coletaveis = new ListaEncadeada<>();
    public void adicionarColetavel(Coletavel c) { coletaveis.adicionar(c); }

    public final Fila<String> falasIntro = new Fila<>(); // Falas do fantasminha ANTES da fase (depois do fade-in)
    public final Fila<String> falasFim = new Fila<>(); // Falas do fantasminha quando o gato chega no fim
    private boolean temIntro, temFim;

    /** Cada linha da tabela: {x0, x1, yTopo} ou {x0, x1, yTopo, yBase} (pilar sólido). */
    public Fase(int numero, String mapa, float largura, float altura, float[][] tabela,
                float inicioX, float inicioY, float chegadaX) {
        this.numero = numero;
        this.mapa = mapa;
        this.largura = largura;
        this.altura = altura;
        this.inicioX = inicioX;
        this.inicioY = inicioY;
        this.chegadaX = chegadaX;
        for (float[] p : tabela) {
            plataformas.adicionar(p.length > 3
                    ? new Plataforma(p[0], p[1], p[2], p[3])
                    : new Plataforma(p[0], p[1], p[2]));
        }
    }

    public void adicionarFalaIntro(String fala) { falasIntro.enfileirar(fala); temIntro = true; }
    public void adicionarFalaFim(String fala)   { falasFim.enfileirar(fala);   temFim = true; }

    public boolean temIntro() { return temIntro; }
    public boolean temFim()   { return temFim; }
    public boolean ultima()   { return numero >= Fases.TOTAL; }
}