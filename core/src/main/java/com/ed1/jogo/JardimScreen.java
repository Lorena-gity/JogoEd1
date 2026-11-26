package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class JardimScreen extends ScreenAdapter {
    private static final float LARGURA = 1376;
    private static final float ALTURA = 768;

    private static final String ARQUIVO_FUNDO = "cutscene/jardim.jpeg";
    private static final String ARQUIVO_GATO = "gato_andando.png"; // gato_andando.png = 6 quadros lado a lado, cada um com 41x30 pixels

    private static final int CELULA_W = 41;
    private static final int CELULA_H = 30;
    private static final int ESCALA = 3; // tamanho do gato na tela (ex.: 3 = cada pixel vira 3x3)

    // ordem dos quadros. Vai e volta (0..5..1) pra não "pular" quando o ciclo recomeça.
    // Se preferir em loop simples, troque por {0, 1, 2, 3, 4, 5}.
    private static final int[] ORDEM = {0, 1, 2, 3, 4, 5, 4, 3, 2, 1};
    private static final float TEMPO_QUADRO = 0.07f; // segundos que cada quadro fica na tela
    private static final float VELOCIDADE = 180; // pixels por segundo
    // Se os pés parecerem "escorregar" no chão: diminua VELOCIDADE ou aumente TEMPO_QUADRO.

    // Caminho dos PÉS do gato, em coordenadas como na imagem (x, y contado DE CIMA).
    // Ele começa fora da tela (x negativo), anda reto pelo chão de pedra, sobe a rampa e entra no arco.
    private static final float[][] CAMINHO = {
            {-70, 595},
            {940, 595},
            {1255, 492}
    };

    private static final float DURACAO_FADE = 0.8f;   // aparecer / escurecer
    private static final float PAUSA_NA_PORTA = 0.5f; // espera depois de chegar no arco

    private final Main game;
    private final SpriteBatch batch;
    private final FitViewport viewport;
    private final Texture fundo;
    private final Texture gato;
    private final TextureRegion[] quadros;

    private final Vector2 pos = new Vector2(CAMINHO[0][0], CAMINHO[0][1]);
    private int alvo = 1; // próximo ponto do CAMINHO
    private boolean chegou = false;

    private float tempo = 0; // tempo total nesta tela (pro fade de entrada)
    private float tempoAnim = 0; // só conta enquanto o gato anda
    private float tempoDepois = 0; // tempo desde que o gato chegou no arco

    public JardimScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        viewport = new FitViewport(LARGURA, ALTURA);

        fundo = new Texture(ARQUIVO_FUNDO);
        fundo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        gato = new Texture(ARQUIVO_GATO);
        gato.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        quadros = TextureRegion.split(gato, CELULA_W, CELULA_H)[0]; // a primeira (e única) linha
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            irParaJogo();
            return;
        }

        tempo += delta;
        if (!chegou) {
            andar(delta);
            tempoAnim += delta;
        } else {
            tempoDepois += delta;
            if (tempoDepois >= PAUSA_NA_PORTA + DURACAO_FADE) {
                irParaJogo();
                return;
            }
        }

        // brilho: sobe de 0 a 1 no começo e desce de 1 a 0 no final (escurece a tela)
        float brilho = Math.min(1f, tempo / DURACAO_FADE);
        if (chegou) {
            brilho *= 1f - MathUtils.clamp((tempoDepois - PAUSA_NA_PORTA) / DURACAO_FADE, 0f, 1f);
        }

        // quadro da animação: parado (quadro 0) quando já chegou
        int quadro = chegou ? 0 : ORDEM[(int) (tempoAnim / TEMPO_QUADRO) % ORDEM.length];

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(brilho, brilho, brilho, 1f);

        // fundo ocupando a tela toda
        batch.draw(fundo, 0, 0, LARGURA, ALTURA);

        // gato: os pés ficam em pos. O Y do LibGDX conta de baixo pra cima, por isso ALTURA - pos.y.
        // Arredondar deixa os pixels do gato sempre alinhados (sem tremer).
        float w = CELULA_W * ESCALA;
        float h = CELULA_H * ESCALA;
        batch.draw(quadros[quadro], Math.round(pos.x - w / 2f), Math.round(ALTURA - pos.y), w, h);

        batch.end();
    }

    /** Anda pelo CAMINHO na velocidade fixa, passando de ponto em ponto */
    private void andar(float delta) {
        float resto = VELOCIDADE * delta; // quanto ainda pode andar neste frame
        while (resto > 0 && alvo < CAMINHO.length) {
            float dx = CAMINHO[alvo][0] - pos.x;
            float dy = CAMINHO[alvo][1] - pos.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist <= resto) { // chega no ponto e continua pro próximo
                pos.set(CAMINHO[alvo][0], CAMINHO[alvo][1]);
                resto -= dist;
                alvo++;
            } else { // anda só um pedaço na direção do ponto
                pos.add(dx / dist * resto, dy / dist * resto);
                resto = 0;
            }
        }
        if (alvo >= CAMINHO.length) {
            chegou = true;
        }
    }

    private void irParaJogo() {
        game.setScreen(new GameScreen(game));
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        fundo.dispose();
        gato.dispose();
    }
}