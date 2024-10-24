package com.mrbysco.echoes.client.model;// Made with Blockbench 4.11.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.google.common.collect.ImmutableList;
import com.mrbysco.echoes.entity.creeper.EchoCreeper;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import java.util.List;

public class EchoCreeperModel extends HierarchicalModel<EchoCreeper> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftTendril;
	private final ModelPart rightTendril;
	private final ModelPart body;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightHindLeg;
	private final List<ModelPart> tendrilsLayerModelParts;
	private final List<ModelPart> heartLayerModelParts;
	private final List<ModelPart> pulsatingSpotsLayerModelParts;

	public EchoCreeperModel(ModelPart root) {
		this.root = root;
		this.head = this.root.getChild("head");
		ModelPart tendrils = this.head.getChild("tendrils");
		this.leftTendril = tendrils.getChild("left");
		this.rightTendril = tendrils.getChild("right");
		this.body = this.root.getChild("body");
		this.leftFrontLeg = this.root.getChild("left_front_leg");
		this.rightFrontLeg = this.root.getChild("right_front_leg");
		this.leftHindLeg = this.root.getChild("left_hind_leg");
		this.rightHindLeg = this.root.getChild("right_hind_leg");

		this.tendrilsLayerModelParts = ImmutableList.of(this.leftTendril, this.rightTendril);
		this.heartLayerModelParts = ImmutableList.of(this.body);
		this.pulsatingSpotsLayerModelParts = ImmutableList.of(this.body, this.head,
				this.leftFrontLeg, this.rightFrontLeg, this.leftHindLeg, this.rightHindLeg);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tendrils = head.addOrReplaceChild("tendrils", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition left = tendrils.addOrReplaceChild("left", CubeListBuilder.create().texOffs(54, 0).addBox(-9.0F, -10.0F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = tendrils.addOrReplaceChild("right", CubeListBuilder.create().texOffs(54, 0).mirror().addBox(4.0F, -10.0F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -18.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition left_front_leg = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 18.0F, -4.0F));

		PartDefinition right_front_leg = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 18.0F, -4.0F));

		PartDefinition left_hind_leg = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 18.0F, 4.0F));

		PartDefinition right_hind_leg = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 18.0F, 4.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(EchoCreeper echoCreeper, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
		this.head.xRot = headPitch * (float) (Math.PI / 180.0);
		this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		float f = ageInTicks - (float) echoCreeper.tickCount;
		this.animateTendrils(echoCreeper, ageInTicks, f);
	}

	private void animateTendrils(EchoCreeper echoCreeper, float ageInTicks, float partialTick) {
		float f = echoCreeper.getTendrilAnimation(partialTick) * (float) (Math.cos((double) ageInTicks * 2.25) * Math.PI * 0.1F);
		this.leftTendril.xRot = f;
		this.rightTendril.xRot = -f;
	}

	public List<ModelPart> getTendrilsLayerModelParts() {
		return this.tendrilsLayerModelParts;
	}

	public List<ModelPart> getHeartLayerModelParts() {
		return this.heartLayerModelParts;
	}

	public List<ModelPart> getPulsatingSpotsLayerModelParts() {
		return this.pulsatingSpotsLayerModelParts;
	}
}