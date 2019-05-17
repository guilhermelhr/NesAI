package glhr.nesai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static glhr.nesai.AIIndividual.*;

public class AICore {
    public static final int MAX_RUNTIME = 40;
    public static final int POPULATION_SIZE = 50;
    private static AIGenetics genetics;
    private static int evaluated = 0;
    private static int running = 0;
    private File logfile;
    private static File wd = new File("/home/guilherme/eclipse-workspace/laines/");

    public static void main(String[] args) throws InterruptedException {
    	AICore aiCore = new AICore();
    	aiCore.run();
    	
    }
    
    public AICore(){
        genetics = new AIGenetics();
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
        logfile = new File("/home/guilherme/eclipse-workspace/generations-" + timeStamp + ".csv");
        try {
            logfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onEnded(int id, ArrayList<Integer> evaluation) {
        AIIndividual individual = genetics.getIndividual(id);
        individual.fitness = evaluation.get(evaluation.size() - 1);
        individual.distances = evaluation;
        evaluated++;

        /*for(byte b : individual.chromosome) {
            System.out.print(b + "\t");
        }
        System.out.println("DIST: " + individual.fitness);*/
    }

    public void run() {
        genetics.generateInitialPopulation(POPULATION_SIZE);
        Thread[] threads = new Thread[POPULATION_SIZE];
        AIIndividual best = null;
        boolean evaluateBest = true;
        while(genetics.generations.size() < 100) {
            long start = System.currentTimeMillis();
            ArrayList<AIIndividual> individuals = genetics.getPopulation();
            for (int i = 0; i < individuals.size(); i++) {
                while(running >= 10){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                AIIndividual individual = individuals.get(i);
                if(best == null){
                    best = individual;
                }
                boolean headless = individual != best;
                if(!headless && !evaluateBest) {
                	threads[i] = null;
                	evaluated++;
                }else {
	                threads[i] = new Thread(() -> {
	                	running++;
	                	ArrayList<Integer> evaluation = emulate(true, AIIndividual.getInput(individual.chromosome));
	                	running--;
	                	onEnded(individual.id, evaluation);
	                });
	                threads[i].start();
                }
            }

            while (evaluated < POPULATION_SIZE) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            evaluated = 0;
            genetics.normalizeFitness();
            AIIndividual newBest = genetics.getBest();
            evaluateBest = newBest != best;
            best = newBest;
            logGeneration(best);
            System.out.print("Generation " + genetics.generations.size() + " report (" + (System.currentTimeMillis() - start) + "ms" + "): ");
            System.out.println(
            		" Best: " + best.fitness +  
            		" Average: " + genetics.getMeanFitness() +             		 
            		" Worst: " + genetics.getWorst().fitness);
            AIIndividual[] parents = genetics.selectByTourney(30, 10);
            AIIndividual[] children = genetics.reproduce(parents);
            genetics.mutateSeqEach(children, .01f);
            genetics.archive();
            genetics.replaceWorst(genetics.getPopulation(), children);
        }
    }

    private void logGeneration(AIIndividual best) {
        try {
            FileWriter fr = new FileWriter(logfile, true);
            BufferedWriter br = new BufferedWriter(fr);

            br.write(genetics.generations.size() + "," + best.fitness + "," + genetics.getMeanFitness() + "," + genetics.getWorst().fitness + ",");
            for(int c : getInput(best.chromosome)) {
                br.write(c + " ");
            }
            br.write("\n");

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private ArrayList<Integer> emulate(boolean headless, int[] input) {
    	
    	/*String cstr = "33 2 2 18 6 33 1 2 6 9 33 5 5 33 1 10 2 18 17 2 34 34 17 6 10 18 33 9 6 10 18 2 10 5 34 2 5 33 1 18 9 5 34 34 2 10 33 17 34 9 6 5 34 34 2 18 34 1 1 17 5 6 18 17 33 9 6 1 10 10 33 9 17 34 1 33 33 18 10 17 17 10 1 6 5 5 5 5 10 34 9 17 17 9 18 17 34 17 2 33 17 17 18 2 5 9 2 18 33 5 10 1 34 6 18 18 1 10 2 33 33 17 10 33 34 9 33 5 18 6 6 10 2 6 34 2 10 34 17 10 17 33 6 5 2 18 6 18 10 34 5 9 1 34 33 10 6 1 10 34 10 33 17 10 6 17 2 6 9 5 33 34 18 6 9 2 33 10 1 18 5 34 10 9 17 18 17 18 9 2 9 5 10 1 5 33 18 2 10 18";
		String[] ss = cstr.split(" ");
		byte[] chr = new byte[ss.length];
		for(int i = 0; i < ss.length; i++) {
			chr[i] = Byte.valueOf(ss[i]);
		}
		input = AIIndividual.getInput(chr);*/
    	
    	String line;  
        Process p;
        ArrayList<Integer> distances = new ArrayList<>();
		try {
			//0 0 8 25 0 30 4 90 0 95 4 130 0 135 4 175 0 180 8 220 0 225
			int[] commands = {
				RELEASE, 0, START, 25, RELEASE, 30, //single player
                SELECT, 90, RELEASE, 95, SELECT, 130, RELEASE, 135, SELECT, 175, RELEASE, 180,  //track
                START, 220, RELEASE, 225 //start game
			};
			String sCommands = "";
			for(int i = 0; i < commands.length; i++) {
				sCommands += commands[i] + " ";
			}
			for(int i = 0; i < input.length; i++) {
				sCommands += input[i] + " ";
			}
			
			p = Runtime.getRuntime().exec("/home/guilherme/eclipse-workspace/laines/laines " 
						+ (headless?1:0) + " " + MAX_RUNTIME + " " + sCommands, null, wd);
		
			
            BufferedReader br =  
              new BufferedReader  
                (new InputStreamReader(p.getInputStream()));  
            while ((line = br.readLine()) != null) {
            	distances.add(Integer.valueOf(line));
            }  
            br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		
		return distances;
    }
}
