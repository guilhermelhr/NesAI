package glhr.nesai;


import com.grapeshot.halfnes.CPURAM;
import com.grapeshot.halfnes.NES;

import java.awt.*;

import static com.grapeshot.halfnes.ai.ProgrammableController.*;

public class AIGameInterface {
    private NES nes;
    private ProgrammableController controller;

    public static final int START_AI_FRAME = 690;

    private float distance = 0;
    private int speed = 0;
    private float time = 0;
    private boolean started = false;

    private int framesStopped = 0;

    public AIGameInterface(ProgrammableController controller, NES nes) {
        this.nes = nes;
        this.controller = controller;
        controller.addInput(new int[]{
                START, 20, RELEASE, 25,  //single player
                SELECT, 90, RELEASE, 95, SELECT, 130, RELEASE, 135, SELECT, 175, RELEASE, 180,  //track
                START, 220, RELEASE, 225 //start game
        });
        if(nes.isFrameLimiterOn()) {
            nes.toggleFrameLimiter();
        }
    }

    public void update(CPURAM cpuram){
        float newTime = cpuram.read(0x68) * 60 + cpuram.read(0x69) + cpuram.read(0x6A) / 100f;

        if(!started && nes.framecount >= START_AI_FRAME){
            //nes.toggleFrameLimiter();
            controller.addInput(AICore.getInputFor(nes.getId()));
            started = true;
        }

        if(started){
            speed = cpuram.read(0xF3);
            boolean moving = cpuram.read(0xE) == 0;
            if(moving) {
                distance += speed / 60f;
            }

            if(time == newTime){
                framesStopped++;
            }

            boolean timeExceeded = time > AICore.MAX_RUNTIME;
            boolean finished = framesStopped > 60;
            if(timeExceeded || finished){
                AICore.onEnded(nes.getId(), new AIReport(distance, distance / (time + 1), finished));
                nes.quit();
            }
        }

        time = newTime;
    }

    public void render(Graphics graphics){
        CPURAM cpuram = nes.getCPURAM();
        update(cpuram);

        graphics.setColor(Color.magenta);
        graphics.drawString(String.format("Input: %s", controller.getStatus()), 4, 106);
        graphics.drawString(String.format("Time: %.1f Speed: %d Distance: %.1f", time, speed, distance), 4, 128);
        graphics.drawString(String.format("Frames: %d MeanSpeed: %.1f", nes.framecount, distance / (time + 1)), 4, 150);
    }
}
