package com.oleg.gg.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.oleg.gg.CS;
import com.oleg.gg.components.CulledComp;
import com.oleg.gg.components.PauseComp;
import com.oleg.gg.components.TransformComp;
import com.oleg.gg.ecs.MM;
import com.oleg.gg.Meat;
import com.oleg.gg.pojos.LevelData;


public class CullingSystem extends EntitySystem implements Disposable {

    private Array<Array<Cluster>> clusters;
    private float baseW, baseH;
    private int rows, cols;

    private float timer = 0f;
    private float cullingW, cullingH;
    private float unlockingW, unlockingH;

    private static final float CULL_EVENT_CD = 2f;

    private static final float CULL_W = CS.MAIN_VIEW_WIDTH * 3;
    private static final float CULL_H = CS.MAIN_VIEW_HEIGHT * 3;
    private static final float UNLOCK_W = CS.MAIN_VIEW_WIDTH * 2;
    private static final float UNLOCK_H = CS.MAIN_VIEW_HEIGHT * 2;

    private final Rectangle cullingRect = new Rectangle();
    private final Rectangle unlockingRect = new Rectangle();
    private final Vector2 camPos = new Vector2();

    // debugging
/*
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private static Color colorLine = Color.BLACK;
*/

    public CullingSystem(LevelData levData) {
        super(CS.SYSTEM_PRIORITIES.CULLING);

        defineClusters(levData);

        correctCullUnlockRectangles(1f);

        cullingRect.set(0, 0, cullingW, cullingH);
        unlockingRect.set(0, 0, unlockingW, unlockingH);

        Meat.engine.getSystem(CameraSystem.class).setCullingSystem(this);
    }

    @Override
    public void update(float delta) {
        camPos.set(Meat.camera.position.x, Meat.camera.position.y);
        cullingRect.setPosition(camPos.x - cullingW * 0.5f, camPos.y - cullingH * 0.5f);
        unlockingRect.setPosition(camPos.x - unlockingW * 0.5f, camPos.y - unlockingH * 0.5f);

        cullEntities(delta);
        unlockClusters();


/*
        debugRenderViews(Meat.camera.combined);
        debugRenderClusters(Meat.camera.combined);
*/
    }

    @Override
    public void dispose() {
/*
        shapeRenderer.dispose();
*/
    }


    public void correctCullUnlockRectangles(float cameraZoom) {
        cullingW = CULL_W * cameraZoom;
        cullingH = CULL_H * cameraZoom;

        cullingRect.setSize(cullingW, cullingH);

        unlockingW = UNLOCK_W * cameraZoom;
        unlockingH = UNLOCK_H * cameraZoom;

        unlockingRect.setSize(unlockingW, unlockingH);
    }

    private void unlockClusters() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cluster cluster = clusters.get(i).get(j);

