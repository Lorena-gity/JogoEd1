package com.ed1.jogo;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    // dados da partida atual: todas as telas recebem o "game", então todas enxergam isso
    public String nomeJogador = "";
    public int pontos = 0;

    @Override
    public void create() { // só roda uma vez quando o jogo abre, carrega tudo antes do render
        setScreen(new MenuScreen(this));
    }
}