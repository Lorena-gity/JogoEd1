package com.ed1.jogo.entidades;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.ed1.jogo.fases.Fase;
import com.ed1.jogo.fases.Plataforma;

public class Gato {
    // física
    private static final float GRAVIDADE = 1800;
    private static final float FORCA_PULO = 850;
    private static final float VELOCIDADE = 220;
    private static final float SOLTOU_PULO = 2.5f;
    private static final float VEL_QUEDA_MAX = 1300;
    private static final float TEMPO_COYOTE = 0.10f;
    private static final float TEMPO_BUFFER = 0.10f;
    public static final float HITBOX_W = 30;
    public static final float HITBOX_H = 56; // altura usada nas colisões com pilares

    // sprite: 11 quadros de 45x40
    // 0-5 caminhada | 6 agachado | 7 subindo | 8 topo do pulo | 9 caindo | 10 parado
    private static final int CELULA_W = 45;
    private static final int CELULA_H = 40;
    private static final int CENTRO_X = 26;
    private static final int ESCALA = 2;
    private static final int Q_AGACHADO = 6, Q_SUBINDO = 7, Q_TOPO = 8, Q_CAINDO = 9;
    private static final int[] ORDEM_ANDAR = {0, 1, 2, 3, 4, 5, 4, 3, 2, 1};
    private static final float TEMPO_QUADRO = 0.04f;

    public final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();
    private final Texture textura;
    private final TextureRegion[] quadros;

    private boolean noChao, olhandoDireita = true, andando, chegou;
    private float tempoCoyote, bufferPulo, tempoPouso, tempoAnim;

    public Gato() {
        textura = new Texture("gato_parkour.png");
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        quadros = TextureRegion.split(textura, CELULA_W, CELULA_H)[0];
    }

    public void reiniciar(Fase fase) {
        pos.set(fase.inicioX, fase.altura - fase.inicioY);
        vel.set(0, 0);
        noChao = false;
        olhandoDireita = true;
        tempoCoyote = bufferPulo = tempoPouso = 0;
    }

    /** Usado na intro: gato parado no chão (e não "caindo"). */
    public void ficarParado() {
        noChao = true;
        andando = false;
        vel.set(0, 0);
    }

    public boolean chegou() { return chegou; }

    // ======================= LÓGICA =======================

    public void atualizar(float dt, Fase fase) {
        // ---------- entrada ----------
        float dir = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dir -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dir += 1;

        boolean pulaApertou = Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean pulaSegurando = Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.UP)
                || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        andando = dir != 0;
        if (dir != 0) olhandoDireita = dir > 0;
        vel.x = dir * VELOCIDADE;

        // ---------- pulo (coyote time + buffer) ----------
        tempoCoyote = noChao ? TEMPO_COYOTE : tempoCoyote - dt;
        bufferPulo = pulaApertou ? TEMPO_BUFFER : bufferPulo - dt;
        if (bufferPulo > 0 && tempoCoyote > 0) {
            vel.y = FORCA_PULO;
            noChao = false;
            tempoCoyote = 0;
            bufferPulo = 0;
        }

        // ---------- gravidade ----------
        float g = (vel.y > 0 && !pulaSegurando) ? GRAVIDADE * SOLTOU_PULO : GRAVIDADE;
        vel.y = Math.max(vel.y - g * dt, -VEL_QUEDA_MAX);

        // ---------- mover X e resolver paredes ----------
        float xAntes = pos.x;
        pos.x = MathUtils.clamp(pos.x + vel.x * dt, 20, fase.largura - 20);
        colidirComPilaresLateral(fase, xAntes);

        // ---------- mover Y, bater a cabeça e pousar ----------
        float yAntes = pos.y;
        pos.y += vel.y * dt;
        colidirComPilaresPorBaixo(fase, yAntes);
        pousar(fase, yAntes);

        // ---------- caiu no buraco ----------
        if (pos.y < -100) reiniciar(fase);

        // ---------- chegada ----------
        if (noChao && pos.x >= fase.chegadaX) {
            chegou = true;
            vel.set(0, 0);
            andando = false;
        }

        // ---------- animação ----------
        tempoPouso = Math.max(0, tempoPouso - dt);
        tempoAnim = (andando && noChao) ? tempoAnim + dt : 0;
    }

    private boolean sobrepoeX(Plataforma p) {
        return pos.x + HITBOX_W / 2f > p.x0 && pos.x - HITBOX_W / 2f < p.x1;
    }

    /** Pilares bloqueiam o gato pelos lados. */
    private void colidirComPilaresLateral(Fase fase, float xAntes) {
        for (Plataforma p : fase.plataformas) {
            if (!p.solida) continue;
            float topo = fase.altura - p.y;
            float base = fase.altura - p.yBase;
            // "topo - 2": quem está EM CIMA do pilar não conta como batendo na parede
            boolean sobrepoeY = pos.y < topo - 2 && pos.y + HITBOX_H > base;
            if (sobrepoeY && sobrepoeX(p)) {
                if (pos.x > xAntes) pos.x = p.x0 - HITBOX_W / 2f;
                else if (pos.x < xAntes) pos.x = p.x1 + HITBOX_W / 2f;
            }
        }
    }

    /** Pilares também bloqueiam a cabeça do gato quando ele pula por baixo. */
    private void colidirComPilaresPorBaixo(Fase fase, float yAntes) {
        if (vel.y <= 0) return;
        for (Plataforma p : fase.plataformas) {
            if (!p.solida || !sobrepoeX(p)) continue;
            float base = fase.altura - p.yBase;
            if (yAntes + HITBOX_H <= base + 0.5f && pos.y + HITBOX_H > base) {
                pos.y = base - HITBOX_H;
                vel.y = 0;
            }
        }
    }

    private void pousar(Fase fase, float yAntes) {
        boolean estavaNoChao = noChao;
        float velQueda = vel.y;
        noChao = false;
        if (vel.y > 0) return;

        float melhorTopo = -1;
        for (Plataforma p : fase.plataformas) {
            float topo = fase.altura - p.y;
            boolean atravessou = yAntes >= topo - 0.5f && pos.y <= topo;
            if (sobrepoeX(p) && atravessou && topo > melhorTopo) melhorTopo = topo;
        }
        if (melhorTopo >= 0) {
            pos.y = melhorTopo;
            vel.y = 0;
            noChao = true;
            if (!estavaNoChao && velQueda < -350) tempoPouso = 0.07f;
        }
    }

    // ======================= DESENHO =======================

    private int escolherQuadro() {
        if (!noChao) {
            if (vel.y > 150) return Q_SUBINDO;
            if (vel.y < -150) return Q_CAINDO;
            return Q_TOPO;
        }
        if (tempoPouso > 0) return Q_AGACHADO;
        if (andando) return ORDEM_ANDAR[(int) (tempoAnim / TEMPO_QUADRO) % ORDEM_ANDAR.length];
        return 0;
    }

    public void desenhar(SpriteBatch batch, float alfa) {
        TextureRegion q = quadros[escolherQuadro()];
        float w = CELULA_W * ESCALA;
        float h = CELULA_H * ESCALA;
        float c = CENTRO_X * ESCALA;
        float y = Math.round(pos.y);

        batch.setColor(1, 1, 1, alfa);
        if (olhandoDireita) batch.draw(q, Math.round(pos.x - c), y, w, h);
        else                batch.draw(q, Math.round(pos.x + c), y, -w, h);
        batch.setColor(Color.WHITE);
    }

    public void dispose() { textura.dispose(); }
}