package finalCampaign.dialog;

import arc.*;
import arc.scene.*;
import arc.scene.actions.Actions;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import finalCampaign.*;
import finalCampaign.util.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetRenderer.*;
import mindustry.type.*;

public class load extends BaseDialog implements PlanetInterfaceRenderer {

    public PlanetRenderer planets = new customPlanetRenderer();
    public customPlanetParams state = new customPlanetParams();
    
    private boolean shouldStopFakeUpdate;
    private Label zoomLabel;
    private Label posLabel;
    private fakeUniverse fake;
    private Universe origin;
    private float stepProgress;
    private int totalStep;
    private int step;
    private String stepName;
    private Label progressLabel;
    private Table progressTable;
    private boolean finish;
    private boolean finalZoom;

    private final Vec3 rawCamPos = new Vec3(-85f, 21f, 21f);
    private final float rawFakeDelta = 250f;
    private final float rawZoom = 9.7f;

    public load() {
        super("", Styles.fullDialog);
        titleTable.clear();
        shouldPause = true;

        step = 0;
        totalStep = 0;
        stepProgress = 0f;
        stepName = "";

        state.renderer = this;
        state.drawUi = true;

        state.zoom = rawZoom;
        state.uiAlpha = 0.5f;
        state.planet = Planets.sun;
        state.camPos.set(rawCamPos);

        dragged((cx, cy) -> {
            if(Core.input.getTouches() > 1) return;

            Vec3 pos = state.camPos;

            float upV = pos.angle(Vec3.Y);
            float xscale = 9f, yscale = 10f;
            float margin = 1;

            float speed = 1f - Math.abs(upV - 90) / 90f;

            pos.rotate(state.camUp, cx / xscale * speed);

            float amount = cy / yscale;
            amount = Mathf.clamp(upV + amount, margin, 180f - margin) - upV;

            pos.rotate(Tmp.v31.set(state.camUp).rotate(state.camDir, 90), amount);
        });

        addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                if(event.targetActor == load.this){
                    state.zoom = Mathf.clamp(state.zoom + amountY / 10f, state.planet.minZoom, 10f);
                    state.uiAlpha = Mathf.clamp(-1.66667f * (state.zoom - 9.7f) + 0.5f);
                }
                return true;
            }
        });

        addCaptureListener(new ElementGestureListener(){
            float lastZoom = -1f;

            @Override
            public void zoom(InputEvent event, float initialDistance, float distance){
                if(lastZoom < 0){
                    lastZoom = state.zoom;
                }

                state.zoom = Mathf.clamp(initialDistance / distance * lastZoom, state.planet.minZoom, 10f);
                state.uiAlpha = Mathf.clamp(-1.66667f * (state.zoom - 9.7f) + 0.5f);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                lastZoom = state.zoom;
            }
        });

        Table testData = new Table(t -> {
            t.parent = this;
            t.fillParent = false;
            t.setWidth(layout.getSceneWidth());

            zoomLabel = t.add("zoom: " + Float.toString(state.zoom)).left().padLeft(15f).growX().get();
            t.row();
            posLabel = t.add("pos: \nx:" + Float.toString(state.camPos.x) + "\ny:" + Float.toString(state.camPos.y) + "\nz:" + Float.toString(state.camPos.z) + 
                "\nserpulo pos: \nx:" + Float.toString(Planets.serpulo.position.x) + "\ny:" + Float.toString(Planets.serpulo.position.y) + "\nz:" + Float.toString(Planets.serpulo.position.z)).left().padLeft(15f).growX().get();

            t.setHeight(zoomLabel.getHeight() * 9);
            t.setPosition(load.this.x, layout.getSceneHeight() - zoomLabel.getHeight() * 9);
        });

        progressTable = new Table(t -> {
            t.parent = this;
            t.fillParent = false;
            t.setSize(layout.getSceneWidth(), 100f);
            t.setPosition(load.this.x, load.this.y);

            progressLabel = t.add(this.stepName).center().get();
            t.row();
            t.add(new Bar(() -> {
                String p = Float.toString((this.stepProgress + this.step) / this.totalStep * 100);
                return p.substring(0, Math.min(5, p.length())) + "%";
            }, () -> {
                return Pal.accent.cpy();
            }, () -> {
                return (this.stepProgress + this.step) / this.totalStep;
            })).size(500f, 20f).center().padTop(8f).padBottom(10f).get().blink(Pal.accent.cpy()).snap();
        });

        stack(new Element() {
            @Override
            public void act(float delta) {
                if (Core.scene.getDialog() == load.this && !Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true).isDescendantOf(e -> e instanceof ScrollPane)) {
                    Core.scene.setScrollFocus(load.this);
                }

                super.act(delta);
                progressTable.act(delta);

                if (finish) {

                    fake.delta = (float) Mathf.clamp(fake.delta - (rawFakeDelta - 2.5) * delta / 2.5f, 2.5f, rawFakeDelta);

                    Vec3 targetPos = new Vec3(Planets.serpulo.position);
                    targetPos.setLength(targetPos.len() - Planets.serpulo.radius * 2.5f);

                    Vec3 deltaPos = new Vec3(targetPos);
                    deltaPos.minus(state.camPos);
                    deltaPos.setLength(deltaPos.len() * delta / 0.5f);

                    Vec3 lookAtPos = new Vec3(Planets.serpulo.position);
                    Vec3 deltaLookAtPos = new Vec3(lookAtPos);
                    deltaLookAtPos.minus(state.lookAtPos);
                    deltaLookAtPos.setLength(deltaLookAtPos.len() * delta / 0.25f);

                    final float targetZoom = 5.7f;
                    float deltaZoom = (targetZoom - state.zoom) * delta / 0.25f;

                    if (!finalZoom) state.zoom = Mathf.clamp(state.zoom + deltaZoom, targetZoom, rawZoom);
                    state.camPos.add(deltaPos);
                    state.lookAtPos.add(deltaLookAtPos);

                    // backup 2.5025f
                    if (targetPos.minus(state.camPos).len() < 2.5025f && !finalZoom) {
                        Time.run(45f, () -> {
                            finalZoom = true;
                        });
                    }

                    if (finalZoom) {
                        final float targetFov = 1f;
                        float deltaFov = (targetFov - planets.cam.fov) * delta / 0.1125f;
                        planets.cam.fov = Mathf.clamp(planets.cam.fov + deltaFov, targetFov, 67f);

                        if (planets.cam.fov - targetFov < 5f) {
                            Time.run(30f, () -> {
                                load.this.hide();
                            });
                        }
                    }
                }
            }

            @Override
            public void draw() {
                planets.render(state);

                if (version.debug) {
                    posLabel.setText("pos: \nx:" + Float.toString(state.camPos.x) + "\ny:" + Float.toString(state.camPos.y) + "\nz:" + Float.toString(state.camPos.z) + 
                        "\nserpulo pos: \nx:" + Float.toString(Planets.serpulo.position.x) + "\ny:" + Float.toString(Planets.serpulo.position.y) + "\nz:" + Float.toString(Planets.serpulo.position.z));
                    zoomLabel.setText("zoom: " + Float.toString(state.zoom));
                    testData.draw();
                }

                progressTable.draw();
            }
        });

        shown(() -> {
            fakeUniverse fake = new fakeUniverse();
            fake.delta = rawFakeDelta;
            fake.planets = Vars.content.planets().toArray(Planet.class);

            Thread updateFake = new Thread(() -> {
                while (!shouldStopFakeUpdate) {
                    fake.update();
                    try {
                        Thread.sleep(16);
                    } catch(Exception e) {}
                }
                Log.debug("fake universe: update thread stopped.");
            });
            updateFake.setDaemon(false);
            shouldStopFakeUpdate = false;
            finish = false;
            finalZoom = false;
            updateFake.start();

            origin = Vars.universe;
            Vars.universe = fake;
            this.fake = fake;

            step = 0;
            totalStep = 0;
            stepProgress = 0f;
            stepName = "";

            state.lookAtPos = new Vec3(Planets.sun.position);
        });

        hidden(() -> {
            Time.run(40f, () -> {
                Vars.universe = origin;
                shouldStopFakeUpdate = true;
            });
        });

    }

    @Override
    public void draw() {
        // avoid thread null pointer problem
        try {
            super.draw();
        } catch (Exception e) {}
    }

    public void renderSectors(Planet planet) {
        return;
    }

    public void renderProjections(Planet planet) {
        return;
    }

    public void setStepName(String name) {
        Log.debug("load: step name update: " + name);
        stepName = name;
        progressLabel.setText(stepName);
    }

    public void setTotalStep(int value) {
        totalStep = value;
    }

    public void nextStep(String name) {
        stepProgress = 0f;
        step = Mathf.clamp(step + 1, 0, totalStep);
        setStepName(name);

        if (step == totalStep) {
            Time.run(30f, () -> {
                progressTable.actions(Actions.fadeOut(15f));
            });

            finish = true;
        }
    }

    public void setStepProgress(float value) {
        stepProgress = Mathf.clamp(value);
    }

}
