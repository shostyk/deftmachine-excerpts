package com.oleg.gg.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.oleg.gg.Assets;
import com.oleg.gg.CS;
import com.oleg.gg.Meat;
import com.oleg.gg.Vars;
import com.oleg.gg.components.HealthComp;
import com.oleg.gg.components.PlayerComp;
import com.oleg.gg.components.WeaponComp;
import com.oleg.gg.ecs.MM;


public class Meters {

    private final Stage stage;
    private final Assets assets;
    private final Entity player;
    private final ShapeRenderer shapeRenderer;

    private Actor fireCharge, fireCD, jumpCharge, jumpCD, turboCharge, turboCD;
    private UINumber fireChargeLabel, fireCDLabel;
    private UINumber jumpChargeLabel, jumpCDLabel;
    private UINumber turboChargeLabel, turboCDLabel;

    private static final Color S_COLOR = CS.COLORS.THEME_GRAY;
    private static final Color C_COLOR = CS.COLORS.THEME_LIGHT;
    private static final Color R_COLOR = CS.COLORS.THEME_BLACK;

    private static final float CHARGE_HEIGHT_SCALE = 2.0f, CD_HEIGHT_SCALE = 1.5f;

    private static final float PAD = CS.STAGE_W * 0.012f;
    private static final float MIN_WIDTH = CS.STAGE_W * 0.055f;

    public Meters(Stage stage, Entity player) {
        this.stage = stage;
        this.player = player;

        assets = Meat.gg.assets;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        makeMetersTable();
    }

    public void drawMeters() {
        float alpha = Vars.IF.HUDOpacity;

        HealthComp healthC = MM.HEALTH.get(player);
        PlayerComp playerC = MM.PLAYER.get(player);
        WeaponComp weaponC = MM.WEAPON.get(player);

        float fireChargePer  = playerC.fireFactor;
        float fireCDPer      = MathUtils.clamp(weaponC.time / weaponC.spec.gunCoolDown, 0, 1);
        float jumpChargePer  = playerC.jumpChargeTime / CS.PLAYER.JUMP_CHARGE_TIME;
        float jumpCDPer      = MathUtils.clamp(playerC.jumpCoolDown / CS.PLAYER.JUMP_COOL_DOWN, 0, 1);
        float turboChargePer = playerC.turboTime / CS.PLAYER.TURBO_MAX_TIME;
        float turboCDPer     = MathUtils.clamp(playerC.turboCoolDown / CS.PLAYER.TURBO_COOL_DOWN, 0, 1);

        // drawing meters using shapeRenderer
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        renderFireMeter(alpha, fireChargePer, fireCDPer, weaponC);
        renderJumpMeter(alpha, jumpChargePer, jumpCDPer);
        renderTurboMeter(alpha, turboChargePer, turboCDPer);

        shapeRenderer.end();

        Gdx.gl20.glDisable(GL20.GL_BLEND);
    }

