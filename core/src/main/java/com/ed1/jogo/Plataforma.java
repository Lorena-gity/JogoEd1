package com.ed1.jogo;

public class Plataforma {
    public final float x0, x1;
    public final float y;        // y do TOPO (contado de cima da imagem)
    public final float yBase;    // y da base (só importa se for sólida)
    public final boolean solida; // true = pilar: bloqueia dos lados e por baixo

    public Plataforma(float x0, float x1, float y) {
        this(x0, x1, y, -1);
    }

    public Plataforma(float x0, float x1, float y, float yBase) {
        this.x0 = x0;
        this.x1 = x1;
        this.y = y;
        this.solida = yBase > y;
        this.yBase = solida ? yBase : y;
    }
}