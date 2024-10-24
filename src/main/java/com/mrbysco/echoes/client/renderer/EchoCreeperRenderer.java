package com.mrbysco.echoes.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.client.ClientHandler;
import com.mrbysco.echoes.client.model.EchoCreeperModel;
import com.mrbysco.echoes.client.renderer.layer.EchoEmissiveLayer;
import com.mrbysco.echoes.entity.creeper.EchoCreeper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EchoCreeperRenderer extends MobRenderer<EchoCreeper, EchoCreeperModel> {
	private static final ResourceLocation CREEPER_LOCATION = EchoesMod.modLoc("textures/entity/creeper/echo_creeper.png");
	private static final ResourceLocation HEART_TEXTURE = EchoesMod.modLoc("textures/entity/creeper/echo_creeper_heart.png");
	private static final ResourceLocation PULSES = EchoesMod.modLoc(
			"textures/entity/creeper/echo_creeper_pulses.png"
	);

	public EchoCreeperRenderer(EntityRendererProvider.Context context) {
		super(context, new EchoCreeperModel(context.bakeLayer(ClientHandler.ECHO_CREEPER_LOCATION)), 0.5F);
		this.addLayer(
				new EchoEmissiveLayer<>(
						this,
						PULSES,
						(echoCreeper, partialTick, v1) -> Math.max(0.0F, Mth.cos(v1 * 0.045F) * 0.25F),
						EchoCreeperModel::getPulsatingSpotsLayerModelParts
				)
		);
		this.addLayer(
				new EchoEmissiveLayer<>(
						this, CREEPER_LOCATION, (echoCreeper, partialTick, v1) -> echoCreeper.getTendrilAnimation(partialTick), EchoCreeperModel::getTendrilsLayerModelParts
				)
		);
		this.addLayer(
				new EchoEmissiveLayer<>(
						this, HEART_TEXTURE, (echoCreeper, partialTick, v1) -> echoCreeper.getHeartAnimation(partialTick), EchoCreeperModel::getHeartLayerModelParts
				)
		);
	}

	@Override
	protected void scale(EchoCreeper echoCreeper, PoseStack poseStack, float partialTickTime) {
		float swelling = echoCreeper.getSwelling(partialTickTime);
		float f1 = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
		swelling = Mth.clamp(swelling, 0.0F, 1.0F);
		swelling *= swelling;
		swelling *= swelling;
		float f2 = (1.0F + swelling * 0.4F) * f1;
		float f3 = (1.0F + swelling * 0.1F) / f1;
		poseStack.scale(f2, f3, f2);
	}

	@Override
	protected float getWhiteOverlayProgress(EchoCreeper echoCreeper, float partialTicks) {
		float swelling = echoCreeper.getSwelling(partialTicks);
		return (int) (swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	@Override
	public ResourceLocation getTextureLocation(EchoCreeper entity) {
		return CREEPER_LOCATION;
	}
}
