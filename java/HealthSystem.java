package com.oleg.gg.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.oleg.gg.CS;
import com.oleg.gg.components.CulledComp;
import com.oleg.gg.components.HealthComp;
import com.oleg.gg.ecs.MM;


public class HealthSystem extends IteratingSystem {

    public HealthSystem() {
        super(Family.all(HealthComp.class).
                exclude(CulledComp.class).get(), CS.SYSTEM_PRIORITIES.HEALTH);
    }

    @Override
    protected void processEntity(Entity entity, float delta) {
        HealthComp healthC = MM.HEALTH.get(entity);

        if (healthC.value <= 0) {
            healthC.setZero();
            if (healthC.damageable != null) {
                healthC.damageable.onDie();

                return;
            }
//            else Dbg.error("HealthC has no damageable");
        }

        if (healthC.isRegen) {
            healthC.value += healthC.regenRate * delta;
            if (healthC.value > healthC.maxValue) {
                healthC.value = healthC.maxValue;
            }
        }
    }
}