    public void updateVisibility() {
        WeaponComp weaponC = MM.WEAPON.get(player);

        if (Vars.IF.fireOn) {
            // set forceFire label
            fireChargeLabel.updateTextAndSize("" + weaponC.spec.gunChargeTime);
            // set fireCD
            fireCDLabel.updateTextAndSize("" + weaponC.spec.gunCoolDown);

            fireChargeLabel.setVisible(true);
            fireCDLabel.setVisible(true);
        }
        else {
            fireChargeLabel.setVisible(false);
            fireCDLabel.setVisible(false);
        }


        if (Vars.IF.jumpOn) {
            // set jumpCharge label
            jumpChargeLabel.updateTextAndSize("" + CS.PLAYER.JUMP_CHARGE_TIME);
            // set jumpCD
            jumpCDLabel.updateTextAndSize("" + CS.PLAYER.JUMP_COOL_DOWN);

            jumpChargeLabel.setVisible(true);
            jumpCDLabel.setVisible(true);
        }
        else {
            jumpChargeLabel.setVisible(false);
            jumpCDLabel.setVisible(false);
        }


        if (Vars.IF.turboOn) {
            // set turboCharge label
            turboChargeLabel.updateTextAndSize("" + CS.PLAYER.TURBO_MAX_TIME);
            // set turboCD
            turboCDLabel.updateTextAndSize("" + CS.PLAYER.TURBO_COOL_DOWN);

            turboChargeLabel.setVisible(true);
            turboCDLabel.setVisible(true);
        }
        else {
            turboChargeLabel.setVisible(false);
            turboCDLabel.setVisible(false);
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    private void makeMetersTable() {
        float barWidth = CS.UI.BAR_W;
        float barHeight = CS.UI.BAR_H;

        Table metersTable = new Table();
        stage.addActor(metersTable);

        // ------------- fire --------------------

        // forceFire meter
        fireCharge = new Actor();
        fireCharge.setSize(barWidth, barHeight * CHARGE_HEIGHT_SCALE);
        // forceFire label
        fireChargeLabel = new UINumber("0.0", assets);

        // fireCD bar
        fireCD = new Actor();
        fireCD.setSize(barWidth, barHeight * CD_HEIGHT_SCALE);
        // fireCD label
        fireCDLabel = new UINumber("0.0", assets);

        // ------------- jump -----------------

        // jumpCharge meter
        jumpCharge = new Actor();
        jumpCharge.setSize(barWidth, barHeight * CHARGE_HEIGHT_SCALE);
        // jumpCharge label
        jumpChargeLabel = new UINumber("" + CS.PLAYER.JUMP_CHARGE_TIME, assets);

        // jumpCD bar
        jumpCD = new Actor();
        jumpCD.setSize(barWidth, barHeight * CD_HEIGHT_SCALE);
        // jumpCD label
        jumpCDLabel = new UINumber("" + CS.PLAYER.JUMP_COOL_DOWN, assets);

        // ------------- turbo ---------------

        // turboCharge meter
        turboCharge = new Actor();
        turboCharge.setSize(barWidth, barHeight * CHARGE_HEIGHT_SCALE);
        // turboCharge label
        turboChargeLabel = new UINumber("" + CS.PLAYER.TURBO_MAX_TIME, assets);

        // turboCD bar
        turboCD = new Actor();
        turboCD.setSize(barWidth, barHeight * CD_HEIGHT_SCALE);
        // turboCD label
        turboCDLabel = new UINumber("" + CS.PLAYER.TURBO_COOL_DOWN, assets);


        // add all that stuff to the table
        metersTable.setFillParent(true);
        metersTable.left().top().padLeft(barWidth * 6.5f); // 3.5f

        // 1-st row
        metersTable.add(fireCharge);
        metersTable.add(fireChargeLabel).padLeft(PAD).minWidth(MIN_WIDTH);
        metersTable.add(jumpCharge).padLeft(PAD * 4);
        metersTable.add(jumpChargeLabel).padLeft(PAD).minWidth(MIN_WIDTH);
        metersTable.add(turboCharge).padLeft(PAD * 4);
        metersTable.add(turboChargeLabel).padLeft(PAD).minWidth(MIN_WIDTH);

        metersTable.row();

        // 2-nd row
        metersTable.add(fireCD);
        metersTable.add(fireCDLabel).padLeft(PAD).minWidth(MIN_WIDTH);
        metersTable.add(jumpCD).padLeft(PAD * 4);
        metersTable.add(jumpCDLabel).padLeft(PAD).minWidth(MIN_WIDTH);
        metersTable.add(turboCD).padLeft(PAD * 4);
        metersTable.add(turboCDLabel).padLeft(PAD).minWidth(MIN_WIDTH);

        metersTable.row();

//        metersTable.debug();
    }

    private void renderFireMeter(float alpha, float fireChargePer, float fireCDPer, WeaponComp tWeaponC) {
        if (Vars.IF.fireOn) {
            // render forceFire
            shapeRenderer.setColor(S_COLOR.r, S_COLOR.g, S_COLOR.b, alpha * S_COLOR.a);
            tri(fireCharge);
            shapeRenderer.setColor(C_COLOR.r, C_COLOR.g, C_COLOR.b, alpha * C_COLOR.a);
            tri(fireCharge, fireChargePer);
            // render fireCD
            shapeRenderer.setColor(S_COLOR.r, S_COLOR.g, S_COLOR.b, alpha * S_COLOR.a);
            rect(fireCD);
            shapeRenderer.setColor(C_COLOR.r, C_COLOR.g, C_COLOR.b, alpha * C_COLOR.a);
            rect(fireCD, 1 - fireCDPer);

            // draw fire ready
            if (fireCDPer == 0 ) {
                shapeRenderer.setColor(R_COLOR.r, R_COLOR.g, R_COLOR.b, alpha * R_COLOR.a);
                drawFireReady(fireCD);
            }

            // set forceFire label
            fireChargeLabel.updateText("" + tWeaponC.spec.gunChargeTime);
            // set fireCD
            fireCDLabel.updateText("" + tWeaponC.spec.gunCoolDown);
        }
    }

    private void renderJumpMeter(float alpha, float jumpChargePer, float jumpCDPer)  {
        if (Vars.IF.jumpOn) {
            // render jumpCharge
            shapeRenderer.setColor(S_COLOR.r, S_COLOR.g, S_COLOR.b, alpha * S_COLOR.a);
            tri(jumpCharge);
            shapeRenderer.setColor(C_COLOR.r, C_COLOR.g, C_COLOR.b, alpha * C_COLOR.a);
            tri(jumpCharge, jumpChargePer);
            // render jumpCD
            shapeRenderer.setColor(S_COLOR.r, S_COLOR.g, S_COLOR.b, alpha * S_COLOR.a);
            rect(jumpCD);
            shapeRenderer.setColor(C_COLOR.r, C_COLOR.g, C_COLOR.b, alpha * C_COLOR.a);
            rect(jumpCD, 1 - jumpCDPer);

            // draw jump ready
            if (jumpCDPer == 0 ) {
                shapeRenderer.setColor(R_COLOR.r, R_COLOR.g, R_COLOR.b, alpha * R_COLOR.a);
                drawJumpReady(jumpCD);
            }
        }
    }

    private void renderTurboMeter(float alpha, float turboChargePer, float turboCDPer) {
        if (Vars.IF.turboOn) {
            // render turboCharge
            shapeRenderer.setColor(S_COLOR.r, S_COLOR.g, S_COLOR.b, alpha * S_COLOR.a);
            tri(turboCharge);
            shapeRenderer.setColor(C_COLOR.r, C_COLOR.g, C_COLOR.b, alpha * C_COLOR.a);
            tri(turboCharge, turboChargePer);
            // render turboCD
            shapeRenderer.setColor(S_COLOR.r, S_COLOR.g, S_COLOR.b, alpha * S_COLOR.a);
            rect(turboCD);
            shapeRenderer.setColor(C_COLOR.r, C_COLOR.g, C_COLOR.b, alpha * C_COLOR.a);
            rect(turboCD, 1 - turboCDPer);

            // draw turbo ready
            if (turboCDPer == 0 ) {
                shapeRenderer.setColor(R_COLOR.r, R_COLOR.g, R_COLOR.b, alpha * R_COLOR.a);
                drawTurboReady(turboCD);
            }
        }
    }


    private void drawFireReady(Actor actor) {
        float x = actor.getX();
        float y = actor.getY();
        float w = actor.getWidth();
        float h = actor.getHeight();
        float b = h * 0.3f;

        // bottom
        shapeRenderer.rect(x, y, w, b);
        // top
        shapeRenderer.rect(x, y + h - b, w, b);
    }

    private void drawJumpReady(Actor actor) {
        float x = actor.getX();
        float y = actor.getY();
        float w = actor.getWidth();
        float h = actor.getHeight();
        float b = w * 0.15f;

        // left
        shapeRenderer.rect(x + b, y , b, h);
        // center
        shapeRenderer.rect(x + (w - b) / 2, y, b, h);
        // right
        shapeRenderer.rect(x + w - b * 2, y, b, h);
    }

    private void drawTurboReady(Actor actor) {
        float x = actor.getX();
        float y = actor.getY();
        float w = actor.getWidth();
        float h = actor.getHeight();
        float b = h * 0.2f;

        // center
        shapeRenderer.rect(x + b, y + b, w - b * 2, h - b * 2);
    }


    private void rect(Actor actor) {
        rect(actor, 1);
    }

    private void rect(Actor actor, float percent) {
        shapeRenderer.rect(actor.getX(), actor.getY(), actor.getWidth() * percent, actor.getHeight());
    }

    private void tri(Actor actor) {
        tri(actor, 1);
    }

    private void tri(Actor actor, float percent) {
        shapeRenderer.triangle(actor.getX(), actor.getY(), actor.getX() + actor.getWidth() * percent,
                actor.getY() + actor.getHeight() * percent, actor.getX() + actor.getWidth() * percent,
                actor.getY());
    }

}