                if (unlockingRect.overlaps(cluster) || unlockingRect.contains(cluster)) {
                    // now cluster unlocks all its entities
                    for (int k = 0; k < cluster.entities.size; k++) {
                        Entity entity = cluster.entities.get(k);

                        if (entity != null) {
                            PauseComp pauseC = MM.PAUSE.get(entity);

//                        if (Dbg.DEBUG) Dbg.log("       unPause cluster = " + cluster);
//                        if (Dbg.DEBUG) Dbg.logEnt("k = " + k + " / " + (cluster.entities.size - 1), entity);

                            // bug was persisted here
                            if (pauseC != null) pauseC.pausable.unPause(entity);
                        }
                    }

                    cluster.entities.clear();
                }
            }
        }
    }

    private void cullEntities(float delta) {
        timer -= delta;
        if (timer <= 0) {
            for (Entity entity : Meat.engine.getEntitiesFor(Family.all(TransformComp.class, PauseComp.class).
                    exclude(CulledComp.class).get())) {
                tryCullEntity(entity);
            }

            timer = CULL_EVENT_CD;
        }
    }

    private void tryCullEntity(Entity entity) {
        TransformComp transformC = MM.TRANSFORM.get(entity);

        // if entity is beyond culling rectangle
        if (!cullingRect.contains(transformC.pos)) {
            // try to pause entity
            if (MM.PAUSE.get(entity).pausable.pause(entity)) {
                int row = MathUtils.floor(transformC.pos.y / baseH);
                int col = MathUtils.floor(transformC.pos.x / baseW);

                // if center of entity is out of map bounds
                if (row < 0) row = 0;
                if (row >= rows) row = rows - 1;
                if (col < 0) col = 0;
                if (col >= cols) col = cols - 1;

                Cluster cluster = clusters.get(row).get(col);
                cluster.entities.add(entity);
            }
        }
    }

    private void defineClusters(LevelData levData) {
        clusters = new Array<>(4);

        baseW = CS.MAIN_VIEW_WIDTH * 0.5f;
        baseH = CS.MAIN_VIEW_HEIGHT * 0.5f;

        rows = MathUtils.ceil(levData.mapH / baseH);
        cols = MathUtils.ceil(levData.mapW / baseW);

        baseW = levData.mapW / cols;
        baseH = levData.mapH / rows;

        // create default clusters
        // iterating through clusters rows
        for (int i = 0; i < rows; i++) {
            Array<Cluster> clustersRow = new Array<>(8);
            // iterating through clusters columns
            for (int j = 0; j < cols; j++) {
                Cluster cluster = new Cluster(j * baseW, i * baseH, baseW, baseH);

                clustersRow.add(cluster);
            }
            clusters.add(clustersRow);
        }

//        if (Dbg.DEBUG) Dbg.log("rows = " + rows + ";  cols = " + cols + ";  baseW = " + baseW + ";  baseH = " + baseH);
//        if (Dbg.DEBUG) Dbg.log("");
    }


/*
    private void debugRenderViews(Matrix4 matrix) {
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(matrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

//        // render default viewBounds
//        shapeRenderer.setColor(Color.CHARTREUSE);
//        shapeRenderer.rect(camPos.x - CS.WORLD_WIDTH / 2, camPos.y - CS.WORLD_HEIGHT / 2,
//                CS.WORLD_WIDTH, CS.WORLD_HEIGHT);

        // render unlocking rectangle
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(unlockingRect.x, unlockingRect.y,
                unlockingRect.width, unlockingRect.height);

        // render culling rectangle
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(cullingRect.x, cullingRect.y,
                cullingRect.width, cullingRect.height);

        shapeRenderer.end();

        Gdx.gl20.glDisable(GL20.GL_BLEND);
    }

    private void debugRenderClusters(Matrix4 matrix) {
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(matrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int i = 0; i < clusters.size; i++) {
            Array<Cluster> clustersRow = clusters.get(i);
            for (int j = 0; j < clustersRow.size; j++) {
                Cluster cluster = clustersRow.get(j);

                float x = cluster.x;
                float y = cluster.y;
                float width = cluster.width;
                float height = cluster.height;

                shapeRenderer.setColor(cluster.debugColor);
                shapeRenderer.rect(x, y, width, height);
            }
        }

        shapeRenderer.end();

        Gdx.gl20.glDisable(GL20.GL_BLEND);
    }
*/

    // inner class
    private static class Cluster extends Rectangle {

        Array<Entity> entities = new Array<>();


/*
        boolean scaled = false;
        static float dc = 0.02f;
        Color debugColor = colorLine;
*/

        Cluster(float x, float y, float width, float height) {
            super(x, y, width, height);
        }


        // for debugging clusters (scale true if it's on otherwise false)
/*
        void scale(boolean down) {
            if (down) {
                if (scaled) return;
                scaled = true;
                debugColor = Color.CYAN;

                x += dc;
                y += dc;
                width -= dc * 2;
                height -= dc * 2;
            }
            else {
                if (!scaled) return;
                scaled = false;
                debugColor = colorLine;

                x -= dc;
                y -= dc;
                width += dc * 2;
                height += dc * 2;
            }
        }
*/
    }
}
