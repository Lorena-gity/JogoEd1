package com.ed1.jogo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ed1.jogo.dialogo.CaixaDialogo;
import com.ed1.jogo.entidades.Fantasma;
import com.ed1.jogo.entidades.Gato;
import com.ed1.jogo.fases.Fase;
import com.ed1.jogo.fases.Fases;
import com.ed1.jogo.fases.Plataforma;
import com.ed1.jogo.itens.Coletavel;

public class GameScreen extends ScreenAdapter {
    private static final float ZOOM = 1.5f;
    private static final float HUD_W = 1376, HUD_H = 768;
    private static final float VIEW_W = HUD_W / ZOOM;
    private static final float VIEW_H = HUD_H / ZOOM;
    private static final float TEMPO_APARECER = 3f; // fade-in da intro (segundos)

    private enum Estado { INTRO, JOGANDO, FINAL }

    private final Main game;
    private final Fase fase;

    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(VIEW_W, VIEW_H, camera);
    private final OrthographicCamera hud = new OrthographicCamera();
    private final Texture mapa;
    private final Texture pixel;
    private final BitmapFont fonte = new BitmapFont();

    private final Gato gato = new Gato();
    private final Fantasma fantasma = new Fantasma();
    private CaixaDialogo dialogo;

    private Estado estado;
    private float tempoIntro = 0;
    private float tempoTotal = 0;
    private float alfaIntro = 1;
    private boolean debug = false;       // F1 mostra as caixas de colisão
    private Screen proximaTela = null;   // se != null, troca de tela no fim do frame

    private int pontos;
    private int ultimoGanho = 0;
    private float tempoGanho = 0;   // quanto tempo o "+2000" fica na tela

    public GameScreen(Main game) {
        this(game, 1, 0);
    }

