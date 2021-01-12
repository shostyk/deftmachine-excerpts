package com.oleg.gg.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.oleg.gg.Assets;
import com.oleg.gg.CS;
import com.oleg.gg.ecs.data.ItemType;
import com.oleg.gg.ecs.weapons.Arms;
import com.oleg.gg.ecs.weapons.Spec;
import com.oleg.gg.tools.UITool;
import com.oleg.gg.ui.custom.CustomLabel;
import com.oleg.gg.ui.custom.MyTinyButton;

import java.util.Map;


public class Slot extends Table {

    public Assets assets;
    public Image frame = new Image();
    Image mainImage = new Image();
    Image background = new Image();
    Image backImage = new Image();

    Table tNumbers;
    public CustomLabel labelMain;
    public boolean hasContent = false;
    SellWidget sellWidget;
    static float frameW = CS.UI.SLOT_W;
    static float frameH = CS.UI.SLOT_H;

    static final Color FRAME_DEFAULT = new Color(0.6f, 0.6f, 0.6f, 0.5f);
    static final Color FRAME_SELECTED = new Color(0.99f, 0.99f, 0.99f, 0.89f);
    static final Color FRAME_TINT = new Color(0.5f, 0.7f, 0.5f, 0.8f);
    static final Color FRAME_DELETE_DEFAULT = new Color(0.3f, 0.3f, 0.3f, 0.8f);
    static final Color FRAME_DELETE_SELECTED = new Color(0.9f, 0.1f, 0.1f, 0.8f);

    private static final Color C_BACKGROUND = new Color(0.38f, 0.3f, 0.25f, 0.5f);
    static final Color C_BACK_IMAGE = new Color(1f, 1f, 1f, 0.3f);

    Slot(Assets assets, boolean isBackImage) {
        setSkin(assets.skin);

        this.assets = assets;

        Map<String, TextureRegion> imagesMap = assets.imagesMap;

        frame.setDrawable(new TextureRegionDrawable(imagesMap.get(Assets.UIControls.FRAME)));
        frame.setColor(FRAME_DEFAULT);

        background.setDrawable(new TextureRegionDrawable(imagesMap.get(Assets.UIControls.BACKGROUND)));
        background.setColor(C_BACKGROUND);

        if (isBackImage) {
            backImage.setDrawable(new TextureRegionDrawable(imagesMap.get(Assets.UIControls.BACK_IMAGE)));
            backImage.setColor(C_BACK_IMAGE);
            backImage.setVisible(false);
        }

        labelMain = new CustomLabel(getSkin());
        labelMain.setColor(CS.COLORS.SLOT_NORMAL);

        tNumbers = new Table();
        tNumbers.add(labelMain).minWidth(frameW * 0.15f);

        padLeft(CS.STAGE_W * 0.006f);
        padRight(CS.STAGE_W * 0.007f);

        add(frame).size(frameW, frameH);
        add(background).size(frameW * 0.95f, frameH * 0.95f).padLeft(-frameW);
        if (isBackImage) add(backImage).size(frameW, frameH * 0.6f).padLeft(-frameW).padBottom(-frameH * 0.3f);
        add(mainImage).size(frameW * 0.7f, frameH * 0.8f).padLeft(-frameW).padBottom(-frameH * 0.05f).bottom();
        add(tNumbers).padLeft(-frameW * 0.9f).padBottom(frameH * 0.6f).left();

        sellWidget = new SellWidget(assets);
    }

    void showSellTable(boolean show) {
        if (show) {
            add(sellWidget).padLeft(-frameW).padBottom(-frameH * 1.2f);
        }
        else {
            removeActor(sellWidget);
        }
    }

    @Override
    public void clear() {
        backImage.setVisible(false);
        mainImage.setDrawable(null);
        labelMain.setText("");
        labelMain.setColor(CS.COLORS.SLOT_NORMAL);
        hasContent = false;

        showSellTable(false);
    }

    // inner classes
    public static class WeaponSlot extends Slot {

        public int id;
        public Spec spec;
        public int mags = 0;
        public int rs = 0;

        public CustomLabel labelDelimiter, labelRs;

        WeaponSlot(Assets assets, int id) {
            super(assets, true);

            this.id = id;

            getCell(mainImage).width(frameW * 0.8f).height(frameH * 0.35f).padBottom(frameH * 0.18f);

            labelDelimiter = new CustomLabel(getSkin());
            labelDelimiter.setColor(CS.COLORS.SLOT_NORMAL);

            labelRs = new CustomLabel(getSkin());
            labelRs.setColor(CS.COLORS.SLOT_NORMAL);

            tNumbers.add(labelDelimiter).padBottom(frameH * 0.04f);
            tNumbers.add(labelRs);
        }

