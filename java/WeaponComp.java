package com.oleg.gg.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.oleg.gg.ecs.weapons.Spec;


public class WeaponComp implements Component, Pool.Poolable {

    public Spec spec = null;

    public boolean isActive = false;
    public float activeTime = 0f;
    public float time = 0f;

    @Override
    public void reset() {
        spec = null;

        activeTime = 0f;
        isActive = false;
        time = 0f;
    }
}
