package com.ed1.jogo.dialogo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.ed1.jogo.Fila;

public class CaixaDialogo {
    private static final float LETRAS_POR_SEGUNDO = 30;

    private final Fila<String> falas;   // vem da Fase
    private String atual = null;
    private float tempo = 0;
    private boolean iniciada = false;

    public CaixaDialogo(Fila<String> falas) {
        this.falas = falas;
    }

    public void comecar() {
        iniciada = true;
        atual = falas.desenfileirar();
        tempo = 0;
    }

    public boolean iniciada() { return iniciada; }
    public boolean ativa()    { return atual != null; }
    public boolean terminou() { return iniciada && atual == null; }

    public void atualizar(float dt) {
        if (atual != null) tempo += dt;
    }

    private int letrasVisiveis() {
        if (atual == null) return 0;
        return Math.min(atual.length(), (int) (tempo * LETRAS_POR_SEGUNDO));
    }

    /** ENTER: completa a fala atual ou passa pra próxima. */
    public void avancar() {
        if (atual == null) return;
        if (letrasVisiveis() < atual.length()) {
            tempo = 999;
        } else {
            atual = falas.desenfileirar(); // null se a fila acabou
            tempo = 0;
        }
    }

    public void desenhar(SpriteBatch batch, BitmapFont fonte, Texture pixel) {
        if (atual == null) return;
        float x = 288, y = 110, w = 800, h = 150;

        batch.setColor(1, 1, 1, 1);                       // borda
        batch.draw(pixel, x - 4, y - 4, w + 8, h + 8);
        batch.setColor(0.08f, 0.08f, 0.14f, 0.95f);       // fundo
        batch.draw(pixel, x, y, w, h);
        batch.setColor(Color.WHITE);

        fonte.getData().setScale(1.6f);
        fonte.setColor(1f, 0.8f, 0.2f, 1f);
        fonte.draw(batch, "Fantasminha", x + 25, y + h - 18);

        fonte.getData().setScale(2.2f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, atual.substring(0, letrasVisiveis()),
                x + 25, y + h - 65, w - 50, Align.left, true);

        if (letrasVisiveis() >= atual.length()) {
            fonte.getData().setScale(1.4f);
            fonte.setColor(0.7f, 0.7f, 0.7f, 1f);
            fonte.draw(batch, "ENTER", x + w - 110, y + 35);
        }
        fonte.setColor(Color.WHITE);
    }
}