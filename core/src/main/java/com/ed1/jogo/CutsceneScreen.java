package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Cutscene de abertura: mostra uma imagem por vez com a legenda embaixo.
 * ENTER passa pra próxima. Depois da última, abre o jogo.
 * As cenas ficam numa FILA: a cada ENTER a gente tira a primeira da fila (desenfileirar).
 */
public class CutsceneScreen extends ScreenAdapter {
    private static final float LARGURA = 1376;
    private static final float ALTURA = 768;
    private static final float ALTURA_LEGENDA = 170; // faixa preta embaixo, onde fica o texto
    private static final float MARGEM_TOPO = 60; // espaço vazio em cima da foto (aumenta = foto menor)
    private static final float ESPACO_FOTO_LEGENDA = 30; // espaço entre a foto e a legenda
    private static final float DURACAO_FADE = 0.6f; // segundos pra imagem "aparecer"

    // uma cena = o caminho da imagem (dentro de assets/) + o texto da legenda
    private static class Cena {
        final String imagem;
        final String legenda;

        Cena(String imagem, String legenda) {
            this.imagem = imagem;
            this.legenda = legenda;
        }
    }

    private final Main game;
    private final SpriteBatch batch;
    private final FitViewport viewport;
    private final BitmapFont fonte;
    private final Fila<Cena> fila = new Fila<>();

    private Cena atual; // a cena que está na tela agora (já saiu da fila)
    private Texture textura; // imagem da cena atual
    private float tempo; // tempo desde que a cena atual apareceu (pro fade)

    public CutsceneScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        viewport = new FitViewport(LARGURA, ALTURA);

        fonte = new BitmapFont();
        fonte.getData().setScale(2.2f);
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // AQUI você monta a cutscene: ordem de enfileirar = ordem que aparece =====
        fila.enfileirar(new Cena("cutscene/cena1.jpeg", "Legenda da cena 1"));
        fila.enfileirar(new Cena("cutscene/cena2.jpeg", "Legenda da cena 2"));
        fila.enfileirar(new Cena("cutscene/cena3.jpeg", "Legenda da cena 3"));
        fila.enfileirar(new Cena("cutscene/cena4.png", "Legenda da cena 4"));
        fila.enfileirar(new Cena("cutscene/cena5.png", "Legenda da cena 5"));

        proxima(); // tira a primeira cena da fila e mostra
    }

    /** Tira a próxima cena da fila. Se a fila acabou, vai pro jogo. */
    private void proxima() {
        if (textura != null) {
            textura.dispose(); // a cena anterior já foi vista, libera a imagem da memória
            textura = null;
        }

        atual = fila.desenfileirar();

        if (atual == null) { // fila vazia = acabou a cutscene
            game.setScreen(new JardimScreen(game));
            dispose();
            return;
        }

        textura = new Texture(atual.imagem);
        tempo = 0;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            proxima();
            if (atual == null) return; // a cutscene acabou, esta tela já foi descartada
        }

        tempo += delta;
        float alfa = Math.min(1f, tempo / DURACAO_FADE);

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // imagem: cabe na parte de cima da tela mantendo a proporção, centralizada
        float areaH = ALTURA - ALTURA_LEGENDA - MARGEM_TOPO - ESPACO_FOTO_LEGENDA;
        float escala = Math.min(LARGURA / textura.getWidth(), areaH / textura.getHeight());
        float w = textura.getWidth() * escala;
        float h = textura.getHeight() * escala;
        batch.setColor(1, 1, 1, alfa);
        batch.draw(textura, (LARGURA - w) / 2f, ALTURA_LEGENDA + ESPACO_FOTO_LEGENDA + (areaH - h) / 2f, w, h);
        batch.setColor(Color.WHITE);

        // legenda embaixo, centralizada, quebrando a linha sozinha se for grande
        fonte.setColor(1, 1, 1, alfa);
        fonte.draw(batch, atual.legenda, 60, ALTURA_LEGENDA - 30, LARGURA - 120, Align.center, true);

        // dica no canto
        fonte.setColor(0.6f, 0.6f, 0.6f, alfa);
        fonte.draw(batch, "ENTER >", 0, 45, LARGURA - 40, Align.right, false);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        fonte.dispose();
        if (textura != null) {
            textura.dispose();
        }
    }
}