package glhr.nesai;


public class AIReport {
    public float distance;
    public float meanSpeed;
    public boolean finished;

    public AIReport(float distance, float meanSpeed, boolean finished){
        this.distance = distance;
        this.meanSpeed = meanSpeed;
        this.finished = finished;
    }

    @Override
    public String toString(){
        return "Finished? " + finished + " Distance: " + distance + " MeanSpeed: " + meanSpeed;
    }

    public float getFitness() {
        return distance;// + meanSpeed;
    }
}