    public GameScreen(Main game, int numeroFase, int pontos) {
        this.game = game;
        this.pontos = pontos;
        this.fase = Fases.criar(numeroFase);

        hud.setToOrtho(false, HUD_W, HUD_H);

        mapa = new Texture(fase.mapa);
        mapa.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        fonte.getData().setScale(2.5f);
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        gato.reiniciar(fase);

        if (fase.temIntro()) {
            estado = Estado.INTRO;
            alfaIntro = 0;
            gato.ficarParado();
            fantasma.aparecerParado(fase.inicioX + 60, (fase.altura - fase.inicioY) + 55);
            dialogo = new CaixaDialogo(fase.falasIntro);
        } else {
            estado = Estado.JOGANDO;
        }
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        if (estado != Estado.INTRO) tempoTotal += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) debug = !debug;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) proximaTela = new MenuScreen(game);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) apertouEnter();

        switch (estado) {
            case INTRO:   atualizarIntro(delta);   break;
            case JOGANDO: atualizarJogo(delta);    break;
            case FINAL:   atualizarFinal(delta);   break;
        }
        fantasma.atualizar(delta, fase.altura);
        for (Coletavel c : fase.coletaveis) c.atualizar(delta);
        tempoGanho = Math.max(0, tempoGanho - delta);

        if (proximaTela != null) {
            game.setScreen(proximaTela);
            dispose();
            return;
        }

        desenhar();
    }

    // ======================= ESTADOS =======================

    private void apertouEnter() {
        if (estado == Estado.INTRO) {
            dialogo.avancar();
            if (dialogo.terminou()) {       // acabou a última fala: ele vai embora
                estado = Estado.JOGANDO;
                fantasma.sair();
            }
        } else if (estado == Estado.FINAL) {
            boolean falando = dialogo != null && !dialogo.terminou();
            if (falando) {
                dialogo.avancar();
                if (dialogo.terminou() && !fase.ultima()) irParaProximaFase();
            } else if (fase.ultima()) {
                proximaTela = new MenuScreen(game);
            }
        }
    }

    private void atualizarIntro(float dt) {
        tempoIntro += dt;
        alfaIntro = MathUtils.clamp(tempoIntro / TEMPO_APARECER, 0, 1);
        fantasma.setAlfa(alfaIntro);

        if (alfaIntro >= 1 && !dialogo.iniciada()) dialogo.comecar(); // fade-in acabou: ele fala
        dialogo.atualizar(dt);
    }

    private void atualizarJogo(float dt) {
        gato.atualizar(dt, fase);
        for (Coletavel c : fase.coletaveis) {
            if (c.tentarPegar(gato.pos, fase.altura)) {
                pontos += c.pontos;
                ultimoGanho = c.pontos;
                tempoGanho = 1.5f;
            }
        }
        if (gato.chegou()) terminarFase();
    }

    private void terminarFase() {
        estado = Estado.FINAL;
        if (fase.falasFim.tamanho() > 0) {
            // o fantasminha nasce acima da tela e desce até ficar ao lado do gato
            fantasma.entrarDescendo(Math.max(0, gato.pos.x - 140),
                    camera.position.y + VIEW_H / 2f, gato.pos.y + 55);
            dialogo = new CaixaDialogo(fase.falasFim);
        } else if (!fase.ultima()) {
            irParaProximaFase();
        }
    }

    private void atualizarFinal(float dt) {
        if (dialogo == null) return;
        if (fantasma.parado() && !dialogo.iniciada()) dialogo.comecar();
        dialogo.atualizar(dt);
    }

    private void irParaProximaFase() {
        proximaTela = new GameScreen(game, fase.numero + 1, pontos);
    }

    // ======================= DESENHO =======================

    private void desenhar() {
        // câmera: segue o gato, mas nunca sai do mapa
        float camX = MathUtils.clamp(gato.pos.x, VIEW_W / 2f, fase.largura - VIEW_W / 2f);
        float camY = MathUtils.clamp(gato.pos.y + 30, VIEW_H / 2f, fase.altura - VIEW_H / 2f);
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(mapa, 0, 0, fase.largura, fase.altura);
        for (Coletavel c : fase.coletaveis) c.desenhar(batch, fase.altura);
        gato.desenhar(batch, alfaIntro);
        if (fantasma.visivel()) fantasma.desenhar(batch);
        if (debug) desenharDebug();
        batch.end();

        desenharTextos();
    }

    /** F1: vermelho = pilar sólido, verde = topo onde o gato pousa. */
    private void desenharDebug() {
        for (Plataforma p : fase.plataformas) {
            float w = p.x1 - p.x0;
            if (p.solida) {
                batch.setColor(1, 0, 0, 0.35f);
                batch.draw(pixel, p.x0, fase.altura - p.yBase, w, p.yBase - p.y);
            }
            batch.setColor(0, 1, 0, 0.9f);
            batch.draw(pixel, p.x0, fase.altura - p.y - 1, w, 2);
        }
        batch.setColor(Color.WHITE);
    }

    private void desenharTextos() {
        batch.setProjectionMatrix(hud.combined);
        batch.begin();
        fonte.getData().setScale(1.6f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Pontos: " + pontos, 30, HUD_H - 25);
        if (tempoGanho > 0) {
            fonte.setColor(1f, 0.85f, 0.2f, MathUtils.clamp(tempoGanho, 0, 1));
            fonte.draw(batch, "+" + ultimoGanho, 30, HUD_H - 70);
        }
        fonte.setColor(Color.WHITE);

        if (estado == Estado.JOGANDO && tempoTotal < 7) {
            float alfa = MathUtils.clamp(7 - tempoTotal, 0, 1);
            escreverComSombra("A / D: andar     W ou ESPACO: pular     ESC: menu", 60, 1f, alfa);
        }

        if (dialogo != null && dialogo.ativa()) {
            dialogo.desenhar(batch, fonte, pixel);
        }

        // só a ÚLTIMA fase mostra tela de fim; as outras vão direto pra próxima
        boolean falando = dialogo != null && !dialogo.terminou();
        if (estado == Estado.FINAL && fase.ultima() && !falando) {
            escreverComSombra("Fim da fase " + fase.numero + "!", 430, 1.4f, 1f);
            escreverComSombra("Aperte ENTER pra voltar ao menu", 360, 1f, 1f);
        }

        batch.end();
    }

    private void escreverComSombra(String texto, float y, float tamanho, float alfa) {
        fonte.getData().setScale(2.5f * tamanho);
        fonte.setColor(0, 0, 0, alfa);
        fonte.draw(batch, texto, 3, y - 3, HUD_W, Align.center, false);
        fonte.setColor(1, 1, 1, alfa);
        fonte.draw(batch, texto, 0, y, HUD_W, Align.center, false);
        fonte.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapa.dispose();
        pixel.dispose();
        fonte.dispose();
        gato.dispose();
        fantasma.dispose();
        for (Coletavel c : fase.coletaveis) c.dispose();
    }
}