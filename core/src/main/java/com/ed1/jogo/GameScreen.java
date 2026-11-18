package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {
    // o que a câmera enxerga (metade do mapa = zoom de 2x numa janela 1376x768)
    private static final float VIEW_W = 688;
    private static final float VIEW_H = 384;

    private static final float VELOCIDADE = 150; // pixels do mapa por segundo
    private static final float JOG_W = 28;
    private static final float JOG_H = 44;

    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final Texture mapa;
    private final Vector2 jogador = new Vector2(700, 280); // ajuste pra um chão livre

    public GameScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_W, VIEW_H, camera);
        mapa = new Texture("mapa1.jpeg");
        mapa.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    @Override
    public void render(float delta) {
        atualizar(delta);

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();

        // a câmera segue o jogador
        camera.position.set(jogador.x, jogador.y, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(mapa, 0, 0);
        batch.end();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.RED);
        shapes.rect(jogador.x - JOG_W / 2f, jogador.y, JOG_W, JOG_H);
        shapes.end();
    }

    private void atualizar(float delta) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    dy += 1;

        // nor() deixa o vetor com tamanho 1, senão a diagonal seria mais rápida
        Vector2 mov = new Vector2(dx, dy).nor().scl(VELOCIDADE * delta);
        jogador.add(mov);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        mapa.dispose();
    }
}
