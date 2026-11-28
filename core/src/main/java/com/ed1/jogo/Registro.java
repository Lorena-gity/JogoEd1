package com.ed1.jogo;

/** Uma linha do ranking: nome do jogador + pontos. */
public class Registro {
    public final String nome;
    public final int pontos;

    public Registro(String nome, int pontos) {
        this.nome = nome;
        this.pontos = pontos;
    }
}