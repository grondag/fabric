/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.indigo.renderer.mixin;

import java.nio.IntBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.indigo.renderer.accessor.AccessBufferBuilder;
import net.fabricmc.indigo.renderer.mesh.QuadViewImpl;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements AccessBufferBuilder {
    @Shadow private IntBuffer bufInt;
    @Shadow private int vertexCount;
    @Shadow abstract void grow(int size);
    @Shadow abstract int getCurrentSize();
    @Shadow public abstract VertexFormat getVertexFormat();

	private static final int VERTEX_STRIDE_INTS = 7;
    private static final int QUAD_STRIDE_INTS = VERTEX_STRIDE_INTS * 4;
    private static final int QUAD_STRIDE_BYTES = QUAD_STRIDE_INTS * 4;

    @FunctionalInterface
    private static interface VertexHandler {
        void accept(QuadViewImpl quad);
    }
    
    private VertexHandler fabric_itemHandler;
    private VertexHandler fabric_blockHandler;
    
    @Inject(at = @At("RETURN"), method = "begin")
	private void afterBegin(int mode, VertexFormat passedFormat, CallbackInfo info) {
        final VertexFormat activeFormat = getVertexFormat();
        if(activeFormat == VertexFormats.POSITION_COLOR_UV_LMAP) {
            fabric_itemHandler = this::bufferCompatibly;
            fabric_blockHandler = this::bufferFast;
        } else if(activeFormat == VertexFormats.POSITION_COLOR_UV_NORMAL) {
            fabric_itemHandler = this::bufferFast;
            fabric_blockHandler = this::bufferCompatibly;
        } else {
            fabric_itemHandler = this::bufferCompatibly;
            fabric_blockHandler = this::bufferCompatibly;
        }
	}

    /**
     * Similar to {@link BufferBuilder#putVertexData(int[])} but
     * accepts an array index so that arrays containing more than one
     * quad don't have to be copied to a transfer array before the call.
	 *
	 * It also always assumes the vanilla data format and is capable of
	 * transforming data from it to a different, non-vanilla data format.
     */
    @Override
    public void fabric_putQuad(QuadViewImpl quad, boolean isItemFormat) {
        bufferCompatibly(quad);
//        if(isItemFormat) {
//            fabric_itemHandler.accept(quad);
//        } else {
//            fabric_blockHandler.accept(quad);
//        }
    }
    
    private void bufferFast(QuadViewImpl quad) {
        grow(QUAD_STRIDE_BYTES);
        bufInt.position(getCurrentSize());
        bufInt.put(quad.data(), quad.vertexStart(), QUAD_STRIDE_INTS);
        vertexCount += 4;
    }
    
    private void bufferCompatibly(QuadViewImpl quad) {
        final VertexFormat format = getVertexFormat();;
        final int elementCount = format.getElementCount();
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < elementCount; j++) {
                VertexFormatElement e = format.getElement(j);
                switch(e.getType()) {
                case COLOR:
                    final int c = quad.spriteColor(i, 0);
                    ((BufferBuilder)(Object)this).color((c >>> 16) & 0xFF, (c >>> 8) & 0xFF, c & 0xFF, (c >>> 24) & 0xFF);
                    break;
                case NORMAL:
                    ((BufferBuilder)(Object)this).normal(quad.normalX(i), quad.normalY(i), quad.normalZ(i));
                    break;
                case POSITION:
                    ((BufferBuilder)(Object)this).vertex(quad.x(i), quad.y(i), quad.z(i));
                    break;
                case UV:
                    if(e.getIndex() == 0) {
                        ((BufferBuilder)(Object)this).texture(quad.spriteU(i, 0), quad.spriteV(i, 0));
                    } else {
                        final int b = quad.lightmap(i);
                        ((BufferBuilder)(Object)this).texture((b >> 16) & 0xFFFF, b & 0xFFFF);
                    }
                    break;
                    
                // these types should never occur and/or require no action
                case MATRIX:
                case BLEND_WEIGHT:
                case PADDING:
                default:
                    break;
                
                }
            }
            ((BufferBuilder)(Object)this).next();
        }
    }
}
