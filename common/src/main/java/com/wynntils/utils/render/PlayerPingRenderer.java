/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.pipelines.CustomRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class PlayerPingRenderer {
    private static final float BASE_RADIUS = 0.4f;
    private static final float BASE_HEIGHT = 0.1f;
    private static final float ROD_RADIUS = 0.06f;
    private static final float ROD_HEIGHT = 1.4f;
    private static final float ORB_RADIUS = 0.22f;
    private static final float ORB_ROTATION_PERIOD_TICKS = 40f;
    private static final float ORB_BOB_PERIOD_TICKS = 30f;
    private static final float ORB_BOB_AMPLITUDE = 0.08f;
    private static final float ORB_SPARK_RADIUS = 0.045f;
    private static final float ORB_SPARK_ORBIT_RADIUS = 0.31f;
    private static final long ANIMATION_PERIOD_TICKS = 120L;
    private static final int RIPPLE_COUNT = 2;
    private static final float RIPPLE_CYCLE_TICKS = 24f;
    private static final float RIPPLE_THICKNESS = 0.06f;
    private static final float RIPPLE_MIN_RADIUS = BASE_RADIUS + RIPPLE_THICKNESS;
    private static final float RIPPLE_MAX_RADIUS = BASE_RADIUS * 3f;
    private static final float RIPPLE_HEIGHT = BASE_HEIGHT + 0.02f;

    public static void submit(
            SubmitNodeCollector submitNodeCollector,
            Vec3 cameraPosition,
            PoseStack poseStack,
            Vec3 worldPosition,
            long gameTime,
            int color) {
        float animationTime = (gameTime % ANIMATION_PERIOD_TICKS)
                + McUtils.mc().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 centeredPosition = worldPosition.add(0.5d, 0d, 0.5d);

        poseStack.pushPose();
        poseStack.translate(
                centeredPosition.x - cameraPosition.x,
                centeredPosition.y - cameraPosition.y,
                centeredPosition.z - cameraPosition.z);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                CustomRenderTypes.POSITION_COLOR_QUAD,
                (pose, consumer) -> drawMarker(pose, consumer, color, animationTime));

        poseStack.popPose();
    }

    private static void drawMarker(PoseStack.Pose pose, VertexConsumer consumer, int color, float animationTime) {
        Matrix4f matrix = pose.pose();
        CustomColor markerColor = CustomColor.fromARGBInt(color);

        drawSquarePrism(
                matrix,
                consumer,
                BASE_RADIUS,
                0f,
                BASE_HEIGHT,
                markerColor.withAlpha(140).asInt(),
                markerColor.withAlpha(140).asInt());

        float rodTop = BASE_HEIGHT + ROD_HEIGHT;
        drawSquarePrism(
                matrix,
                consumer,
                ROD_RADIUS,
                BASE_HEIGHT,
                rodTop,
                markerColor.withAlpha(40).asInt(),
                markerColor.withAlpha(200).asInt());

        float bobOffset = (float)
                (Math.sin(cycleProgress(animationTime, ORB_BOB_PERIOD_TICKS) * 2 * Math.PI) * ORB_BOB_AMPLITUDE);
        double rotation = cycleProgress(animationTime, ORB_ROTATION_PERIOD_TICKS) * 2 * Math.PI;
        drawOrb(matrix, consumer, rodTop + ORB_RADIUS + bobOffset, rotation);
        drawPulseRings(matrix, consumer, markerColor, animationTime);
    }

    private static double cycleProgress(float time, float period) {
        return (time % period) / (double) period;
    }

    private static void drawSquarePrism(
            Matrix4f matrix,
            VertexConsumer consumer,
            float radius,
            float bottom,
            float top,
            int bottomColor,
            int topColor) {
        float min = -radius;
        float max = radius;

        drawQuad(matrix, consumer, min, top, min, max, top, min, max, top, max, min, top, max, topColor);
        drawSide(matrix, consumer, min, min, max, min, bottom, top, bottomColor, topColor);
        drawSide(matrix, consumer, max, min, max, max, bottom, top, bottomColor, topColor);
        drawSide(matrix, consumer, max, max, min, max, bottom, top, bottomColor, topColor);
        drawSide(matrix, consumer, min, max, min, min, bottom, top, bottomColor, topColor);
    }

    private static void drawSide(
            Matrix4f matrix,
            VertexConsumer consumer,
            float ax,
            float az,
            float bx,
            float bz,
            float bottom,
            float top,
            int bottomColor,
            int topColor) {
        addVertex(matrix, consumer, ax, bottom, az, bottomColor);
        addVertex(matrix, consumer, bx, bottom, bz, bottomColor);
        addVertex(matrix, consumer, bx, top, bz, topColor);
        addVertex(matrix, consumer, ax, top, az, topColor);
    }

    private static void drawOrb(Matrix4f matrix, VertexConsumer consumer, float centerY, double rotation) {
        float top = centerY + ORB_RADIUS;
        float bottom = centerY - ORB_RADIUS;

        for (int i = 0; i < 4; i++) {
            double start = rotation + i * Math.PI / 2;
            double end = start + Math.PI / 2;
            float ax = (float) (Math.cos(start) * ORB_RADIUS);
            float az = (float) (Math.sin(start) * ORB_RADIUS);
            float bx = (float) (Math.cos(end) * ORB_RADIUS);
            float bz = (float) (Math.sin(end) * ORB_RADIUS);
            drawTriangle(matrix, consumer, ax, centerY, az, bx, centerY, bz, 0f, top, 0f);
            drawTriangle(matrix, consumer, ax, centerY, az, bx, centerY, bz, 0f, bottom, 0f);
        }

        float sparkX = (float) (Math.cos(rotation) * ORB_SPARK_ORBIT_RADIUS);
        float sparkZ = (float) (Math.sin(rotation) * ORB_SPARK_ORBIT_RADIUS);
        drawSpark(matrix, consumer, sparkX, centerY, sparkZ);
    }

    private static void drawSpark(
            Matrix4f matrix, VertexConsumer consumer, float centerX, float centerY, float centerZ) {
        float radius = ORB_SPARK_RADIUS;
        drawTriangle(
                matrix,
                consumer,
                centerX - radius,
                centerY,
                centerZ,
                centerX + radius,
                centerY,
                centerZ,
                centerX,
                centerY + radius,
                centerZ);
        drawTriangle(
                matrix,
                consumer,
                centerX - radius,
                centerY,
                centerZ,
                centerX + radius,
                centerY,
                centerZ,
                centerX,
                centerY - radius,
                centerZ);
    }

    private static void drawTriangle(
            Matrix4f matrix,
            VertexConsumer consumer,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz) {
        int color = CommonColors.WHITE.withAlpha(220).asInt();
        drawQuad(matrix, consumer, ax, ay, az, bx, by, bz, cx, cy, cz, cx, cy, cz, color);
    }

    private static void drawPulseRings(
            Matrix4f matrix, VertexConsumer consumer, CustomColor color, float animationTime) {
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            float progress =
                    (float) ((cycleProgress(animationTime, RIPPLE_CYCLE_TICKS) + i / (double) RIPPLE_COUNT) % 1d);
            float easedProgress = 1f - (1f - progress) * (1f - progress);
            float radius = RIPPLE_MIN_RADIUS + (RIPPLE_MAX_RADIUS - RIPPLE_MIN_RADIUS) * easedProgress;
            drawSquareRing(
                    matrix,
                    consumer,
                    radius,
                    color.withAlpha(Math.round(180 * (1f - progress))).asInt());
        }
    }

    private static void drawSquareRing(Matrix4f matrix, VertexConsumer consumer, float radius, int color) {
        float innerRadius = Math.max(0f, radius - RIPPLE_THICKNESS / 2f);
        float outerRadius = radius + RIPPLE_THICKNESS / 2f;

        drawQuad(
                matrix,
                consumer,
                -innerRadius,
                RIPPLE_HEIGHT,
                -innerRadius,
                -outerRadius,
                RIPPLE_HEIGHT,
                -outerRadius,
                outerRadius,
                RIPPLE_HEIGHT,
                -outerRadius,
                innerRadius,
                RIPPLE_HEIGHT,
                -innerRadius,
                color);
        drawQuad(
                matrix,
                consumer,
                innerRadius,
                RIPPLE_HEIGHT,
                -innerRadius,
                outerRadius,
                RIPPLE_HEIGHT,
                -outerRadius,
                outerRadius,
                RIPPLE_HEIGHT,
                outerRadius,
                innerRadius,
                RIPPLE_HEIGHT,
                innerRadius,
                color);
        drawQuad(
                matrix,
                consumer,
                innerRadius,
                RIPPLE_HEIGHT,
                innerRadius,
                outerRadius,
                RIPPLE_HEIGHT,
                outerRadius,
                -outerRadius,
                RIPPLE_HEIGHT,
                outerRadius,
                -innerRadius,
                RIPPLE_HEIGHT,
                innerRadius,
                color);
        drawQuad(
                matrix,
                consumer,
                -innerRadius,
                RIPPLE_HEIGHT,
                innerRadius,
                -outerRadius,
                RIPPLE_HEIGHT,
                outerRadius,
                -outerRadius,
                RIPPLE_HEIGHT,
                -outerRadius,
                -innerRadius,
                RIPPLE_HEIGHT,
                -innerRadius,
                color);
    }

    private static void drawQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz,
            int color) {
        addVertex(matrix, consumer, ax, ay, az, color);
        addVertex(matrix, consumer, bx, by, bz, color);
        addVertex(matrix, consumer, cx, cy, cz, color);
        addVertex(matrix, consumer, dx, dy, dz, color);
    }

    private static void addVertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, int color) {
        consumer.addVertex(matrix, x, y, z).setColor(color);
    }
}
