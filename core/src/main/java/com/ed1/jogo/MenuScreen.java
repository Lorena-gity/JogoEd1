package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen extends ScreenAdapter {
    private static final float LARGURA = 1376;
    private static final float ALTURA = 768;

    private static final float BTN_W = 240;
    private static final float BTN_H = 78;
    private static final float ESPACO = 14;
    private static final float PRIMEIRO_Y = 360; // y do botão de cima (conta de baixo pra cima)

    private static final String[] NOMES = {"jogar", "continuar", "ranking", "sair"};

    private final Main game;
    private final SpriteBatch batch;
    private final FitViewport viewport;
    private final Texture fundo;
    private final Texture titulo;
    private final Texture[] normal = new Texture[NOMES.length];
    private final Texture[] hover = new Texture[NOMES.length];
    private final Rectangle[] areas = new Rectangle[NOMES.length];
    private final Vector2 mouse = new Vector2();

    // animação
    private final float[] offsetY = new float[NOMES.length];
    private final float[] escala = new float[NOMES.length];
    private int pressionado = -1;

    public MenuScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        viewport = new FitViewport(LARGURA, ALTURA);
        fundo = new Texture("Fundo_menu.jpeg");
        titulo = new Texture("Lavlor.png");

        float x = (LARGURA - BTN_W) / 2f;
        for (int i = 0; i < NOMES.length; i++) {
            normal[i] = new Texture("botoes/" + NOMES[i] + ".png");
            hover[i] = new Texture("botoes/" + NOMES[i] + "-hover.png");
            float y = PRIMEIRO_Y - i * (BTN_H + ESPACO);
            areas[i] = new Rectangle(x, y, BTN_W, BTN_H);
            escala[i] = 1f;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();

        // posição do mouse convertida pras coordenadas do jogo (1376x768)
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(fundo, 0, 0, LARGURA, ALTURA);

        float larguraTitulo = 500;
        float alturaTitulo = larguraTitulo * titulo.getHeight() / titulo.getWidth();
        batch.draw(titulo, (LARGURA - larguraTitulo) / 2f, ALTURA - alturaTitulo - 40,
            larguraTitulo, alturaTitulo);

        for (int i = 0; i < NOMES.length; i++) {
            boolean sobre = areas[i].contains(mouse);

            if (sobre && Gdx.input.justTouched()) pressionado = i;
            boolean apertado = pressionado == i && sobre && Gdx.input.isTouched();

            // o clique só "vale" quando o botão do mouse é solto em cima dele
            if (pressionado == i && !Gdx.input.isTouched()) {
                if (sobre) clicou(i);
                pressionado = -1;
            }

            float alvoY = apertado ? -3 : (sobre ? 8 : 0);
            float alvoEscala = apertado ? 0.95f : 1f;

            float t = Math.min(1f, 18f * delta);
            offsetY[i] += (alvoY - offsetY[i]) * t;
            escala[i] += (alvoEscala - escala[i]) * t;

            float w = BTN_W * escala[i];
            float h = BTN_H * escala[i];
            float x = areas[i].x + (BTN_W - w) / 2f;
            float y = areas[i].y + offsetY[i] + (BTN_H - h) / 2f;

            batch.draw(sobre ? hover[i] : normal[i], x, y, w, h);
        }
        batch.end();
    }

    private void clicou(int i) {
        switch (NOMES[i]) {
            case "sair":
                Gdx.app.exit();
                break;
            default:
                System.out.println("Clicou em " + NOMES[i]); // ainda não existe essa tela
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        fundo.dispose();
        titulo.dispose();
        for (int i = 0; i < NOMES.length; i++) {
            normal[i].dispose();
            hover[i].dispose();
        }
    }
}
