package com.ed1.jogo.fases;

import com.ed1.jogo.itens.Coletavel;

public final class Fases {
    public static final int TOTAL = 2;

    private Fases() {}

    public static Fase criar(int numero) {
        switch (numero) {
            case 2:  return fase2();
            default: return fase1();
        }
    }

    // Plataformas: {x da esquerda, x da direita, y do TOPO}, y contado DE CIMA da imagem.
    // Com 4 números, o último é a BASE: vira um pilar sólido.
    private static Fase fase1() {
        float[][] t = {
                {   0,  322, 452}, // A: onde o gato começa
                { 338,  494, 572}, // B
                { 210,  337, 746}, // C
                { 578,  716, 481}, // D
                { 735,  893, 330}, // E
                { 872, 1025, 519}, // F
                { 656,  828, 664}, // G
                {1065, 1172, 416}, // H
                {1036, 1206, 666}, // I
                {1258, 1423, 432}, // J
                {1332, 1466, 752}, // K
                {1425, 1506, 328}, // L1
                {1506, 1675, 338}, // L2: chegada
        };
        Fase f = new Fase(1, "mapas/mapa1.png", 1675, 939, t, 110, 452, 1560);
        f.adicionarFalaIntro("~Uhhh~ Faz um favor pra mim? Pega os coletáveis da fase e traz pra mim? ~Uhhh~");
        f.adicionarFalaIntro("Eu te espero do outro lado. Boa sorte!");
        f.adicionarFalaFim("~Uhhh~ Muito obrigado! Você é muito gentil! ~Uhhh~");
        f.adicionarFalaFim("Para onde está indo?");
        f.adicionarFalaFim("Vou te ajudar no caminho, vamos!");

        // em cima do pilar alto do meio (plataforma E)
        f.adicionarColetavel(new Coletavel("assets/itens/cristal_estrela.png", 814, 285, 2000));
        return f;
    }

    private static Fase fase2() {
        final float H = 938; // altura do mapa: pilar com base = H vai até o chão
        float[][] t = {
                {  40,  298, 392},      // A: onde o gato começa
                { 320,  373, 452, H},   // pilarzinho 1 (sólido)
                { 400,  462, 500, H},   // pilarzinho 2 (sólido)
                { 507,  565, 548, H},   // pilarzinho 3 (sólido)
                { 583,  668, 390},      // plataforma pendurada (baixa)
                { 475,  555, 338},      // plataforma pendurada (média)
                { 545,  592, 287},      // plataforma pendurada (pequena)
                { 645,  757, 592},      // degrau na base do pilar grande
                { 755,  885, 255, H},   // pilar grande 1 (sólido)
                { 885,  943, 380},      // degrau entre os pilares (alto)
                { 885,  943, 490},      // degrau entre os pilares (meio)
                { 965, 1035, 305},      // degrau à esquerda do pilar 2 (alto)
                { 965, 1035, 590},      // degrau à esquerda do pilar 2 (baixo)
                { 880, 1040, 735},      // chão entre os pilares
                {1035, 1137, 268, H},   // pilar grande 2 (sólido)
                {1168, 1230, 252},      // plataforma flutuante 1
                {1280, 1340, 225},      // plataforma flutuante 2
                {1380, 1640, 205},      // porta da caveira: chegada
        };
        Fase f = new Fase(2, "mapas/mapa2.png", 1676, 938, t, 130, 392, 1500);
        f.adicionarFalaIntro("~Uhhh~ Adeus Kaz! Boa sorte!");
        return f;
    }
}