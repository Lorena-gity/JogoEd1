package com.ed1.jogo;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Tela do jogo (PROVISÓRIA): só pinta tudo de rosa, sem usar nenhuma imagem.
 */
public class GameScreen extends ScreenAdapter {
    private final Main game;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1f, 0.4f, 0.7f, 1);   // rosa
    }
}