package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Tela onde o jogador digita o nome antes de começar (Menu -> NomeScreen -> Cutscene).
 * O nome fica em game.nomeJogador, pra ser salvo no ranking no fim do jogo.
 */
public class NomeScreen extends ScreenAdapter {
    private static final float LARGURA = 1376;
    private static final float ALTURA = 768;
    private static final int MAX_LETRAS = 12;

    private final Main game;
    private final SpriteBatch batch;
    private final FitViewport viewport;
    private final Texture fundo;
    private final Texture pixel;
    private final BitmapFont fonte;
    private final GlyphLayout medida = new GlyphLayout();

    private final StringBuilder nome = new StringBuilder();
    private float tempo = 0;
    private float segurandoApagar = 0;   // tempo com o BACKSPACE apertado (pra apagar repetindo)
    private float avisoVazio = 0;        // mostra aviso se tentar continuar sem nome

    public NomeScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        viewport = new FitViewport(LARGURA, ALTURA);
        fundo = new Texture("menu/Fundo_menu.jpeg");

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        fonte = new BitmapFont();
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // recebe as letras digitadas (keyTyped já devolve o caractere certo, com maiúscula etc.)
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char c) {
                // só aceita ASCII visível; ";" fica de fora porque separa nome e pontos no ranking.txt
                if (c >= 32 && c < 127 && c != ';' && nome.length() < MAX_LETRAS) {
                    if (!(c == ' ' && nome.length() == 0)) {   // não começa com espaço
                        nome.append(c);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        tempo += delta;
        avisoVazio = Math.max(0, avisoVazio - delta);

        // apagar: uma vez ao apertar, e repete se segurar
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            apagar();
            segurandoApagar = 0;
        } else if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)) {
            segurandoApagar += delta;
            if (segurandoApagar > 0.4f) {
                apagar();
                segurandoApagar = 0.35f;   // depois de 0.4s apaga a cada ~0.05s
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String limpo = nome.toString().trim();
            if (limpo.isEmpty()) {
                avisoVazio = 1.5f;
            } else {
                game.nomeJogador = limpo;
                game.pontos = 0;
                game.setScreen(new CutsceneScreen(game));
                dispose();
                return;
            }
        }

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        batch.draw(fundo, 0, 0, LARGURA, ALTURA);
        batch.setColor(0, 0, 0, 0.6f);                       // escurece o fundo
        batch.draw(pixel, 0, 0, LARGURA, ALTURA);

        // caixa de texto
        float cx = 360, cy = 340, cw = 656, ch = 100;
        batch.setColor(1, 1, 1, 1);
        batch.draw(pixel, cx - 4, cy - 4, cw + 8, ch + 8);   // borda
        batch.setColor(0.08f, 0.08f, 0.14f, 1);
        batch.draw(pixel, cx, cy, cw, ch);                   // fundo da caixa
        batch.setColor(Color.WHITE);

        fonte.getData().setScale(3f);
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "Digite seu nome", 0, 520, LARGURA, Align.center, false);

        // nome + cursor piscando (o cursor é um "|" que some e aparece)
        boolean cursor = ((int) (tempo * 2)) % 2 == 0;
        String texto = nome + (cursor ? "|" : " ");
        fonte.getData().setScale(3.2f);
        medida.setText(fonte, texto);
        fonte.draw(batch, texto, cx + 30, cy + ch / 2f + medida.height / 2f);

        fonte.getData().setScale(1.8f);
        fonte.setColor(0.7f, 0.7f, 0.7f, 1);
        fonte.draw(batch, "ENTER para continuar", 0, 280, LARGURA, Align.center, false);

        if (avisoVazio > 0) {
            fonte.setColor(1f, 0.45f, 0.45f, 1);
            fonte.draw(batch, "Digite um nome primeiro!", 0, 230, LARGURA, Align.center, false);
        }
        fonte.setColor(Color.WHITE);

        batch.end();
    }

    private void apagar() {
        if (nome.length() > 0) {
            nome.deleteCharAt(nome.length() - 1);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);   // solta o teclado pras próximas telas
        batch.dispose();
        fundo.dispose();
        pixel.dispose();
        fonte.dispose();
    }
}