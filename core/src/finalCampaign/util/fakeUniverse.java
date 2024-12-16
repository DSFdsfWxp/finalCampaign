package finalCampaign.util;

import mindustry.game.*;
import mindustry.type.*;

public class fakeUniverse extends Universe {
    public float delta;
    public float second;

    public Planet[] planets;

    @Override
    public int seconds() {
        return Float.valueOf(delta).intValue();
    }

    @Override
    public float secondsf() {
        return second;
    }

    @Override
    public void runTurn() {
        return;
    }

    private void updatePlanet(Planet planet) {
        planet.position.setZero();
        planet.addParentOffset(planet.position);
        if(planet.parent != null){
            planet.position.add(planet.parent.position);
        }
        for(Planet child : planet.children){
            updatePlanet(child);
        }
    }

    @Override
    public synchronized void updateGlobal() {
        for(Planet planet : planets){
            if(planet.parent == null) {
                // avoid thread problem.
                try {
                    updatePlanet(planet);
                } catch(Exception e) {}
            }
        }
    }

    @Override
    public synchronized void update() {
        if (delta < 0) delta = 0;
        second += delta;
    }
}
