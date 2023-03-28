package singleplayer.update.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.IllusionerEntityRenderer;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IllusionerEntityRenderer.class)
public class IllusionerRendererMixin extends IllagerEntityRenderer<IllusionerEntity> {
    protected IllusionerRendererMixin(EntityRendererFactory.Context ctx, IllagerEntityModel<IllusionerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    private static final Identifier TEXTURE = new Identifier("textures/entity/illager/illusioner.png");

    @Override
    public void render(IllusionerEntity illusionerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (!illusionerEntity.isInvisible()){super.render(illusionerEntity, f, g, matrixStack, vertexConsumerProvider, i);}
    }

    @Override
    public boolean isVisible(IllusionerEntity illusionerEntity) {return super.isVisible(illusionerEntity);}

    public Identifier getTexture(IllusionerEntity entity) {
        return TEXTURE;
    }
}
