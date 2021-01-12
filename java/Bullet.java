package com.oleg.gg.ecs.entities;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.oleg.gg.CS;
import com.oleg.gg.components.CollisionComp;
import com.oleg.gg.components.DamagerComp;
import com.oleg.gg.components.FSMComp;
import com.oleg.gg.components.FactionComp;
import com.oleg.gg.components.HealthComp;
import com.oleg.gg.components.PhysicsComp;
import com.oleg.gg.components.RenderComp;
import com.oleg.gg.components.TransformComp;
import com.oleg.gg.ecs.Builder;
import com.oleg.gg.ecs.Collidable;
import com.oleg.gg.ecs.CollisionData;
import com.oleg.gg.ecs.Damageable;
import com.oleg.gg.ecs.GameObject;
import com.oleg.gg.ecs.MM;
import com.oleg.gg.ecs.data.Flags;
import com.oleg.gg.ecs.data.Zs;
import com.oleg.gg.ecs.fsm.BulletFSM;
import com.oleg.gg.Meat;
import com.oleg.gg.ecs.weapons.Spec;
import com.oleg.gg.tools.FSMTool;
import com.oleg.gg.tools.Factory;
import com.oleg.gg.tools.SoundTool;


public class Bullet extends GameObject {

    private TransformComp transformC;
    private PhysicsComp physicsC;

    private RenderComp renderC;

    private HealthComp healthC;
    private DamagerComp damagerC;

    private FSMComp fsmC;
    private FactionComp factionC;
    private CollisionComp collisionC;

    protected Spec.ShootData data;
    protected Spec spec;

    public Bullet(Spec.ShootData data, Spec spec) {
        super(Flags.BULLET);

        this.data = data;
        this.spec = spec;

        // ----------------------------------------

        setTransformC();
        setPhysicsC();

        setRenderC();

        setHealthC();
        setDamagerC();

        setFsmC();
        setFactionC();
        setCollisionC();

        // ----------------------------------------

        Factory.addComponentsToEntity(getEntity(),
                transformC, physicsC, renderC, healthC, damagerC, fsmC, factionC, collisionC);
    }

    private void setTransformC() {
        transformC = Builder.transformC();

        transformC.pos.set(data.position);
        transformC.z = Zs.BULLET;
    }

    private void setPhysicsC() {
        physicsC = Builder.physicsC();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(data.position);
        bodyDef.gravityScale = spec.bulGravityScale;
        physicsC.body = Meat.world.createBody(bodyDef);
        physicsC.body.setBullet(true);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(spec.bulRadius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = spec.bulDensity;
        fixtureDef.restitution = 0f;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = CS.MASK.BULLET;

        physicsC.body.createFixture(fixtureDef);

        // apply gun force
        physicsC.body.applyForceToCenter(data.force, true);


        circleShape.dispose();
    }

    private void setRenderC() {
        renderC = Builder.renderC();

        renderC.textureRegion = Meat.gg.assets.imagesMap.get(spec.bulAssetName);
        renderC.canRotate = spec.bulHasRotation;
        renderC.texWidth = spec.bulRadius * 2;
        renderC.texHeight = spec.bulRadius * 2;
        renderC.originX = renderC.texWidth * 0.5f;
        renderC.originY = renderC.texHeight * 0.5f;
    }

    private void setHealthC() {
        healthC = Builder.healthC();

        healthC.value = spec.bulHealth;
        healthC.maxValue = spec.bulHealth;

        healthC.damageable = new Damageable() {
            @Override
            public void onDie() {
                if (MM.FSM.get(getEntity()).stateMachine.getCurrentState() != BulletFSM.DIE) {
                    MM.FSM.get(getEntity()).stateMachine.changeState(BulletFSM.DIE);
                }
            }
        };
    }

    private void setDamagerC() {
        damagerC = Builder.damagerC();

        damagerC.damage = spec.bulDamage;
        damagerC.particleType = spec.bulParticlesType;
        damagerC.lifeTime = spec.bulLifeTime;
    }

    private void setFsmC() {
        fsmC = Builder.fsmC();

        fsmC.stateMachine = new DefaultStateMachine<>(getEntity(), BulletFSM.START, null);
    }

    private void setFactionC() {
        factionC = Builder.factionC();

        factionC.faction = data.faction;
    }

    private void setCollisionC() {
        collisionC = Builder.collisionC();

        collisionC.collidable = new Collidable() {
            @Override
            public void onCollisionEnter(CollisionData data) {
                if (damagerC.isUsed) return;

                if (MM.HEALTH.has(data.receiver)) {
                    float inflictedDamage = FSMTool.inflictDamage(data.impactor, data.receiver, damagerC.damage);

                    if (inflictedDamage > 0) SoundTool.makeHitSound(data.impactor, data.receiver);

                    if (spec.bulletSpecialAction != null) spec.bulletSpecialAction.commitSpecialAction(data);
                }

                Factory.crParticlesEjector(data.collisionPoint, damagerC.particleType);

                damagerC.isUsed = true;
                Meat.engine.removeEntity(data.impactor);
            }
        };
    }
}
