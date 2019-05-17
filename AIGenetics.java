package glhr.nesai;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class AIGenetics {
    public static final int N_GENES = AICore.MAX_RUNTIME * 5; //5 genes per second of runtime
    public int nextOffspringId = 0;
    public ArrayList<ArrayList<AIIndividual>> generations = new ArrayList<>();
    public ArrayList<AIIndividual> individuals = new ArrayList<>();
    public static final Random gen = new Random();

    public void generateInitialPopulation(int populationSize){
        for(int i = 0; i < populationSize; i++){
            AIIndividual individual = new AIIndividual(nextOffspringId++, N_GENES);
            individual.initialize();
            individuals.add(individual);
        }
    }

    /**
     * linear normalize
     */
    public void normalizeFitness(){
        individuals.sort((o1, o2) -> (int) Math.signum(o1.fitness - o2.fitness));

        float min = individuals.get(0).fitness;
        float max = individuals.get(individuals.size() - 1).fitness;
        float step = (max - min) / (individuals.size() - 1);
        for(int i = 0; i < individuals.size(); i++){
            individuals.get(i).fitness = min + step * i;
        }
    }

    /**
     * tourney selection
     * @param nIndividuals amount of individuals to select (must be even)
     * @param tourneySize size of a selection tourney round
     * @return selected individuals
     */
    public AIIndividual[] selectByTourney(int nIndividuals, int tourneySize){
        if(nIndividuals % 2 != 0){
            new Exception("Amount of individuals must be even").printStackTrace();
            return null;
        }

        AIIndividual[] selected = new AIIndividual[nIndividuals];

        for(int i = 0; i < nIndividuals; i++){
            AIIndividual tourneyWinner = null;
            for(int j = 0; j < tourneySize; j++) {
                int randIndex = (int) (Math.random() * individuals.size());
                AIIndividual individual = individuals.get(randIndex);
                if(tourneyWinner == null){
                    tourneyWinner = individual;
                }else if(individual.fitness > tourneyWinner.fitness){
                    tourneyWinner = individual;
                }
            }
            selected[i] = tourneyWinner;
        }

        return selected;
    }

    public AIIndividual[] selectByRoulette(int nIndividuals){
        if(nIndividuals % 2 != 0){
            new Exception("Amount of individuals must be even").printStackTrace();
            return null;
        }

        AIIndividual[] selected = new AIIndividual[nIndividuals];
     
        int sum = 0;
        for (AIIndividual individual : individuals) {
            sum += individual.fitness;
        }

        for(int i = 0; i < nIndividuals; i++) {
            int r = (int) (Math.random() * (sum + 1));

            int partialSum = 0;
            for (AIIndividual individual : individuals) {
                partialSum += individual.fitness;
                if (partialSum >= r) {
                    selected[i] = individual;
                    break;
                }
            }
        }
        
        return selected;
    }
    
    public AIIndividual[] reproduce(AIIndividual[] parents){
    	AIIndividual[] children = new AIIndividual[parents.length];
    	
    	int framesPerInput = (int) ((1f / (parents[0].chromosome.length / AICore.MAX_RUNTIME)) * 60f);
    	
    	for(int i = 0; i < parents.length; i += 2){
            AIIndividual p1 = parents[i];
            AIIndividual p2 = parents[i + 1];
            
            ArrayList<Integer> d1 = p1.distances;
            ArrayList<Integer> d2 = p2.distances;
            
            ArrayList<Integer> closePoints = new ArrayList<>(); //frames where the parents were close to each other
            
            int min = Integer.MAX_VALUE;
            int max = -1;
            for(int frame = 0; frame < d1.size(); frame++) {
            	float distance = (d1.get(frame) - d2.get(frame)) * (d1.get(frame) - d2.get(frame));
            	if(distance <= 15) {
            		closePoints.add(frame);
            		min = Math.min(min, frame);
            		max = Math.max(max, frame);
            	}
            }
            
            int sf = (int) (min + (max-min) * Math.random());
            int selectedFrame = closePoints.get(0);
            for(int point : closePoints) {
            	if(Math.abs(point - sf) < Math.abs(selectedFrame - sf)) {
            		selectedFrame = point;
            	}
            }
            
            //int selectedFrame = closePoints.get((int) (Math.random() * closePoints.size()));
            
            //System.out.println(String.format("Sel time: %.2f", selectedFrame / (float) d2.size()));
            int selectedGene = selectedFrame / framesPerInput;
            
            byte[] c1 = p1.chromosome;
            byte[] c2 = p2.chromosome;
            
            int nGenes = c1.length;
            AIIndividual child1 = new AIIndividual(nextOffspringId++, nGenes);
            AIIndividual child2 = new AIIndividual(nextOffspringId++, nGenes);

            for(int j = 0; j < nGenes; j++){
                if(j < selectedGene) {
	                child1.chromosome[j] = c1[j];
	                child2.chromosome[j] = c2[j];
                }else {
                	child1.chromosome[j] = c2[j];
	                child2.chromosome[j] = c1[j];
                }
            }

            children[i] = child1;
            children[i + 1] = child2;
    	}
    	
    	return children;
    }
    
    public AIIndividual[] reproduceSimple(AIIndividual[] parents, int crossoverPoints){
        int partitions = crossoverPoints + 1;
        AIIndividual[] children = new AIIndividual[parents.length];
        for(int i = 0; i < parents.length; i += 2){
            byte[] p1 = Arrays.copyOf(parents[i].chromosome, parents[i].chromosome.length);
            byte[] p2 = Arrays.copyOf(parents[i + 1].chromosome, parents[i + 1].chromosome.length);

            //shuffle(p1, p2);

            int nGenes = p1.length;
            AIIndividual child1 = new AIIndividual(nextOffspringId++, nGenes);
            AIIndividual child2 = new AIIndividual(nextOffspringId++, nGenes);

            int perPartition = nGenes / partitions;
            byte[] p = p1;
            for(int j = 0; j < nGenes; j++){
                if(j % perPartition == 0 && p == p1){
                    p = p2;
                }else if(j % perPartition == 0 && p == p2){
                    p = p1;
                }

                child1.chromosome[j] = p[j];
                child2.chromosome[j] = (p == p1?p2 : p1)[j];
            }

            children[i] = child1;
            children[i + 1] = child2;
        }

        return children;
    }

    /**
     * mutates current generation (flip mutation)
     * @param rate percentage to mutate
     */
    public void mutate(float rate){
        int n = (int) (rate * (individuals.size() * N_GENES));
        System.out.println("mutating " + ((float) n / (individuals.size() * N_GENES)) * 100 + "% of chromosomes");

        for(int i = 0; i < n; i++){
            int randIndex = (int) (Math.random() * individuals.size());
            AIIndividual individual = individuals.get(randIndex);

            //if(Math.random() > .4) {//flip
                randIndex = (int) (Math.random() * individual.chromosome.length);
                individual.chromosome[randIndex] = AIIndividual.GetRandomChromosome();
            /*}else{//swap seq
                int i1 = (int) (Math.random() * (individual.chromosome.length - 20));
                int i2 = (int) (Math.random() * (individual.chromosome.length - 20));

                for(int j = 0; j < 20; j++){
                    byte temp = individual.chromosome[i2 + j];
                    individual.chromosome[i2 + j] = individual.chromosome[i1 + j];
                    individual.chromosome[i1 + j] = temp;
                }
            }*/
        }
    }
    
    public void mutateEach(AIIndividual[] individuals, float rate){
        System.out.println("mutating " + rate * 100 + "% of each chromosome");
        int len = (int) (rate * N_GENES);
        
        ArrayList<Integer> mutated = new ArrayList<>();
        
        for(AIIndividual individual : individuals) {
        	for(int i = 0; i < len; i++) {
	        	int randIndex;
	        	do {
	        		randIndex = (int) (Math.random() * individual.chromosome.length);
	        	}while(mutated.contains(randIndex));
	        	
				individual.chromosome[randIndex] = AIIndividual.GetRandomChromosome();
				mutated.add(randIndex);
        	}
        	
        	mutated.clear();
        }
    }
    
    public void mutateSeqEach(AIIndividual[] individuals, float rate){
        System.out.println("mutating " + rate * 100 + "% of each chromosome");
        int len = (int) (rate * N_GENES);
        
        for(AIIndividual individual : individuals) {
    		int randIndex = (int) (Math.random() * (individual.chromosome.length - len));
        	
    		for(int i = 0; i < len; i++) {
    			individual.chromosome[randIndex + i] = AIIndividual.GetRandomChromosome();
    		}
        }
    }

    public void archiveAndReplace(AIIndividual[] newGeneration){
        generations.add(individuals);
        individuals = new ArrayList<>();
        individuals.addAll(Arrays.asList(newGeneration));
    }
    
    public void archive() {
    	generations.add(new ArrayList<>(individuals));
    }

    public AIIndividual getIndividual(int id){
        for(AIIndividual individual : individuals){
            if(individual.id == id){
                return individual;
            }
        }

        return null;
    }

    public AIIndividual getBest(){
        AIIndividual best = individuals.get(0);
        for(AIIndividual individual : individuals){
            if(individual.fitness > best.fitness){
                best = individual;
            }
        }

        return best;
    }

    public AIIndividual getWorst(){
        AIIndividual worst = individuals.get(0);
        for(AIIndividual individual : individuals){
            if(individual.fitness < worst.fitness){
                worst = individual;
            }
        }

        return worst;
    }

    public float getMeanFitness(){
        float sum = 0;
        for(AIIndividual individual : individuals){
            sum += individual.fitness;
        }

        return sum / individuals.size();
    }

    public ArrayList<AIIndividual> getPopulation(){
        return individuals;
    }

    // version for array of ints
    public static void shuffle (byte[] a1, byte[] a2) {
        int n = a1.length;
        while (n > 1) {
            int k = gen.nextInt(n--); //decrements after using the value
            byte temp = a1[n];
            a1[n] = a1[k];
            a1[k] = temp;
            temp = a2[n];
            a2[n] = a2[k];
            a2[k] = temp;
        }
    }

	public void replaceWorst(ArrayList<AIIndividual> population, AIIndividual[] children) {
		Collections.sort(population, (AIIndividual arg0, AIIndividual arg1) -> {
			return (int) Math.signum(arg0.fitness - arg1.fitness);
		});
		
		for(int i = 0; i < children.length; i++) {
			population.set(i, children[i]);
		}
	}
}
