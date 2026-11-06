package com.ed1.jogo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() { // só roda uma vez quando o jogo abre, carrega tudo antes do render
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render() { // renderização, é onde o jogo desenha a tela, lógica de atualização (ex.: mover)
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() { // libera memória que foi criada no create
        batch.dispose();
        image.dispose();
    }
}
