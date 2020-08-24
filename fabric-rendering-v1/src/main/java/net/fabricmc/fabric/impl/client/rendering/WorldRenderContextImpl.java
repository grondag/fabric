package net.fabricmc.fabric.impl.client.rendering;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.WorldRenderContext;

public class WorldRenderContextImpl implements WorldRenderContext {
	protected WorldRenderer worldRenderer;
	protected MatrixStack matrixStack;
	protected float tickDelta;
	protected long limitTime;
	protected boolean blockOutlines;
	protected Camera camera;
	protected Matrix4f projectionMatrix;
	protected Frustum frustum;

	public void prepare(
			WorldRenderer worldRenderer,
			MatrixStack matrixStack,
			float tickDelta,
			long limitTime,
			boolean blockOutlines,
			Camera camera,
			Matrix4f projectionMatrix) {
		this.worldRenderer = worldRenderer;
		this.matrixStack = matrixStack;
		this.tickDelta = tickDelta;
		this.limitTime = limitTime;
		this.blockOutlines = blockOutlines;
		this.camera = camera;
		this.projectionMatrix = projectionMatrix;
	}

	public void setFrustum(Frustum frustum) {
		this.frustum = frustum;
	}

	@Override
	public WorldRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public MatrixStack matrixStack() {
		return matrixStack;
	}

	@Override
	public float tickDelta() {
		return tickDelta;
	}

	@Override
	public long limitTime() {
		return limitTime;
	}

	@Override
	public boolean blockOutlines() {
		return blockOutlines;
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	public Matrix4f projectionMatrix() {
		return projectionMatrix;
	}

	@Override
	public Frustum frustum() {
		return frustum;
	}

}
