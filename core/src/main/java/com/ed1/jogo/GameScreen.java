package com.ed1.jogo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {
    private static final int TOTAL_FASES = 2;

    // tipos de coletável pra usar nas tabelas (o valor em pontos está em TipoColetavel)
    private static final int ESTRELA = 0, MUNDO = 1, FLORES = 2;

    // Plataformas: {x da esquerda, x da direita, y do TOPO}, y contado DE CIMA da imagem.
    // Com 4 números, o último é a BASE: vira um pilar sólido (bloqueia dos lados e por baixo).
    // --------------------------- FASE 1 ---------------------------
    private static final String MAPA_1 = "mapas/mapa1.png";
    private static final float MAPA_1_W = 1675;
    private static final float MAPA_1_H = 939;
    private static final float[][] PLATAFORMAS_1 = {
            {   0,  322, 452}, // A: onde o gato começa (perto da porta)
            { 338,  494, 572}, // B
            { 210,  337, 746}, // C (embaixo, à esquerda)
            { 578,  716, 481}, // D
            { 735,  893, 330}, // E (o pilar alto do meio)
            { 872, 1025, 519}, // F
            { 656,  828, 664}, // G (embaixo)
            {1065, 1172, 416}, // H
            {1036, 1206, 666}, // I (embaixo)
            {1258, 1423, 432}, // J
            {1332, 1466, 752}, // K (embaixo, à direita)
            {1425, 1506, 328}, // L1
            {1506, 1675, 338}, // L2: a plataforma da chegada
    };
    private static final float INICIO_1_X = 110;
    private static final float INICIO_1_Y = 452;
    private static final float CHEGADA_1_X = 1560;
    // coletáveis: {tipo, x do CENTRO, y do CHÃO onde ele fica (contado de cima, igual às plataformas)}
    // 4 nas plataformas de baixo e só 1 lá em cima
    private static final float[][] COLETAVEIS_1 = {
            {FLORES, 273, 746},   // C (embaixo, à esquerda)
            {ESTRELA, 742, 664},  // G (embaixo)
            {FLORES, 1121, 666},  // I (embaixo)
            {ESTRELA, 1399, 752}, // K (embaixo, à direita)
            {MUNDO, 814, 330},    // E (o pilar alto do meio): o único lá em cima, vale mais
    };

    // --------------------------- FASE 2 ---------------------------
    private static final String MAPA_2 = "mapas/mapa2.png";
    private static final float MAPA_2_W = 1676;
    private static final float MAPA_2_H = 938;
    private static final float H2 = 938; // base = H2 -> o pilar vai até o chão
    private static final float[][] PLATAFORMAS_2 = {
            {  40,  298, 392},      // A: onde o gato começa (porta da esquerda)
            { 320,  373, 452, H2},  // pilarzinho 1 (sólido)
            { 400,  462, 500, H2},  // pilarzinho 2 (sólido)
            { 507,  565, 548, H2},  // pilarzinho 3 (sólido)
            { 583,  668, 390},      // plataforma pendurada (baixa)
            { 475,  555, 338},      // plataforma pendurada (média)
            { 545,  592, 287},      // plataforma pendurada (pequena)
            { 645,  757, 592},      // degrau na base do pilar grande
            { 755,  885, 255, H2},  // pilar grande 1 (sólido)
            { 885,  943, 380},      // degrau entre os pilares (alto)
            { 885,  943, 490},      // degrau entre os pilares (meio)
            { 965, 1035, 305},      // degrau à esquerda do pilar 2 (alto)
            { 965, 1035, 590},      // degrau à esquerda do pilar 2 (baixo)
            { 880, 1040, 735},      // chão entre os pilares (embaixo)
            {1035, 1137, 268, H2},  // pilar grande 2 (sólido)
            {1168, 1230, 252},      // plataforma flutuante 1
            {1280, 1340, 225},      // plataforma flutuante 2
            {1380, 1640, 205},      // porta da caveira: chegada
    };
    private static final float INICIO_2_X = 130;
    private static final float INICIO_2_Y = 392;
    private static final float CHEGADA_2_X = 1500;
    private static final float[][] COLETAVEIS_2 = {
            {FLORES, 915, 735},   // chão entre os pilares (embaixo)
            {ESTRELA, 985, 735},  // chão entre os pilares (embaixo)
            {MUNDO, 1000, 590},   // degrau baixo à esquerda do pilar 2
    };

    // ---------------- LÓGICA FÍSICA ----------------
    private static final float GRAVIDADE = 1800;
    private static final float FORCA_PULO = 850;
    private static final float VELOCIDADE = 220;
    private static final float SOLTOU_PULO = 2.5f;
    private static final float VEL_QUEDA_MAX = 1300;
    private static final float TEMPO_COYOTE = 0.10f;
    private static final float TEMPO_BUFFER = 0.10f;
    private static final float HITBOX_W = 30;
    private static final float HITBOX_H = 56; // altura do gato nas colisões com pilares

    private static final float ZOOM = 1.5f;
    private static final float VIEW_W = 1376 / ZOOM;
    private static final float VIEW_H = 768 / ZOOM;

    // 11 quadros lado a lado, cada um com 45x40 pixels:
    // 0-5 caminhada | 6 agachado | 7 subindo | 8 topo do pulo | 9 caindo | 10 parado (sobra)
    private static final int CELULA_W = 45;
    private static final int CELULA_H = 40;
    private static final int CENTRO_X = 26;
    private static final int ESCALA = 2;
    private static final int Q_AGACHADO = 6, Q_SUBINDO = 7, Q_TOPO = 8, Q_CAINDO = 9;
    private static final int[] ORDEM_ANDAR = {0, 1, 2, 3, 4, 5, 4, 3, 2, 1};
    private static final float TEMPO_QUADRO = 0.04f;

    // ---------------- FANTASMINHA ----------------
    private static final float FANTASMA_W = 112;
    private static final float FANTASMA_H = FANTASMA_W * 505f / 758f;
    private static final float VEL_DESCIDA = 180;
    private static final float LETRAS_POR_SEGUNDO = 30;
    private static final float VEL_VOO = 350; // velocidade dele indo esperar o gato
    // onde o fantasminha espera na fase 1 (x = canto esquerdo, y = topo da plataforma, contado de cima)
    private static final float ESPERA_1_X = 1575;
    private static final float ESPERA_1_Y = 338;

    // ---------------- COLETÁVEL ----------------
    private static final float FOLGA_COLETAVEL = 8; // quanto ele flutua acima do chão

    /** Um item coletável. Só guarda dados; o GameScreen cuida do resto. */
    private static class Coletavel {
        final TipoColetavel tipo;
        final float x, y; // x = centro, y = do CHÃO onde ele fica (contado DE CIMA da imagem)
        boolean pego = false;

        Coletavel(TipoColetavel tipo, float x, float y) {
            this.tipo = tipo;
            this.x = x;
            this.y = y;
        }
    }

    private final Main game;
    private final int fase;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final OrthographicCamera hud;
    private final Texture mapa;
    private final Texture gato;
    private final TextureRegion[] quadros;
    private final BitmapFont fonte;

    // dados da fase atual
    private final float mapaW, mapaH;
    private final ListaEncadeada<Plataforma> plataformas;
    private final ListaEncadeada<Coletavel> coletaveis;
    private final float inicioX, inicioY, chegadaX;

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();
    private boolean noChao = false;
    private boolean olhandoDireita = true;
    private boolean andando = false;
    private float tempoCoyote = 0;
    private float bufferPulo = 0;
    private float tempoPouso = 0;
    private float tempoAnim = 0;
    private float tempoTotal = 0;
    private boolean venceu = false;
    private boolean debug = false; // F1 mostra as caixas de colisão

    // pontos e coletável
    private int pontos;
    private int ultimoGanho = 0;
    private float tempoGanho = 0;
    private final Texture[] texColetaveis = new Texture[TipoColetavel.values().length]; // uma por tipo
    private float tempoColetavel = 0;

    // fantasminha + diálogo
    private final Texture fantasma;
    private final Texture pixel;
    private final Fila<String> falasIntro = new Fila<>(); // antes da fase
    private final Fila<String> falasFim = new Fila<>();   // quando o gato chega no fim
    private final boolean temFim;
    private Fila<String> falas;       // a fila que está sendo falada agora
    private String falaAtual = null;
    private float tempoFala = 0;
    private float fantasmaX, fantasmaY, fantasmaAlvoY;
    private float tempoFlutuar = 0;
    private boolean fantasmaChegou = false;
    private boolean fantasmaSaindo = false;

    // introdução (gato e fantasminha aparecendo devagar)
    private boolean introAtiva = false;
    private float alfaIntro = 0;

    private float tempoEntrada = 0;
    private static final float DURACAO_ENTRADA_GATO = 1.5f;
    private static final float DURACAO_ENTRADA_FANTASMA = 1.5f;
    private static final float ATRASO_FANTASMA = 0.8f;

    private boolean fantasmaIndoEsperar = false;
    private boolean fantasmaEsperando = false;
    private float esperaX, esperaY;

    public GameScreen(Main game) {
        this(game, 1, 0);
    }

    public GameScreen(Main game, int fase, int pontos) {
        this.game = game;
        this.fase = fase;
        this.pontos = pontos;

        if (fase == 2) {
            mapaW = MAPA_2_W;
            mapaH = MAPA_2_H;
            plataformas = montarPlataformas(PLATAFORMAS_2);
            coletaveis = montarColetaveis(COLETAVEIS_2);
            inicioX = INICIO_2_X;
            inicioY = INICIO_2_Y;
            chegadaX = CHEGADA_2_X;
            // início da fase 2: só se despede
            falasIntro.enfileirar("Adeus e boa sorte na sua jornada!");
        } else {
            mapaW = MAPA_1_W;
            mapaH = MAPA_1_H;
            plataformas = montarPlataformas(PLATAFORMAS_1);
            coletaveis = montarColetaveis(COLETAVEIS_1);
            inicioX = INICIO_1_X;
            inicioY = INICIO_1_Y;
            chegadaX = CHEGADA_1_X;
            // início da fase 1: pede o favor
            falasIntro.enfileirar("Faz um favor pra mim? Pega os coletáveis da fase e traz pra mim?");
            falasIntro.enfileirar("Eu te espero do outro lado..");
            esperaX = ESPERA_1_X;
            esperaY = ESPERA_1_Y;
            // fim da fase 1: agradece e decide ajudar
            falasFim.enfileirar("Muito obrigada! Você é muito gentil!");
            falasFim.enfileirar("Para onde está indo?");
            falasFim.enfileirar("Vou te ajudar no caminho, vamos!");
        }
        temFim = !falasFim.estaVazia();

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_W, VIEW_H, camera);
        hud = new OrthographicCamera();
        hud.setToOrtho(false, 1376, 768);

        mapa = new Texture(fase == 2 ? MAPA_2 : MAPA_1);
        mapa.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        gato = new Texture("gato_parkour.png");
        gato.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        quadros = TextureRegion.split(gato, CELULA_W, CELULA_H)[0];

        for (TipoColetavel t : TipoColetavel.values()) {
            texColetaveis[t.ordinal()] = new Texture(t.arquivo);
            texColetaveis[t.ordinal()].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        fonte = new BitmapFont();
        fonte.getData().setScale(2.5f);
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        fantasma = new Texture("fantasminha.png");
        fantasma.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        reiniciar();

        // toda fase começa com a intro: gato parado, fantasminha ao lado
        introAtiva = true;
        noChao = true;
        falas = falasIntro;

        // fase 1 aparece normalmente; fase 2 começa com fade-in
        alfaIntro = (fase == 2) ? 0 : 1;
        if (fase == 1) {
            // fase 1: o fantasminha desce do céu
            float camY = MathUtils.clamp(mapaH - inicioY + 30, VIEW_H / 2f, mapaH - VIEW_H / 2f);
            fantasmaX = inicioX + 60;
            fantasmaY = camY + VIEW_H / 2f;          // começa acima da tela
            fantasmaAlvoY = (mapaH - inicioY) + 55;
        } else {
            // fase 2: o fantasma começa invisível e aparece gradualmente
            fantasmaX = inicioX + 60;
            fantasmaY = (mapaH - inicioY) + 55;
            fantasmaChegou = false;
        }
    }

    private void atualizarEntrada(float dt) {
        tempoEntrada += dt;

        // gato aparece primeiro
        alfaIntro = MathUtils.clamp(
                tempoEntrada / DURACAO_ENTRADA_GATO, 0, 1
        );

        // fantasma começa a aparecer um pouquinho depois
        float tempoFantasma = tempoEntrada - ATRASO_FANTASMA;

        float alfaFantasma = MathUtils.clamp(
                tempoFantasma / DURACAO_ENTRADA_FANTASMA, 0, 1
        );

        if (tempoFantasma >= DURACAO_ENTRADA_FANTASMA && !fantasmaChegou) {
            fantasmaChegou = true;
            falaAtual = falas.desenfileirar();
            tempoFala = 0;
        }
    }

    /** Transforma a tabela de números numa lista encadeada de Plataforma. */
    private static ListaEncadeada<Plataforma> montarPlataformas(float[][] tabela) {
        ListaEncadeada<Plataforma> lista = new ListaEncadeada<>();
        for (float[] p : tabela) {
            lista.adicionar(p.length > 3
                    ? new Plataforma(p[0], p[1], p[2], p[3])
                    : new Plataforma(p[0], p[1], p[2]));
        }
        return lista;
    }

    private static ListaEncadeada<Coletavel> montarColetaveis(float[][] tabela) {
        ListaEncadeada<Coletavel> lista = new ListaEncadeada<>();
        for (float[] c : tabela) {
            lista.adicionar(new Coletavel(TipoColetavel.values()[(int) c[0]], c[1], c[2]));
        }
        return lista;
    }

    /** Volta o gato pro começo (usado no início e quando ele cai no buraco). */
    private void reiniciar() {
        pos.set(inicioX, mapaH - inicioY);
        vel.set(0, 0);
        noChao = false;
        olhandoDireita = true;
        tempoCoyote = 0;
        bufferPulo = 0;
        tempoPouso = 0;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        if (!introAtiva) tempoTotal += delta;
        tempoColetavel += delta;
        tempoGanho = Math.max(0, tempoGanho - delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) debug = !debug;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (venceu) {
                if (temFim) {
                    avancarDialogo();
                    if (dialogoTerminou()) { // última fala acabou: vai direto pra próxima fase
                        game.setScreen(new GameScreen(game, fase + 1, pontos));
                        dispose();
                        return;
                    }
                } else {
                    // fase sem fala final: ENTER vai pra próxima fase (ou pro menu, se for a última)
                    if (fase < TOTAL_FASES) game.setScreen(new GameScreen(game, fase + 1, pontos));
                    else game.setScreen(new MenuScreen(game));
                    dispose();
                    return;
                }
            } else if (introAtiva) {
                avancarDialogo();
                if (dialogoTerminou()) { // acabou a última fala
                    introAtiva = false;
                    if (fase == 1) fantasmaIndoEsperar = true; // voa pro outro lado
                    else fantasmaSaindo = true;                // se despede e sai
                }
            }
        }

        if (introAtiva) {
            if (fase == 2) {
                atualizarEntrada(delta);
                tempoFlutuar += delta;

                if (fantasmaChegou && falaAtual != null) {
                    tempoFala += delta;
                }
            } else {
                atualizarFantasma(delta);
            }
        } else if (!venceu) {
            atualizar(delta);
        } else if (temFim) {
            atualizarFantasma(delta);
        }
        if (fantasmaSaindo) atualizarSaidaFantasma(delta);
        if (fantasmaIndoEsperar) atualizarVooAteEspera(delta);
        if (fantasmaEsperando && !venceu) tempoFlutuar += delta;

        // ---------- câmera: segue o gato, mas nunca sai do mapa ----------
        float camX = MathUtils.clamp(pos.x, VIEW_W / 2f, mapaW - VIEW_W / 2f);
        float camY = MathUtils.clamp(pos.y + 30, VIEW_H / 2f, mapaH - VIEW_H / 2f);
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(mapa, 0, 0, mapaW, mapaH);
        desenharColetaveis();
        desenharGato();
        if (introAtiva || fantasmaSaindo || fantasmaIndoEsperar || fantasmaEsperando) desenharFantasma();
        if (debug) desenharDebug();
        batch.end();

        desenharTextos();
    }

    // ---------------- LÓGICA DO GATO ----------------

    private void atualizar(float dt) {
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

        // ---------- pulo (com "coyote time" e "buffer") ----------
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

        // ---------- mover X e bater nas paredes dos pilares ----------
        float xAntes = pos.x;
        pos.x = MathUtils.clamp(pos.x + vel.x * dt, 20, mapaW - 20);
        for (Plataforma p : plataformas) {
            if (!p.solida) continue;
            float topo = mapaH - p.y;
            float base = mapaH - p.yBase;
            // "topo - 2": quem está EM CIMA do pilar não conta como batendo na parede
            boolean sobrepoeY = pos.y < topo - 2 && pos.y + HITBOX_H > base;
            if (sobrepoeY && sobrepoeX(p)) {
                if (pos.x > xAntes) pos.x = p.x0 - HITBOX_W / 2f;
                else if (pos.x < xAntes) pos.x = p.x1 + HITBOX_W / 2f;
            }
        }

        // ---------- mover Y e bater a cabeça por baixo dos pilares ----------
        float yAntes = pos.y;
        pos.y += vel.y * dt;
        if (vel.y > 0) {
            for (Plataforma p : plataformas) {
                if (!p.solida || !sobrepoeX(p)) continue;
                float base = mapaH - p.yBase;
                if (yAntes + HITBOX_H <= base + 0.5f && pos.y + HITBOX_H > base) {
                    pos.y = base - HITBOX_H;
                    vel.y = 0;
                }
            }
        }

        // ---------- pousar nas plataformas ----------
        boolean estavaNoChao = noChao;
        float velQueda = vel.y;
        noChao = false;
        if (vel.y <= 0) {
            float melhorTopo = -1;
            for (Plataforma p : plataformas) {   // percorre a lista encadeada, nó por nó
                float topo = mapaH - p.y;
                boolean atravessouPraBaixo = yAntes >= topo - 0.5f && pos.y <= topo;
                if (sobrepoeX(p) && atravessouPraBaixo && topo > melhorTopo) {
                    melhorTopo = topo;
                }
            }
            if (melhorTopo >= 0) {
                pos.y = melhorTopo;
                vel.y = 0;
                noChao = true;
                if (!estavaNoChao && velQueda < -350) {
                    tempoPouso = 0.07f;
                }
            }
        }

        // ---------- caiu no buraco ----------
        if (pos.y < -100) {
            reiniciar();
        }

        // ---------- coletáveis ----------
        for (Coletavel c : coletaveis) {         // percorre a lista encadeada
            if (c.pego) continue;
            Texture img = texColetaveis[c.tipo.ordinal()];
            float baixo = mapaH - c.y + FOLGA_COLETAVEL;   // y (de baixo pra cima) do pé do coletável
            boolean encostou =
                    pos.x + HITBOX_W / 2f > c.x - img.getWidth() / 2f
                            && pos.x - HITBOX_W / 2f < c.x + img.getWidth() / 2f
                            && pos.y + HITBOX_H > baixo
                            && pos.y < baixo + img.getHeight();
            if (encostou) {
                c.pego = true;
                pontos += c.tipo.pontos;
                ultimoGanho = c.tipo.pontos;
                tempoGanho = 1.5f;
            }
        }

        // ---------- chegada ----------
        if (noChao && pos.x >= chegadaX) {
            venceu = true;
            vel.set(0, 0);
            andando = false;

            if (temFim) {
                // o fantasminha já está esperando aqui: é só ele começar a falar
                falas = falasFim;
                fantasmaIndoEsperar = false;
                fantasmaEsperando = true;
                fantasmaX = esperaX;
                fantasmaY = (mapaH - esperaY) + 55;
                fantasmaChegou = true;
                falaAtual = falas.desenfileirar();
                tempoFala = 0;
            }
        }

        // ---------- tempos da animação ----------
        tempoPouso = Math.max(0, tempoPouso - dt);
        if (andando && noChao) {
            tempoAnim += dt;
        } else {
            tempoAnim = 0;
        }
    }

    private boolean sobrepoeX(Plataforma p) {
        return pos.x + HITBOX_W / 2f > p.x0 && pos.x - HITBOX_W / 2f < p.x1;
    }

    // ---------------- INTRO ----------------

    private void atualizarVooAteEspera(float dt) {
        tempoFlutuar += dt;
        float alvoX = esperaX;
        float alvoY = (mapaH - esperaY) + 55;
        float dx = alvoX - fantasmaX, dy = alvoY - fantasmaY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float passo = VEL_VOO * dt;
        if (dist <= passo) {
            fantasmaX = alvoX;
            fantasmaY = alvoY;
            fantasmaIndoEsperar = false;
            fantasmaEsperando = true;
        } else {
            fantasmaX += dx / dist * passo;
            fantasmaY += dy / dist * passo;
        }
    }

    private void atualizarSaidaFantasma(float dt) {
        tempoFlutuar += dt;
        fantasmaY += VEL_DESCIDA * 1.2f * dt;   // sai voando pra cima
        if (fantasmaY > mapaH + 100) {
            fantasmaSaindo = false;
        }
    }

    // ---------------- FANTASMINHA + DIÁLOGO ----------------

    private void atualizarFantasma(float dt) {
        tempoFlutuar += dt;
        if (!fantasmaChegou) {
            fantasmaY -= VEL_DESCIDA * dt;
            if (fantasmaY <= fantasmaAlvoY) {
                fantasmaY = fantasmaAlvoY;
                fantasmaChegou = true;
                falaAtual = falas.desenfileirar();
                tempoFala = 0;
            }
        } else {
            tempoFala += dt;
        }
    }

    private int letrasVisiveis() {
        if (falaAtual == null) return 0;
        return Math.min(falaAtual.length(), (int) (tempoFala * LETRAS_POR_SEGUNDO));
    }

    private void avancarDialogo() {
        if (!fantasmaChegou || falaAtual == null) return;
        if (letrasVisiveis() < falaAtual.length()) {
            tempoFala = 999;                    // mostra tudo de uma vez
        } else {
            falaAtual = falas.desenfileirar();  // próxima fala (null se a fila acabou)
            tempoFala = 0;
        }
    }

    private boolean dialogoTerminou() {
        return fantasmaChegou && falaAtual == null;
    }

    // ---------------- DESENHO ----------------

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

    private void desenharGato() {
        TextureRegion q = quadros[escolherQuadro()];
        float w = CELULA_W * ESCALA;
        float h = CELULA_H * ESCALA;
        float c = CENTRO_X * ESCALA;
        float y = Math.round(pos.y);

        batch.setColor(1, 1, 1, alfaIntro);   // fade-in da intro
        if (olhandoDireita) {
            batch.draw(q, Math.round(pos.x - c), y, w, h);
        } else {
            batch.draw(q, Math.round(pos.x + c), y, -w, h);
        }
        batch.setColor(Color.WHITE);
    }

    private void desenharColetaveis() {
        float balanco = MathUtils.sin(tempoColetavel * 3f) * 4f; // flutua devagarinho
        for (Coletavel c : coletaveis) {
            if (c.pego) continue;
            Texture img = texColetaveis[c.tipo.ordinal()];
            batch.draw(img,
                    Math.round(c.x - img.getWidth() / 2f),
                    Math.round(mapaH - c.y + FOLGA_COLETAVEL + balanco));
        }
    }

    private void desenharFantasma() {
        float balanco = (fantasmaChegou && !fantasmaSaindo && !fantasmaIndoEsperar)
                ? MathUtils.sin(tempoFlutuar * 3f) * 5f
                : MathUtils.sin(tempoFlutuar * 10f) * 3f;

        float alfa = alfaIntro;

        // na fase 2, o fantasma tem seu próprio fade-in
        if (fase == 2 && introAtiva) {
            alfa = MathUtils.clamp(
                    (tempoEntrada - ATRASO_FANTASMA) / DURACAO_ENTRADA_FANTASMA,
                    0, 1
            );
        }

        batch.setColor(1, 1, 1, alfa);
        batch.draw(fantasma, Math.round(fantasmaX), Math.round(fantasmaY + balanco),
                FANTASMA_W, FANTASMA_H);

        batch.setColor(Color.WHITE);
    }

    /** F1: vermelho = pilar sólido, verde = topo onde o gato pousa, amarelo = coletável. */
    private void desenharDebug() {
        for (Plataforma p : plataformas) {
            float w = p.x1 - p.x0;
            if (p.solida) {
                batch.setColor(1, 0, 0, 0.35f);
                batch.draw(pixel, p.x0, mapaH - p.yBase, w, p.yBase - p.y);
            }
            batch.setColor(0, 1, 0, 0.9f);
            batch.draw(pixel, p.x0, mapaH - p.y - 1, w, 2);
        }
        // amarelo = área de cada coletável
        batch.setColor(1, 1, 0, 0.35f);
        for (Coletavel c : coletaveis) {
            if (c.pego) continue;
            Texture img = texColetaveis[c.tipo.ordinal()];
            batch.draw(pixel, c.x - img.getWidth() / 2f, mapaH - c.y + FOLGA_COLETAVEL,
                    img.getWidth(), img.getHeight());
        }
        batch.setColor(Color.WHITE);
    }

    private void desenharCaixaDeFala() {
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
        String texto = falaAtual.substring(0, letrasVisiveis());
        fonte.draw(batch, texto, x + 25, y + h - 65, w - 50, Align.left, true);

        if (letrasVisiveis() >= falaAtual.length()) {
            fonte.getData().setScale(1.4f);
            fonte.setColor(0.7f, 0.7f, 0.7f, 1f);
            fonte.draw(batch, "ENTER", x + w - 110, y + 35);
        }
        fonte.setColor(Color.WHITE);
    }

    private void desenharTextos() {
        batch.setProjectionMatrix(hud.combined);
        batch.begin();

        // placar
        fonte.getData().setScale(1.6f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Pontos: " + pontos, 30, 768 - 25);
        if (tempoGanho > 0) {
            fonte.setColor(1f, 0.85f, 0.2f, MathUtils.clamp(tempoGanho, 0, 1));
            fonte.draw(batch, "+" + ultimoGanho, 30, 768 - 70);
        }
        fonte.setColor(Color.WHITE);

        if (tempoTotal < 7 && !venceu && !introAtiva) {
            float alfa = MathUtils.clamp(7 - tempoTotal, 0, 1);
            escreverComSombra("A / D: andar     W ou ESPACO: pular     ESC: menu", 60, 1f, alfa);
        }

        // fala do fantasminha (fim da fase ou introdução)
        if ((venceu || introAtiva) && fantasmaChegou && falaAtual != null) {
            desenharCaixaDeFala();
        }

        // fases com fala final vão direto pra próxima; as outras mostram "Fim da fase"
        if (venceu && !temFim) {
            escreverComSombra("Fim da fase " + fase + "!", 430, 1.4f, 1f);
            escreverComSombra(fase < TOTAL_FASES ? "Aperte ENTER pra ir pra proxima fase"
                    : "Aperte ENTER pra voltar ao menu", 360, 1f, 1f);
        }

        batch.end();
    }

    private void escreverComSombra(String texto, float y, float tamanho, float alfa) {
        fonte.getData().setScale(2.5f * tamanho);
        fonte.setColor(0, 0, 0, alfa);
        fonte.draw(batch, texto, 3, y - 3, 1376, Align.center, false);
        fonte.setColor(1, 1, 1, alfa);
        fonte.draw(batch, texto, 0, y, 1376, Align.center, false);
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
        gato.dispose();
        fonte.dispose();
        fantasma.dispose();
        pixel.dispose();
        for (Texture t : texColetaveis) t.dispose();
    }
}