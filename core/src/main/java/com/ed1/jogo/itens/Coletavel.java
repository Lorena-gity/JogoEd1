package com.ed1.jogo.itens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.ed1.jogo.entidades.Gato;

public class Coletavel {
    private static final float ALTURA = 44; // tamanho no mapa; a largura segue a proporção da imagem

    public final float x, y;   // CENTRO do item; y contado de cima da imagem, igual às plataformas
    public final int pontos;

    private final Texture textura;
    private final float largura;
    private boolean pego = false;
    private float tempo = 0;

    public Coletavel(String sprite, float x, float y, int pontos) {
        this.x = x;
        this.y = y;
        this.pontos = pontos;
        textura = new Texture(sprite);
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        largura = ALTURA * textura.getWidth() / textura.getHeight();
    }

    public void atualizar(float dt) { tempo += dt; }

    /** Devolve true UMA vez: no momento em que o gato encosta. */
    public boolean tentarPegar(Vector2 gatoPos, float alturaMapa) {
        if (pego) return false;
        float cy = alturaMapa - y; // centro do item em coordenadas do mundo (y pra cima)
        boolean encostou =
                gatoPos.x + Gato.HITBOX_W / 2f > x - largura / 2f
                        && gatoPos.x - Gato.HITBOX_W / 2f < x + largura / 2f
                        && gatoPos.y + Gato.HITBOX_H > cy - ALTURA / 2f
                        && gatoPos.y < cy + ALTURA / 2f;
        if (encostou) pego = true;
        return encostou;
    }

    public void desenhar(SpriteBatch batch, float alturaMapa) {
        if (pego) return;
        float balanco = MathUtils.sin(tempo * 3f) * 4f; // flutua devagarinho
        batch.draw(textura,
                Math.round(x - largura / 2f),
                Math.round(alturaMapa - y - ALTURA / 2f + balanco),
                largura, ALTURA);
    }

    public void dispose() { textura.dispose(); }
}