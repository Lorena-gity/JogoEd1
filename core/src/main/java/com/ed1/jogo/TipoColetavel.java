package com.ed1.jogo;
/** Os tipos de coletável: cada um tem a imagem (dentro de assets/) e quantos pontos vale. */
public enum TipoColetavel {
    ESTRELA("itens/estrela.png", 2000),
    MUNDO("itens/mundo.png", 5000),
    FLORES("itens/flores.png", 1000);

    public final String arquivo;
    public final int pontos;

    TipoColetavel(String arquivo, int pontos) {
        this.arquivo = arquivo;
        this.pontos = pontos;
    }
}