package com.mrbysco.echoes.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class EchoEmissiveLayer<T extends LivingEntity, M extends HierarchicalModel<T>> extends RenderLayer<T, M> {
	private final ResourceLocation texture;
	private final EchoEmissiveLayer.AlphaFunction<T> alphaFunction;
	private final EchoEmissiveLayer.DrawSelector<T, M> drawSelector;

	public EchoEmissiveLayer(
			RenderLayerParent<T, M> renderer,
			ResourceLocation texture,
			EchoEmissiveLayer.AlphaFunction<T> alphaFunction,
			EchoEmissiveLayer.DrawSelector<T, M> drawSelector
	) {
		super(renderer);
		this.texture = texture;
		this.alphaFunction = alphaFunction;
		this.drawSelector = drawSelector;
	}

	public void render(
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			T livingEntity,
			float limbSwing,
			float limbSwingAmount,
			float partialTick,
			float ageInTicks,
			float netHeadYaw,
			float headPitch
	) {
		if (!livingEntity.isInvisible()) {
			this.onlyDrawSelectedParts();
			VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
			float f = this.alphaFunction.apply(livingEntity, partialTick, ageInTicks);
			int i = FastColor.ARGB32.color(Mth.floor(f * 255.0F), 255, 255, 255);
			this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), i);
			this.resetDrawForAllParts();
		}
	}

	private void onlyDrawSelectedParts() {
		List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
		this.getParentModel().root().getAllParts().forEach(p_234918_ -> p_234918_.skipDraw = true);
		list.forEach(p_234916_ -> p_234916_.skipDraw = false);
	}

	private void resetDrawForAllParts() {
		this.getParentModel().root().getAllParts().forEach(p_234913_ -> p_234913_.skipDraw = false);
	}

	@OnlyIn(Dist.CLIENT)
	public interface AlphaFunction<T extends LivingEntity> {
		float apply(T livingEntity, float partialTick, float ageInTicks);
	}

	@OnlyIn(Dist.CLIENT)
	public interface DrawSelector<T extends LivingEntity, M extends EntityModel<T>> {
		List<ModelPart> getPartsToDraw(M parentModel);
	}
}
