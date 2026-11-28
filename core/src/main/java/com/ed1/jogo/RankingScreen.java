package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Mostra o top 10 e deixa digitar um nome pra buscar (ENTER busca, ESC volta). */
public class RankingScreen extends ScreenAdapter {
    private static final float LARGURA = 1376;
    private static final float ALTURA = 768;
    private static final int MAX_LINHAS = 10;
    private static final int MAX_NOME = 15;

    private final Main game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport viewport = new FitViewport(LARGURA, ALTURA);
    private final Texture fundo = new Texture("menu/Fundo_menu.jpeg");
    private final Texture pixel;
    private final BitmapFont fonte = new BitmapFont();

    private final Registro[] registros;
    private final StringBuilder busca = new StringBuilder();
    private String resultado = "";

    public RankingScreen(Main game) {
        this.game = game;
        registros = Ranking.carregar();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char c) {
                if (c == '\b') {
                    if (busca.length() > 0) busca.deleteCharAt(busca.length() - 1);
                } else if (c == '\r' || c == '\n') {
                    fazerBusca();
                } else if (c >= 32 && c < 127 && c != ';' && busca.length() < MAX_NOME) {
                    busca.append(c);
                }
                return true;
            }
        });
    }

    private void fazerBusca() {
        if (busca.length() == 0) {
            resultado = "";
            return;
        }
        int i = Ranking.buscar(registros, busca.toString());
        if (i == -1) {
            resultado = "Jogador nao encontrado.";
        } else {
            resultado = registros[i].nome + ": " + (i + 1) + "o lugar, " + registros[i].pontos + " pontos";
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setInputProcessor(null);
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(fundo, 0, 0, LARGURA, ALTURA);

        // painel escuro atrás do texto
        batch.setColor(0.05f, 0.05f, 0.1f, 0.85f);
        batch.draw(pixel, 388, 40, 600, 690);
        batch.setColor(Color.WHITE);

        fonte.getData().setScale(2.5f);
        fonte.setColor(1f, 0.8f, 0.2f, 1f);
        fonte.draw(batch, "RANKING", 388, 710, 600, Align.center, false);

        fonte.getData().setScale(1.6f);
        if (registros.length == 0) {
            fonte.setColor(Color.WHITE);
            fonte.draw(batch, "Ninguem jogou ainda!", 388, 600, 600, Align.center, false);
        }
        int n = Math.min(registros.length, MAX_LINHAS);
        for (int i = 0; i < n; i++) {
            float y = 650 - i * 38;
            fonte.setColor(i == 0 ? new Color(1f, 0.85f, 0.2f, 1f) : Color.WHITE);
            fonte.draw(batch, (i + 1) + ".  " + registros[i].nome, 420, y);
            fonte.draw(batch, String.valueOf(registros[i].pontos), 420, y, 540, Align.right, false);
        }

        // busca
        fonte.setColor(0.7f, 0.7f, 0.7f, 1f);
        fonte.getData().setScale(1.3f);
        fonte.draw(batch, "Buscar jogador (digite e aperte ENTER):", 420, 230);
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(1.6f);
        String cursor = ((int) (System.currentTimeMillis() / 500) % 2 == 0) ? "_" : "";
        fonte.draw(batch, busca + cursor, 420, 190);
        fonte.setColor(0.4f, 1f, 0.5f, 1f);
        fonte.draw(batch, resultado, 420, 130);

        fonte.setColor(0.7f, 0.7f, 0.7f, 1f);
        fonte.getData().setScale(1.2f);
        fonte.draw(batch, "ESC: voltar", 420, 75);
        fonte.setColor(Color.WHITE);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        fundo.dispose();
        pixel.dispose();
        fonte.dispose();
    }
}