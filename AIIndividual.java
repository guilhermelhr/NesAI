package glhr.nesai;

import java.util.ArrayList;

public class AIIndividual {
	public final static int BIT0 = 1, BIT1 = 2, BIT2 = 4, BIT3 = 8, BIT4 = 16,
            BIT5 = 32, BIT6 = 64, BIT7 = 128, BIT8 = 256, BIT9 = 512,
            BIT10 = 1024, BIT11 = 2048, BIT12 = 4096, BIT13 = 8192,
            BIT14 = 16384, BIT15 = 32768;
	public static final int RELEASE = 0, UP = BIT4, DOWN = BIT5, LEFT = BIT6, RIGHT = BIT7, A = BIT0, B = BIT1, SELECT = BIT2, START = BIT3;
    public int id;
    public byte[] chromosome;
    public float fitness = -1;
	public ArrayList<Integer> distances;

    private static final byte[] GENE_OPTIONS = {
            //RLDUBA
            //0b000000,
            0b000001,
            0b000010,
            //0b100000,
            //0b010000,
            //0b001000,
            //0b000100,
            0b100001,
            0b010001,
            0b001001,
            0b000101,
            0b100010,
            0b010010,
            0b001010,
            0b000110,
            //0b011001,
            //0b011010,
            //0b010101,
            //0b010110,
            //0b011000,
            //0b010100,
    };

    public AIIndividual(int id, int nGenes){
        this.id = id;
        chromosome = new byte[nGenes];
    }

    public void initialize(){
        for(int i = 0; i < chromosome.length; i++){
    		chromosome[i] = GetRandomChromosome();
        }
    }

    public static int[] getInput(byte[] chromosome) {
        int[] input = new int[chromosome.length * 2];
        int framesPerInput = (int) ((1f / (chromosome.length / AICore.MAX_RUNTIME)) * 60f);
        int frame = AIGameInterface.START_AI_FRAME;
        for(int i = 0, j = 0; i < chromosome.length; i++, j += 2){
            byte c = chromosome[i];
            input[j] = 0;
            if((c & 1) > 0){
                input[j] |= A;
            }
            if((c & 2) > 0){
                input[j] |= B;
            }
            if((c & 4) > 0){
                input[j] |= UP;
            }
            if((c & 8) > 0){
                input[j] |= DOWN;
            }
            if((c & 16) > 0){
                input[j] |= LEFT;
            }
            if((c & 32) > 0){
                input[j] |= RIGHT;
            }
            input[j + 1] = frame;
            frame += framesPerInput;
        }

        return input;
    }

    public static byte GetRandomChromosome() {
        return GENE_OPTIONS[(int) Math.floor(Math.random() * GENE_OPTIONS.length)];
    }
}