        public void putNewWeapon(int weaponType) {
            hasContent = true;

            Spec spec = Arms.createSpec(weaponType);

//            if (Dbg.DEBUG) Dbg.log("  putNewWeapon " + weaponType);
//            if (Dbg.DEBUG) Dbg.log("    spec " + spec);

            TextureRegion textureRegion = assets.imagesMap.get(spec.gunAssetName);
            mainImage.setDrawable(new TextureRegionDrawable(textureRegion));

            backImage.setVisible(true);

            this.spec = spec;
            mags = 0;
            rs = spec.magCapacity;
            updateLabel();
        }

        public void getCopyFrom(WeaponSlot weaponSlot) {
            hasContent = weaponSlot.hasContent;

            mainImage.setDrawable(weaponSlot.mainImage.getDrawable());

            backImage.setVisible(true);

            spec = weaponSlot.spec;
            mags = weaponSlot.mags;
            rs = weaponSlot.rs;

            updateLabel();
        }

        public void updateLabel() {
            labelMain.setText(mags);
            if (mags == 0) labelMain.setColor(CS.COLORS.SLOT_LOW);
            else if (mags == spec.magMax) labelMain.setColor(CS.COLORS.SLOT_FULL);
            else labelMain.setColor(CS.COLORS.SLOT_NORMAL);

            labelDelimiter.setText(" : ");

            labelRs.setText(rs);
            if (rs == spec.magCapacity) labelRs.setColor(CS.COLORS.SLOT_FULL);
            else labelRs.setColor(CS.COLORS.SLOT_NORMAL);
        }

        void preScale() {
            frame.setSize(frameW, frameH);
            background.setSize(frameW * 0.95f, frameH * 0.95f);
        }

        void rescale(float s) {
            frame.setScale(s);
            frame.setOrigin(Align.center);

            background.setScale(s);
            background.setOrigin(Align.center);
        }

        boolean isFullMags() {
            return mags == spec.magMax;
        }

        public void magsChange(boolean increase) {
            if (increase) {
                mags++;

                UITool.lightenUnitChange(labelMain);
            }
            else {
                mags--;
            }

            updateLabel();
        }

        @Override
        public void showSellTable(boolean show) {
            if (show) sellWidget.price.setCost(true, spec.magCostSell);

            super.showSellTable(show);
        }

        @Override
        public void clear() {
            super.clear();

            spec = null;
            mags = 0;
            rs = 0;

            labelDelimiter.setText("");
            labelDelimiter.setColor(CS.COLORS.SLOT_NORMAL);
            labelRs.setText("");
            labelRs.setColor(CS.COLORS.SLOT_NORMAL);
        }
    }

    public static class ItemSlot extends Slot {

        public int id;
        public ItemType itemType;
        public int qty = 0;
        public MyTinyButton useButton;
        public boolean useShowed = false;

        ItemSlot(Assets assets, int id) {
            super(assets, false);

            this.id = id;

            getCell(mainImage).padRight(-frameW * 0.25f);

            useButton = new MyTinyButton("use", getSkin(), false);
        }

        public void putNewItem(ItemType itemType) {
            hasContent = true;

            TextureRegion textureRegion = assets.imagesMap.get(itemType.textureName);
            mainImage.setDrawable(new TextureRegionDrawable(textureRegion));

            this.itemType = itemType;
            qty = 1;

            updateLabel();
        }

        void updateLabel() {
            labelMain.setText(qty);
            if (qty == 0) labelMain.setColor(CS.COLORS.SLOT_LOW);
            else if (qty == itemType.maxQty) labelMain.setColor(CS.COLORS.SLOT_FULL);
            else labelMain.setColor(CS.COLORS.SLOT_NORMAL);
        }

        boolean isFullUnits() {
            return qty == itemType.maxQty;
        }

        void unitsChange(boolean increase) {
            if (increase) {
                qty++;

                UITool.lightenUnitChange(labelMain);
            }
            else {
                qty--;
            }

            updateLabel();
        }

        @Override
        public void showSellTable(boolean show) {
            if (show) sellWidget.price.setCost(true, itemType.sellCost);

            super.showSellTable(show);
        }

        @Override
        public void clear() {
            super.clear();

            itemType = null;
            qty = 0;

            showUseButton(false);
        }

        void showUseButton(boolean show) {
            if (show) {
                add(useButton).size(CS.UI.BUTTON_TINY_W, CS.UI.BUTTON_TINY_H).padLeft(-frameW).padBottom(-frameH * 1.75f);
                useShowed = true;
            }
            else {
                removeActor(useButton);
                useShowed = false;
            }
        }
    }

    static class SellWidget extends Table {

        MyTinyButton button;
        Price price;

        SellWidget(Assets assets) {
            button = new MyTinyButton("sell", assets.skin, false);
            price = new Price(true, 0, assets);

            add(button).size(CS.UI.BUTTON_TINY_W, CS.UI.BUTTON_TINY_H);
            row();
            add(price).center();
        }
    }
}
