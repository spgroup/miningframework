package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.client.utils.BatchingRenderTypeBuffer;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Type;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Usage;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.opengl.GL11;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class ClientUtils {

    static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();

    public static TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];

    public static Tessellator tes() {
        return Tessellator.getInstance();
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static void bindTexture(String path) {
        mc().getTextureManager().bindTexture(getResource(path));
    }

    public static void bindAtlas() {
        mc().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
    }

    public static ResourceLocation getResource(String path) {
        ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
        if (!resourceMap.containsKey(path))
            resourceMap.put(path, rl);
        return rl;
    }

    public static TextureAtlasSprite getSprite(ResourceLocation rl) {
        return mc().getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getSprite(rl);
    }

    public static FontRenderer font() {
        return mc().fontRenderer;
    }

    public static float partialTicks() {
        return mc().getRenderPartialTicks();
    }

    public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage;
        try {
            bufferedimage = ImageIO.read(imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
        return bufferedimage;
    }

    private static FontRenderer unicodeFontRender;

    static {
        IEApi.renderCacheClearers.add(() -> unicodeFontRender = null);
    }

    public static FontRenderer unicodeFontRender() {
        if (unicodeFontRender == null)
            unicodeFontRender = mc().getFontResourceManager().getFontRenderer(new ResourceLocation(ImmersiveEngineering.MODID, "unicode"));
        return unicodeFontRender;
    }

    public enum TimestampFormat {

        D,
        H,
        M,
        S,
        MS,
        HMS,
        HM,
        DHMS,
        DHM,
        DH;

        static TimestampFormat[] coreValues = { TimestampFormat.D, TimestampFormat.H, TimestampFormat.M, TimestampFormat.S };

        public boolean containsFormat(TimestampFormat format) {
            return this.toString().contains(format.toString());
        }

        public long getTickCut() {
            return this == D ? 1728000L : this == H ? 72000L : this == M ? 1200L : this == S ? 20L : 1;
        }

        public String getLocalKey() {
            return this == D ? "day" : this == H ? "hour" : this == M ? "minute" : this == S ? "second" : "";
        }
    }

    public static String fomatTimestamp(long timestamp, TimestampFormat format) {
        String s = "";
        for (TimestampFormat core : TimestampFormat.coreValues) if (format.containsFormat(core) && timestamp >= core.getTickCut()) {
            s += I18n.format(Lib.DESC_INFO + core.getLocalKey(), Long.toString(timestamp / core.getTickCut()));
            timestamp %= core.getTickCut();
        }
        if (s.isEmpty())
            for (int i = TimestampFormat.coreValues.length - 1; i >= 0; i--) if (format.containsFormat(TimestampFormat.coreValues[i])) {
                s = I18n.format(Lib.DESC_INFO + TimestampFormat.coreValues[i].getLocalKey(), 0);
                break;
            }
        return s;
    }

    public static int getDarkenedTextColour(int colour) {
        int r = (colour >> 16 & 255) / 4;
        int g = (colour >> 8 & 255) / 4;
        int b = (colour & 255) / 4;
        return r << 16 | g << 8 | b;
    }

    public static IETileSound generatePositionedIESound(SoundEvent soundEvent, float volume, float pitch, boolean repeat, int delay, BlockPos pos) {
        IETileSound sound = new IETileSound(soundEvent, volume, pitch, repeat, delay, pos, AttenuationType.LINEAR, SoundCategory.BLOCKS);
        sound.evaluateVolume();
        ClientUtils.mc().getSoundHandler().play(sound);
        return sound;
    }

    public static List<ModelRenderer> copyModelRenderers(Model model, List<ModelRenderer> oldRenderers) {
        return oldRenderers;
    }

    public static void handleBipedRotations(BipedModel model, Entity entity) {
        if (!IEConfig.GENERAL.fancyItemHolding.get())
            return;
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            for (Hand hand : Hand.values()) {
                ItemStack heldItem = player.getHeldItem(hand);
                if (!heldItem.isEmpty()) {
                    boolean right = (hand == Hand.MAIN_HAND) == (player.getPrimaryHand() == HandSide.RIGHT);
                    if (heldItem.getItem() instanceof RevolverItem) {
                        if (right) {
                            model.bipedRightArm.rotateAngleX = -1.39626f + model.bipedHead.rotateAngleX;
                            model.bipedRightArm.rotateAngleY = -.08726f + model.bipedHead.rotateAngleY;
                        } else {
                            model.bipedLeftArm.rotateAngleX = -1.39626f + model.bipedHead.rotateAngleX;
                            model.bipedLeftArm.rotateAngleY = .08726f + model.bipedHead.rotateAngleY;
                        }
                    } else if (heldItem.getItem() instanceof DrillItem || heldItem.getItem() instanceof ChemthrowerItem) {
                        if (right) {
                            model.bipedLeftArm.rotateAngleX = -.87266f;
                            model.bipedLeftArm.rotateAngleY = .52360f;
                        } else {
                            model.bipedRightArm.rotateAngleX = -.87266f;
                            model.bipedRightArm.rotateAngleY = -.52360f;
                        }
                    } else if (heldItem.getItem() instanceof BuzzsawItem) {
                        if (right) {
                            model.bipedLeftArm.rotateAngleX = -.87266f;
                            model.bipedLeftArm.rotateAngleY = .78539f;
                        } else {
                            model.bipedRightArm.rotateAngleX = -.87266f;
                            model.bipedRightArm.rotateAngleY = -.78539f;
                        }
                    } else if (heldItem.getItem() instanceof RailgunItem) {
                        if (right)
                            model.bipedRightArm.rotateAngleX = -.87266f;
                        else
                            model.bipedLeftArm.rotateAngleX = -.87266f;
                    }
                }
            }
        }
    }

    public static void drawBlockDamageTexture(MatrixStack matrix, IRenderTypeBuffer buffers, Entity entityIn, float partialTicks, World world, Collection<BlockPos> blocks) {
        int progress = (int) (Minecraft.getInstance().playerController.curBlockDamageMP * 10f) - 1;
        if (progress < 0 || progress >= ModelBakery.DESTROY_RENDER_TYPES.size())
            return;
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        for (BlockPos blockpos : blocks) {
            matrix.push();
            matrix.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            IVertexBuilder worldRendererIn = buffers.getBuffer(ModelBakery.DESTROY_RENDER_TYPES.get(progress));
            worldRendererIn = new MatrixApplyingVertexBuilder(worldRendererIn, matrix.getLast());
            Block block = world.getBlockState(blockpos).getBlock();
            TileEntity te = world.getTileEntity(blockpos);
            boolean hasBreak = block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof AbstractSignBlock || block instanceof SkullBlock;
            if (!hasBreak)
                hasBreak = te != null && te.canRenderBreaking();
            if (!hasBreak) {
                BlockState iblockstate = world.getBlockState(blockpos);
                if (iblockstate.getMaterial() != Material.AIR)
                    blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, world, matrix, worldRendererIn);
            }
            matrix.pop();
        }
    }

    public static void drawColouredRect(int x, int y, int w, int h, int colour) {
        IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        drawColouredRect(x, y, w, h, colour, buffers, new MatrixStack());
        buffers.finish();
    }

    public static void drawColouredRect(int x, int y, int w, int h, int colour, IRenderTypeBuffer buffers, MatrixStack transform) {
        Matrix4f mat = transform.getLast().getMatrix();
        IVertexBuilder worldrenderer = buffers.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
        worldrenderer.pos(mat, x, y + h, 0).color(colour >> 16 & 255, colour >> 8 & 255, colour & 255, colour >> 24 & 255).endVertex();
        worldrenderer.pos(mat, x + w, y + h, 0).color(colour >> 16 & 255, colour >> 8 & 255, colour & 255, colour >> 24 & 255).endVertex();
        worldrenderer.pos(mat, x + w, y, 0).color(colour >> 16 & 255, colour >> 8 & 255, colour & 255, colour >> 24 & 255).endVertex();
        worldrenderer.pos(mat, x, y, 0).color(colour >> 16 & 255, colour >> 8 & 255, colour & 255, colour >> 24 & 255).endVertex();
    }

    public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1) {
        float alpha0 = (colour0 >> 24 & 255) / 255.0F;
        float blue0 = (colour0 >> 16 & 255) / 255.0F;
        float green0 = (colour0 >> 8 & 255) / 255.0F;
        float red0 = (colour0 & 255) / 255.0F;
        float alpha1 = (colour1 >> 24 & 255) / 255.0F;
        float blue1 = (colour1 >> 16 & 255) / 255.0F;
        float green1 = (colour1 >> 8 & 255) / 255.0F;
        float red1 = (colour1 & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x1, y0, 0).color(blue0, green0, red0, alpha0).endVertex();
        worldrenderer.pos(x0, y0, 0).color(blue0, green0, red0, alpha0).endVertex();
        worldrenderer.pos(x0, y1, 0).color(blue1, green1, red1, alpha1).endVertex();
        worldrenderer.pos(x1, y1, 0).color(blue1, green1, red1, alpha1).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void drawTexturedRect(IVertexBuilder builder, MatrixStack transform, float x, float y, float w, float h, float r, float g, float b, float alpha, float u0, float u1, float v0, float v1) {
        Matrix4f mat = transform.getLast().getMatrix();
        builder.pos(mat, x, y + h, 0).color(r, g, b, alpha).tex(u0, v1).overlay(OverlayTexture.NO_OVERLAY).lightmap(0xf000f0).normal(1, 1, 1).endVertex();
        builder.pos(mat, x + w, y + h, 0).color(r, g, b, alpha).tex(u1, v1).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(1, 1, 1).endVertex();
        builder.pos(mat, x + w, y, 0).color(r, g, b, alpha).tex(u1, v0).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(1, 1, 1).endVertex();
        builder.pos(mat, x, y, 0).color(r, g, b, alpha).tex(u0, v0).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(1, 1, 1).endVertex();
    }

    public static void drawTexturedRect(IVertexBuilder builder, MatrixStack transform, int x, int y, int w, int h, float picSize, int u0, int u1, int v0, int v1) {
        drawTexturedRect(builder, transform, x, y, w, h, 1, 1, 1, 1, u0 / picSize, u1 / picSize, v0 / picSize, v1 / picSize);
    }

    public static void drawRepeatedFluidSpriteGui(IRenderTypeBuffer buffer, MatrixStack transform, FluidStack fluid, float x, float y, float w, float h) {
        RenderType renderType = IERenderTypes.getGui(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        IVertexBuilder builder = buffer.getBuffer(renderType);
        drawRepeatedFluidSprite(builder, transform, fluid, x, y, w, h);
    }

    public static void drawRepeatedFluidSprite(IVertexBuilder builder, MatrixStack transform, FluidStack fluid, float x, float y, float w, float h) {
        TextureAtlasSprite sprite = getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid));
        int col = fluid.getFluid().getAttributes().getColor(fluid);
        int iW = sprite.getWidth();
        int iH = sprite.getHeight();
        if (iW > 0 && iH > 0)
            drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), (col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f, 1);
    }

    public static void drawRepeatedSprite(IVertexBuilder builder, MatrixStack transform, float x, float y, float w, float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax, float r, float g, float b, float alpha) {
        int iterMaxW = (int) (w / iconWidth);
        int iterMaxH = (int) (h / iconHeight);
        float leftoverW = w % iconWidth;
        float leftoverH = h % iconHeight;
        float leftoverWf = leftoverW / (float) iconWidth;
        float leftoverHf = leftoverH / (float) iconHeight;
        float iconUDif = uMax - uMin;
        float iconVDif = vMax - vMin;
        for (int ww = 0; ww < iterMaxW; ww++) {
            for (int hh = 0; hh < iterMaxH; hh++) drawTexturedRect(builder, transform, x + ww * iconWidth, y + hh * iconHeight, iconWidth, iconHeight, r, g, b, alpha, uMin, uMax, vMin, vMax);
            drawTexturedRect(builder, transform, x + ww * iconWidth, y + iterMaxH * iconHeight, iconWidth, leftoverH, r, g, b, alpha, uMin, uMax, vMin, (vMin + iconVDif * leftoverHf));
        }
        if (leftoverW > 0) {
            for (int hh = 0; hh < iterMaxH; hh++) drawTexturedRect(builder, transform, x + iterMaxW * iconWidth, y + hh * iconHeight, leftoverW, iconHeight, r, g, b, alpha, uMin, (uMin + iconUDif * leftoverWf), vMin, vMax);
            drawTexturedRect(builder, transform, x + iterMaxW * iconWidth, y + iterMaxH * iconHeight, leftoverW, leftoverH, r, g, b, alpha, uMin, (uMin + iconUDif * leftoverWf), vMin, (vMin + iconVDif * leftoverHf));
        }
    }

    public static void drawSlot(int x, int y, int w, int h) {
        drawSlot(x, y, w, h, 0xff);
    }

    public static void drawSlot(int x, int y, int w, int h, int alpha) {
        drawColouredRect(x + 8 - w / 2, y + 8 - h / 2 - 1, w, 1, (alpha << 24) + 0x373737);
        drawColouredRect(x + 8 - w / 2 - 1, y + 8 - h / 2 - 1, 1, h + 1, (alpha << 24) + 0x373737);
        drawColouredRect(x + 8 - w / 2, y + 8 - h / 2, w, h, (alpha << 24) + 0x8b8b8b);
        drawColouredRect(x + 8 - w / 2, y + 8 + h / 2, w + 1, 1, (alpha << 24) + 0xffffff);
        drawColouredRect(x + 8 + w / 2, y + 8 - h / 2, 1, h, (alpha << 24) + 0xffffff);
    }

    public static void drawDarkSlot(int x, int y, int w, int h) {
        drawColouredRect(x + 8 - w / 2, y + 8 - h / 2 - 1, w, 1, 0x77222222);
        drawColouredRect(x + 8 - w / 2 - 1, y + 8 - h / 2 - 1, 1, h + 1, 0x77222222);
        drawColouredRect(x + 8 - w / 2, y + 8 - h / 2, w, h, 0x77111111);
        drawColouredRect(x + 8 - w / 2, y + 8 + h / 2, w + 1, 1, 0x77999999);
        drawColouredRect(x + 8 + w / 2, y + 8 - h / 2, 1, h, 0x77999999);
    }

    public static void drawHoveringText(List<ITextComponent> list, int x, int y, FontRenderer font, int xSize, int ySize) {
        List<String> textTooltip = new ArrayList<>(list.size());
        for (ITextComponent c : list) textTooltip.add(c.getFormattedText());
        GuiUtils.drawHoveringText(textTooltip, x, y, xSize, ySize, -1, font);
    }

    public static void handleGuiTank(IFluidTank tank, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, List<ITextComponent> tooltip) {
        handleGuiTank(tank.getFluid(), tank.getCapacity(), x, y, w, h, oX, oY, oW, oH, mX, mY, originalTexture, tooltip);
    }

    public static void handleGuiTank(FluidStack fluid, int capacity, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, List<ITextComponent> tooltip) {
        if (tooltip == null) {
            RenderSystem.pushMatrix();
            IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
            MatrixStack transform = new MatrixStack();
            if (fluid != null && fluid.getFluid() != null) {
                int fluidHeight = (int) (h * (fluid.getAmount() / (float) capacity));
                drawRepeatedFluidSpriteGui(buffer, transform, fluid, x, y + h - fluidHeight, w, fluidHeight);
                RenderSystem.color3f(1, 1, 1);
            }
            int xOff = (w - oW) / 2;
            int yOff = (h - oH) / 2;
            RenderType renderType = IERenderTypes.getGui(new ResourceLocation(originalTexture));
            drawTexturedRect(buffer.getBuffer(renderType), transform, x + xOff, y + yOff, oW, oH, 256f, oX, oX + oW, oY, oY + oH);
            buffer.finish(renderType);
            RenderSystem.popMatrix();
        } else {
            if (mX >= x && mX < x + w && mY >= y && mY < y + h)
                addFluidTooltip(fluid, tooltip, capacity);
        }
    }

    public static void addFluidTooltip(FluidStack fluid, List<ITextComponent> tooltip, int tankCapacity) {
        if (!fluid.isEmpty())
            tooltip.add(fluid.getDisplayName().setStyle(new Style().setColor(fluid.getFluid().getAttributes().getRarity(fluid).color)));
        else
            tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
        if (fluid.getFluid() instanceof IEFluid)
            ((IEFluid) fluid.getFluid()).addTooltipInfo(fluid, null, tooltip);
        if (mc().gameSettings.advancedItemTooltips && !fluid.isEmpty()) {
            if (!Screen.hasShiftDown())
                tooltip.add(new TranslationTextComponent(Lib.DESC_INFO + "holdShiftForInfo"));
            else {
                Style darkGray = new Style().setColor(TextFormatting.DARK_GRAY);
                tooltip.add(new StringTextComponent("Fluid Registry: " + fluid.getFluid().getRegistryName()).setStyle(darkGray));
                tooltip.add(new StringTextComponent("Density: " + fluid.getFluid().getAttributes().getDensity(fluid)).setStyle(darkGray));
                tooltip.add(new StringTextComponent("Temperature: " + fluid.getFluid().getAttributes().getTemperature(fluid)).setStyle(darkGray));
                tooltip.add(new StringTextComponent("Viscosity: " + fluid.getFluid().getAttributes().getViscosity(fluid)).setStyle(darkGray));
                tooltip.add(new StringTextComponent("NBT Data: " + fluid.getTag()).setStyle(darkGray));
            }
        }
        Style gray = new Style().setColor(TextFormatting.GRAY);
        if (tankCapacity > 0)
            tooltip.add(new StringTextComponent(fluid.getAmount() + "/" + tankCapacity + "mB").setStyle(gray));
        else
            tooltip.add(new StringTextComponent(fluid.getAmount() + "mB").setStyle(gray));
    }

    public static Quaternion degreeToQuaterion(double x, double y, double z) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);
        Quaternion qYaw = new Quaternion(0, (float) Math.sin(y / 2), 0, (float) Math.cos(y / 2));
        Quaternion qPitch = new Quaternion((float) Math.sin(x / 2), 0, 0, (float) Math.cos(x / 2));
        Quaternion qRoll = new Quaternion(0, 0, (float) Math.sin(z / 2), (float) Math.cos(z / 2));
        Quaternion quat = qYaw;
        quat.multiply(qRoll);
        quat.multiply(qPitch);
        return quat;
    }

    private static final Vec3d fadingOffset = new Vec3d(.0001F, .0001F, .0001F);

    private static float[] alphaFirst2Fading = { 0, 0, 1, 1 };

    private static float[] alphaNoFading = { 1, 1, 1, 1 };

    public static List<BakedQuad> convertConnectionFromBlockstate(BlockPos here, Set<Connection.RenderData> data, TextureAtlasSprite t) {
        List<BakedQuad> ret = new ArrayList<>();
        if (data == null)
            return ret;
        Vec3d dir = Vec3d.ZERO;
        Vec3d up = new Vec3d(0, 1, 0);
        for (Connection.RenderData connData : data) {
            int color = connData.color;
            float[] rgb = { (color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, (color >> 24 & 255) / 255f };
            if (rgb[3] == 0)
                rgb[3] = 1;
            float radius = (float) (connData.type.getRenderDiameter() / 2);
            for (int segmentEndId = 1; segmentEndId <= connData.pointsToRenderSolid; segmentEndId++) {
                int segmentStartId = segmentEndId - 1;
                Vec3d segmentEnd = connData.getPoint(segmentEndId);
                Vec3d segmentStart = connData.getPoint(segmentStartId);
                boolean vertical = segmentEnd.x == segmentStart.x && segmentEnd.z == segmentStart.z;
                Vec3d cross;
                if (!vertical) {
                    dir = segmentEnd.subtract(segmentStart);
                    cross = up.crossProduct(dir);
                    cross = cross.scale(radius / cross.length());
                } else
                    cross = new Vec3d(radius, 0, 0);
                Vec3d[] vertices = { segmentEnd.add(cross), segmentEnd.subtract(cross), segmentStart.subtract(cross), segmentStart.add(cross) };
                ret.add(createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.DOWN, t, rgb, false, alphaNoFading, here));
                ret.add(createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.UP, t, rgb, true, alphaNoFading, here));
                if (!vertical) {
                    cross = dir.crossProduct(cross);
                    cross = cross.scale(radius / cross.length());
                } else
                    cross = new Vec3d(0, 0, radius);
                vertices = new Vec3d[] { segmentEnd.add(cross), segmentEnd.subtract(cross), segmentStart.subtract(cross), segmentStart.add(cross) };
                ret.add(createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.WEST, t, rgb, false, alphaNoFading, here));
                ret.add(createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.EAST, t, rgb, true, alphaNoFading, here));
            }
        }
        return ret;
    }

    public static int getSolidVertexCountForSide(ConnectionPoint start, Connection conn, int totalPoints) {
        List<Integer> crossings = new ArrayList<>();
        Vec3d lastPoint = conn.getPoint(0, start);
        for (int i = 1; i <= totalPoints; i++) {
            Vec3d current = conn.getPoint(i / (double) totalPoints, start);
            if (crossesChunkBoundary(current, lastPoint, start.getPosition()))
                crossings.add(i);
            lastPoint = current;
        }
        boolean greater = conn.isPositiveEnd(start);
        if (crossings.size() > 0) {
            int index = crossings.size() / 2;
            if (crossings.size() % 2 == 0 && greater)
                index--;
            return crossings.get(index) - (greater ? 0 : 1);
        } else
            return greater ? totalPoints : 0;
    }

    public static Vec3d[] applyMatrixToVertices(TransformationMatrix matrix, Vec3d... vertices) {
        if (matrix == null)
            return vertices;
        Vec3d[] ret = new Vec3d[vertices.length];
        for (int i = 0; i < ret.length; i++) {
            Vector4f vec = new Vector4f((float) vertices[i].x, (float) vertices[i].y, (float) vertices[i].z, 1);
            matrix.transformPosition(vec);
            vec.perspectiveDivide();
            ret[i] = new Vec3d(vec.getX(), vec.getY(), vec.getZ());
        }
        return ret;
    }

    public static Set<BakedQuad> createBakedBox(Vec3d from, Vec3d to, Matrix4 matrix, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour) {
        return createBakedBox(from, to, matrix, Direction.NORTH, textureGetter, colour);
    }

    public static Set<BakedQuad> createBakedBox(Vec3d from, Vec3d to, Matrix4 matrix, Direction facing, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour) {
        return createBakedBox(from, to, matrix, facing, vertices -> vertices, textureGetter, colour);
    }

    @Nonnull
    public static Set<BakedQuad> createBakedBox(Vec3d from, Vec3d to, Matrix4 matrixIn, Direction facing, Function<Vec3d[], Vec3d[]> vertexTransformer, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour) {
        TransformationMatrix matrix = matrixIn.toTransformationMatrix();
        HashSet<BakedQuad> quads = new HashSet<>();
        if (vertexTransformer == null)
            vertexTransformer = v -> v;
        Vec3d[] vertices = { new Vec3d(from.x, from.y, from.z), new Vec3d(from.x, from.y, to.z), new Vec3d(to.x, from.y, to.z), new Vec3d(to.x, from.y, from.z) };
        TextureAtlasSprite sprite = textureGetter.apply(Direction.DOWN);
        if (sprite != null)
            quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), sprite, new double[] { from.x * 16, 16 - from.z * 16, to.x * 16, 16 - to.z * 16 }, colour, true));
        for (int i = 0; i < vertices.length; i++) {
            Vec3d v = vertices[i];
            vertices[i] = new Vec3d(v.x, to.y, v.z);
        }
        sprite = textureGetter.apply(Direction.UP);
        if (sprite != null)
            quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.UP, facing), sprite, new double[] { from.x * 16, from.z * 16, to.x * 16, to.z * 16 }, colour, false));
        vertices = new Vec3d[] { new Vec3d(to.x, to.y, from.z), new Vec3d(to.x, from.y, from.z), new Vec3d(from.x, from.y, from.z), new Vec3d(from.x, to.y, from.z) };
        sprite = textureGetter.apply(Direction.NORTH);
        if (sprite != null)
            quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, facing), sprite, new double[] { from.x * 16, 16 - to.y * 16, to.x * 16, 16 - from.y * 16 }, colour, false));
        for (int i = 0; i < vertices.length; i++) {
            Vec3d v = vertices[i];
            vertices[i] = new Vec3d(v.x, v.y, to.z);
        }
        sprite = textureGetter.apply(Direction.SOUTH);
        if (sprite != null)
            quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, facing), sprite, new double[] { to.x * 16, 16 - to.y * 16, from.x * 16, 16 - from.y * 16 }, colour, true));
        vertices = new Vec3d[] { new Vec3d(from.x, to.y, to.z), new Vec3d(from.x, from.y, to.z), new Vec3d(from.x, from.y, from.z), new Vec3d(from.x, to.y, from.z) };
        sprite = textureGetter.apply(Direction.WEST);
        if (sprite != null)
            quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.WEST, facing), sprite, new double[] { to.z * 16, 16 - to.y * 16, from.z * 16, 16 - from.y * 16 }, colour, true));
        for (int i = 0; i < vertices.length; i++) {
            Vec3d v = vertices[i];
            vertices[i] = new Vec3d(to.x, v.y, v.z);
        }
        sprite = textureGetter.apply(Direction.EAST);
        if (sprite != null)
            quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.EAST, facing), sprite, new double[] { 16 - to.z * 16, 16 - to.y * 16, 16 - from.z * 16, 16 - from.y * 16 }, colour, false));
        return quads;
    }

    public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, float[] colour, boolean invert, float[] alpha) {
        return createBakedQuad(format, vertices, facing, sprite, new double[] { 0, 0, 16, 16 }, colour, invert, alpha);
    }

    public static BakedQuad createSmartLightingBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, float[] colour, boolean invert, float[] alpha, BlockPos base) {
        return createBakedQuad(format, vertices, facing, sprite, new double[] { 0, 0, 16, 16 }, colour, invert, alpha, true, base);
    }

    public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert) {
        return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alphaNoFading);
    }

    public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha) {
        return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alpha, false, null);
    }

    public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha, boolean smartLighting, BlockPos basePos) {
        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(facing);
        builder.setApplyDiffuseLighting(true);
        Vec3d faceNormal = new Vec3d(facing.getDirectionVec());
        int vId = invert ? 3 : 0;
        int u = vId > 1 ? 2 : 0;
        putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, alpha[vId]);
        vId = invert ? 2 : 1;
        u = vId > 1 ? 2 : 0;
        putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, alpha[vId]);
        vId = invert ? 1 : 2;
        u = vId > 1 ? 2 : 0;
        putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, alpha[vId]);
        vId = invert ? 0 : 3;
        u = vId > 1 ? 2 : 0;
        putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, alpha[vId]);
        BakedQuad tmp = builder.build();
        return smartLighting ? new SmartLightingQuad(tmp.getVertexData(), -1, facing, sprite, basePos) : tmp;
    }

    public static void putVertexData(VertexFormat format, BakedQuadBuilder builder, Vec3d pos, Vec3d faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha) {
        for (int e = 0; e < format.getElements().size(); e++) switch(format.getElements().get(e).getUsage()) {
            case POSITION:
                builder.put(e, (float) pos.x, (float) pos.y, (float) pos.z);
                break;
            case COLOR:
                float d = 1;
                builder.put(e, d * colour[0], d * colour[1], d * colour[2], 1 * colour[3] * alpha);
                break;
            case UV:
                if (format.getElements().get(e).getType() == Type.FLOAT) {
                    if (sprite == null)
                        sprite = ClientUtils.getMissingSprite();
                    builder.put(e, sprite.getInterpolatedU(u), sprite.getInterpolatedV(v));
                } else
                    builder.put(e, 0, 0);
                break;
            case NORMAL:
                builder.put(e, (float) faceNormal.getX(), (float) faceNormal.getY(), (float) faceNormal.getZ());
                break;
            default:
                builder.put(e);
        }
    }

    private static TextureAtlasSprite getMissingSprite() {
        return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
    }

    public static boolean crossesChunkBoundary(Vec3d start, Vec3d end, BlockPos offset) {
        if (crossesChunkBorderSingleDim(start.x, end.x, offset.getX()))
            return true;
        if (crossesChunkBorderSingleDim(start.y, end.y, offset.getY()))
            return true;
        return crossesChunkBorderSingleDim(start.z, end.z, offset.getZ());
    }

    private static boolean crossesChunkBorderSingleDim(double a, double b, int offset) {
        return ((int) Math.floor(a + offset)) >> 4 != ((int) Math.floor(b + offset)) >> 4;
    }

    public static ResourceLocation getSideTexture(@Nonnull ItemStack stack, Direction side) {
        IBakedModel model = mc().getItemRenderer().getItemModelWithOverrides(stack, null, null);
        return getSideTexture(model, side, null);
    }

    public static ResourceLocation getSideTexture(@Nonnull BlockState state, Direction side) {
        IBakedModel model = mc().getBlockRendererDispatcher().getModelForState(state);
        return getSideTexture(model, side, state);
    }

    public static ResourceLocation getSideTexture(@Nonnull IBakedModel model, Direction side, @Nullable BlockState state) {
        List<BakedQuad> quads = model.getQuads(state, side, Utils.RAND);
        if (quads == null || quads.isEmpty())
            quads = model.getQuads(state, null, Utils.RAND);
        if (quads == null || quads.isEmpty())
            return null;
        return quads.get(0).func_187508_a().getName();
    }

    public static Vector4f pulseRGBAlpha(Vector4f rgba, int tickrate, float min, float max) {
        float f_alpha = mc().player.ticksExisted % (tickrate * 2) / (float) tickrate;
        if (f_alpha > 1)
            f_alpha = 2 - f_alpha;
        return new Vector4f(rgba.getX(), rgba.getY(), rgba.getZ(), MathHelper.clamp(f_alpha, min, max));
    }

    public static void renderBox(IVertexBuilder wr, double x0, double y0, double z0, double x1, double y1, double z1) {
        renderBox(wr, new MatrixStack(), (float) x0, (float) y0, (float) z0, (float) x1, (float) y1, (float) z1);
    }

    public static void renderBox(IVertexBuilder wr, MatrixStack m, float x0, float y0, float z0, float x1, float y1, float z1) {
        Matrix4f transform = m.getLast().getMatrix();
        wr.pos(transform, x0, y0, z1).endVertex();
        wr.pos(transform, x1, y0, z1).endVertex();
        wr.pos(transform, x1, y1, z1).endVertex();
        wr.pos(transform, x0, y1, z1).endVertex();
        wr.pos(transform, x0, y1, z0).endVertex();
        wr.pos(transform, x1, y1, z0).endVertex();
        wr.pos(transform, x1, y0, z0).endVertex();
        wr.pos(transform, x0, y0, z0).endVertex();
        wr.pos(transform, x0, y0, z0).endVertex();
        wr.pos(transform, x1, y0, z0).endVertex();
        wr.pos(transform, x1, y0, z1).endVertex();
        wr.pos(transform, x0, y0, z1).endVertex();
        wr.pos(transform, x0, y1, z1).endVertex();
        wr.pos(transform, x1, y1, z1).endVertex();
        wr.pos(transform, x1, y1, z0).endVertex();
        wr.pos(transform, x0, y1, z0).endVertex();
        wr.pos(transform, x0, y0, z0).endVertex();
        wr.pos(transform, x0, y0, z1).endVertex();
        wr.pos(transform, x0, y1, z1).endVertex();
        wr.pos(transform, x0, y1, z0).endVertex();
        wr.pos(transform, x1, y1, z0).endVertex();
        wr.pos(transform, x1, y1, z1).endVertex();
        wr.pos(transform, x1, y0, z1).endVertex();
        wr.pos(transform, x1, y0, z0).endVertex();
    }

    public static void renderTexturedBox(BufferBuilder wr, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite tex, boolean yForV) {
        throw new UnsupportedOperationException();
    }

    public static void renderTexturedBox(IVertexBuilder wr, MatrixStack stack, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite tex, boolean yForV) {
        float minU = tex.getInterpolatedU(x0 * 16);
        float maxU = tex.getInterpolatedU(x1 * 16);
        float minV = tex.getInterpolatedV((yForV ? y1 : z0) * 16);
        float maxV = tex.getInterpolatedV((yForV ? y0 : z1) * 16);
        renderTexturedBox(wr, stack, x0, y0, z0, x1, y1, z1, minU, minV, maxU, maxV);
    }

    public static void renderTexturedBox(BufferBuilder wr, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1) {
        throw new UnsupportedOperationException();
    }

    public static void renderTexturedBox(IVertexBuilder wr, MatrixStack stack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1) {
        Matrix4f mat = stack.getLast().getMatrix();
        Matrix3f nMat = stack.getLast().getNormal();
        float normalX = 0;
        float normalY = 0;
        float normalZ = 1;
        putVertex(wr, stack, x0, y0, z1, u0, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y0, z1, u1, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y1, z1, u1, v1, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y1, z1, u0, v1, normalX, normalY, normalZ);
        normalZ = -1;
        putVertex(wr, stack, x0, y1, z0, u0, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y1, z0, u1, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y0, z0, u1, v1, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y0, z0, u0, v1, normalX, normalY, normalZ);
        normalZ = 0;
        normalY = -1;
        putVertex(wr, stack, x0, y0, z0, u0, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y0, z0, u1, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y0, z1, u1, v1, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y0, z1, u0, v1, normalX, normalY, normalZ);
        normalY = 1;
        putVertex(wr, stack, x0, y1, z1, u0, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y1, z1, u1, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y1, z0, u1, v1, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y1, z0, u0, v1, normalX, normalY, normalZ);
        normalY = 0;
        normalX = -1;
        putVertex(wr, stack, x0, y0, z0, u0, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y0, z1, u1, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y1, z1, u1, v1, normalX, normalY, normalZ);
        putVertex(wr, stack, x0, y1, z0, u0, v1, normalX, normalY, normalZ);
        normalX = 1;
        putVertex(wr, stack, x1, y1, z0, u0, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y1, z1, u1, v0, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y0, z1, u1, v1, normalX, normalY, normalZ);
        putVertex(wr, stack, x1, y0, z0, u0, v1, normalX, normalY, normalZ);
    }

    public static int findOffset(VertexFormat vf, Usage u, Type t) {
        int offset = 0;
        for (VertexFormatElement element : vf.getElements()) {
            if (element.getUsage() == u && element.getType() == t) {
                Preconditions.checkState(offset % 4 == 0);
                return offset / 4;
            }
            offset += element.getSize();
        }
        throw new IllegalStateException();
    }

    public static int findTextureOffset(VertexFormat vf) {
        return findOffset(vf, Usage.UV, Type.FLOAT);
    }

    public static int findPositionOffset(VertexFormat vf) {
        return findOffset(vf, Usage.POSITION, Type.FLOAT);
    }

    private static void putVertex(IVertexBuilder b, MatrixStack mat, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        b.pos(mat.getLast().getMatrix(), x, y, z).color(1F, 1F, 1F, 1F).tex(u, v).lightmap(0, 0).normal(mat.getLast().getNormal(), nX, nY, nZ).endVertex();
    }

    public static int intFromRgb(float[] rgb) {
        int ret = (int) (255 * rgb[0]);
        ret = (ret << 8) + (int) (255 * rgb[1]);
        ret = (ret << 8) + (int) (255 * rgb[2]);
        return ret;
    }

    private static final float[][] quadCoords = new float[4][3];

    private static final int[][] neighbourBrightness = new int[2][6];

    private static final float[][] normalizationFactors = new float[2][8];

    public static void renderModelTESRFancy(List<BakedQuad> quads, IVertexBuilder renderer, World world, BlockPos pos, boolean useCached, int color, int light) {
        if (IEConfig.GENERAL.disableFancyTESR.get())
            renderModelTESRFast(quads, renderer, new MatrixStack(), world.getLightSubtracted(pos, 0), color);
        else {
            if (!useCached) {
                for (Direction f : Direction.VALUES) {
                    int val = WorldRenderer.getCombinedLight(world, pos.offset(f));
                    neighbourBrightness[0][f.getIndex()] = (val >> 16) & 255;
                    neighbourBrightness[1][f.getIndex()] = val & 255;
                }
                for (int type = 0; type < 2; type++) for (int i = 0; i < 8; i++) {
                    float sSquared = 0;
                    if ((i & 1) != 0)
                        sSquared += scaledSquared(neighbourBrightness[type][5], 255F);
                    else
                        sSquared += scaledSquared(neighbourBrightness[type][4], 255F);
                    if ((i & 2) != 0)
                        sSquared += scaledSquared(neighbourBrightness[type][1], 255F);
                    else
                        sSquared += scaledSquared(neighbourBrightness[type][0], 255F);
                    if ((i & 4) != 0)
                        sSquared += scaledSquared(neighbourBrightness[type][3], 255F);
                    else
                        sSquared += scaledSquared(neighbourBrightness[type][2], 255F);
                    normalizationFactors[type][i] = (float) Math.sqrt(sSquared);
                }
            }
            int[] rgba = { 255, 255, 255, 255 };
            if (color >= 0) {
                rgba[0] = color >> 16 & 255;
                rgba[1] = color >> 8 & 255;
                rgba[2] = color & 255;
            }
            for (BakedQuad quad : quads) {
                int[] vData = quad.getVertexData();
                VertexFormat format = DefaultVertexFormats.BLOCK;
                int size = format.getIntegerSize();
                int uvOffset = ClientUtils.findTextureOffset(format);
                int posOffset = ClientUtils.findPositionOffset(format);
                for (int i = 0; i < 4; i++) {
                    quadCoords[i][0] = Float.intBitsToFloat(vData[size * i + posOffset]);
                    quadCoords[i][1] = Float.intBitsToFloat(vData[size * i + posOffset + 1]);
                    quadCoords[i][2] = Float.intBitsToFloat(vData[size * i + posOffset + 2]);
                }
                Vec3d side1 = new Vec3d(quadCoords[1][0] - quadCoords[3][0], quadCoords[1][1] - quadCoords[3][1], quadCoords[1][2] - quadCoords[3][2]);
                Vec3d side2 = new Vec3d(quadCoords[2][0] - quadCoords[0][0], quadCoords[2][1] - quadCoords[0][1], quadCoords[2][2] - quadCoords[0][2]);
                Vec3d normal = side1.crossProduct(side2);
                normal = normal.normalize();
                int l1 = getLightValue(neighbourBrightness[1], normalizationFactors[1], light & 255, normal);
                int l2 = getLightValue(neighbourBrightness[0], normalizationFactors[0], (light >> 16) & 255, normal);
                for (int i = 0; i < 4; ++i) {
                    renderer.pos(quadCoords[i][0], quadCoords[i][1], quadCoords[i][2]).color(rgba[0], rgba[1], rgba[2], rgba[3]).tex(Float.intBitsToFloat(vData[size * i + uvOffset]), Float.intBitsToFloat(vData[size * i + uvOffset + 1])).lightmap(l1, l2).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
                }
            }
        }
    }

    private static int getLightValue(int[] neighbourBrightness, float[] normalizationFactors, int localBrightness, Vec3d normal) {
        double sideBrightness;
        byte type = 0;
        if (normal.x > 0) {
            sideBrightness = normal.x * neighbourBrightness[5];
            type |= 1;
        } else
            sideBrightness = -normal.x * neighbourBrightness[4];
        if (normal.y > 0) {
            sideBrightness += normal.y * neighbourBrightness[1];
            type |= 2;
        } else
            sideBrightness += -normal.y * neighbourBrightness[0];
        if (normal.z > 0) {
            sideBrightness += normal.z * neighbourBrightness[3];
            type |= 4;
        } else
            sideBrightness += -normal.z * neighbourBrightness[2];
        return (int) ((localBrightness + sideBrightness / normalizationFactors[type]) / 2);
    }

    private static float scaledSquared(int val, float scale) {
        return (val / scale) * (val / scale);
    }

    public static void renderModelTESRFast(List<BakedQuad> quads, IVertexBuilder renderer, MatrixStack transform, int light) {
        renderModelTESRFast(quads, renderer, transform, -1, light);
    }

    public static void renderModelTESRFast(List<BakedQuad> quads, IVertexBuilder renderer, MatrixStack transform, int color, int light) {
        int[] rgba = { 255, 255, 255, 255 };
        if (color >= 0) {
            rgba[0] = color >> 16 & 255;
            rgba[1] = color >> 8 & 255;
            rgba[2] = color & 255;
        }
        VertexFormat format = DefaultVertexFormats.BLOCK;
        int size = format.getIntegerSize();
        int uv = findTextureOffset(format);
        int position = findPositionOffset(format);
        int normal = findOffset(format, Usage.NORMAL, Type.BYTE);
        for (BakedQuad quad : quads) {
            int[] vData = quad.getVertexData();
            for (int i = 0; i < 4; ++i) {
                int normalPacked = vData[size * i + normal];
                float normalX = (normalPacked & 255) / 255F;
                float normalY = ((normalPacked >> 8) & 255) / 255F;
                float normalZ = ((normalPacked >> 16) & 255) / 255F;
                renderer.pos(transform.getLast().getMatrix(), Float.intBitsToFloat(vData[size * i + position]), Float.intBitsToFloat(vData[size * i + position + 1]), Float.intBitsToFloat(vData[size * i + position + 2])).color(rgba[0], rgba[1], rgba[2], rgba[3]).tex(Float.intBitsToFloat(vData[size * i + uv]), Float.intBitsToFloat(vData[size * i + uv + 1])).lightmap(light).normal(transform.getLast().getNormal(), normalX, normalY, normalZ).endVertex();
            }
        }
    }

    public static void setLightmapDisabled(boolean disabled) {
        throw new UnsupportedOperationException();
    }

    public static void toggleLightmap(boolean pre, boolean disabled) {
        throw new UnsupportedOperationException();
    }

    public static boolean isSneakKeyPressed() {
        if (Minecraft.getInstance().gameSettings == null)
            return false;
        KeyBinding keybind = Minecraft.getInstance().gameSettings.keyBindSneak;
        Input keyCode = keybind.getKey();
        if (keyCode.getType() == InputMappings.Type.KEYSYM && keyCode.getKeyCode() != InputMappings.INPUT_INVALID.getKeyCode())
            return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), keyCode.getKeyCode());
        else
            return false;
    }

    public static TransformationMatrix rotateTo(Direction d) {
        return new TransformationMatrix(null).blockCornerToCenter().compose(toModelRotation(d).getRotation()).blockCenterToCorner();
    }

    public static ModelRotation toModelRotation(Direction d) {
        switch(d) {
            case DOWN:
                return ModelRotation.X90_Y0;
            case UP:
                return ModelRotation.X270_Y0;
            case NORTH:
                return ModelRotation.X0_Y0;
            case SOUTH:
                return ModelRotation.X0_Y180;
            case WEST:
                return ModelRotation.X0_Y270;
            case EAST:
                return ModelRotation.X0_Y90;
        }
        throw new IllegalArgumentException(String.valueOf(d));
    }

    public static void renderItemWithOverlayIntoGUI(IRenderTypeBuffer buffer, MatrixStack transform, ItemStack stack, int x, int y) {
        buffer = IERenderTypes.disableLighting(buffer);
        transform.push();
        transform.translate(x, y, 100);
        transform.push();
        transform.translate(8, 8, 0);
        transform.scale(1, -1, 1);
        transform.scale(16, 16, 16);
        BatchingRenderTypeBuffer batchBuffer = new BatchingRenderTypeBuffer();
        mc().getItemRenderer().renderItem(stack, TransformType.GUI, 0xf000f0, OverlayTexture.NO_OVERLAY, transform, batchBuffer);
        batchBuffer.pipe(buffer);
        transform.pop();
        renderDurabilityBar(stack, buffer, transform);
        transform.pop();
    }

    public static void renderDurabilityBar(ItemStack stack, IRenderTypeBuffer buffer, MatrixStack transform) {
        if (!stack.isEmpty() && stack.getItem().showDurabilityBar(stack)) {
            double health = stack.getItem().getDurabilityForDisplay(stack);
            int i = Math.round(13.0F - (float) health * 13.0F);
            int j = stack.getItem().getRGBDurabilityForDisplay(stack);
            draw(transform, buffer, 2, 13, 13, 2, 0, 0, 0);
            draw(transform, buffer, 2, 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255);
        }
    }

    private static void draw(MatrixStack transform, IRenderTypeBuffer buffer, int x, int y, int width, int height, int red, int green, int blue) {
        IVertexBuilder builder = buffer.getBuffer(IERenderTypes.ITEM_DAMAGE_BAR);
        transform.push();
        transform.translate(x, y, 0);
        Matrix4f mat = transform.getLast().getMatrix();
        builder.pos(mat, 0, 0, 0).color(red, green, blue, 255).endVertex();
        builder.pos(mat, 0, height, 0).color(red, green, blue, 255).endVertex();
        builder.pos(mat, width, height, 0).color(red, green, blue, 255).endVertex();
        builder.pos(mat, width, 0, 0).color(red, green, blue, 255).endVertex();
        transform.pop();
    }
}
