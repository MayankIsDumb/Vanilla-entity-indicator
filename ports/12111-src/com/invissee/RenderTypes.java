package com.invissee;

/**
 * RenderTypes for 1.21.11+ new pipeline.
 * Only depth-tested lines - boxes are occluded by walls and
 * only show when the entity is in view.
 */
public class RenderTypes {
    public static final net.minecraft.client.renderer.rendertype.RenderType LINES_NORMAL =
            net.minecraft.client.renderer.rendertype.RenderTypes.LINES;
}
