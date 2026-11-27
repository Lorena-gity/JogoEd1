package com.ed1.jogo.entidades;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Fantasma {
    private static final float LARGURA = 112;
    private static final float ALTURA = LARGURA * 505f / 758f;
    private static final float VEL_DESCIDA = 180;

    private enum Estado { ESCONDIDO, DESCENDO, PARADO, SAINDO }

    private final Texture textura;
    private Estado estado = Estado.ESCONDIDO;
    private float x, y, alvoY, alfa = 1, tempo;

    public Fantasma() {
        textura = new Texture("fantasminha.png");
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    /** Aparece parado numa posição (começa transparente, use setAlfa pro fade-in). */
    public void aparecerParado(float x, float y) {
        this.x = x;
        this.y = y;
        alfa = 0;
        estado = Estado.PARADO;
    }

    /** Nasce em yInicio (acima da tela) e desce até alvoY. */
    public void entrarDescendo(float x, float yInicio, float alvoY) {
        this.x = x;
        this.y = yInicio;
        this.alvoY = alvoY;
        alfa = 1;
        estado = Estado.DESCENDO;
    }

    public void sair() { if (estado != Estado.ESCONDIDO) estado = Estado.SAINDO; }

    public void setAlfa(float alfa) { this.alfa = alfa; }
    public boolean visivel() { return estado != Estado.ESCONDIDO; }
    public boolean parado() { return estado == Estado.PARADO; }

    public void atualizar(float dt, float alturaMapa) {
        tempo += dt;
        if (estado == Estado.DESCENDO) {
            y -= VEL_DESCIDA * dt;
            if (y <= alvoY) {
                y = alvoY;
                estado = Estado.PARADO;
            }
        } else if (estado == Estado.SAINDO) {
            y += VEL_DESCIDA * 1.2f * dt;
            if (y > alturaMapa + 100) estado = Estado.ESCONDIDO;
        }
    }

    public void desenhar(SpriteBatch batch) {
        float balanco = (estado == Estado.PARADO)
                ? MathUtils.sin(tempo * 3f) * 5f
                : MathUtils.sin(tempo * 10f) * 3f;
        batch.setColor(1, 1, 1, alfa);
        batch.draw(textura, Math.round(x), Math.round(y + balanco), LARGURA, ALTURA);
        batch.setColor(Color.WHITE);
    }

    public void dispose() { textura.dispose(); }
}